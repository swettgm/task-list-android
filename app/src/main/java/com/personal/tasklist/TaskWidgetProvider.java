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
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/** Native, offline home-screen checklist. Each instance has independent settings. */
public final class TaskWidgetProvider extends AppWidgetProvider {
    static final String ACTION_ITEM="com.personal.tasklist.WIDGET_ITEM";
    static final String ITEM_ACTION="item_action";
    static final String EXTRA_TASK_ID = "task_id";
    private static final String ACTION_COMPLETE = "com.personal.tasklist.WIDGET_COMPLETE";

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
        if (ACTION_COMPLETE.equals(intent.getAction())||ACTION_ITEM.equals(intent.getAction())) {
            int id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            boolean registered = false;
            for (int actual : AppWidgetManager.getInstance(context).getAppWidgetIds(
                    new android.content.ComponentName(context, TaskWidgetProvider.class))) {
                if (actual == id) { registered = true; break; }
            }
            if(registered&&ACTION_ITEM.equals(intent.getAction())&&"open".equals(intent.getStringExtra(ITEM_ACTION))){
                WidgetFeedback.tick(context);SharedPreferences widget=preferences(context,id);context.startActivity(new Intent(context,MainActivity.class).putExtra("widget_label",widget.getString("label","")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP));return;
            }
            String itemAction=intent.getStringExtra(ITEM_ACTION);
            if(ACTION_ITEM.equals(intent.getAction())&&!"complete".equals(itemAction)&&!"undo".equals(itemAction)&&!"start".equals(itemAction)&&!"stop".equals(itemAction)&&!"edit".equals(itemAction))return;
            String taskId = intent.getStringExtra(EXTRA_TASK_ID);
            if(registered&&ACTION_ITEM.equals(intent.getAction())){try{SharedPreferences widget=preferences(context,id);boolean visible=false;for(TaskStore.Task row:todayTasks(context,widget.getString("list",""),widget.getString("label","")))if(row.id.equals(taskId)){visible=true;break;}if(!visible){refreshAll(context);return;}}catch(RuntimeException unavailable){refreshAll(context);return;}}
            if(registered&&taskId!=null)WidgetFeedback.tick(context);
            if (registered && taskId != null && "edit".equals(itemAction)) {
                context.startActivity(new Intent(context,TaskEditorActivity.class)
                        .putExtra(EXTRA_TASK_ID,taskId)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                return;
            }
            if (registered && taskId != null) {
                TaskStore store = new TaskStore(context);
                // The store atomically completes it; stale taps must never reopen a task.
                try {
                    if(ACTION_ITEM.equals(intent.getAction())&&"undo".equals(itemAction)){
                        WidgetCompletions.cancel(context,taskId,intent.getStringExtra("completion_token"));refreshAll(context);
                    }else if(ACTION_ITEM.equals(intent.getAction())&&"complete".equals(itemAction)){
                        WidgetCompletions.begin(context,taskId);refreshAll(context);
                        final Context app=context.getApplicationContext();final PendingResult pending=goAsync();
                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(()->{try{WidgetCompletions.settleExpired(app);refreshAll(app);}finally{if(pending!=null)pending.finish();}},WidgetCompletions.DELAY_MS+50);
                    }else if(ACTION_ITEM.equals(intent.getAction())&&("start".equals(itemAction)||"stop".equals(itemAction))){
                        if(!WidgetCompletions.token(context,taskId).isEmpty())return;
                        if(!new ReferenceStore(context).setTaskInProgress(taskId,"start".equals(itemAction)))store.setStatus(taskId,"start".equals(itemAction)?TaskStore.IN_PROGRESS:TaskStore.NEXT);
                    }else if (!new ReferenceStore(context).completeTask(taskId)) store.complete(taskId);
                }
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

    /** Only rows belonging to the app's current Today list; completed history stays in storage. */
    static List<TaskStore.Task> todayTasks(Context context,String listName,String label){return todayTasks(context,listName,label,System.currentTimeMillis());}
    static List<TaskStore.Task> todayTasks(Context context,String listName,String label,long nowMillis){return todayTasks(context,listName,label,false,nowMillis);}
    private static List<TaskStore.Task> todayTasks(Context context,String listName,String label,boolean includeHidden,long nowMillis){
        WidgetCompletions.settleExpired(context);LocalDate today=Instant.ofEpochMilli(nowMillis).atZone(ReminderPlanner.ZONE).toLocalDate();List<TaskStore.Task> result=new ArrayList<>();
        org.json.JSONObject state;try{state=new org.json.JSONObject(new ReferenceStore(context).readState());}catch(org.json.JSONException invalid){throw new IllegalStateException(invalid);}org.json.JSONArray definitions;try{definitions=ReminderPlanner.effective(new org.json.JSONArray(TaskReminders.prefs(context).getString("definitions","[]")),state);}catch(org.json.JSONException invalid){throw new IllegalStateException(invalid);}
        for(TaskStore.Task task:selectedTasks(context,listName,false,label)){
            org.json.JSONObject source=task.sourceRecord();String progress=source.optString("progressKey");
            int split=progress.lastIndexOf(':');String taskId=split>0?progress.substring(0,split):"",occurrence=source.optString("stateDate",effectiveDate(task));
            if(split>0&&ReminderPlanner.removed(taskId,progress.substring(split+1),state))continue;
            org.json.JSONObject definition=null;
            for(int i=0;i<definitions.length();i++){org.json.JSONObject candidate=definitions.optJSONObject(i);if(candidate!=null&&taskId.equals(candidate.optString("id"))){definition=candidate;break;}}
            if(definition!=null)try{if(!ReminderPlanner.scheduled(definition,LocalDate.parse(occurrence)))continue;}catch(RuntimeException invalid){continue;}
            if(definition==null&&today.toString().equals(source.optString("snapshotDate"))&&!source.optBoolean("onToday"))continue;
            String chosenDate=ReminderPlanner.chosenDate(definition==null?source:definition,occurrence,state);
            // Ignore stale onToday/plannedDate/Friday-preview flags from older snapshots.
            if(!today.toString().equals(chosenDate))continue;
            org.json.JSONObject visibilityTask=definition==null?source:definition;
            if(!includeHidden&&!TaskStore.IN_PROGRESS.equals(task.status)&&!TaskVisibility.visible(visibilityTask,chosenDate,nowMillis))continue;
            result.add(task);
        }
        result.sort(Comparator.comparingInt((TaskStore.Task t)->TaskStore.PRIORITIES.indexOf(t.priority))
                .thenComparingInt(t->t.sourceRecord().optBoolean("urgent")?0:1)
                .thenComparingInt(t->{java.time.LocalTime time=ReminderPlanner.time(t.sourceRecord().optString("time"));return time==null?Integer.MAX_VALUE:time.toSecondOfDay();})
                .thenComparing(t->t.title,String.CASE_INSENSITIVE_ORDER).thenComparing(t->t.id));
        return result;
    }

    static String emptyMessage(Context context,String listName,String label){return emptyMessage(context,listName,label,System.currentTimeMillis());}
    static String emptyMessage(Context context,String listName,String label,long now){
        long next=0;LocalDate today=Instant.ofEpochMilli(now).atZone(ReminderPlanner.ZONE).toLocalDate();org.json.JSONObject state;org.json.JSONArray definitions;
        try{state=new org.json.JSONObject(new ReferenceStore(context).readState());definitions=ReminderPlanner.effective(new org.json.JSONArray(TaskReminders.prefs(context).getString("definitions","[]")),state);}catch(org.json.JSONException invalid){throw new IllegalStateException(invalid);}
        for(TaskStore.Task task:todayTasks(context,listName,label,true,now)){org.json.JSONObject source=task.sourceRecord(),definition=source;String key=source.optString("progressKey"),id=key.contains(":")?key.substring(0,key.indexOf(':')):"";for(int i=0;i<definitions.length();i++){org.json.JSONObject candidate=definitions.optJSONObject(i);if(candidate!=null&&id.equals(candidate.optString("id"))){definition=candidate;break;}}org.json.JSONObject moves=state.optJSONObject("movedTo");String date=source.optString("stateDate",effectiveDate(task));date=ReminderPlanner.chosenDate(definition,date,state);long at=TaskVisibility.showAt(definition,date);if(at>now&&Instant.ofEpochMilli(at).atZone(ReminderPlanner.ZONE).toLocalDate().equals(today))next=next==0?at:Math.min(next,at);}
        if(next==0)return context.getString(R.string.widget_empty);String time=Instant.ofEpochMilli(next).atZone(ReminderPlanner.ZONE).format(DateTimeFormatter.ofPattern("h:mm a",java.util.Locale.US));return "You’re done until "+time+"! Get some rest. 🪻";
    }

    static void update(Context context, AppWidgetManager manager, int id) {
        SharedPreferences prefs=preferences(context,id);String list=prefs.getString("list",""),label=prefs.getString("label","");boolean dark=prefs.getBoolean("dark",true);
        int foreground=Color.parseColor(dark?"#F3F7FC":"#1F2B40"),secondary=Color.parseColor(dark?"#BAC9D9":"#56677F"),accent=Color.parseColor(dark?"#8FC6E7":"#195B7E");
        RemoteViews views=new RemoteViews(context.getPackageName(),R.layout.widget_today_centered_empty_v3);
        views.setImageViewResource(R.id.widget_background,dark?R.drawable.widget_background_dark:R.drawable.widget_background_light);
        views.setInt(R.id.widget_background,"setImageAlpha",Math.round(255*Math.max(0,Math.min(100,prefs.getInt("opacity",100)))/100f));
        views.setViewPadding(R.id.widget_empty,0,0,0,0);
        views.setTextViewText(R.id.widget_title,"Today");views.setTextColor(R.id.widget_title,foreground);views.setImageViewBitmap(R.id.widget_add,PlusIcon.render(context));views.setInt(R.id.widget_add,"setBackgroundColor",Color.TRANSPARENT);
        views.setViewVisibility(R.id.widget_context_icon,TaskStore.usesGuildImage(context,label)?View.VISIBLE:View.GONE);views.setContentDescription(R.id.widget_context_icon,label);
        String empty=context.getString(R.string.widget_empty);try{if(todayTasks(context,list,label).isEmpty())empty=emptyMessage(context,list,label);}catch(RuntimeException unavailable){empty="Open the app to check your saved tasks.";}android.os.Bundle bounds=manager.getAppWidgetOptions(id);
        boolean landscape=context.getResources().getConfiguration().orientation==android.content.res.Configuration.ORIENTATION_LANDSCAPE;
        int width=bounds.getInt(landscape?AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH:AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH,250);
        views.setImageViewBitmap(R.id.widget_empty,WidgetEmptyMessage.render(context,empty,secondary,width));
        views.setContentDescription(R.id.widget_empty,empty);
        Intent service=new Intent(context,TaskWidgetService.class).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id).setData(Uri.parse("tasklist-widget://rows/"+id));
        views.setRemoteAdapter(R.id.widget_rows,service);views.setEmptyView(R.id.widget_rows,R.id.widget_empty);
        Intent item=new Intent(context,TaskWidgetProvider.class).setAction(ACTION_ITEM).setData(Uri.parse("tasklist-widget://items/"+id)).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id);
        int flags=PendingIntent.FLAG_UPDATE_CURRENT;if(android.os.Build.VERSION.SDK_INT>=31)flags|=PendingIntent.FLAG_MUTABLE;
        views.setPendingIntentTemplate(R.id.widget_rows,PendingIntent.getBroadcast(context,id,item,flags));
        views.setOnClickPendingIntent(R.id.widget_title,openApp(context,id,list,label,false));views.setOnClickPendingIntent(R.id.widget_add,openApp(context,id,list,label,true));
        manager.updateAppWidget(id,views);manager.notifyAppWidgetViewDataChanged(id,R.id.widget_rows);
    }

    private static PendingIntent openApp(Context context, int widgetId, String listName, String label, boolean add) {
        Intent intent = new Intent(context, add ? QuickAddActivity.class : MainActivity.class)
                .setData(Uri.parse("tasklist-widget://" + (add ? "add/" : "open/") + widgetId))
                .putExtra("widget_list", listName).putExtra("widget_label", label).putExtra("add_task", add).putExtra("widget_haptic",!add)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(context, widgetId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static String effectiveDate(TaskStore.Task task) {
        return task.plannedDate.isEmpty() ? task.dueDate : task.plannedDate;
    }

    static String taskDetail(TaskStore.Task task) {
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
