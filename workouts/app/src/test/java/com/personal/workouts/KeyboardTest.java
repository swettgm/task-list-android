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
}
