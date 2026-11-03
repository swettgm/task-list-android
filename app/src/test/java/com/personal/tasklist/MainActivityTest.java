package com.personal.tasklist;

import static org.junit.Assert.*;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.android.controller.ActivityController;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class MainActivityTest {
    private Context context;
    @Before public void reset() {
        context = RuntimeEnvironment.getApplication();
        context.getSharedPreferences("tasks", Context.MODE_PRIVATE).edit().clear().commit();
        context.getSharedPreferences("reference_state", Context.MODE_PRIVATE).edit().clear().commit();
    }
    @Test public void widgetEditorOpensExactTaskWithoutChangingStateAndClosesSeparately() {
        Intent intent=new Intent(context,TaskEditorActivity.class).putExtra(TaskWidgetProvider.EXTRA_TASK_ID,"html:serum:2026-11-03");
        ActivityController<TaskEditorActivity> controller=Robolectric.buildActivity(TaskEditorActivity.class,intent).setup();
        TaskEditorActivity activity=controller.get();String before=activity.taskBridgeForTests().readState();
        android.webkit.WebView web=activity.webForTests();web.getWebViewClient().onPageFinished(web,"https://tasklist.local/");
        assertTrue(Shadows.shadowOf(web).getLastEvaluatedJavascript().contains("editWidgetTask(\"html:serum:2026-11-03\")"));
        activity.taskBridgeForTests().setThemeColors("#ffffff");Shadows.shadowOf(Looper.getMainLooper()).idle();
        assertEquals(android.graphics.Color.TRANSPARENT,((android.graphics.drawable.ColorDrawable)((android.view.View)web.getParent()).getBackground()).getColor());
        assertEquals(before,activity.taskBridgeForTests().readState());
        activity.taskBridgeForTests().finishTaskEditor();Shadows.shadowOf(Looper.getMainLooper()).idle();assertTrue(activity.isFinishing());
        controller.pause().stop().destroy();
    }
    @Test public void editorCloseBridgeDoesNotCloseTheNormalApp() {
        ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).setup();
        controller.get().taskBridgeForTests().finishTaskEditor();Shadows.shadowOf(Looper.getMainLooper()).idle();
        assertFalse(controller.get().isFinishing());controller.pause().stop().destroy();
    }
    @Test public void priorityHoldUsesNativeLongPressHapticsWithoutChangingTaskState() {
        ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).setup();MainActivity activity=controller.get();String before=activity.taskBridgeForTests().readState();assertFalse(activity.webForTests().isVerticalScrollBarEnabled());assertFalse(activity.webForTests().isHorizontalScrollBarEnabled());
        activity.taskBridgeForTests().priorityHaptic();Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();assertEquals(android.view.HapticFeedbackConstants.LONG_PRESS,Shadows.shadowOf(activity.webForTests()).lastHapticFeedbackPerformed());assertEquals(before,activity.taskBridgeForTests().readState());controller.pause().stop().destroy();activity.taskBridgeForTests().priorityHaptic();Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
    }
    @Test public void widgetHeaderFeedbackIsConsumedOnceWithoutChangingLaunchBehavior() {
        android.os.Vibrator vibrator=(android.os.Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);Shadows.shadowOf(vibrator).setHasVibrator(true);android.provider.Settings.System.putInt(context.getContentResolver(),android.provider.Settings.System.HAPTIC_FEEDBACK_ENABLED,1);vibrator.cancel();
        Intent intent=new Intent(context,MainActivity.class).putExtra("widget_haptic",true);ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class,intent).setup();assertTrue(Shadows.shadowOf(vibrator).isVibrating());assertFalse(controller.get().getIntent().hasExtra("widget_haptic"));vibrator.cancel();controller.newIntent(new Intent(context,MainActivity.class));assertFalse(Shadows.shadowOf(vibrator).isVibrating());assertFalse(controller.get().isFinishing());controller.pause().stop().destroy();
    }
    @Test public void detailedEditorCanDismissTheQuickAddKeyboard() {
        ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).setup();MainActivity activity=controller.get();
        android.view.inputmethod.InputMethodManager keyboard=(android.view.inputmethod.InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        keyboard.showSoftInput(activity.webForTests(),android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);assertTrue(Shadows.shadowOf(keyboard).isSoftInputVisible());
        activity.taskBridgeForTests().hideKeyboard();Shadows.shadowOf(Looper.getMainLooper()).idle();assertFalse(Shadows.shadowOf(keyboard).isSoftInputVisible());
        controller.pause().stop().destroy();activity.taskBridgeForTests().hideKeyboard();Shadows.shadowOf(Looper.getMainLooper()).idle();
    }
    @Test public void actionFeedbackUsesALightTickWithoutChangingSavedTasks(){
        ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).setup();MainActivity activity=controller.get();String before=activity.taskBridgeForTests().readState();activity.taskBridgeForTests().actionHaptic();Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();assertEquals(android.view.HapticFeedbackConstants.CLOCK_TICK,Shadows.shadowOf(activity.webForTests()).lastHapticFeedbackPerformed());assertEquals(before,activity.taskBridgeForTests().readState());controller.pause().stop().destroy();activity.taskBridgeForTests().actionHaptic();Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
    }
    @Test public void nativeStateSurvivesActivityRecreation() throws Exception {
        ActivityController<MainActivity> first = Robolectric.buildActivity(MainActivity.class).setup();
        JSONObject state = new JSONObject().put("version", 1).put("completed",
                new JSONObject().put("task:2026-11-01", true)).put("customTasks", new JSONArray());
        first.get().taskBridgeForTests().writeState(state.toString());
        first.pause().stop().destroy();
        ActivityController<MainActivity> reopened = Robolectric.buildActivity(MainActivity.class).setup();
        JSONObject restored = new JSONObject(reopened.get().taskBridgeForTests().readState());
        assertTrue(restored.getJSONObject("completed").getBoolean("task:2026-11-01"));
        assertTrue(restored.has("customTasks"));
        reopened.pause().stop().destroy();
    }
    @Test public void progressCopyUsesTheAndroidClipboard() {
        ActivityController<MainActivity> activity = Robolectric.buildActivity(MainActivity.class).setup();
        activity.get().taskBridgeForTests().copyText("A saved progress update");
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        assertEquals("A saved progress update", clipboard.getPrimaryClip().getItemAt(0).getText().toString());
        activity.pause().stop().destroy();
    }
    @Test public void widgetShortcutCanResumeTheOfflineTaskScreen() {
        Intent intent = new Intent(context, MainActivity.class).putExtra("widget_label", "At home")
                .putExtra("widget_list", "My tasks").putExtra("add_task", true);
        ActivityController<MainActivity> activity = Robolectric.buildActivity(MainActivity.class, intent).setup();
        activity.newIntent(new Intent(context, MainActivity.class).putExtra("widget_label", "On the phone"));
        assertFalse(activity.get().isFinishing());
        assertEquals("On the phone", activity.get().getIntent().getStringExtra("widget_label"));
        activity.pause().stop().destroy();
    }
    @Test public void backChecksTheOpenTaskMenuBeforeLeavingTheActivity() {
        ActivityController<MainActivity> activity = Robolectric.buildActivity(MainActivity.class).setup();
        android.webkit.WebView web = activity.get().webForTests();
        web.getWebViewClient().onPageFinished(web, "https://tasklist.local/");
        activity.get().onBackPressed();
        assertTrue(Shadows.shadowOf(web).getLastEvaluatedJavascript().contains("handleBack"));
        assertFalse(activity.get().isFinishing());
        activity.pause().stop().destroy();
    }
    @Test public void validHexBackgroundUpdatesSystemBarsAndInvalidInputIsIgnored() {
        ActivityController<MainActivity> activity = Robolectric.buildActivity(MainActivity.class).setup();
        activity.get().taskBridgeForTests().setThemeColors("#ffffff");
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        assertEquals(android.graphics.Color.WHITE, activity.get().getWindow().getStatusBarColor());
        assertEquals(android.graphics.Color.WHITE, activity.get().getWindow().getNavigationBarColor());
        activity.get().taskBridgeForTests().setThemeColors("not a color");
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        assertEquals(android.graphics.Color.WHITE, activity.get().getWindow().getStatusBarColor());
        activity.pause().stop().destroy();
    }
}
