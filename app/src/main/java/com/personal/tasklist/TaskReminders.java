package com.personal.tasklist;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import org.json.*;
import java.time.*;

/** Two one-shot alarms: next timed task and a daily reminder/maintenance wake-up. */
final class TaskReminders {
    static final String TASK="com.personal.tasklist.REMIND_TASK",DAILY="com.personal.tasklist.REMIND_DAILY";
    static final String WEEKEND="com.personal.tasklist.REMIND_WEEKEND";
    static final String CHANNEL="task-times",DAILY_CHANNEL="task-daily";
    private static final Object LOCK=new Object();
    static android.content.SharedPreferences prefs(Context c){return c.getSharedPreferences("task_reminders",Context.MODE_PRIVATE);}
    static void channels(Context c){NotificationManager manager=c.getSystemService(NotificationManager.class);manager.createNotificationChannel(new NotificationChannel(CHANNEL,"Scheduled task reminders",NotificationManager.IMPORTANCE_DEFAULT));manager.createNotificationChannel(new NotificationChannel(DAILY_CHANNEL,"Daily task reminder",NotificationManager.IMPORTANCE_DEFAULT));}
    static boolean allowed(Context c){return (Build.VERSION.SDK_INT<33||c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)==PackageManager.PERMISSION_GRANTED)&&c.getSystemService(NotificationManager.class).areNotificationsEnabled();}
    static void definitions(Context c,String json){try{JSONArray tasks=new JSONArray(json);if(json.length()>2*1024*1024)throw new IllegalArgumentException("Reminder definitions are too large");synchronized(LOCK){if(json.equals(prefs(c).getString("definitions","[]"))&&prefs(c).getLong("plannedRevision",-1)==new ReferenceStore(c).revision())return;if(!json.equals(prefs(c).getString("definitions","[]"))){if(!prefs(c).edit().putString("definitions",tasks.toString()).commit())throw new IllegalStateException("Reminder settings could not be saved");}}reschedule(c);}catch(JSONException e){throw new IllegalArgumentException("Invalid reminder definitions",e);}}
    private static PendingIntent alarm(Context c,String action){return PendingIntent.getBroadcast(c,0,new Intent(c,ReminderReceiver.class).setAction(action).setData(Uri.parse("tasks-reminder:"+action)),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
    static boolean precise(Context c){return Build.VERSION.SDK_INT<31||c.getSystemService(AlarmManager.class).canScheduleExactAlarms();}
    @android.annotation.SuppressLint("ScheduleExactAlarm") // Capability is checked; revocation races fall back safely.
    private static void schedule(Context c,AlarmManager manager,long at,PendingIntent operation){
        if(precise(c))try{manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,operation);return;}catch(SecurityException revoked){}
        manager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,at,operation);
    }
    static void reschedule(Context c){
        synchronized(LOCK){try{
            channels(c);JSONObject state=new JSONObject(new ReferenceStore(c).readState()),settings=state.optJSONObject("reminders");boolean timed=settings==null||settings.optBoolean("scheduled",true),daily=settings==null||settings.optBoolean("daily",true);
            AlarmManager alarms=c.getSystemService(AlarmManager.class);alarms.cancel(alarm(c,TASK));alarms.cancel(alarm(c,DAILY));
            long nowMillis=System.currentTimeMillis();JSONArray tasks=ReminderPlanner.effective(new JSONArray(prefs(c).getString("definitions","[]")),state),next=timed?ReminderPlanner.keepDue(new JSONArray(prefs(c).getString("next","[]")),ReminderPlanner.next(tasks,state,nowMillis),tasks,state,nowMillis):new JSONArray();
            if(!prefs(c).edit().putString("next",next.toString()).putLong("plannedRevision",new ReferenceStore(c).revision()).commit())throw new IllegalStateException("Reminder schedule could not be saved");
            NotificationManager notifications=c.getSystemService(NotificationManager.class);
            for(android.service.notification.StatusBarNotification shown:notifications.getActiveNotifications()){String tag=shown.getTag();if(tag==null)continue;if(tag.equals("daily")){if(!daily)notifications.cancel(tag,0);continue;}for(int i=0;i<tasks.length();i++){JSONObject task=tasks.getJSONObject(i);String prefix=task.optString("id")+":";if(tag.startsWith(prefix)&&(!timed||!task.optBoolean("notify")||ReminderPlanner.done(task,tag.substring(prefix.length()),state)))notifications.cancel(tag,0);}}
            if(next.length()>0)schedule(c,alarms,Math.max(nowMillis+1000,next.getJSONObject(0).getLong("at")),alarm(c,TASK));
            schedule(c,alarms,WeekendSchedule.nextOpening(ZonedDateTime.now(ReminderPlanner.ZONE),WeekendSchedule.cutoff(state)).toInstant().toEpochMilli(),alarm(c,WEEKEND));
            if(daily||timed){LocalTime time=ReminderPlanner.time(settings==null?"09:00":settings.optString("dailyTime","09:00"));if(time==null)time=LocalTime.of(9,0);ZonedDateTime now=ZonedDateTime.now(ReminderPlanner.ZONE),when=now.toLocalDate().atTime(time).atZone(ReminderPlanner.ZONE);if(!when.isAfter(now))when=now.toLocalDate().plusDays(1).atTime(time).atZone(ReminderPlanner.ZONE);schedule(c,alarms,when.toInstant().toEpochMilli(),alarm(c,DAILY));}
        }catch(JSONException|RuntimeException error){Log.e("Tasks","Could not refresh reminder alarms",error);}}
    }
    static void receive(Context c,String action){
        synchronized(LOCK){try{
            channels(c);JSONObject state=new JSONObject(new ReferenceStore(c).readState()),settings=state.optJSONObject("reminders");
            if(DAILY.equals(action)){if(settings==null||settings.optBoolean("daily",true))post(c,"daily","Your daily task reminder","Open Tasks to review today's list.",DAILY_CHANNEL);}
            else if(TASK.equals(action)&&(settings==null||settings.optBoolean("scheduled",true))){JSONArray next=new JSONArray(prefs(c).getString("next","[]")),tasks=ReminderPlanner.effective(new JSONArray(prefs(c).getString("definitions","[]")),state);long now=System.currentTimeMillis();for(int i=0;i<next.length();i++){JSONObject reminder=next.getJSONObject(i);long at=reminder.getLong("at");if(now<at||now-at>86400000L)continue;for(int j=0;j<tasks.length();j++){JSONObject task=tasks.getJSONObject(j);if(task.optString("id").equals(reminder.getString("id"))&&task.optBoolean("notify")&&ReminderPlanner.time(task.optString("time"))!=null&&!ReminderPlanner.done(task,reminder.getString("date"),state))post(c,reminder.getString("key"),task.optString("title"),"It's time for this task.",CHANNEL);}}JSONArray remaining=new JSONArray();for(int i=0;i<next.length();i++)if(next.getJSONObject(i).getLong("at")>now)remaining.put(next.getJSONObject(i));if(!prefs(c).edit().putString("next",remaining.toString()).commit())throw new IllegalStateException("Could not acknowledge reminder delivery");}
        }catch(JSONException|RuntimeException error){Log.e("Tasks","Could not show reminder",error);}}
        reschedule(c);
    }
    @android.annotation.SuppressLint("MissingPermission") // allowed() checks runtime permission and Android notification settings.
    private static void post(Context c,String tag,String title,String body,String channel){if(!allowed(c))return;Intent intent=new Intent(c,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);PendingIntent open=PendingIntent.getActivity(c,0,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);Notification n=new Notification.Builder(c,channel).setSmallIcon(R.drawable.ic_notification).setContentTitle(title).setContentText(body).setContentIntent(open).setAutoCancel(true).setCategory(Notification.CATEGORY_REMINDER).build();c.getSystemService(NotificationManager.class).notify(tag,0,n);}
}
