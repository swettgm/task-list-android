package com.personal.habits;
import android.app.*;
import android.content.*;
import java.time.*;
public final class DayReceiver extends android.content.BroadcastReceiver {
 private static PendingIntent alarm(Context c){return PendingIntent.getBroadcast(c,0,new Intent(c,DayReceiver.class).setAction("com.personal.habits.MIDNIGHT"),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
 static void schedule(Context c){if(HabitWidget.ids(c).length==0)return;AlarmManager a=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);if(a!=null)a.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,HabitStore.nextMidnight(Clock.systemDefaultZone()),alarm(c));}
 static void cancel(Context c){AlarmManager a=(AlarmManager)c.getSystemService(Context.ALARM_SERVICE);if(a!=null)a.cancel(alarm(c));}
 @Override public void onReceive(Context c,Intent i){HabitWidget.refresh(c);}
}
