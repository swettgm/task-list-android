package com.personal.routemanager;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.webkit.*;
import android.widget.FrameLayout;
import android.widget.Toast;
import java.io.*;
import java.nio.charset.StandardCharsets;
/** Small offline WebView shell; no account, service, widget or reminder permissions. */
public final class MainActivity extends Activity {
    private final Bridge bridge=new Bridge();private Object backCallback;private WebView web;private FrameLayout root;private NotesStore store;private boolean modal,ready;private int top=Color.rgb(32,55,71),bottom=Color.rgb(90,92,97);
    @Override public void onCreate(Bundle saved){super.onCreate(saved);store=new NotesStore(this);root=new FrameLayout(this);root.setBackgroundColor(top);root.setOnApplyWindowInsetsListener((view,insets)->{view.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets;});
        web=new WebView(this);web.setBackgroundColor(top);web.setAlpha(0);web.setVerticalScrollBarEnabled(false);web.setHorizontalScrollBarEnabled(false);root.addView(web,new FrameLayout.LayoutParams(-1,-1));setContentView(root);root.requestApplyInsets();
        WebSettings settings=web.getSettings();settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(false);settings.setAllowFileAccess(false);settings.setAllowContentAccess(false);settings.setBlockNetworkLoads(true);settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);settings.setSupportMultipleWindows(false);settings.setJavaScriptCanOpenWindowsAutomatically(false);
        web.addJavascriptInterface(bridge,"RouteNative");web.setWebChromeClient(new WebChromeClient());web.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){android.net.Uri uri=request.getUrl();if(request.isForMainFrame()&&request.hasGesture()&&("https".equalsIgnoreCase(uri.getScheme())||"http".equalsIgnoreCase(uri.getScheme()))&&uri.getHost()!=null&&!"route-manager.local".equalsIgnoreCase(uri.getHost()))try{startActivity(new Intent(Intent.ACTION_VIEW,uri).addCategory(Intent.CATEGORY_BROWSABLE));}catch(android.content.ActivityNotFoundException|SecurityException missing){Toast.makeText(MainActivity.this,"No browser is available to open this link.",Toast.LENGTH_SHORT).show();}return true;}
            @Override public WebResourceResponse shouldInterceptRequest(WebView view,WebResourceRequest request){return new WebResourceResponse("text/plain","UTF-8",new ByteArrayInputStream(new byte[0]));}
        });
        try(InputStream in=getAssets().open("index.html")){ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int read;while((read=in.read(buffer))!=-1)bytes.write(buffer,0,read);web.loadDataWithBaseURL("https://route-manager.local/",bytes.toString(StandardCharsets.UTF_8.name()),"text/html","UTF-8",null);}catch(IOException failed){Toast.makeText(this,"Could not open Route Notes.",Toast.LENGTH_LONG).show();finish();}
        if(android.os.Build.VERSION.SDK_INT>=33)backCallback=BackApi33.register(this,this::handleBack);
    }
    @Override public void onBackPressed(){handleBack();}
    private void handleBack(){if(web==null||!ready){finish();return;}web.evaluateJavascript("Boolean(window.RouteManager&&RouteManager.back())",value->{if(!isFinishing()&&!"true".equals(value))finish();});}
    @Override protected void onResume(){super.onResume();if(ready&&web!=null)web.evaluateJavascript("window.RouteManager&&RouteManager.openToday()",null);}
    @Override protected void onDestroy(){if(android.os.Build.VERSION.SDK_INT>=33&&backCallback!=null)BackApi33.unregister(this,backCallback);if(web!=null){web.removeJavascriptInterface("RouteNative");web.destroy();web=null;}super.onDestroy();}
    private static int dim(int color){return Color.rgb((int)(Color.red(color)*.45),(int)(Color.green(color)*.45),(int)(Color.blue(color)*.45));}
    private void bars(){int a=modal?dim(top):top,b=modal?dim(bottom):bottom;getWindow().setStatusBarColor(a);getWindow().setNavigationBarColor(b);root.setBackgroundColor(top);int flags=0;if(Color.red(a)*.299+Color.green(a)*.587+Color.blue(a)*.114>160)flags|=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;if(Color.red(b)*.299+Color.green(b)*.587+Color.blue(b)*.114>160)flags|=View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;getWindow().getDecorView().setSystemUiVisibility(flags);}
    final class Bridge {
        @JavascriptInterface public String readState(){try{return store.read();}catch(RuntimeException invalid){return null;}}
        @JavascriptInterface public boolean writeState(String raw){try{store.write(raw);return true;}catch(RuntimeException failed){return false;}}
        private boolean exportable(String text){return text!=null&&!text.trim().isEmpty()&&text.length()<=200000;}
        @JavascriptInterface public boolean copyText(String text){if(!exportable(text))return false;try{android.content.ClipboardManager clipboard=(android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);if(clipboard==null)return false;clipboard.setPrimaryClip(android.content.ClipData.newPlainText("Route Notes stops",text));return true;}catch(RuntimeException failed){return false;}}
        @JavascriptInterface public boolean shareText(String text){if(!exportable(text))return false;runOnUiThread(()->{if(isFinishing())return;try{Intent send=new Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT,text);startActivity(Intent.createChooser(send,"Share stop list"));}catch(android.content.ActivityNotFoundException|SecurityException failed){Toast.makeText(MainActivity.this,"Sharing is unavailable. Use Copy instead.",Toast.LENGTH_SHORT).show();}});return true;}
        @JavascriptInterface public void haptic(){runOnUiThread(()->{if(web!=null)web.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);});}
        @JavascriptInterface public void setThemeColors(String first,String last){if(first==null||last==null||!first.matches("#[0-9A-Fa-f]{6}")||!last.matches("#[0-9A-Fa-f]{6}"))return;runOnUiThread(()->{top=Color.parseColor(first);bottom=Color.parseColor(last);bars();});}
        @JavascriptInterface public void setModalOpen(boolean open){runOnUiThread(()->{modal=open;bars();});}
        @JavascriptInterface public void showKeyboard(){runOnUiThread(()->{if(web!=null){web.requestFocus();web.postDelayed(()->{if(web!=null&&!isFinishing())((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(web,InputMethodManager.SHOW_IMPLICIT);},60);}});}
        @JavascriptInterface public void hideKeyboard(){runOnUiThread(()->{if(web!=null)((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(web.getWindowToken(),0);});}
        @JavascriptInterface public void ready(){runOnUiThread(()->{ready=true;if(web!=null)web.setAlpha(1);});}
    }
    @android.annotation.TargetApi(33) private static final class BackApi33 {
        static Object register(Activity activity,Runnable action){android.window.OnBackInvokedCallback callback=action::run;activity.getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,callback);return callback;}
        static void unregister(Activity activity,Object callback){activity.getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback((android.window.OnBackInvokedCallback)callback);}
    }
    WebView webForTests(){return web;}Bridge bridgeForTests(){return bridge;}
}
