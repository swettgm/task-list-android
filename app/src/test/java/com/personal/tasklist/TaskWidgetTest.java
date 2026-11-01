package com.personal.tasklist;

import static org.junit.Assert.*;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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
        context.getSharedPreferences("reference_state", Context.MODE_PRIVATE).edit().clear().commit();
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
        LocalDate today = LocalDate.now(java.time.ZoneId.of("America/New_York"));
        store.add("Overdue", "Work", today.minusDays(2).toString());
        store.add("Due today", "Work", today.toString());
        store.add("Tomorrow", "Work", today.plusDays(1).toString());
        store.add("Undated", "Work", "");
        store.add("Already done", "Work", today.toString());
        for (TaskStore.Task task : store.all()) if (task.title.equals("Already done")) store.complete(task.id);
        assertEquals(Arrays.asList("Overdue", "Due today"), titles(TaskWidgetProvider.selectedTasks(context, "Work", true)));
    }

    @Test public void contextFilterCombinesChecklistAndOpenStateWithoutChangingOtherContexts() {
        task("Call supplier", "Work", "", "", TaskStore.NEXT, "On the phone", "On the road");
        task("Call family", "Personal", "", "", TaskStore.NEXT, "On the phone");
        task("Write report", "Work", "", "", TaskStore.NEXT, "On the computer");
        task("Finished call", "Work", "", "", TaskStore.DONE, "On the phone");

        assertEquals(Arrays.asList("Call supplier"),
                titles(TaskWidgetProvider.selectedTasks(context, "Work", false, "On the phone")));
        assertEquals(new HashSet<>(Arrays.asList("Call supplier", "Call family")),
                new HashSet<>(titles(TaskWidgetProvider.selectedTasks(context, "", false, "On the phone"))));
        assertEquals(titles(TaskWidgetProvider.selectedTasks(context, "", false)),
                titles(TaskWidgetProvider.selectedTasks(context, "", false, "")));
        assertTrue(TaskWidgetProvider.selectedTasks(context, "", false, "At home").isEmpty());
    }

    @Test public void todayViewUsesPlannedDayBeforeDeadlineAndExcludesWaitingOrSomeday() {
        LocalDate today = LocalDate.now(java.time.ZoneId.of("America/New_York"));
        task("Planned tomorrow", "Work", today.minusDays(1).toString(), today.plusDays(1).toString(),
                TaskStore.NEXT, "On the computer");
        task("Planned today", "Work", today.plusDays(7).toString(), today.toString(),
                TaskStore.NEXT, "On the computer");
        task("Unfinished plan", "Work", "", today.minusDays(1).toString(),
                TaskStore.NEXT, "On the computer");
        task("Waiting for reply", "Work", today.toString(), today.toString(),
                TaskStore.WAITING, "On the computer");
        task("Someday project", "Work", today.toString(), today.toString(),
                TaskStore.SOMEDAY, "On the computer");
        task("Completed plan", "Work", today.toString(), today.toString(),
                TaskStore.DONE, "On the computer");
        task("Working now", "Work", "", "", TaskStore.IN_PROGRESS, "On the computer");

        assertEquals(new HashSet<>(Arrays.asList("Planned today", "Unfinished plan", "Working now")),
                new HashSet<>(titles(TaskWidgetProvider.selectedTasks(context, "Work", true, "On the computer"))));
        TaskStore.Task postponed = null;
        for (TaskStore.Task item : store.all()) if (item.title.equals("Planned tomorrow")) postponed = item;
        assertNotNull(postponed);
        assertEquals("Filtering must not rewrite the original deadline", today.minusDays(1).toString(), postponed.dueDate);
    }

    @Test public void allOpenContextViewStillIncludesFutureWaitingAndSomedayTasks() {
        String tomorrow = LocalDate.now(java.time.ZoneId.of("America/New_York")).plusDays(1).toString();
        task("Tomorrow call", "Work", "", tomorrow, TaskStore.NEXT, "On the phone");
        task("Waiting call", "Work", "", "", TaskStore.WAITING, "On the phone");
        task("Someday call", "Work", "", "", TaskStore.SOMEDAY, "On the phone");
        assertEquals(new HashSet<>(Arrays.asList("Tomorrow call", "Waiting call", "Someday call")),
                new HashSet<>(titles(TaskWidgetProvider.selectedTasks(context, "Work", false, "On the phone"))));
    }

    @Test public void eachWidgetKeepsItsOwnSettingsAndDeletionDoesNotAffectOthers() {
        TaskWidgetProvider.preferences(context, 17).edit().putString("title", "Work checklist").putString("list", "Work").putString("label", "On the phone").putBoolean("dark", true).commit();
        TaskWidgetProvider.preferences(context, 29).edit().putString("title", "Personal").putString("list", "Personal").putString("label", "At home").putBoolean("dark", false).putInt("opacity", 60).commit();
        assertEquals("Work checklist", TaskWidgetProvider.preferences(context, 17).getString("title", ""));
        assertFalse(TaskWidgetProvider.preferences(context, 29).getBoolean("dark", false));
        assertEquals(60, TaskWidgetProvider.preferences(context, 29).getInt("opacity", 100));
        assertEquals("On the phone", TaskWidgetProvider.preferences(context, 17).getString("label", ""));
        assertEquals("At home", TaskWidgetProvider.preferences(context, 29).getString("label", ""));
        new TaskWidgetProvider().onDeleted(context, new int[] {17});
        assertTrue(TaskWidgetProvider.preferences(context, 17).getAll().isEmpty());
        assertEquals("Personal", TaskWidgetProvider.preferences(context, 29).getString("title", ""));
        assertEquals("At home", TaskWidgetProvider.preferences(context, 29).getString("label", ""));
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

    @Test public void widgetCompletionPersistsInTheFullTaskEngine() throws Exception {
        ReferenceStore reference = new ReferenceStore(context);
        String taskId = reference.addCustomTask("Widget task", Arrays.asList("At home"), "HIGH", false);
        String day = LocalDate.now(java.time.ZoneId.of("America/New_York")).toString();
        String key = taskId + ":" + day;
        int widgetId = Shadows.shadowOf(AppWidgetManager.getInstance(context))
                .createWidget(TaskWidgetProvider.class, R.layout.widget_tasks);
        Intent complete = new Intent(context, TaskWidgetProvider.class)
                .setAction("com.personal.tasklist.WIDGET_COMPLETE")
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                .putExtra("task_id", "html:" + key);
        new TaskWidgetProvider().onReceive(context, complete);
        assertTrue(new org.json.JSONObject(reference.readState()).getJSONObject("completed").getBoolean(key));
        assertTrue(TaskWidgetProvider.selectedTasks(context, "", false).isEmpty());
        new TaskWidgetProvider().onReceive(context, complete);
        assertTrue(new org.json.JSONObject(reference.readState()).getJSONObject("completed").getBoolean(key));
    }

    private static List<String> titles(List<TaskStore.Task> tasks) {
        List<String> titles = new ArrayList<>();
        for (TaskStore.Task task : tasks) titles.add(task.title);
        return titles;
    }

    private void task(String title, String listName, String dueDate, String plannedDate,
                      String status, String... labels) {
        store.add(title, listName, dueDate, Arrays.asList(labels), status, plannedDate, "UNSET", 0);
    }
}
