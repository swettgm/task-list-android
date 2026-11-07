package com.personal.tasklist;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Binder;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import java.util.ArrayList;
import java.util.List;

/** Launcher-managed recycling list with quiet priority headings and no task-row cap. */
public final class TaskWidgetService extends RemoteViewsService {
    @Override public RemoteViewsFactory onGetViewFactory(Intent intent){return new Factory(getApplicationContext(),intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID));}
    static final class Factory implements RemoteViewsFactory {
        private static final class Entry { final String priority;final TaskStore.Task task;Entry(String priority,TaskStore.Task task){this.priority=priority;this.task=task;} }
        private final Context context;private final int widgetId;private volatile List<Entry> rows=new ArrayList<>();private volatile boolean dark=true;
        Factory(Context context,int widgetId){this.context=context;this.widgetId=widgetId;}
        @Override public void onCreate(){onDataSetChanged();}
        @Override public void onDataSetChanged(){long token=Binder.clearCallingIdentity();try{
            if(widgetId==AppWidgetManager.INVALID_APPWIDGET_ID){rows=new ArrayList<>();return;}
            SharedPreferences prefs=TaskWidgetProvider.preferences(context,widgetId);dark=prefs.getBoolean("dark",true);
            List<TaskStore.Task> tasks=TaskWidgetProvider.todayTasks(context,prefs.getString("list",""),prefs.getString("label",""));
            List<Entry> next=new ArrayList<>();
            for(TaskStore.Task task:tasks)if(TaskStore.IN_PROGRESS.equals(task.status))next.add(new Entry(task.priority,task));
            for(String priority:TaskStore.PRIORITIES){boolean heading=false;for(TaskStore.Task task:tasks)if(priority.equals(task.priority)&&!TaskStore.IN_PROGRESS.equals(task.status)){if(!heading){next.add(new Entry(priority,null));heading=true;}next.add(new Entry(priority,task));}}
            rows=next;
        }catch(RuntimeException unavailable){rows=new ArrayList<>();}finally{Binder.restoreCallingIdentity(token);}}
        @Override public void onDestroy(){rows=new ArrayList<>();}
        @Override public int getCount(){return rows.size();}
        private String priorityLabel(String value){switch(value){case "HIGH":return "High";case "MEDIUM":return "Medium";case "LOW":return "Low";default:return "Low";}}
        @Override public RemoteViews getViewAt(int position){
            List<Entry> snapshot=rows;if(position<0||position>=snapshot.size())return null;Entry entry=snapshot.get(position);
            if(entry.task==null){
                RemoteViews header=new RemoteViews(context.getPackageName(),R.layout.widget_priority_header);
                header.setTextViewText(R.id.widget_priority,priorityLabel(entry.priority));
                header.setTextColor(R.id.widget_priority,dark?Color.argb(135,186,201,217):Color.argb(140,86,103,127));
                header.setInt(R.id.widget_priority_divider,"setBackgroundColor",dark?Color.argb(24,255,255,255):Color.argb(18,0,0,0));
                return header;
            }
            TaskStore.Task task=entry.task;RemoteViews row=new RemoteViews(context.getPackageName(),R.layout.widget_task_row_centered);
            row.setTextViewText(R.id.widget_task_title,task.title);
            row.setOnClickFillInIntent(R.id.widget_task_title,new Intent().putExtra(TaskWidgetProvider.EXTRA_TASK_ID,task.id).putExtra(TaskWidgetProvider.ITEM_ACTION,"edit"));
            row.setTextViewTextSize(R.id.widget_task_title,android.util.TypedValue.COMPLEX_UNIT_SP,13.5f);
            row.setTextColor(R.id.widget_task_title,Color.parseColor(dark?"#F3F7FC":"#1F2B40"));
            row.setTextColor(R.id.widget_check,Color.parseColor(dark?"#8FC6E7":"#195B7E"));
            row.setTextColor(R.id.widget_due,Color.parseColor(dark?"#BAC9D9":"#56677F"));
            boolean active=TaskStore.IN_PROGRESS.equals(task.status),urgent=task.sourceRecord().optBoolean("urgent");
            boolean overdue;try{overdue=java.time.LocalDate.parse(ReminderPlanner.chosenDate(task.sourceRecord(),task.sourceRecord().optString("stateDate",task.dueDate),new org.json.JSONObject(new ReferenceStore(context).readState()))).isBefore(java.time.LocalDate.now(ReminderPlanner.ZONE));}catch(Exception invalid){overdue=false;}
            String status=(active?"🟢 IN PROGRESS":urgent?"🔴 URGENT":overdue?"Overdue":"Today")+((active||urgent)&&overdue?" · Overdue":"");
            row.setTextViewText(R.id.widget_due,status);
            row.setTextColor(R.id.widget_due,active||urgent?Color.WHITE:Color.parseColor(dark?"#BAC9D9":"#56677F"));
            row.setInt(R.id.widget_due,"setBackgroundResource",active?R.drawable.widget_progress_badge:urgent?R.drawable.widget_urgent_badge:0);
            row.setContentDescription(R.id.widget_task_title,"Edit "+task.title+", "+status);row.setContentDescription(R.id.widget_check,"Complete "+task.title);
            row.setContentDescription(R.id.widget_status_toggle,(active?"Stop Do now for ":"Do now: ")+task.title);row.setOnClickFillInIntent(R.id.widget_status_toggle,new Intent().putExtra(TaskWidgetProvider.EXTRA_TASK_ID,task.id).putExtra(TaskWidgetProvider.ITEM_ACTION,active?"stop":"start"));
            String pending=WidgetCompletions.token(context,task.id);boolean staged=!pending.isEmpty();
            row.setTextViewText(R.id.widget_check,staged?"✓":"○");row.setContentDescription(R.id.widget_check,(staged?"Undo completion of ":"Complete ")+task.title);
            row.setTextColor(R.id.widget_task_title,Color.parseColor(staged?(dark?"#A5AFBA":"#6B7684"):(dark?"#F3F7FC":"#1F2B40")));
            row.setOnClickFillInIntent(R.id.widget_check,new Intent().putExtra(TaskWidgetProvider.EXTRA_TASK_ID,task.id).putExtra(TaskWidgetProvider.ITEM_ACTION,staged?"undo":"complete").putExtra("completion_token",pending));
            return row;
        }
        @Override public RemoteViews getLoadingView(){return null;}
        @Override public int getViewTypeCount(){return 2;}
        @Override public long getItemId(int position){List<Entry> snapshot=rows;if(position<0||position>=snapshot.size())return -1;Entry entry=snapshot.get(position);String id=entry.task==null?"priority:"+entry.priority:"task:"+entry.task.id;long value=0xcbf29ce484222325L;for(char c:id.toCharArray()){value^=c;value*=0x100000001b3L;}return value;}
        @Override public boolean hasStableIds(){return true;}
    }
}
