package com.personal.tasklist;

import static org.junit.Assert.*;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.shadows.ShadowAlertDialog;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class MainActivityTest {
    @Before public void clearTasks() {
        RuntimeEnvironment.getApplication().getSharedPreferences("tasks", Context.MODE_PRIVATE).edit().clear().commit();
    }

    @Test public void taskCanBeCreatedThroughNativeEditorAndSurvivesReopen() {
        ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).setup();
        MainActivity activity = controller.get();
        Button add = findButton(activity.getWindow().getDecorView(), "+  Add task");
        assertNotNull(add);
        add.performClick();
        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(dialog);
        EditText title = findFirstEditor(dialog.getWindow().getDecorView());
        assertNotNull(title);
        title.setText("Prepare for work");
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        assertFalse(dialog.isShowing());
        TaskStore store = new TaskStore(activity);
        assertEquals(1, store.all().size());
        assertEquals("Prepare for work", store.all().get(0).title);
        controller.pause().stop().destroy();

        ActivityController<MainActivity> reopened = Robolectric.buildActivity(MainActivity.class).setup();
        assertEquals("Prepare for work", new TaskStore(reopened.get()).all().get(0).title);
        reopened.pause().stop().destroy();
    }

    @Test public void widgetAddShortcutUsesItsChecklistWhenAppIsAlreadyOpen() {
        Context context = RuntimeEnvironment.getApplication();
        TaskStore store = new TaskStore(context);
        store.add("Existing work task", "Work", "");
        ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class).setup();
        controller.newIntent(new Intent(context, MainActivity.class)
                .putExtra("widget_list", "Work").putExtra("add_task", true));
        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull("The widget's plus shortcut must open the task editor", dialog);
        EditText title = findFirstEditor(dialog.getWindow().getDecorView());
        title.setText("Added from widget");
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        TaskStore.Task added = null;
        for (TaskStore.Task task : store.all()) if (task.title.equals("Added from widget")) added = task;
        assertNotNull(added);
        assertEquals("Work", added.listName);
        controller.pause().stop().destroy();
    }

    @Test public void widgetAddShortcutPreservesItsChecklistAfterLastTaskWasDeleted() {
        Context context = RuntimeEnvironment.getApplication();
        TaskStore store = new TaskStore(context);
        store.add("Old work task", "Work", "");
        store.delete(store.all().get(0).id);
        assertFalse("Deleted lists are currently absent from the store's derived names", store.lists().contains("Work"));
        Intent shortcut = new Intent(context, MainActivity.class)
                .putExtra("widget_list", "Work").putExtra("add_task", true);
        ActivityController<MainActivity> controller = Robolectric.buildActivity(MainActivity.class, shortcut).setup();
        org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle();
        AlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
        assertNotNull(dialog);
        EditText title = findFirstEditor(dialog.getWindow().getDecorView());
        title.setText("First task back in Work");
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).performClick();
        assertEquals(1, store.all().size());
        assertEquals("Work", store.all().get(0).listName);
        controller.pause().stop().destroy();
    }

    private static Button findButton(View view, String text) {
        if (view instanceof Button && text.contentEquals(((Button) view).getText())) return (Button) view;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                Button found = findButton(group.getChildAt(i), text);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static EditText findFirstEditor(View view) {
        if (view instanceof EditText) return (EditText) view;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                EditText found = findFirstEditor(group.getChildAt(i));
                if (found != null) return found;
            }
        }
        return null;
    }
}
