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
}
