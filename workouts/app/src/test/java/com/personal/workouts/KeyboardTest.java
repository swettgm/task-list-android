package com.personal.workouts;

import android.content.Context;
import android.os.Looper;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import java.time.Duration;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class) @Config(sdk=28)
public class KeyboardTest {
    private ActivityController<MainActivity> controller;
    private MainActivity activity;
    private MainActivity.Bridge bridge;
    private InputMethodManager keyboard;
    @Before public void setup() {
        controller=Robolectric.buildActivity(MainActivity.class).setup();activity=controller.get();
        bridge=activity.new Bridge();keyboard=(InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
    }
    @After public void cleanup(){controller.pause().stop().destroy();}
    @Test public void editingCanShowAndHideTheAndroidKeyboard() {
        bridge.showKeyboard();shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(100));
        assertTrue(shadowOf(keyboard).isSoftInputVisible());
        bridge.hideKeyboard();shadowOf(Looper.getMainLooper()).idle();
        assertFalse(shadowOf(keyboard).isSoftInputVisible());
    }
    @Test public void aClosedEditorCannotBeReopenedByADelayedKeyboardRequest() {
        bridge.showKeyboard();bridge.hideKeyboard();
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(150));
        assertFalse(shadowOf(keyboard).isSoftInputVisible());
    }
    @Test public void aNewEditorWinsOverAnOldHideRequest() {
        bridge.hideKeyboard();bridge.showKeyboard();
        shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(150));
        assertTrue(shadowOf(keyboard).isSoftInputVisible());
    }
    @Test public void theWindowDoesNotForceTheKeyboardToRemainHidden() {
        int state=activity.getWindow().getAttributes().softInputMode&WindowManager.LayoutParams.SOFT_INPUT_MASK_STATE;
        assertNotEquals(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN,state);
    }
    @Test public void bridgeCommitsAndSchedulesRestFromAWorkerThread()throws Exception {
        WorkoutStore store=new WorkoutStore(activity);store.prefs.edit().clear().commit();
        org.json.JSONObject data=WorkoutStoreTest.fixture();data.put("active",WorkoutStoreTest.session(data));
        long end=System.currentTimeMillis()+90000;data.put("restStartedAt",end-90000).put("restDurationMs",90000).put("restEndAt",end);
        final String raw=data.toString();String[] saved={null};Thread worker=new Thread(()->saved[0]=bridge.writeState(raw));worker.start();worker.join();
        assertNotNull(saved[0]);assertEquals(end,store.read().getLong("restEndAt"));
        shadowOf(Looper.getMainLooper()).idle();assertEquals(end,store.prefs.getLong("alarmEnd",0));
        data=store.read();end+=1000;data.put("restEndAt",end);assertNotNull(bridge.writeState(data.toString()));
        shadowOf(Looper.getMainLooper()).idle();assertEquals(end,store.prefs.getLong("alarmEnd",0));
    }

}
