package com.personal.tasklist;

import static org.junit.Assert.*;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
public class TaskWidgetTest {
    private Context context;
    private TaskStore store;

    @Before public void setUp() {
        context = RuntimeEnvironment.getApplication();
        context.getSharedPreferences("tasks", Context.MODE_PRIVATE).edit().clear().commit();
        context.deleteSharedPreferences("widget_17");
        context.deleteSharedPreferences("widget_29");
        store = new TaskStore(context);
    }

    @Test public void checklistFilterIncludesOnlyOpenTasksFromChosenList() {
        store.add("Personal task", "Personal", "");
        store.add("Work task", "Work", "");
        store.add("Completed work task", "Work", "");
        for (TaskStore.Task task : store.all()) if (task.title.startsWith("Completed")) store.complete(task.id);
        assertEquals(Arrays.asList("Work task"), titles(TaskWidgetProvider.selectedTasks(context, "Work", false)));
        List<String> allTitles = titles(TaskWidgetProvider.selectedTasks(context, "", false));
        assertEquals(2, allTitles.size());
        assertTrue(allTitles.contains("Personal task"));
        assertTrue(allTitles.contains("Work task"));
    }

    @Test public void todayViewIncludesOverdueButExcludesFutureUndatedAndDone() {
        LocalDate today = LocalDate.now();
        store.add("Overdue", "Work", today.minusDays(2).toString());
        store.add("Due today", "Work", today.toString());
        store.add("Tomorrow", "Work", today.plusDays(1).toString());
        store.add("Undated", "Work", "");
        store.add("Already done", "Work", today.toString());
        for (TaskStore.Task task : store.all()) if (task.title.equals("Already done")) store.complete(task.id);
        assertEquals(Arrays.asList("Overdue", "Due today"), titles(TaskWidgetProvider.selectedTasks(context, "Work", true)));
    }

    @Test public void eachWidgetKeepsItsOwnSettingsAndDeletionDoesNotAffectOthers() {
        TaskWidgetProvider.preferences(context, 17).edit().putString("title", "Work checklist").putString("list", "Work").putBoolean("dark", true).commit();
        TaskWidgetProvider.preferences(context, 29).edit().putString("title", "Personal").putString("list", "Personal").putInt("opacity", 60).commit();
        assertEquals("Work checklist", TaskWidgetProvider.preferences(context, 17).getString("title", ""));
        assertFalse(TaskWidgetProvider.preferences(context, 29).getBoolean("dark", false));
        assertEquals(60, TaskWidgetProvider.preferences(context, 29).getInt("opacity", 100));
        new TaskWidgetProvider().onDeleted(context, new int[] {17});
        assertTrue(TaskWidgetProvider.preferences(context, 17).getAll().isEmpty());
        assertEquals("Personal", TaskWidgetProvider.preferences(context, 29).getString("title", ""));
    }

    @Test public void configurationWithoutWidgetIdCancelsSafely() {
        ActivityController<WidgetConfigActivity> controller = Robolectric.buildActivity(WidgetConfigActivity.class).create();
        WidgetConfigActivity activity = controller.get();
        assertTrue(activity.isFinishing());
        assertEquals(Activity.RESULT_CANCELED, Shadows.shadowOf(activity).getResultCode());
        controller.destroy();
    }

    @Test public void configurationWithUnboundWidgetIdDoesNotCreateSettings() {
        Intent intent = new Intent(context, WidgetConfigActivity.class).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, 17);
        ActivityController<WidgetConfigActivity> controller = Robolectric.buildActivity(WidgetConfigActivity.class, intent).create();
        assertTrue(controller.get().isFinishing());
        assertEquals(Activity.RESULT_CANCELED, Shadows.shadowOf(controller.get()).getResultCode());
        assertTrue(TaskWidgetProvider.preferences(context, 17).getAll().isEmpty());
        controller.destroy();
    }

    private static List<String> titles(List<TaskStore.Task> tasks) {
        List<String> titles = new ArrayList<>();
        for (TaskStore.Task task : tasks) titles.add(task.title);
        return titles;
    }
}
