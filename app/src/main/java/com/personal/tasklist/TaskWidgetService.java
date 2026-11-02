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

/** Launcher-managed recycling list: every open Today row is scrollable. */
public final class TaskWidgetService extends RemoteViewsService {
    @Override public RemoteViewsFactory onGetViewFactory(Intent intent){return new Factory(getApplicationContext(),intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,AppWidgetManager.INVALID_APPWIDGET_ID));}
    static final class Factory implements RemoteViewsFactory {
        private final Context context;private final int widgetId;private volatile List<TaskStore.Task> tasks=new ArrayList<>();private volatile boolean dark=true;
        Factory(Context context,int widgetId){this.context=context;this.widgetId=widgetId;}
        @Override public void onCreate(){onDataSetChanged();}
        @Override public void onDataSetChanged(){long token=Binder.clearCallingIdentity();try{
            if(widgetId==AppWidgetManager.INVALID_APPWIDGET_ID){tasks=new ArrayList<>();return;}
            SharedPreferences prefs=TaskWidgetProvider.preferences(context,widgetId);dark=prefs.getBoolean("dark",true);
            tasks=TaskWidgetProvider.todayTasks(context,prefs.getString("list",""),prefs.getString("label",""));
        }catch(RuntimeException unavailable){tasks=new ArrayList<>();}finally{Binder.restoreCallingIdentity(token);}}
        @Override public void onDestroy(){tasks=new ArrayList<>();}
        @Override public int getCount(){return tasks.size();}
        @Override public RemoteViews getViewAt(int position){
            List<TaskStore.Task> rows=tasks;if(position<0||position>=rows.size())return null;TaskStore.Task task=rows.get(position);
            RemoteViews row=new RemoteViews(context.getPackageName(),R.layout.widget_task_row);
            row.setTextViewText(R.id.widget_task_title,(TaskStore.IN_PROGRESS.equals(task.status)?"▶ ":"")+task.title);
            row.setTextColor(R.id.widget_task_title,Color.parseColor(dark?"#F3F7FC":"#1F2B40"));
            row.setTextColor(R.id.widget_check,Color.parseColor(dark?"#8FC6E7":"#195B7E"));
            row.setTextColor(R.id.widget_due,Color.parseColor(dark?"#BAC9D9":"#56677F"));
            row.setTextViewText(R.id.widget_due,"Today");row.setContentDescription(R.id.widget_task_title,task.title+", Today, "+task.status);row.setContentDescription(R.id.widget_check,"Complete "+task.title);
            row.setOnClickFillInIntent(R.id.widget_check,new Intent().putExtra(TaskWidgetProvider.EXTRA_TASK_ID,task.id).putExtra(TaskWidgetProvider.ITEM_ACTION,"complete"));
            row.setOnClickFillInIntent(R.id.widget_task_title,new Intent().putExtra(TaskWidgetProvider.EXTRA_TASK_ID,task.id).putExtra(TaskWidgetProvider.ITEM_ACTION,"open"));
            return row;
        }
        @Override public RemoteViews getLoadingView(){return null;}
        @Override public int getViewTypeCount(){return 1;}
        @Override public long getItemId(int position){List<TaskStore.Task> rows=tasks;if(position<0||position>=rows.size())return -1;long value=0xcbf29ce484222325L;for(char c:rows.get(position).id.toCharArray()){value^=c;value*=0x100000001b3L;}return value;}
        @Override public boolean hasStableIds(){return true;}
    }
}
