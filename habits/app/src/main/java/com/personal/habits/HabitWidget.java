package com.personal.habits;
import android.app.*;
import android.appwidget.*;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.widget.RemoteViews;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
public final class HabitWidget extends AppWidgetProvider {
 static final String CYCLE="com.personal.habits.CYCLE";
 static final int[] DAYS={R.id.day0,R.id.day1,R.id.day2,R.id.day3,R.id.day4,R.id.day5,R.id.day6};
 @Override public void onUpdate(Context c,AppWidgetManager m,int[] ids){for(int id:ids)update(c,m,id);DayReceiver.schedule(c);}
 @Override public void onAppWidgetOptionsChanged(Context c,AppWidgetManager m,int id,Bundle options){update(c,m,id);}
 @Override public void onEnabled(Context c){DayReceiver.schedule(c);}
 @Override public void onDisabled(Context c){DayReceiver.cancel(c);}
 @Override public void onRestored(Context c,int[] oldIds,int[] newIds){onUpdate(c,AppWidgetManager.getInstance(c),newIds);}
 @Override public void onReceive(Context c,Intent intent){if(CYCLE.equals(intent.getAction())){int id=intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,-1);boolean registered=false;for(int actual:ids(c))if(actual==id)registered=true;if(!registered)return;try{LocalDate day=LocalDate.parse(intent.getStringExtra("day"));/* A date travels with the rendered circle, never a moving offset. */int result=new HabitStore(c).cycle(intent.getStringExtra("habit_id"),day);if(result>=0&&new Appearance(c).haptics){Vibrator v=(Vibrator)c.getSystemService(Context.VIBRATOR_SERVICE);if(v!=null&&v.hasVibrator())v.vibrate(VibrationEffect.createOneShot(18,VibrationEffect.DEFAULT_AMPLITUDE));}}catch(RuntimeException ignored){/* Malformed or deleted stale rows cannot create records. */}refresh(c);return;}super.onReceive(c,intent);}
 static int[] ids(Context c){return AppWidgetManager.getInstance(c).getAppWidgetIds(new ComponentName(c,HabitWidget.class));}
 public static void refresh(Context c){AppWidgetManager m=AppWidgetManager.getInstance(c);for(int id:ids(c))update(c,m,id);DayReceiver.schedule(c);}
 static void update(Context c,AppWidgetManager m,int id){m.updateAppWidget(id,build(c,id,HabitStore.today()));m.notifyAppWidgetViewDataChanged(id,R.id.widget_rows);}
 static RemoteViews build(Context c,int id,LocalDate today){Appearance a=new Appearance(c);RemoteViews v=new RemoteViews(c.getPackageName(),R.layout.widget_habits);v.setImageViewBitmap(R.id.widget_background,Artwork.background(a));List<LocalDate> days=HabitStore.window(today);for(int i=0;i<7;i++){LocalDate d=days.get(i);v.setTextViewText(DAYS[i],d.format(DateTimeFormatter.ofPattern("EEE",Locale.getDefault()))+"\n"+(i==2?"●":" "));v.setTextColor(DAYS[i],i==2?a.text:((a.text&0xFFFFFF)|0xB8000000));v.setContentDescription(DAYS[i],(i==2?"Today, ":"")+d.toString());}
 Intent adapter=new Intent(c,HabitRowsService.class).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id).putExtra("anchor",today.toString());adapter.setData(Uri.parse("habits://rows/"+id+"/"+today));v.setRemoteAdapter(R.id.widget_rows,adapter);v.setEmptyView(R.id.widget_rows,R.id.widget_empty);v.setTextColor(R.id.widget_empty,a.text);
 Intent click=new Intent(c,HabitWidget.class).setAction(CYCLE).setData(Uri.parse("habits://cycle/"+id)).putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,id);v.setPendingIntentTemplate(R.id.widget_rows,PendingIntent.getBroadcast(c,id,click,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_MUTABLE));
 return v;}
}
