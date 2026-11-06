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

/** One-shot alarms for timed tasks, snoozes, the daily reminder and calendar-day refresh. */
final class TaskReminders {
    static final String TASK="com.personal.tasklist.REMIND_TASK",DAILY="com.personal.tasklist.REMIND_DAILY";
    static final String PROGRESS_COMPLETE="com.personal.tasklist.COMPLETE_PROGRESS";
    static final String PROGRESS_DISMISSED="com.personal.tasklist.PROGRESS_DISMISSED";
    static final String VISIBILITY="com.personal.tasklist.REFRESH_TASK_VISIBILITY";
    static final String WEEKEND="com.personal.tasklist.REMIND_WEEKEND";
    static final String COMPLETE="com.personal.tasklist.COMPLETE_REMINDER",SNOOZE="com.personal.tasklist.SNOOZE_REMINDER",SNOOZED="com.personal.tasklist.SNOOZED_REMINDER";
    static final String CHANNEL="task-times",DAILY_CHANNEL="task-daily",PROGRESS_CHANNEL="task-progress";
    private static final Object LOCK=new Object();
    static android.content.SharedPreferences prefs(Context c){return c.getSharedPreferences("task_reminders",Context.MODE_PRIVATE);}
    static void channels(Context c){NotificationManager manager=c.getSystemService(NotificationManager.class);manager.createNotificationChannel(new NotificationChannel(CHANNEL,"Scheduled task reminders",NotificationManager.IMPORTANCE_DEFAULT));manager.createNotificationChannel(new NotificationChannel(DAILY_CHANNEL,"Daily task reminder",NotificationManager.IMPORTANCE_DEFAULT));manager.createNotificationChannel(new NotificationChannel(PROGRESS_CHANNEL,"Tasks in progress",NotificationManager.IMPORTANCE_LOW));}
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
            JSONObject shown=new JSONObject(prefs(c).getString("shown","{}"));java.util.List<String> stale=new java.util.ArrayList<>();for(java.util.Iterator<String> it=shown.keys();it.hasNext();){String key=it.next();JSONObject record=shown.optJSONObject(key),task=record==null?null:taskFor(tasks,record.optString("id"));if(!timed||record==null||!valid(task,record.optString("date"),state)||!sameSchedule(record,task,state))stale.add(key);}for(String key:stale){shown.remove(key);notifications.cancel(key,0);}if(!prefs(c).edit().putString("shown",shown.toString()).commit())throw new IllegalStateException("Could not update reminder actions");
            alarms.cancel(alarm(c,VISIBILITY));long midnight=Instant.ofEpochMilli(nowMillis).atZone(ReminderPlanner.ZONE).toLocalDate().plusDays(1).atStartOfDay(ReminderPlanner.ZONE).toInstant().toEpochMilli();long nextReveal=TaskVisibility.next(tasks,state,nowMillis),reveal=nextReveal>0?Math.min(nextReveal,midnight):midnight;if(!prefs(c).edit().putLong("visibilityAt",reveal).commit())throw new IllegalStateException("Could not save task reveal time");schedule(c,alarms,reveal,alarm(c,VISIBILITY));
            progressNotification(c,state,tasks,settings);
            rescheduleSnoozes(c,alarms,state,tasks,timed,nowMillis);
            if(next.length()>0)schedule(c,alarms,Math.max(nowMillis+1000,next.getJSONObject(0).getLong("at")),alarm(c,TASK));
            alarms.cancel(alarm(c,WEEKEND));
            if(daily||timed){LocalTime time=ReminderPlanner.time(settings==null?"09:00":settings.optString("dailyTime","09:00"));if(time==null)time=LocalTime.of(9,0);ZonedDateTime now=ZonedDateTime.now(ReminderPlanner.ZONE),when=now.toLocalDate().atTime(time).atZone(ReminderPlanner.ZONE);if(!when.isAfter(now))when=now.toLocalDate().plusDays(1).atTime(time).atZone(ReminderPlanner.ZONE);schedule(c,alarms,when.toInstant().toEpochMilli(),alarm(c,DAILY));}
        }catch(JSONException|RuntimeException error){Log.e("Tasks","Could not refresh reminder alarms",error);}}
    }
    static void receive(Context c,String action){
        synchronized(LOCK){try{
            channels(c);JSONObject state=new JSONObject(new ReferenceStore(c).readState()),settings=state.optJSONObject("reminders");
            if(DAILY.equals(action)){if(settings==null||settings.optBoolean("daily",true))post(c,"daily",settingText(settings,"dailyTitle","Your daily task reminder"),settingText(settings,"dailyBody","Open Tasks to review today's list."),DAILY_CHANNEL);}
            else if(TASK.equals(action)&&(settings==null||settings.optBoolean("scheduled",true))){JSONArray next=new JSONArray(prefs(c).getString("next","[]")),tasks=ReminderPlanner.effective(new JSONArray(prefs(c).getString("definitions","[]")),state);long now=System.currentTimeMillis();for(int i=0;i<next.length();i++){JSONObject reminder=next.getJSONObject(i);long at=reminder.getLong("at");if(now<at||now-at>86400000L)continue;for(int j=0;j<tasks.length();j++){JSONObject task=tasks.getJSONObject(j);if(task.optString("id").equals(reminder.getString("id"))&&task.optBoolean("notify")&&ReminderPlanner.time(task.optString("time"))!=null&&!ReminderPlanner.done(task,reminder.getString("date"),state))postTask(c,reminder,task,state,settings);}}JSONArray remaining=new JSONArray();for(int i=0;i<next.length();i++)if(next.getJSONObject(i).getLong("at")>now)remaining.put(next.getJSONObject(i));if(!prefs(c).edit().putString("next",remaining.toString()).commit())throw new IllegalStateException("Could not acknowledge reminder delivery");}
        }catch(JSONException|RuntimeException error){Log.e("Tasks","Could not show reminder",error);}}
        reschedule(c);
    }
    @android.annotation.SuppressLint("MissingPermission") // allowed() checks runtime permission and notification settings.
    private static void progressNotification(Context c,JSONObject state,JSONArray tasks,JSONObject settings)throws JSONException{
        NotificationManager manager=c.getSystemService(NotificationManager.class);JSONArray targets=progressTargets(c,state,tasks);java.util.LinkedHashSet<String> titles=new java.util.LinkedHashSet<>();for(int i=0;i<targets.length();i++)titles.add(targets.getJSONObject(i).getString("title"));
        titles.remove("");if(titles.isEmpty()||!allowed(c)){manager.cancel("in-progress",0);return;}
        String names=String.join(", ",titles),title=progressText(settings,"progressTitle","In progress",names,titles.size()),body=progressText(settings,"progressBody","{tasks}",names,titles.size());
        if(title.equals("In progress"))title="🟢 In progress";
        Intent intent=new Intent(c,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);PendingIntent open=PendingIntent.getActivity(c,0,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        PendingIntent dismissed=PendingIntent.getBroadcast(c,0,new Intent(c,ReminderReceiver.class).setAction(PROGRESS_DISMISSED).setData(Uri.parse("tasks-progress:dismissed")),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder builder=new Notification.Builder(c,PROGRESS_CHANNEL).setSmallIcon(R.drawable.ic_notification_progress).setContentTitle(title).setContentText(body).setStyle(new Notification.BigTextStyle().bigText(body)).setContentIntent(open).setDeleteIntent(dismissed).setOngoing(true).setOnlyAlertOnce(true).setCategory(Notification.CATEGORY_STATUS);
        PendingIntent complete;
        if(targets.length()==1)complete=action(c,PROGRESS_COMPLETE,targets.getJSONObject(0));
        else{Intent choose=new Intent(c,MainActivity.class).setData(Uri.parse("tasks-progress:choose")).putExtra("complete_in_progress",true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);complete=PendingIntent.getActivity(c,1,choose,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
        builder.addAction(new Notification.Action.Builder(R.drawable.ic_notification_complete,targets.length()==1?"Complete":"Complete…",complete).build());Notification notification=builder.build();notification.flags|=Notification.FLAG_NO_CLEAR;manager.notify("in-progress",0,notification);
    }
    /** Parent occurrences represented by the notification; checklist steps collapse into their parent. */
    private static JSONArray progressTargets(Context c,JSONObject state,JSONArray tasks)throws JSONException{
        java.util.LinkedHashSet<String> active=new java.util.LinkedHashSet<>();JSONArray list=state.optJSONArray("inProgressTasks");if(list!=null)for(int i=0;i<list.length();i++)if(!list.optString(i).isEmpty())active.add(list.optString(i));String legacy=state.optString("inProgress","");if(!legacy.isEmpty()){if(!active.contains(legacy)){active.clear();active.add(legacy);}}else if(state.has("inProgress"))active.clear();
        JSONObject previous=new JSONObject(prefs(c).getString("progressActions","{}")),tokens=new JSONObject(),completed=state.optJSONObject("completed");JSONArray result=new JSONArray(),snapshot=new JSONArray(c.getSharedPreferences("reference_state",Context.MODE_PRIVATE).getString("snapshot","[]"));java.util.Set<String> seen=new java.util.HashSet<>();
        for(String value:active){String[] parts=value.split(":",3);if(parts.length<2||completed!=null&&completed.optBoolean(value))continue;try{LocalDate.parse(parts[1]);}catch(RuntimeException invalid){continue;}String id=parts[0],date=parts[1],key=id+":"+date;if(seen.contains(key)||ReminderPlanner.removed(id,date,state))continue;JSONObject task=taskFor(tasks,id);String title="";
            if(task!=null){if(task.optBoolean("retired")||!ReminderPlanner.scheduled(task,LocalDate.parse(date))||ReminderPlanner.done(task,date,state))continue;title=task.optString("title");}
            else for(int i=0;i<snapshot.length();i++){JSONObject row=snapshot.getJSONObject(i);if(key.equals(row.optString("progressKey"))&&!row.optBoolean("done")&&!TaskStore.DONE.equals(row.optString("status"))){JSONArray keys=row.optJSONArray("completionKeys");boolean done=keys!=null&&keys.length()>0;if(keys!=null)for(int k=0;k<keys.length();k++)if(completed==null||!completed.optBoolean(keys.optString(k)))done=false;if(!done)title=row.optString("title");break;}}
            if(title.isEmpty())continue;String token=previous.optString(key);if(token.isEmpty())token=java.util.UUID.randomUUID().toString();tokens.put(key,token);seen.add(key);result.put(new JSONObject().put("id",id).put("date",date).put("key",key).put("title",title).put("token",token));
        }
        if(!tokens.toString().equals(previous.toString())&&!prefs(c).edit().putString("progressActions",tokens.toString()).commit())throw new IllegalStateException("Could not save progress notification actions");return result;
    }
    static JSONArray progressChoices(Context c){synchronized(LOCK){try{JSONObject state=new JSONObject(new ReferenceStore(c).readState());return progressTargets(c,state,ReminderPlanner.effective(new JSONArray(prefs(c).getString("definitions","[]")),state));}catch(JSONException|RuntimeException error){Log.e("Tasks","Could not read active notification tasks",error);return new JSONArray();}}}
    static boolean completeProgress(Context c,String key,String token){
        boolean applied=false;
        synchronized(LOCK){try{if(key==null||token==null)return false;JSONObject state=new JSONObject(new ReferenceStore(c).readState());JSONArray tasks=ReminderPlanner.effective(new JSONArray(prefs(c).getString("definitions","[]")),state),choices=progressTargets(c,state,tasks);JSONObject target=null;for(int i=0;i<choices.length();i++){JSONObject value=choices.getJSONObject(i);if(key.equals(value.optString("key"))&&token.equals(value.optString("token"))){target=value;break;}}if(target==null)return false;
            JSONObject task=taskFor(tasks,target.getString("id"));ReferenceStore store=new ReferenceStore(c);applied=task!=null?store.completeProgressReminder(task,target.getString("date")):store.completeProgressTask("html:"+key);
        }catch(JSONException|RuntimeException error){Log.e("Tasks","Could not complete an in-progress task",error);}}
        if(applied){reschedule(c);TaskWidgetProvider.refreshAll(c);}return applied;
    }
    /** Android 14+ permits dismissing ongoing notifications; restore only while tasks remain active. */
    static void restoreProgress(Context c){synchronized(LOCK){try{channels(c);JSONObject state=new JSONObject(new ReferenceStore(c).readState());JSONArray tasks=ReminderPlanner.effective(new JSONArray(prefs(c).getString("definitions","[]")),state);progressNotification(c,state,tasks,state.optJSONObject("reminders"));}catch(JSONException|RuntimeException error){Log.e("Tasks","Could not restore active-task notification",error);}}}
    static String progressText(JSONObject settings,String key,String fallback,String names,int count){String template=settingText(settings,key,fallback);java.util.regex.Matcher tokens=java.util.regex.Pattern.compile("\\{(tasks|count)\\}").matcher(template);StringBuffer result=new StringBuffer();while(tokens.find())tokens.appendReplacement(result,java.util.regex.Matcher.quoteReplacement(tokens.group(1).equals("tasks")?names:Integer.toString(count)));tokens.appendTail(result);return result.toString();}
    static int snoozeMinutes(JSONObject settings){int minutes=settings==null?10:settings.optInt("snoozeMinutes",10);return minutes>=1&&minutes<=10080?minutes:10;}
    private static JSONObject taskFor(JSONArray tasks,String id){for(int i=0;i<tasks.length();i++){JSONObject task=tasks.optJSONObject(i);if(task!=null&&id.equals(task.optString("id")))return task;}return null;}
    private static String displayDate(JSONObject state,JSONObject task,String date){return ReminderPlanner.chosenDate(task,date,state);}
    private static boolean valid(JSONObject task,String date,JSONObject state){
        if(task==null||task.optBoolean("retired")||!task.optBoolean("notify")||ReminderPlanner.time(task.optString("time"))==null||ReminderPlanner.done(task,date,state)||(task.has("recurrenceResetOn")&&date.compareTo(task.optString("recurrenceResetOn"))<0))return false;
        try{LocalDate parsed=LocalDate.parse(date);JSONObject moved=state.optJSONObject("movedTo");return ReminderPlanner.scheduled(task,parsed);}catch(RuntimeException invalid){return false;}
    }
    private static boolean sameSchedule(JSONObject record,JSONObject task,JSONObject state){return task!=null&&record.optString("time").equals(task.optString("time"))&&record.optString("version").equals(task.optString("reminderScheduleVersion"))&&record.optString("display").equals(displayDate(state,task,record.optString("date")));}
    private static void rescheduleSnoozes(Context c,AlarmManager alarms,JSONObject state,JSONArray tasks,boolean enabled,long now)throws JSONException{
        alarms.cancel(alarm(c,SNOOZED));JSONArray saved=new JSONArray(prefs(c).getString("snoozes","[]")),kept=new JSONArray();long earliest=Long.MAX_VALUE;
        for(int i=0;i<saved.length();i++){JSONObject record=saved.getJSONObject(i),task=taskFor(tasks,record.optString("id"));long at=record.optLong("at");if(enabled&&valid(task,record.optString("date"),state)&&sameSchedule(record,task,state)&&at>=now-86400000L){kept.put(record);earliest=Math.min(earliest,at);}}
        if(!prefs(c).edit().putString("snoozes",kept.toString()).commit())throw new IllegalStateException("Could not save snoozed reminders");if(earliest<Long.MAX_VALUE)schedule(c,alarms,Math.max(now+1000,earliest),alarm(c,SNOOZED));
    }
    static void handleAction(Context c,Intent intent){
        synchronized(LOCK){try{String action=intent.getAction(),id=intent.getStringExtra("id"),date=intent.getStringExtra("date"),key=intent.getStringExtra("key"),token=intent.getStringExtra("token");if((!COMPLETE.equals(action)&&!SNOOZE.equals(action))||id==null||date==null||!((id+":"+date).equals(key))||token==null)return;
            JSONObject shown=new JSONObject(prefs(c).getString("shown","{}")),record=shown.optJSONObject(key);if(record==null||!token.equals(record.optString("token")))return;JSONObject state=new JSONObject(new ReferenceStore(c).readState()),settings=state.optJSONObject("reminders");JSONArray tasks=ReminderPlanner.effective(new JSONArray(prefs(c).getString("definitions","[]")),state);JSONObject task=taskFor(tasks,id);if((settings!=null&&!settings.optBoolean("scheduled",true))||!valid(task,date,state)||!sameSchedule(record,task,state))return;
            if(COMPLETE.equals(action)){if(!new ReferenceStore(c).completeReminder(task,date))return;}
            else{JSONArray snoozes=new JSONArray(prefs(c).getString("snoozes","[]")),next=new JSONArray();for(int i=0;i<snoozes.length();i++)if(!key.equals(snoozes.getJSONObject(i).optString("key")))next.put(snoozes.getJSONObject(i));JSONObject snooze=new JSONObject(record.toString()).put("at",System.currentTimeMillis()+snoozeMinutes(settings)*60000L);next.put(snooze);if(!prefs(c).edit().putString("snoozes",next.toString()).commit())throw new IllegalStateException("Could not snooze reminder");}
            // Consume the action token so a queued double tap cannot repeat it.
            shown=new JSONObject(prefs(c).getString("shown","{}"));shown.remove(key);JSONArray pending=new JSONArray(prefs(c).getString("next","[]")),remaining=new JSONArray();for(int i=0;i<pending.length();i++)if(!key.equals(pending.getJSONObject(i).optString("key")))remaining.put(pending.getJSONObject(i));if(!prefs(c).edit().putString("shown",shown.toString()).putString("next",remaining.toString()).commit())throw new IllegalStateException("Could not acknowledge reminder action");c.getSystemService(NotificationManager.class).cancel(key,0);
        }catch(JSONException|RuntimeException error){Log.e("Tasks","Could not apply reminder action",error);}}
        reschedule(c);TaskWidgetProvider.refreshAll(c);
    }
    static void receiveSnoozed(Context c){
        synchronized(LOCK){try{JSONObject state=new JSONObject(new ReferenceStore(c).readState()),settings=state.optJSONObject("reminders");JSONArray tasks=ReminderPlanner.effective(new JSONArray(prefs(c).getString("definitions","[]")),state),saved=new JSONArray(prefs(c).getString("snoozes","[]")),remaining=new JSONArray();long now=System.currentTimeMillis();for(int i=0;i<saved.length();i++){JSONObject record=saved.getJSONObject(i);if(record.getLong("at")>now){remaining.put(record);continue;}JSONObject task=taskFor(tasks,record.optString("id"));if((settings==null||settings.optBoolean("scheduled",true))&&now-record.getLong("at")<=86400000L&&valid(task,record.optString("date"),state)&&sameSchedule(record,task,state))postTask(c,record,task,state,settings);}if(!prefs(c).edit().putString("snoozes",remaining.toString()).commit())throw new IllegalStateException("Could not acknowledge snooze");}catch(JSONException|RuntimeException error){Log.e("Tasks","Could not show snoozed reminder",error);}}
        reschedule(c);
    }
    private static PendingIntent action(Context c,String action,JSONObject record){Intent intent=new Intent(c,ReminderReceiver.class).setAction(action).setData(Uri.parse("tasks-action:"+action+":"+Uri.encode(record.optString("key"))+":"+record.optString("token"))).putExtra("id",record.optString("id")).putExtra("date",record.optString("date")).putExtra("key",record.optString("key")).putExtra("token",record.optString("token"));return PendingIntent.getBroadcast(c,0,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);}
    private static void postTask(Context c,JSONObject reminder,JSONObject task,JSONObject state,JSONObject settings)throws JSONException{
        JSONObject record=new JSONObject().put("id",task.getString("id")).put("date",reminder.getString("date")).put("key",reminder.getString("key")).put("token",java.util.UUID.randomUUID().toString()).put("time",task.optString("time")).put("version",task.optString("reminderScheduleVersion")).put("display",displayDate(state,task,reminder.getString("date")));
        if(!allowed(c))return;JSONObject shown=new JSONObject(prefs(c).getString("shown","{}"));shown.put(record.getString("key"),record);if(!prefs(c).edit().putString("shown",shown.toString()).commit())throw new IllegalStateException("Could not save reminder action");String date=record.getString("display");post(c,record.getString("key"),notificationText(settings,"taskTitle","{task}",task,date),notificationText(settings,"taskBody","It's time for this task.",task,date),CHANNEL,record);
    }
    private static String settingText(JSONObject settings,String key,String fallback){return settings==null||settings.isNull(key)?fallback:settings.optString(key,fallback);}
    static String notificationText(JSONObject settings,String key,String fallback,JSONObject task,String date){
        String template=settingText(settings,key,fallback);
        java.util.regex.Matcher tokens=java.util.regex.Pattern.compile("\\{(task|time|date)\\}").matcher(template);StringBuffer result=new StringBuffer();
        while(tokens.find()){String name=tokens.group(1),value=name.equals("task")?task.optString("title"):name.equals("time")?task.optString("time"):date;tokens.appendReplacement(result,java.util.regex.Matcher.quoteReplacement(value));}tokens.appendTail(result);return result.toString();
    }
    @android.annotation.SuppressLint("MissingPermission") // allowed() checks runtime permission and Android notification settings.
    private static void post(Context c,String tag,String title,String body,String channel){post(c,tag,title,body,channel,null);}
    @android.annotation.SuppressLint("MissingPermission")
    private static void post(Context c,String tag,String title,String body,String channel,JSONObject record){if(!allowed(c))return;Intent intent=new Intent(c,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);PendingIntent open=PendingIntent.getActivity(c,0,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);Notification.Builder builder=new Notification.Builder(c,channel).setSmallIcon(R.drawable.ic_notification).setContentTitle(title).setContentText(body).setStyle(new Notification.BigTextStyle().bigText(body)).setContentIntent(open).setAutoCancel(true).setCategory(Notification.CATEGORY_REMINDER);if(record!=null){builder.addAction(new Notification.Action.Builder(R.drawable.ic_notification_snooze,"Snooze",action(c,SNOOZE,record)).build());builder.addAction(new Notification.Action.Builder(R.drawable.ic_notification_complete,"Complete",action(c,COMPLETE,record)).build());}c.getSystemService(NotificationManager.class).notify(tag,0,builder.build());}
}
