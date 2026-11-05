package com.personal.tasklist;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneId;

/** Offline shell for the original task system, with native persistence and widgets. */
public class MainActivity extends Activity {
    private static final int EXPORT_BACKUP = 201;
    private ReferenceStore reference;
    private SyncEngine desktopSync;
    private String pendingPairUrl,pendingPairToken;
    private volatile String bridgeBase;
    private final android.os.Handler syncUi=new android.os.Handler(android.os.Looper.getMainLooper());
    private boolean syncReloadPending;
    private WebView web;
    private FrameLayout root;
    private Object backCallback;
    private final TaskBridge bridge = new TaskBridge();
    private boolean pageReady, destroyed, addRequested;
    private String contextFilter;
    private String editRequested;
    private boolean modalOpen;
    private long pageGeneration;
    private int themeColor=Color.rgb(16,21,31);
    protected boolean isWidgetEditor() { return false; }
    private volatile long loadedRevision;
    private String loadedDay = "";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        reference = new ReferenceStore(this);
        if(!isWidgetEditor())desktopSync=new SyncEngine(this,this::queueSyncedReload);
        root = new FrameLayout(this);
        root.setBackgroundColor(isWidgetEditor() ? Color.TRANSPARENT : Color.rgb(16, 21, 31));
        if(isWidgetEditor())updateModalBackdrop();
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets;
        });
        web = new WebView(this);web.setVerticalScrollBarEnabled(false);web.setHorizontalScrollBarEnabled(false);
        web.setBackgroundColor(isWidgetEditor() ? Color.TRANSPARENT : Color.rgb(16, 21, 31));
        if(isWidgetEditor())web.setVisibility(android.view.View.INVISIBLE);
        root.addView(web, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
        root.requestApplyInsets();
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setBlockNetworkLoads(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(false);
        settings.setSupportMultipleWindows(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        web.addJavascriptInterface(bridge, "TaskListNative");
        web.setWebChromeClient(new WebChromeClient());
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) { return true; }
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return new WebResourceResponse("text/plain", "UTF-8", new ByteArrayInputStream(new byte[0]));
            }
            @Override public void onPageFinished(WebView view, String url) {
                if (destroyed || pageReady) return;
                pageReady = true;
                loadedRevision = reference.revision();
                applyIntent();
                if (!isWidgetEditor()){revealReadyPage();requestReminderPermission(false);}
            }
        });
        readIntent(getIntent());
        loadTaskList();
        if (android.os.Build.VERSION.SDK_INT >= 33)
            backCallback = BackApi33.register(this, this::handleBackPress);
    }

    @Override public void onBackPressed() { handleBackPress(); }

    private void handleBackPress() {
        if (web == null || !pageReady) { finish(); return; }
        web.evaluateJavascript("Boolean(window.TaskListApp&&window.TaskListApp.handleBack())", result -> {
            if (!destroyed && !"true".equals(result)) finish();
        });
    }

    @android.annotation.TargetApi(33)
    private static final class BackApi33 {
        static Object register(Activity activity, Runnable action) {
            android.window.OnBackInvokedCallback callback = action::run;
            activity.getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT, callback);
            return callback;
        }
        static void unregister(Activity activity, Object callback) {
            activity.getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(
                    (android.window.OnBackInvokedCallback) callback);
        }
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        readIntent(intent);
        if (reference.revision() != loadedRevision) loadTaskList();
        else applyIntent();
    }

    private void readIntent(Intent intent) {
        android.net.Uri uri=intent.getData();
        if(!isWidgetEditor()&&uri!=null&&"tasks".equals(uri.getScheme())&&"pair".equals(uri.getHost())){
            intent.setData(null);
            try{pendingPairUrl=SyncEngine.validateEndpoint(uri.getQueryParameter("url"));pendingPairToken=SyncEngine.validateToken(uri.getQueryParameter("token"));}
            catch(IllegalArgumentException invalid){message("This PC pairing link is invalid.");}
        }
        if(intent.getBooleanExtra("widget_haptic",false)){intent.removeExtra("widget_haptic");WidgetFeedback.tick(this);}
        String label = intent.getStringExtra("widget_label");
        contextFilter = intent.hasExtra("widget_label") ? (label == null ? "" : label) : null;
        addRequested = intent.getBooleanExtra("add_task", false);
        editRequested = isWidgetEditor() ? intent.getStringExtra(TaskWidgetProvider.EXTRA_TASK_ID) : null;
    }

    private void applyIntent() {
        if (!pageReady || web == null) return;
        if (isWidgetEditor()) {
            String taskId = editRequested; editRequested = null;
            if (taskId == null) { finish(); return; }
            web.evaluateJavascript("Boolean(window.TaskListApp&&window.TaskListApp.editWidgetTask(" + JSONObject.quote(taskId) + "))",
                    result -> { if(destroyed)return; if ("true".equals(result))revealReadyPage(); else { message("This task is no longer available."); finish(); } });
            return;
        }
        if(pendingPairUrl!=null){String url=pendingPairUrl,token=pendingPairToken;pendingPairUrl=null;pendingPairToken=null;confirmPC(url,token);}
        web.evaluateJavascript("window.TaskListApp&&window.TaskListApp.showToday()", null);
        if (contextFilter != null) web.evaluateJavascript("window.TaskListApp&&window.TaskListApp.setContextFilter(" + JSONObject.quote(contextFilter) + ")", null);
        if (addRequested) {
            addRequested = false;
            JSONObject values = new JSONObject();
            try {
                JSONArray labels = new JSONArray();
                if (contextFilter != null && !contextFilter.isEmpty()) labels.put(contextFilter);
                values.put("labels", labels);
            } catch (JSONException ignored) { }
            web.evaluateJavascript("window.TaskListApp&&window.TaskListApp.addTask(" + values + ")", null);
        }
    }

    @Override protected void onResume() {
        super.onResume();if(desktopSync!=null)desktopSync.start();WidgetCompletions.settleExpired(this);TaskReminders.reschedule(this);
        if (web != null) {
            web.onResume();
            if (pageReady && (reference.revision() != loadedRevision || !loadedDay.equals(LocalDate.now(ZoneId.of("America/New_York")).toString()))) loadTaskList();
            else if(pageReady&&!isWidgetEditor())web.evaluateJavascript("window.TaskListApp&&window.TaskListApp.showToday()",null);
        }
    }
    @Override protected void onPause() { if(desktopSync!=null)desktopSync.stop(); if (web != null) web.onPause(); super.onPause(); }

    private void queueSyncedReload(){
        if(destroyed||syncReloadPending)return;syncReloadPending=true;syncUi.post(this::applySyncedReload);
    }
    private void applySyncedReload(){
        if(destroyed){syncReloadPending=false;return;}
        if(web==null||!pageReady||modalOpen){syncUi.postDelayed(this::applySyncedReload,250);return;}
        web.evaluateJavascript("Boolean(document.querySelector('dialog[open],.tm-completing')||document.querySelector('.tl-settings-screen')||document.querySelector('.tm-view-animating'))",blocked->{
            if(destroyed)return;if("true".equals(blocked)){syncUi.postDelayed(this::applySyncedReload,250);return;}
            syncReloadPending=false;web.evaluateJavascript("window.TaskListApp&&window.TaskListApp.reloadFromNative()",null);loadedRevision=reference.revision();
        });
    }
    private void confirmPC(String url,String token){
        if(destroyed||desktopSync==null)return;
        ControlAlignment.dialog(new AlertDialog.Builder(this).setTitle("Link this PC?")
            .setMessage("Connect Tasks to "+url+" on your Wi-Fi. Your task completions and in-progress marks will sync while Tasks is open on both devices.")
            .setNegativeButton("Cancel",null).setPositiveButton("Link PC",(dialog,which)->{desktopSync.pair(url,token);desktopSync.requestSync();message("Connecting to your PC…");}).show());
    }
    private void enterPCLink(){
        android.widget.EditText input=new android.widget.EditText(this);input.setHint("Paste the pairing link from your PC");input.setSingleLine(true);input.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_URI);
        android.widget.FrameLayout panel=new android.widget.FrameLayout(this);int pad=Math.round(20*getResources().getDisplayMetrics().density);panel.setPadding(pad,0,pad,0);panel.addView(input);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Link PC").setMessage("Open Tasks on Windows, choose Link phone, and paste its pairing link here. You can also scan its QR code with your camera.").setView(panel).setNegativeButton("Cancel",null).setPositiveButton("Continue",null).create();dialog.setOnShowListener(ignored->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view->{try{
            android.net.Uri uri=android.net.Uri.parse(input.getText().toString().trim());String url,token;
            if("tasks".equals(uri.getScheme())&&"pair".equals(uri.getHost())){url=uri.getQueryParameter("url");token=uri.getQueryParameter("token");}
            else {url=new java.net.URI(uri.getScheme(),null,uri.getHost(),uri.getPort(),null,null,null).toString();token=uri.getFragment();if(!"/pair".equals(uri.getPath()))throw new IllegalArgumentException();}
            url=SyncEngine.validateEndpoint(url);token=SyncEngine.validateToken(token);dialog.dismiss();confirmPC(url,token);
        }catch(Exception invalid){input.setError("Paste the full pairing link shown on your PC.");}}));dialog.show();ControlAlignment.dialog(dialog);
    }

    private String asset(String name) throws Exception {
        try (InputStream in = getAssets().open(name); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192]; int count;
            while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count);
            return out.toString(StandardCharsets.UTF_8.name());
        }
    }

    private void loadTaskList() {
        try {
            pageReady = false;pageGeneration++;
            web.setAlpha(0f);
            if (reference.readState().equals("{}")) {
                String seed = asset("www/initial-state.json").trim();
                if (!seed.isEmpty() && !seed.equals("{}")) reference.writeState(seed);
            }
            String html = asset("www/index.html");

            if (isWidgetEditor()) html = html.replaceFirst("(?i)<head>", "<head><script>window.TaskListWidgetEditor=true;</script><style>body>main,#tasks-app-backdrop{display:none!important}html,body{min-height:100%!important}</style>");
            int head = html.toLowerCase(java.util.Locale.ROOT).indexOf("</head>");
            if (!html.contains("global.TaskListStorage")){String script="<script>"+asset("bridge.js")+"</script>";html=head>=0?html.substring(0,head)+script+html.substring(head):script+html;}
            loadedDay = LocalDate.now(ZoneId.of("America/New_York")).toString();
            loadedRevision = reference.revision();
            web.loadDataWithBaseURL("https://tasklist.local/", html, "text/html", "UTF-8", null);
        } catch (Exception error) {
            ControlAlignment.dialog(new AlertDialog.Builder(this).setTitle("Couldn’t open your task list")
                    .setMessage("Close the app and try opening it again.").setPositiveButton("OK", null).show());
        }
    }

    private void message(String value) {
        runOnUiThread(() -> { if (!destroyed) Toast.makeText(this, value, Toast.LENGTH_LONG).show(); });
    }

    private void requestReminderPermission(boolean explicit) {
        if(android.os.Build.VERSION.SDK_INT<33)return;
        if(checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)==android.content.pm.PackageManager.PERMISSION_GRANTED)return;
        android.content.SharedPreferences prefs=TaskReminders.prefs(this);
        if(!explicit&&prefs.getBoolean("permissionRequested",false))return;
        prefs.edit().putBoolean("permissionRequested",true).apply();
        requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS},302);
    }

    private void revealReadyPage(){
        web.setVisibility(android.view.View.VISIBLE);
        web.postVisualStateCallback(pageGeneration,new WebView.VisualStateCallback(){@Override public void onComplete(long requestId){if(!destroyed&&pageReady&&requestId==pageGeneration)web.setAlpha(1f);}});
    }
    private void updateModalBackdrop(){
        boolean dim=isWidgetEditor()||modalOpen;
        root.setForeground(dim?new StatusBarScrim(root):null);
        int color=isWidgetEditor()?StatusBarScrim.COLOR:dim?Color.rgb(Math.round(Color.red(themeColor)*71f/255),Math.round(Color.green(themeColor)*71f/255),Math.round(Color.blue(themeColor)*71f/255)):themeColor;
        getWindow().setStatusBarColor(color);getWindow().setNavigationBarColor(color);
        int light=android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
        int flags=getWindow().getDecorView().getSystemUiVisibility()&~light;
        if(!dim&&Color.luminance(color)>.5)flags|=light;
        getWindow().getDecorView().setSystemUiVisibility(flags);
    }
    /** Only the packaged offline document receives this narrow native interface. */
    public final class TaskBridge {
        @JavascriptInterface public void appReady(){if(!isWidgetEditor())runOnUiThread(()->{if(destroyed||pageReady)return;pageReady=true;loadedRevision=reference.revision();applyIntent();revealReadyPage();requestReminderPermission(false);});}
        @JavascriptInterface public void editorReady(){if(isWidgetEditor())runOnUiThread(()->{if(destroyed||pageReady)return;pageReady=true;loadedRevision=reference.revision();applyIntent();});}
        @JavascriptInterface public void setModalOpen(boolean open){runOnUiThread(()->{if(destroyed)return;modalOpen=open;updateModalBackdrop();});}
        @JavascriptInterface public void hideKeyboard() { runOnUiThread(()->{if(destroyed||web==null)return;android.view.inputmethod.InputMethodManager keyboard=(android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);if(keyboard!=null)keyboard.hideSoftInputFromWindow(web.getWindowToken(),0);}); }
        @JavascriptInterface public void finishTaskEditor() { if(isWidgetEditor())runOnUiThread(()->{if(!destroyed)finish();}); }
        @JavascriptInterface public void actionHaptic() { runOnUiThread(()->{if(!destroyed&&web!=null)web.performHapticFeedback(android.view.HapticFeedbackConstants.CLOCK_TICK);}); }
        @JavascriptInterface public void priorityHaptic() { runOnUiThread(()->{if(!destroyed&&web!=null)web.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);}); }
        @JavascriptInterface public String readState() { String value=reference.readState();bridgeBase=value;return value; }
        @JavascriptInterface public void writeState(String json) {
            try{JSONObject desired=new JSONObject(json),base=new JSONObject(bridgeBase==null?reference.readState():bridgeBase);boolean saved=false;
                for(int retry=0;retry<16;retry++){long revision=reference.revision();JSONObject current=new JSONObject(reference.readState()),merged=StateMerge.merge(base,desired,current);if(reference.compareAndWriteState(revision,merged.toString())){saved=true;break;}}
                if(!saved)throw new IllegalStateException("Tasks changed while saving. Please try again.");bridgeBase=json;loadedRevision=reference.revision();TaskReminders.reschedule(MainActivity.this);if(desktopSync!=null)desktopSync.requestSync();
            }catch(JSONException invalid){throw new IllegalArgumentException("The task update could not be read.",invalid);}
        }
        @JavascriptInterface public void connectPC(){runOnUiThread(()->{if(desktopSync!=null)enterPCLink();});}
        @JavascriptInterface public String getSyncStatus(){return desktopSync==null?"{}":desktopSync.status();}
        @JavascriptInterface public void syncNow(){if(desktopSync!=null)desktopSync.requestSync();}
        @JavascriptInterface public void disconnectPC(){if(desktopSync!=null)desktopSync.unpair();}
        @JavascriptInterface public void updateReminders(String json) { TaskReminders.definitions(MainActivity.this,json); }
        @JavascriptInterface public void requestNotificationPermission() { runOnUiThread(()->requestReminderPermission(true)); }
        @JavascriptInterface public String notificationStatus() { if(!TaskReminders.allowed(MainActivity.this))return "Notifications are blocked in Android settings";android.app.NotificationChannel channel=getSystemService(android.app.NotificationManager.class).getNotificationChannel(TaskReminders.CHANNEL);return channel!=null&&channel.getImportance()==android.app.NotificationManager.IMPORTANCE_NONE?"Scheduled task reminders are blocked in Android settings":"Notifications allowed"; }
        @JavascriptInterface public String exactAlarmStatus() { return TaskReminders.precise(MainActivity.this)?"Precise reminders enabled":"Precise reminders need Android permission; reminders may be delayed"; }
        @JavascriptInterface public boolean exactAlarmsNeedPermission() { return !TaskReminders.precise(MainActivity.this); }
        @JavascriptInterface public void openExactAlarmSettings() { if(android.os.Build.VERSION.SDK_INT>=31)runOnUiThread(()->{try{startActivity(new Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,android.net.Uri.parse("package:"+getPackageName())));}catch(android.content.ActivityNotFoundException unavailable){startActivity(new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,android.net.Uri.parse("package:"+getPackageName())));}}); }
        @JavascriptInterface public void openNotificationSettings() { runOnUiThread(()->startActivity(new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(android.provider.Settings.EXTRA_APP_PACKAGE,getPackageName()))); }
        @JavascriptInterface public void updateSnapshot(String json) { reference.updateSnapshot(json); }
        @JavascriptInterface public void setThemeColors(String background) {
            if (background == null || !background.matches("#[0-9a-fA-F]{6}")) return;
            final int color = Color.parseColor(background);
            runOnUiThread(() -> {
                if (destroyed) return;
                if(isWidgetEditor())return;
                root.setBackgroundColor(color); web.setBackgroundColor(color);
                themeColor=color;updateModalBackdrop();
                TaskWidgetProvider.refreshAll(MainActivity.this);
            });
        }
        @JavascriptInterface public void copyText(String text) {
            if (text == null || text.length() > 2 * 1024 * 1024) return;
            runOnUiThread(() -> {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("Task List progress", text));
                message("Copied to clipboard");
            });
        }
        @JavascriptInterface public void exportBackup(String json) {
            if (json == null || json.length() > 2 * 1024 * 1024) return;
            try { new JSONObject(json); }
            catch (JSONException error) { message("The backup could not be created."); return; }
            runOnUiThread(() -> {
                try (FileOutputStream out = new FileOutputStream(new File(getCacheDir(), "task-list-export.json"))) {
                    out.write(json.getBytes(StandardCharsets.UTF_8));
                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE)
                            .setType("application/json").putExtra(Intent.EXTRA_TITLE,
                                "TaskList-backup-" + LocalDate.now(ZoneId.of("America/New_York")) + ".json");
                    startActivityForResult(intent, EXPORT_BACKUP);
                } catch (Exception error) { message("The backup could not be created."); }
            });
        }
        @JavascriptInterface public void reloadTaskList() { runOnUiThread(() -> loadTaskList()); }
        @JavascriptInterface public void showWidgetSettings() {
            runOnUiThread(() -> { if (!destroyed) showWidgetHelp(); });
        }
    }
    TaskBridge taskBridgeForTests() { return bridge; }
    WebView webForTests() { return web; }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != EXPORT_BACKUP) return;
        File pending = new File(getCacheDir(), "task-list-export.json");
        if (resultCode == RESULT_OK && data != null && data.getData() != null && pending.isFile()) {
            try (InputStream in = new java.io.FileInputStream(pending);
                 OutputStream out = getContentResolver().openOutputStream(data.getData(), "wt")) {
                if (out == null) throw new java.io.IOException("No destination");
                byte[] buffer = new byte[8192]; int count;
                while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count);
                message("Backup saved");
            } catch (Exception error) { message("The backup could not be saved. Please try again."); }
        }
        pending.delete();
    }

    private void showWidgetHelp() {
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        AlertDialog.Builder dialog = new AlertDialog.Builder(this).setTitle("Home-screen checklist")
                .setMessage("Touch and hold an empty home-screen area, choose Widgets, then add Tasks checklist. "
                    + "Touch and hold the widget to choose its context, colors, and transparency. Scroll to see all of today’s open tasks. "
                    + "Ticking a task in the widget also saves its completion in this task list.")
                .setNegativeButton("Close", null);
        if (manager.isRequestPinAppWidgetSupported()) dialog.setPositiveButton("Add widget", (d, which) -> {
            try { manager.requestPinAppWidget(new ComponentName(this, TaskWidgetProvider.class), null, null); }
            catch (RuntimeException error) { message("Add the widget from your home screen’s Widgets menu."); }
        });
        ControlAlignment.dialog(dialog.show());
    }

    @Override protected void onDestroy() {
        destroyed = true;
        if(desktopSync!=null)desktopSync.close();syncUi.removeCallbacksAndMessages(null);
        if (android.os.Build.VERSION.SDK_INT >= 33 && backCallback != null)
            BackApi33.unregister(this, backCallback);
        if (web != null) {
            web.removeJavascriptInterface("TaskListNative"); web.stopLoading(); web.destroy(); web = null;
        }
        super.onDestroy();
    }
}
