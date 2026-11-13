package com.personal.habits;
import android.widget.*;
import android.content.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
public final class HabitRowsService extends RemoteViewsService {
 @Override public RemoteViewsFactory onGetViewFactory(Intent intent){return new Factory(getApplicationContext(),LocalDate.parse(intent.getStringExtra("anchor")));}
 static final int[] CELLS={R.id.cell0,R.id.cell1,R.id.cell2,R.id.cell3,R.id.cell4,R.id.cell5,R.id.cell6};
 static final class Factory implements RemoteViewsFactory {
  final Context c;final LocalDate anchor;List<HabitStore.Habit> habits=new ArrayList<>();List<LocalDate> days=new ArrayList<>();Appearance appearance;
  Factory(Context c,LocalDate anchor){this.c=c;this.anchor=anchor;}
  public void onCreate(){onDataSetChanged();}
  public void onDataSetChanged(){habits=new HabitStore(c).habits();days=HabitStore.window(anchor);appearance=new Appearance(c);}
  public void onDestroy(){}
  public int getCount(){return habits.size();}
  public RemoteViews getViewAt(int position){if(position<0||position>=habits.size())return null;return row(c,habits.get(position),days,appearance);}
  public RemoteViews getLoadingView(){return null;}
  public int getViewTypeCount(){return 1;}
  public long getItemId(int position){return position;}
  public boolean hasStableIds(){return false;}
 }
 static RemoteViews row(Context c,HabitStore.Habit habit,List<LocalDate> days,Appearance a){return row(c,habit,days,a,true);}
 static RemoteViews row(Context c,HabitStore.Habit habit,List<LocalDate> days,Appearance a,boolean widget){RemoteViews row=new RemoteViews(c.getPackageName(),widget?R.layout.widget_habit_row:R.layout.app_habit_row);row.setTextViewText(R.id.habit_title,habit.name);row.setTextColor(R.id.habit_title,a.text);row.setContentDescription(R.id.habit_title,habit.name);row.setTextViewTextSize(R.id.habit_title,android.util.TypedValue.COMPLEX_UNIT_SP,a.widgetTextSize());HabitStore store=new HabitStore(c);for(int i=0;i<7;i++){LocalDate day=days.get(i);int state=store.state(habit.id,day);row.setImageViewBitmap(CELLS[i],Artwork.mark(c,state,a.text));row.setContentDescription(CELLS[i],description(habit.name,day,state));row.setOnClickFillInIntent(CELLS[i],new Intent().putExtra("habit_id",habit.id).putExtra("day",day.toString()).putExtra("item_action","cycle"));}return row;}
 static String description(String name,LocalDate day,int state){return name+", "+day.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, uuuu",Locale.getDefault()))+", "+(state==1?"done":state==2?"missed":"unmarked")+". Tap for "+(state==0?"done":state==1?"missed":"unmarked");}
}
