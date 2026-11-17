package com.personal.crm;
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
/** Offline CRM with user-selected phone contacts and local reach-out reminders. */
public final class MainActivity extends Activity {
    private final java.util.concurrent.ExecutorService worker=java.util.concurrent.Executors.newSingleThreadExecutor();private String pendingPerson;private final Bridge bridge=new Bridge();private Object backCallback;private WebView web;private FrameLayout root;private CrmStore store;private boolean modal,ready;private int top=Color.rgb(32,55,71),bottom=Color.rgb(90,92,97);
    @Override public void onCreate(Bundle saved){super.onCreate(saved);store=new CrmStore(this);Reminders.channel(this);root=new FrameLayout(this);root.setBackgroundColor(top);root.setOnApplyWindowInsetsListener((view,insets)->{view.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets;});
        web=new WebView(this);web.setBackgroundColor(top);web.setAlpha(0);web.setVerticalScrollBarEnabled(false);web.setHorizontalScrollBarEnabled(false);root.addView(web,new FrameLayout.LayoutParams(-1,-1));setContentView(root);root.requestApplyInsets();
        WebSettings settings=web.getSettings();settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(false);settings.setAllowFileAccess(false);settings.setAllowContentAccess(false);settings.setBlockNetworkLoads(true);settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);settings.setSupportMultipleWindows(false);settings.setJavaScriptCanOpenWindowsAutomatically(false);
        web.addJavascriptInterface(bridge,"CrmNative");web.setWebChromeClient(new WebChromeClient());web.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){android.net.Uri uri=request.getUrl();if(request.isForMainFrame()&&request.hasGesture()&&("https".equalsIgnoreCase(uri.getScheme())||"http".equalsIgnoreCase(uri.getScheme()))&&uri.getHost()!=null&&!"crm.local".equalsIgnoreCase(uri.getHost()))try{startActivity(new Intent(Intent.ACTION_VIEW,uri).addCategory(Intent.CATEGORY_BROWSABLE));}catch(android.content.ActivityNotFoundException|SecurityException missing){Toast.makeText(MainActivity.this,"No browser is available to open this link.",Toast.LENGTH_SHORT).show();}return true;}
            @Override public WebResourceResponse shouldInterceptRequest(WebView view,WebResourceRequest request){return new WebResourceResponse("text/plain","UTF-8",new ByteArrayInputStream(new byte[0]));}
        });
        try(InputStream in=getAssets().open("index.html")){ByteArrayOutputStream bytes=new ByteArrayOutputStream();byte[] buffer=new byte[8192];int read;while((read=in.read(buffer))!=-1)bytes.write(buffer,0,read);web.loadDataWithBaseURL("https://crm.local/",bytes.toString(StandardCharsets.UTF_8.name()),"text/html","UTF-8",null);}catch(IOException failed){Toast.makeText(this,"Could not open CRM.",Toast.LENGTH_LONG).show();finish();}
        pendingPerson=getIntent().getStringExtra("personId");if(android.os.Build.VERSION.SDK_INT>=33)backCallback=BackApi33.register(this,this::handleBack);
    }
    @Override public void onBackPressed(){handleBack();}
    private void handleBack(){if(web==null||!ready){finish();return;}web.evaluateJavascript("Boolean(window.CRM&&CRM.back())",value->{if(!isFinishing()&&!"true".equals(value))finish();});}
    @Override protected void onResume(){super.onResume();if(ready&&web!=null)refreshContacts();Reminders.schedule(this);}
    @Override protected void onDestroy(){if(android.os.Build.VERSION.SDK_INT>=33&&backCallback!=null)BackApi33.unregister(this,backCallback);if(web!=null){web.removeJavascriptInterface("CrmNative");web.destroy();web=null;}worker.shutdown();super.onDestroy();}
    private static int dim(int color){return Color.rgb((int)(Color.red(color)*.45),(int)(Color.green(color)*.45),(int)(Color.blue(color)*.45));}
    private void bars(){int a=modal?dim(top):top,b=modal?dim(bottom):bottom;getWindow().setStatusBarColor(a);getWindow().setNavigationBarColor(b);root.setBackgroundColor(top);int flags=0;if(Color.red(a)*.299+Color.green(a)*.587+Color.blue(a)*.114>160)flags|=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;if(Color.red(b)*.299+Color.green(b)*.587+Color.blue(b)*.114>160)flags|=View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;getWindow().getDecorView().setSystemUiVisibility(flags);}
    private void event(String script){if(!isFinishing()&&web!=null&&ready)web.evaluateJavascript(script,null);}
    private void refreshContacts(){worker.execute(()->{Contacts.refresh(this);runOnUiThread(()->event("window.CRM&&CRM.refresh()"));});}
    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);pendingPerson=intent.getStringExtra("personId");openPending();}
    private void openPending(){if(ready&&pendingPerson!=null){String id=pendingPerson;pendingPerson=null;event("CRM.openPerson("+org.json.JSONObject.quote(id)+")");}}
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] results){super.onRequestPermissionsResult(request,permissions,results);if(request==40)event("CRM.contactsReady()");if(request==41){Reminders.schedule(this);event("CRM.refresh()");}}
    final class Bridge {
        @JavascriptInterface public boolean contactsAllowed(){return Contacts.allowed(MainActivity.this);}
        @JavascriptInterface public void requestContacts(){runOnUiThread(()->requestPermissions(new String[]{android.Manifest.permission.READ_CONTACTS},40));}
        @JavascriptInterface public String contacts(){try{return Contacts.list(MainActivity.this).toString();}catch(Exception e){return null;}}
        @JavascriptInterface public String contact(String key,String id){try{org.json.JSONObject contact=Contacts.info(MainActivity.this,key,id);return contact==null?null:contact.toString();}catch(Exception e){return null;}}
        @JavascriptInterface public boolean notificationsAllowed(){return Reminders.allowed(MainActivity.this);}
        @JavascriptInterface public void requestNotifications(){runOnUiThread(()->{if(android.os.Build.VERSION.SDK_INT>=33&&checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)!=android.content.pm.PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS},41);else startActivity(new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(android.provider.Settings.EXTRA_APP_PACKAGE,getPackageName()));});}
        @JavascriptInterface public long nextDate(long time,int count,String unit){if(count<1||count>365||!java.util.Arrays.asList("days","weeks","months").contains(unit))return 0;return Reminders.advance(time,count,unit,java.time.ZoneId.systemDefault());}
        @JavascriptInterface public boolean contacted(String id){return Reminders.contacted(MainActivity.this,id);}
        @JavascriptInterface public void openLink(String value){if(value==null||value.length()>600)return;android.net.Uri uri=android.net.Uri.parse(value);if(!java.util.Arrays.asList("tel","smsto","mailto").contains(uri.getScheme()))return;runOnUiThread(()->{try{startActivity(new Intent(Intent.ACTION_VIEW,uri));}catch(Exception e){Toast.makeText(MainActivity.this,"No app is available to open this.",Toast.LENGTH_SHORT).show();}});}

        @JavascriptInterface public String readState(){try{return store.read().toString();}catch(RuntimeException invalid){return null;}}
        @JavascriptInterface public boolean writeState(String raw){try{synchronized(CrmStore.LOCK){org.json.JSONObject data=new org.json.JSONObject(raw);if(data.optLong("revision")!=store.read().optLong("revision"))return false;store.write(data);}Reminders.schedule(MainActivity.this);return true;}catch(Exception failed){return false;}}
        @JavascriptInterface public void haptic(){runOnUiThread(()->{if(web!=null)web.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);});}
        @JavascriptInterface public void setThemeColors(String first,String last){if(first==null||last==null||!first.matches("#[0-9A-Fa-f]{6}")||!last.matches("#[0-9A-Fa-f]{6}"))return;runOnUiThread(()->{top=Color.parseColor(first);bottom=Color.parseColor(last);bars();});}
        @JavascriptInterface public void setModalOpen(boolean open){runOnUiThread(()->{modal=open;bars();});}
        @JavascriptInterface public void showKeyboard(){runOnUiThread(()->{if(web!=null){web.requestFocus();web.postDelayed(()->{if(web!=null&&!isFinishing())((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).showSoftInput(web,InputMethodManager.SHOW_IMPLICIT);},60);}});}
        @JavascriptInterface public void hideKeyboard(){runOnUiThread(()->{if(web!=null)((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(web.getWindowToken(),0);});}
        @JavascriptInterface public void ready(){runOnUiThread(()->{ready=true;if(web!=null)web.setAlpha(1);openPending();refreshContacts();});}
    }
    @android.annotation.TargetApi(33) private static final class BackApi33 {
        static Object register(Activity activity,Runnable action){android.window.OnBackInvokedCallback callback=action::run;activity.getOnBackInvokedDispatcher().registerOnBackInvokedCallback(android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT,callback);return callback;}
        static void unregister(Activity activity,Object callback){activity.getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback((android.window.OnBackInvokedCallback)callback);}
    }
    WebView webForTests(){return web;}Bridge bridgeForTests(){return bridge;}
}
