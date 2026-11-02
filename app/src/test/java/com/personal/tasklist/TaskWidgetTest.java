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

    @Test public void widgetSupportsFullyTransparentBackgroundAndKeepsItsTextVisible() {
        store.add("Visible task", "Personal", LocalDate.now(java.time.ZoneId.of("America/New_York")).toString());
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int id = Shadows.shadowOf(manager).createWidget(TaskWidgetProvider.class, R.layout.widget_tasks);
        for (int opacity : new int[] {0, 50, 100}) {
            TaskWidgetProvider.preferences(context, id).edit().putInt("opacity", opacity).commit();
            TaskWidgetProvider.update(context, manager, id);
            android.view.View view = Shadows.shadowOf(manager).getViewFor(id);
            assertEquals(Math.round(255 * opacity / 100f), ((android.widget.ImageView) view.findViewById(R.id.widget_background)).getImageAlpha());
            assertEquals(1f, view.findViewById(R.id.widget_title).getAlpha(), 0f);
            TaskWidgetService.Factory factory=new TaskWidgetService.Factory(context,id);factory.onCreate();android.view.View row=factory.getViewAt(0).apply(context,null);assertEquals(1f,row.findViewById(R.id.widget_check).getAlpha(),0f);
        }
    }

    @Test public void widgetConfigurationSavesZeroOpacityAcrossRecreation() {
        int id = 73;
        android.appwidget.AppWidgetProviderInfo info = new android.appwidget.AppWidgetProviderInfo();
        info.provider = new android.content.ComponentName(context, TaskWidgetProvider.class);
        Shadows.shadowOf(AppWidgetManager.getInstance(context)).addBoundWidget(id, info);
        TaskWidgetProvider.preferences(context, id).edit().putInt("opacity", 60).commit();
        Intent intent = new Intent(context, WidgetConfigActivity.class).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id);
        ActivityController<WidgetConfigActivity> controller = Robolectric.buildActivity(WidgetConfigActivity.class, intent).setup();
        assertFalse(controller.get().isFinishing());
        android.widget.SeekBar slider = findSlider(controller.get().getWindow().getDecorView());
        assertEquals(100, slider.getMax()); assertEquals(60, slider.getProgress());
        slider.setProgress(0);
        controller.recreate();
        slider = findSlider(controller.get().getWindow().getDecorView());
        assertEquals(0, slider.getProgress());
        findButton(controller.get().getWindow().getDecorView(), "Save widget").performClick();
        assertEquals(0, TaskWidgetProvider.preferences(context, id).getInt("opacity", -1));
        controller.pause().stop().destroy();
    }

    private static android.widget.SeekBar findSlider(android.view.View view) {
        if (view instanceof android.widget.SeekBar) return (android.widget.SeekBar) view;
        if (view instanceof android.view.ViewGroup) for (int i = 0; i < ((android.view.ViewGroup) view).getChildCount(); i++) {
            android.widget.SeekBar found = findSlider(((android.view.ViewGroup) view).getChildAt(i));
            if (found != null) return found;
        }
        return null;
    }

    private static android.widget.Button findButton(android.view.View view, String text) {
        if (view instanceof android.widget.Button && text.contentEquals(((android.widget.Button) view).getText())) return (android.widget.Button) view;
        if (view instanceof android.view.ViewGroup) for (int i = 0; i < ((android.view.ViewGroup) view).getChildCount(); i++) {
            android.widget.Button found = findButton(((android.view.ViewGroup) view).getChildAt(i), text);
            if (found != null) return found;
        }
        return null;
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

    @Test public void scrollableWidgetHasNoRowCapAndOnlyShowsToday() throws Exception {
        String today=LocalDate.now(java.time.ZoneId.of("America/New_York")).toString(),tomorrow=LocalDate.parse(today).plusDays(1).toString();
        for(int i=0;i<40;i++)store.add("Today row "+i,"Work",today);
        store.add("Future task","Work",tomorrow);store.add("Undated task","Work","");store.add("Hidden completed","Work",today);
        for(TaskStore.Task task:store.all())if(task.title.equals("Hidden completed"))store.complete(task.id);
        TaskWidgetProvider.preferences(context,17).edit().putBoolean("today",false).putString("list","Work").commit();
        TaskWidgetService.Factory factory=new TaskWidgetService.Factory(context,17);factory.onCreate();
        assertEquals(40,factory.getCount());assertTrue(factory.hasStableIds());
        android.view.View last=factory.getViewAt(39).apply(context,null);
        assertEquals("Today",((android.widget.TextView)last.findViewById(R.id.widget_due)).getText().toString());assertEquals(android.view.View.VISIBLE,last.findViewById(R.id.widget_due).getVisibility());
        long firstId=factory.getItemId(0);String other=TaskWidgetProvider.todayTasks(context,"Work","").get(3).id;store.complete(other);factory.onDataSetChanged();assertEquals(39,factory.getCount());assertEquals(firstId,factory.getItemId(0));
        assertTrue(store.all().stream().anyMatch(t->t.title.equals("Hidden completed")&&t.done));
    }
    @Test public void widgetHeaderIsTodayAndRowsUseANativeScrollingList() {
        AppWidgetManager manager=AppWidgetManager.getInstance(context);int id=Shadows.shadowOf(manager).createWidget(TaskWidgetProvider.class,R.layout.widget_tasks);
        TaskWidgetProvider.preferences(context,id).edit().putString("title","My tasks").putBoolean("today",false).commit();TaskWidgetProvider.update(context,manager,id);
        android.view.View view=Shadows.shadowOf(manager).getViewFor(id);
        assertEquals("Today",((android.widget.TextView)view.findViewById(R.id.widget_title)).getText().toString());assertTrue(view.findViewById(R.id.widget_rows) instanceof android.widget.ListView);assertTrue(view.findViewById(R.id.widget_rows).isVerticalScrollBarEnabled());
        assertEquals(0,context.getResources().getIdentifier("widget_summary","id",context.getPackageName()));
    }
    @Test public void todaySnapshotExcludesHiddenFutureAndOldDailyRows() throws Exception {
        LocalDate today=LocalDate.now(java.time.ZoneId.of("America/New_York"));String day=today.toString(),yesterday=today.minusDays(1).toString(),tomorrow=today.plusDays(1).toString();
        org.json.JSONArray rows=new org.json.JSONArray();
        rows.put(widgetRow("today",day,day,true,false,false,day));
        rows.put(widgetRow("hidden",day,day,true,true,false,day));
        rows.put(widgetRow("not-today",day,day,false,false,false,day));
        rows.put(widgetRow("future",tomorrow,tomorrow,false,false,false,day).put("status",TaskStore.IN_PROGRESS));
        rows.put(widgetRow("old-daily",yesterday,yesterday,true,false,true,yesterday));
        rows.put(widgetRow("new-daily",day,day,false,false,true,yesterday));
        rows.put(widgetRow("carry",yesterday,yesterday,true,false,false,yesterday));
        store.replaceReferenceTasks(rows.toString());
        assertEquals(new HashSet<>(Arrays.asList("today","new-daily","carry")),new HashSet<>(titles(TaskWidgetProvider.todayTasks(context,"",""))));assertEquals(7,store.all().size());
    }
    @Test public void collectionCompletionIsPersistentAndStaleTapCannotReopen() throws Exception {
        String day=LocalDate.now(java.time.ZoneId.of("America/New_York")).toString();String id=new ReferenceStore(context).addCustomTask("Scroll complete",Arrays.asList("At home"),"HIGH",false);
        int widget=Shadows.shadowOf(AppWidgetManager.getInstance(context)).createWidget(TaskWidgetProvider.class,R.layout.widget_tasks);
        Intent tap=new Intent(context,TaskWidgetProvider.class).setAction(TaskWidgetProvider.ACTION_ITEM).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,widget).putExtra(TaskWidgetProvider.ITEM_ACTION,"complete").putExtra(TaskWidgetProvider.EXTRA_TASK_ID,"html:"+id+":"+day);
        new TaskWidgetProvider().onReceive(context,tap);new TaskWidgetProvider().onReceive(context,tap);
        assertTrue(new org.json.JSONObject(new ReferenceStore(context).readState()).getJSONObject("completed").getBoolean(id+":"+day));assertTrue(TaskWidgetProvider.todayTasks(context,"","").isEmpty());
    }
    private org.json.JSONObject widgetRow(String id,String planned,String stateDate,boolean onToday,boolean done,boolean daily,String snapshot) throws Exception {
        return new org.json.JSONObject().put("id","html:"+id+":"+stateDate).put("title",id).put("listName","My tasks").put("dueDate",planned).put("plannedDate",planned).put("done",done).put("status",done?TaskStore.DONE:TaskStore.NEXT).put("labels",new org.json.JSONArray()).put("onToday",onToday).put("recurringDaily",daily).put("stateDate",stateDate).put("snapshotDate",snapshot);
    }
}
