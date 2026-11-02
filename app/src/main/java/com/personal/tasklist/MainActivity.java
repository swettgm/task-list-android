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
public final class MainActivity extends Activity {
    private static final int EXPORT_BACKUP = 201;
    private ReferenceStore reference;
    private WebView web;
    private FrameLayout root;
    private Object backCallback;
    private final TaskBridge bridge = new TaskBridge();
    private boolean pageReady, destroyed, addRequested;
    private String contextFilter;
    private volatile long loadedRevision;
    private String loadedDay = "";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        reference = new ReferenceStore(this);
        root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(16, 21, 31));
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                    insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets;
        });
        web = new WebView(this);web.setVerticalScrollBarEnabled(false);web.setHorizontalScrollBarEnabled(false);
        web.setBackgroundColor(Color.rgb(16, 21, 31));
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
                if (destroyed) return;
                pageReady = true;
                loadedRevision = reference.revision();
                applyIntent();
                requestReminderPermission(false);
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
        String label = intent.getStringExtra("widget_label");
        contextFilter = intent.hasExtra("widget_label") ? (label == null ? "" : label) : null;
        addRequested = intent.getBooleanExtra("add_task", false);
    }

    private void applyIntent() {
        if (!pageReady || web == null) return;
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
        super.onResume();
        if (web != null) {
            web.onResume();
            if (pageReady && (reference.revision() != loadedRevision || !loadedDay.equals(LocalDate.now(ZoneId.of("America/New_York")).toString()))) loadTaskList();
            else if(pageReady)web.evaluateJavascript("window.TaskListApp&&window.TaskListApp.showToday()",null);
        }
    }
    @Override protected void onPause() { if (web != null) web.onPause(); super.onPause(); }

    private String asset(String name) throws Exception {
        try (InputStream in = getAssets().open(name); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192]; int count;
            while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count);
            return out.toString(StandardCharsets.UTF_8.name());
        }
    }

    private void loadTaskList() {
        try {
            pageReady = false;
            if (reference.readState().equals("{}")) {
                String seed = asset("www/initial-state.json").trim();
                if (!seed.isEmpty() && !seed.equals("{}")) reference.writeState(seed);
            }
            String html = asset("www/index.html");
            String script = "<script>" + asset("bridge.js") + "</script>";
            int head = html.toLowerCase(java.util.Locale.ROOT).indexOf("</head>");
            if (!html.contains("global.TaskListStorage")) html = head >= 0 ? html.substring(0, head) + script + html.substring(head) : script + html;
            loadedDay = LocalDate.now(ZoneId.of("America/New_York")).toString();
            loadedRevision = reference.revision();
            web.loadDataWithBaseURL("https://tasklist.local/", html, "text/html", "UTF-8", null);
        } catch (Exception error) {
            new AlertDialog.Builder(this).setTitle("Couldn’t open your task list")
                    .setMessage("Close the app and try opening it again.").setPositiveButton("OK", null).show();
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

    /** Only the packaged offline document receives this narrow native interface. */
    public final class TaskBridge {
        @JavascriptInterface public void priorityHaptic() { runOnUiThread(()->{if(!destroyed&&web!=null)web.performHapticFeedback(android.view.HapticFeedbackConstants.LONG_PRESS);}); }
        @JavascriptInterface public String readState() { return reference.readState(); }
        @JavascriptInterface public void writeState(String json) {
            reference.writeState(json); loadedRevision = reference.revision();
        }
        @JavascriptInterface public void updateReminders(String json) { TaskReminders.definitions(MainActivity.this,json); }
        @JavascriptInterface public void requestNotificationPermission() { runOnUiThread(()->requestReminderPermission(true)); }
        @JavascriptInterface public String notificationStatus() { return TaskReminders.allowed(MainActivity.this)?"Notifications allowed":"Notifications are blocked in Android settings"; }
        @JavascriptInterface public void openNotificationSettings() { runOnUiThread(()->startActivity(new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(android.provider.Settings.EXTRA_APP_PACKAGE,getPackageName()))); }
        @JavascriptInterface public void updateSnapshot(String json) { reference.updateSnapshot(json); }
        @JavascriptInterface public void setThemeColors(String background) {
            if (background == null || !background.matches("#[0-9a-fA-F]{6}")) return;
            final int color = Color.parseColor(background);
            runOnUiThread(() -> {
                if (destroyed) return;
                root.setBackgroundColor(color); web.setBackgroundColor(color);
                getWindow().setStatusBarColor(color); getWindow().setNavigationBarColor(color);
                int light = android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        | android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                int flags = getWindow().getDecorView().getSystemUiVisibility() & ~light;
                if (Color.luminance(color) > .5) flags |= light;
                getWindow().getDecorView().setSystemUiVisibility(flags);
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
        dialog.show();
    }

    @Override protected void onDestroy() {
        destroyed = true;
        if (android.os.Build.VERSION.SDK_INT >= 33 && backCallback != null)
            BackApi33.unregister(this, backCallback);
        if (web != null) {
            web.removeJavascriptInterface("TaskListNative"); web.stopLoading(); web.destroy(); web = null;
        }
        super.onDestroy();
    }
}
