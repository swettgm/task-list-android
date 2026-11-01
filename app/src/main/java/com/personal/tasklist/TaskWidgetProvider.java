package com.personal.tasklist;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Native, offline home-screen checklist. Each instance has independent settings. */
public final class TaskWidgetProvider extends AppWidgetProvider {
    private static final String ACTION_COMPLETE = "com.personal.tasklist.WIDGET_COMPLETE";
    private static final String EXTRA_TASK_ID = "task_id";

    static SharedPreferences preferences(Context context, int widgetId) {
        return context.getSharedPreferences("widget_" + widgetId, Context.MODE_PRIVATE);
    }

    public static void refreshAll(Context context) {
        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        int[] ids = manager.getAppWidgetIds(new android.content.ComponentName(context, TaskWidgetProvider.class));
        for (int id : ids) update(context, manager, id);
    }

    @Override public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        for (int id : ids) update(context, manager, id);
    }

    @Override public void onAppWidgetOptionsChanged(Context context, AppWidgetManager manager,
                                                   int id, Bundle options) {
        update(context, manager, id);
    }

    @Override public void onReceive(Context context, Intent intent) {
        if (ACTION_COMPLETE.equals(intent.getAction())) {
            int id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            boolean registered = false;
            for (int actual : AppWidgetManager.getInstance(context).getAppWidgetIds(
                    new android.content.ComponentName(context, TaskWidgetProvider.class))) {
                if (actual == id) { registered = true; break; }
            }
            String taskId = intent.getStringExtra(EXTRA_TASK_ID);
            if (registered && taskId != null) {
                TaskStore store = new TaskStore(context);
                // The store atomically completes it; stale taps must never reopen a task.
                try { store.complete(taskId); }
                catch (RuntimeException unavailable) { refreshAll(context); }
            }
            return;
        }
        super.onReceive(context, intent);
    }

    @Override public void onDeleted(Context context, int[] ids) {
        for (int id : ids) context.deleteSharedPreferences("widget_" + id);
    }

    @Override public void onRestored(Context context, int[] oldIds, int[] newIds) {
        for (int i = 0; i < Math.min(oldIds.length, newIds.length); i++) {
            SharedPreferences.Editor target = preferences(context, newIds[i]).edit().clear();
            for (Map.Entry<String, ?> entry : preferences(context, oldIds[i]).getAll().entrySet()) {
                Object value = entry.getValue();
                if (value instanceof String) target.putString(entry.getKey(), (String) value);
                else if (value instanceof Boolean) target.putBoolean(entry.getKey(), (Boolean) value);
                else if (value instanceof Integer) target.putInt(entry.getKey(), (Integer) value);
            }
            target.apply();
            if (oldIds[i] != newIds[i]) context.deleteSharedPreferences("widget_" + oldIds[i]);
            AppWidgetManager.getInstance(context).updateAppWidgetOptions(newIds[i], restoredOptions());
            update(context, AppWidgetManager.getInstance(context), newIds[i]);
        }
    }

    private Bundle restoredOptions() {
        Bundle result = new Bundle();
        if (android.os.Build.VERSION.SDK_INT >= 30)
            result.putBoolean(AppWidgetManager.OPTION_APPWIDGET_RESTORE_COMPLETED, true);
        return result;
    }

    static List<TaskStore.Task> selectedTasks(Context context, String listName, boolean today) {
        return selectedTasks(context, listName, today, "");
    }

    static List<TaskStore.Task> selectedTasks(Context context, String listName, boolean today, String label) {
        List<TaskStore.Task> tasks = new ArrayList<>();
        LocalDate date = LocalDate.now(java.time.ZoneId.of("America/New_York"));
        for (TaskStore.Task task : new TaskStore(context).all()) {
            if (task.done || (!listName.isEmpty() && !listName.equals(task.listName))) continue;
            if (!label.isEmpty() && !task.labels.contains(label)) continue;
            if (today && !TaskStore.isForToday(task, date)) continue;
            tasks.add(task);
        }
        tasks.sort(Comparator.comparing((TaskStore.Task t) -> !TaskStore.IN_PROGRESS.equals(t.status))
                .thenComparingInt(t -> TaskStore.PRIORITIES.indexOf(t.priority))
                .thenComparing(t -> effectiveDate(t).isEmpty() ? "9999-99-99" : effectiveDate(t))
                .thenComparingLong(t -> t.createdAt));
        return tasks;
    }

    static void update(Context context, AppWidgetManager manager, int id) {
        SharedPreferences prefs = preferences(context, id);
        String title = prefs.getString("title", "My checklist");
        String listName = prefs.getString("list", "");
        String label = prefs.getString("label", "");
        boolean today = prefs.getBoolean("today", false);
        boolean dark = prefs.getBoolean("dark", true);
        int opacity = Math.max(25, Math.min(100, prefs.getInt("opacity", 100)));
        int foreground = Color.parseColor(dark ? "#F3F7FC" : "#1F2B40");
        int secondary = Color.parseColor(dark ? "#BAC9D9" : "#56677F");
        int accent = Color.parseColor(dark ? "#8FC6E7" : "#195B7E");
        List<TaskStore.Task> tasks;
        boolean unavailable = false;
        try { tasks = selectedTasks(context, listName, today, label); }
        catch (RuntimeException cannotReadTasks) { tasks = new ArrayList<>(); unavailable = true; }

        Bundle options = manager.getAppWidgetOptions(id);
        int height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 230);
        int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 250);
        // Reserve title, summary, footer, host padding; bound row payload size as well.
        int count = Math.max(1, Math.min(12, (height - 148) / 48));
        boolean compact = width < 210;
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_tasks);
        views.setImageViewResource(R.id.widget_background,
                dark ? R.drawable.widget_background_dark : R.drawable.widget_background_light);
        views.setInt(R.id.widget_background, "setImageAlpha", Math.round(255 * opacity / 100f));
        views.setTextViewText(R.id.widget_title, title);
        views.setTextColor(R.id.widget_title, foreground);
        views.setTextColor(R.id.widget_settings, accent);
        views.setTextColor(R.id.widget_summary, secondary);
        views.setTextColor(R.id.widget_empty, secondary);
        views.setTextColor(R.id.widget_open, accent);
        views.setTextColor(R.id.widget_add, accent);
        views.setTextViewText(R.id.widget_summary, unavailable ? "Checklist unavailable" : tasks.size() + " open"
                + (label.isEmpty() ? "" : " · " + label)
                + (today ? " · Today + overdue" : " · " + (listName.isEmpty() ? "All lists" : listName)));
        views.setViewVisibility(R.id.widget_empty, tasks.isEmpty() ? View.VISIBLE : View.GONE);
        views.setTextViewText(R.id.widget_empty, unavailable ? "Open the app to check your saved tasks." : today ? "Nothing due. Add a due date in the app." : "No open tasks. Add one in the app.");
        views.removeAllViews(R.id.widget_rows);

        int displayed = Math.min(count, tasks.size());
        for (int i = 0; i < displayed; i++) {
            TaskStore.Task task = tasks.get(i);
            RemoteViews row = new RemoteViews(context.getPackageName(), R.layout.widget_task_row);
            boolean active = TaskStore.IN_PROGRESS.equals(task.status);
            row.setTextViewText(R.id.widget_task_title, (active ? "▶ " : "") + task.title);
            row.setContentDescription(R.id.widget_task_title, task.title + ", " + task.status
                    + (task.labels.isEmpty() ? "" : ", " + android.text.TextUtils.join(", ", task.labels)));
            row.setTextColor(R.id.widget_task_title, foreground);
            row.setTextColor(R.id.widget_check, accent);
            row.setTextColor(R.id.widget_due, secondary);
            row.setContentDescription(R.id.widget_check, "Complete " + task.title);
            String detail = taskDetail(task);
            row.setTextViewText(R.id.widget_due, detail);
            row.setViewVisibility(R.id.widget_due, compact || detail.isEmpty() ? View.GONE : View.VISIBLE);
            Intent complete = new Intent(context, TaskWidgetProvider.class).setAction(ACTION_COMPLETE)
                    .setData(Uri.parse("tasklist-widget://complete/" + id + "/" + Uri.encode(task.id)))
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id).putExtra(EXTRA_TASK_ID, task.id);
            row.setOnClickPendingIntent(R.id.widget_check, PendingIntent.getBroadcast(context, 0, complete,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
            row.setOnClickPendingIntent(R.id.widget_task_title, openApp(context, id, listName, label, false));
            views.addView(R.id.widget_rows, row);
        }

        views.setTextViewText(R.id.widget_open, tasks.size() > displayed ? "+" + (tasks.size() - displayed) + " more · Open app ›" : "Open app ›");
        views.setOnClickPendingIntent(R.id.widget_title, openApp(context, id, listName, label, false));
        views.setOnClickPendingIntent(R.id.widget_empty, openApp(context, id, listName, label, false));
        views.setOnClickPendingIntent(R.id.widget_open, openApp(context, id, listName, label, false));
        views.setOnClickPendingIntent(R.id.widget_add, openApp(context, id, listName, label, true));
        Intent configure = new Intent(context, WidgetConfigActivity.class)
                .setData(Uri.parse("tasklist-widget://configure/" + id))
                .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        views.setOnClickPendingIntent(R.id.widget_settings, PendingIntent.getActivity(context, id, configure,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
        manager.updateAppWidget(id, views);
    }

    private static PendingIntent openApp(Context context, int widgetId, String listName, String label, boolean add) {
        Intent intent = new Intent(context, add ? QuickAddActivity.class : MainActivity.class)
                .setData(Uri.parse("tasklist-widget://" + (add ? "add/" : "open/") + widgetId))
                .putExtra("widget_list", listName).putExtra("widget_label", label).putExtra("add_task", add)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(context, widgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static String effectiveDate(TaskStore.Task task) {
        return task.plannedDate.isEmpty() ? task.dueDate : task.plannedDate;
    }

    private static String taskDetail(TaskStore.Task task) {
        String date = effectiveDate(task);
        if (!date.isEmpty()) return dueLabel(date);
        if (TaskStore.IN_PROGRESS.equals(task.status)) return "Now";
        if (TaskStore.WAITING.equals(task.status) || TaskStore.SOMEDAY.equals(task.status)) return task.status;
        return "";
    }

    private static String dueLabel(String date) {
        if (date == null || date.isEmpty()) return "";
        try {
            LocalDate due = LocalDate.parse(date);
            LocalDate today = LocalDate.now(java.time.ZoneId.of("America/New_York"));
            if (due.equals(today)) return "Today";
            if (due.isBefore(today)) return "Overdue";
            if (due.equals(today.plusDays(1))) return "Tomorrow";
            return due.format(DateTimeFormatter.ofPattern("MMM d"));
        } catch (RuntimeException invalidDate) { return ""; }
    }
}
