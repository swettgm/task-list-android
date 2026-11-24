package com.personal.workouts;

import android.app.*;
import android.content.*;
import android.os.*;
import org.json.JSONObject;

public final class RestAlarm extends BroadcastReceiver {
    static final String CHANNEL="rest";
    static PendingIntent intent(Context context) {
        return PendingIntent.getBroadcast(context,1,new Intent(context,RestAlarm.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
    static boolean precise(Context context) {
        return Build.VERSION.SDK_INT<31||context.getSystemService(AlarmManager.class).canScheduleExactAlarms();
    }
    static void schedule(Context context) {
        WorkoutStore store=new WorkoutStore(context);
        long end=store.read().optLong("restEndAt");
        if(store.prefs.getLong("alarmEnd",0)==end){RestTimerService.sync(context);return;}
        AlarmManager alarms=context.getSystemService(AlarmManager.class);
        alarms.cancel(intent(context));
        context.getSystemService(NotificationManager.class).cancel(1);
        store.prefs.edit().putLong("alarmEnd",end).apply();
        if(end<=System.currentTimeMillis()){RestTimerService.sync(context);return;}
        try {
            if(precise(context))alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,end,intent(context));
            else alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,end,intent(context));
        } catch(SecurityException unavailable) {
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,end,intent(context));
        }
        RestTimerService.sync(context);
    }
    static void complete(Context context) {
        synchronized(WorkoutStore.LOCK) {
            WorkoutStore store=new WorkoutStore(context);
            JSONObject state=store.read(),settings=state.optJSONObject("settings");
            long end=state.optLong("restEndAt"),now=System.currentTimeMillis();
            if(end<=0||now<end||now-end>1800000||store.prefs.getLong("announcedRest",0)==end||settings==null)return;
            context.stopService(new Intent(context,RestTimerService.class));
            store.prefs.edit().putLong("announcedRest",end).commit();
            int ms=settings.optInt("vibrationMs",350);
            Vibrator vibrator=(Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
            if(ms>0&&vibrator!=null&&vibrator.hasVibrator())vibrator.vibrate(VibrationEffect.createOneShot(ms,VibrationEffect.DEFAULT_AMPLITUDE));
            if(!settings.optBoolean("notifications")||MainActivity.visible)return;
            NotificationManager manager=context.getSystemService(NotificationManager.class);
            NotificationChannel channel=new NotificationChannel(CHANNEL,"Rest timer",NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableVibration(false);manager.createNotificationChannel(channel);
            PendingIntent open=PendingIntent.getActivity(context,2,new Intent(context,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
            try {
                manager.notify(1,new Notification.Builder(context,CHANNEL).setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Rest complete").setContentText("Ready for your next set")
                    .setContentIntent(open).setAutoCancel(true).build());
            } catch(SecurityException denied) { /* The saved workout remains available without notification permission. */ }
        }
    }
    @Override public void onReceive(Context context,Intent intent) {
        if(RestTimerService.MORE.equals(intent.getAction())||RestTimerService.SKIP.equals(intent.getAction())){
            synchronized(WorkoutStore.LOCK){
                WorkoutStore store=new WorkoutStore(context);JSONObject state=store.read();
                if(state.isNull("active"))return;
                try {
                    if(RestTimerService.SKIP.equals(intent.getAction()))state.put("restEndAt",0);
                    else {
                        long now=System.currentTimeMillis(),end=state.optLong("restEndAt");
                        if(end<=now)state.put("restStartedAt",now).put("restDurationMs",30000);
                        else state.put("restDurationMs",state.optLong("restDurationMs",end-now)+30000);
                        state.put("restEndAt",Math.max(now,end)+30000);
                    }
                    if(store.write(state.toString())!=null)schedule(context);
                }catch(org.json.JSONException ignored){ }
            }
        }else complete(context);
    }
}
