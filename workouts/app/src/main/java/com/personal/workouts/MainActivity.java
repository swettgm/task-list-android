package com.personal.workouts;

import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.webkit.*;
import android.widget.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

public final class MainActivity extends Activity {
    static volatile boolean visible;
    private WebView web;
    private FrameLayout root;
    private WorkoutStore store;
    private boolean ready,modal,requestNotificationsAfterOverlay;
    private int top=Color.rgb(32,55,71),bottom=Color.rgb(90,92,97);
    private Object backCallback;
    private final Handler restPreviewHandler=new Handler(Looper.getMainLooper());
    private volatile Object restPreview;
    private boolean openRestOnReady;
    private final java.util.concurrent.atomic.AtomicInteger keyboardRequest=new java.util.concurrent.atomic.AtomicInteger();
    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        store=new WorkoutStore(this);
        openRestOnReady=RestTimerService.OPEN_CONTROLS.equals(getIntent().getAction());
        root=new FrameLayout(this);root.setBackgroundColor(top);
        root.setOnApplyWindowInsetsListener((view,insets)->{
            view.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),
                insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());
            return insets;
        });
        web=new WebView(this);web.setBackgroundColor(top);web.setAlpha(0);
        web.setFocusable(true);web.setFocusableInTouchMode(true);
        web.setVerticalScrollBarEnabled(false);web.setHorizontalScrollBarEnabled(false);
        root.addView(web,new FrameLayout.LayoutParams(-1,-1));
        setContentView(root);root.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);web.requestFocus();root.requestApplyInsets();bars();
        WebSettings settings=web.getSettings();
        settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(false);
        settings.setAllowFileAccess(false);settings.setAllowContentAccess(false);
        settings.setBlockNetworkLoads(true);settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        web.addJavascriptInterface(new Bridge(),"WorkoutNative");
        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request) {
                Uri uri=request.getUrl();
                if(request.isForMainFrame()&&request.hasGesture()&&uri.getHost()!=null&&!"workouts.local".equals(uri.getHost())
                        &&("https".equals(uri.getScheme())||"http".equals(uri.getScheme()))) {
                    try { startActivity(new Intent(Intent.ACTION_VIEW,uri).addCategory(Intent.CATEGORY_BROWSABLE)); }
                    catch(ActivityNotFoundException|SecurityException unavailable) {
                        Toast.makeText(MainActivity.this,"No browser is available.",Toast.LENGTH_SHORT).show();
                    }
                }
                return true;
            }
            @Override public WebResourceResponse shouldInterceptRequest(WebView view,WebResourceRequest request) {
                return new WebResourceResponse("text/plain","UTF-8",new ByteArrayInputStream(new byte[0]));
            }
        });
        try(InputStream in=getAssets().open("index.html");ByteArrayOutputStream bytes=new ByteArrayOutputStream()) {
            byte[] buffer=new byte[8192];int n;
            while((n=in.read(buffer))!=-1)bytes.write(buffer,0,n);
            web.loadDataWithBaseURL("https://workouts.local/",bytes.toString(StandardCharsets.UTF_8.name()),"text/html","UTF-8",null);
        } catch(IOException failed) {
            Toast.makeText(this,"Could not open Exercise.",Toast.LENGTH_LONG).show();finish();
        }
        if(Build.VERSION.SDK_INT>=33)backCallback=BackApi33.register(this,this::back);
    }
    @Override protected void onResume() {
        super.onResume();visible=true;
        if(ready)web.evaluateJavascript("window.Workouts&&Workouts.refresh()",null);
        if(requestNotificationsAfterOverlay){
            requestNotificationsAfterOverlay=false;
            askForNotificationPermission();
        }
        try { RestAlarm.schedule(this); } catch(RuntimeException invalid) { /* The UI shows the recoverable saved-data error. */ }
    }
    @Override protected void onPause() {visible=false;stopRestPreview();super.onPause();}
    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if(RestTimerService.OPEN_CONTROLS.equals(intent.getAction())){
            openRestOnReady=true;
            openRestControlsIfReady();
        }
    }
    private void openRestControlsIfReady(){
        if(ready&&openRestOnReady&&web!=null){
            openRestOnReady=false;
            web.evaluateJavascript("window.Workouts&&Workouts.openRestControls()",null);
        }
    }
    private void stopRestPreview(){
        Object token=restPreview;
        restPreview=null;
        if(token!=null)restPreviewHandler.removeCallbacksAndMessages(token);
    }
    private void playRestPreview(int seconds,int pulseMs,int finishMs,boolean sound,int volume){
        stopRestPreview();
        Object token=new Object();
        restPreview=token;
        long start=SystemClock.uptimeMillis();
        if(sound)HeadphoneCue.preview(this,seconds,volume,()->restPreview==token);
        for(int i=0;i<=seconds;i++){
            boolean finish=i==seconds;
            restPreviewHandler.postAtTime(()->{
                if(restPreview!=token)return;
                RestAlarm.vibrate(this,finish?finishMs:pulseMs);
            },token,start+i*1000L);
        }
    }
    @Override protected void onDestroy() {
        if(Build.VERSION.SDK_INT>=33&&backCallback!=null)BackApi33.unregister(this,backCallback);
        if(web!=null){web.removeJavascriptInterface("WorkoutNative");web.destroy();web=null;}
        super.onDestroy();
    }
    private void askForNotificationPermission() {
        if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS},10);
    }
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] results) {
        super.onRequestPermissionsResult(request,permissions,results);
        RestTimerService.sync(this);
    }
    @Override public void onBackPressed() { back(); }
    private void back() {
        if(!ready){finish();return;}
        web.evaluateJavascript("Boolean(window.Workouts&&Workouts.back())",value->{
            if(!"true".equals(value)&&!isFinishing())finish();
        });
    }
    private static int dim(int value) { return Color.rgb((int)(Color.red(value)*.45),(int)(Color.green(value)*.45),(int)(Color.blue(value)*.45)); }
    private void bars() {
        int a=modal?dim(top):top,b=modal?dim(bottom):bottom;
        root.setBackgroundColor(top);getWindow().setStatusBarColor(a);getWindow().setNavigationBarColor(b);
        int flags=0;
        if(Color.luminance(a)>.5)flags|=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        if(Color.luminance(b)>.5)flags|=View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        getWindow().getDecorView().setSystemUiVisibility(flags);
    }
    final class Bridge {
        @JavascriptInterface public String readState() {
            try { return store.read().toString(); } catch(RuntimeException invalid) { return null; }
        }
        @JavascriptInterface public String writeState(String raw) {
            String value=store.write(raw);
            if(value!=null)runOnUiThread(()->RestAlarm.schedule(MainActivity.this));
            return value;
        }
        @JavascriptInterface public void haptic() {
            runOnUiThread(()->{if(web!=null)web.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);});
        }
        @JavascriptInterface public String restState() {
            try {
                org.json.JSONObject value=store.read();
                return new org.json.JSONObject().put("end",value.optLong("restEndAt"))
                    .put("duration",value.optLong("restDurationMs")).put("started",value.optLong("restStartedAt")).toString();
            }catch(RuntimeException|org.json.JSONException invalid){return null;}
        }
        @JavascriptInterface public boolean floatingRestVisible() {
            return RestTimerService.isFloatingVisible();
        }
        @JavascriptInterface public void previewRestSequence(int seconds,int pulseMs,int finishMs,boolean sound,int volume) {
            if((seconds!=0&&seconds!=3&&seconds!=5)||pulseMs<0||pulseMs>500||finishMs<0||finishMs>2000||volume<0||volume>100)return;
            runOnUiThread(()->{if(visible&&!isFinishing())playRestPreview(seconds,pulseMs,finishMs,sound,volume);});
        }
        @JavascriptInterface public void stopRestSequence() {
            runOnUiThread(MainActivity.this::stopRestPreview);
        }
        @JavascriptInterface public void restComplete() { RestAlarm.complete(MainActivity.this); }
        @JavascriptInterface public void previewVibration(int ms) {
            if(ms<1||ms>2000)return;
            runOnUiThread(()->{
                Vibrator vibrator=(Vibrator)getSystemService(VIBRATOR_SERVICE);
                if(vibrator!=null&&vibrator.hasVibrator())vibrator.vibrate(VibrationEffect.createOneShot(ms,VibrationEffect.DEFAULT_AMPLITUDE));
            });
        }
        @JavascriptInterface public boolean preciseTimers() { return RestAlarm.precise(MainActivity.this); }
        @JavascriptInterface public void allowPreciseTimers() {
            if(Build.VERSION.SDK_INT>=31)runOnUiThread(()->{
                try { startActivity(new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+getPackageName()))); }
                catch(ActivityNotFoundException ignored) { }
            });
        }
        @JavascriptInterface public boolean overlayAllowed() {
            return android.provider.Settings.canDrawOverlays(MainActivity.this);
        }
        @JavascriptInterface public void allowOverlay(boolean requestNotificationsNext) {
            runOnUiThread(()->{
                try {
                    requestNotificationsAfterOverlay=requestNotificationsNext;
                    startActivity(new Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:"+getPackageName())));
                }catch(ActivityNotFoundException unavailable){
                    requestNotificationsAfterOverlay=false;
                    if(requestNotificationsNext)askForNotificationPermission();
                    Toast.makeText(MainActivity.this,"Allow Exercise under Display over other apps in Android Settings.",Toast.LENGTH_LONG).show();
                }
            });
        }
        @JavascriptInterface public void requestNotifications() {
            runOnUiThread(MainActivity.this::askForNotificationPermission);
        }
        @JavascriptInterface public void setThemeColors(String first,String last) {
            if(first==null||last==null||!first.matches("#[a-fA-F0-9]{6}")||!last.matches("#[a-fA-F0-9]{6}"))return;
            runOnUiThread(()->{top=Color.parseColor(first);bottom=Color.parseColor(last);bars();});
        }
        @JavascriptInterface public void setModalOpen(boolean open) { runOnUiThread(()->{modal=open;bars();}); }
        @JavascriptInterface public void showKeyboard() {
            final int request=keyboardRequest.incrementAndGet();
            runOnUiThread(()->{
                if(web==null||isFinishing()||request!=keyboardRequest.get())return;
                web.requestFocus();
                web.postDelayed(()->{
                    if(web!=null&&!isFinishing()&&request==keyboardRequest.get())
                        ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(web,InputMethodManager.SHOW_IMPLICIT);
                },80);
            });
        }
        @JavascriptInterface public void hideKeyboard() {
            final int request=keyboardRequest.incrementAndGet();
            runOnUiThread(()->{
                if(web!=null&&request==keyboardRequest.get())
                    ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(web.getWindowToken(),0);
            });
        }
        @JavascriptInterface public String spotifyState() { return SpotifyAccess.state(MainActivity.this).toString(); }
        @JavascriptInterface public void spotifyAccess() {
            runOnUiThread(()->startActivity(new Intent(android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)));
        }
        @JavascriptInterface public void openSpotify() {
            runOnUiThread(()->{
                Intent launch=getPackageManager().getLaunchIntentForPackage("com.spotify.music");
                if(launch!=null)startActivity(launch);
                else Toast.makeText(MainActivity.this,"Install Spotify to use the player.",Toast.LENGTH_SHORT).show();
            });
        }
        @JavascriptInterface public void spotifyCommand(String command) {
            runOnUiThread(()->SpotifyAccess.command(MainActivity.this,command));
        }
        @JavascriptInterface public void ready() { runOnUiThread(()->{ready=true;if(web!=null)web.setAlpha(1);openRestControlsIfReady();}); }
    }
    @android.annotation.TargetApi(33) private static final class BackApi33 {
        static Object register(Activity activity,Runnable action) {
            android.window.OnBackInvokedCallback callback=action::run;
            activity.getOnBackInvokedDispatcher().registerOnBackInvokedCallback(0,callback);return callback;
        }
        static void unregister(Activity activity,Object callback) {
            activity.getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback((android.window.OnBackInvokedCallback)callback);
        }
    }
}
