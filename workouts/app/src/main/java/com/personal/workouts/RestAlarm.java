package com.personal.workouts;

import android.app.*;
import android.content.*;
import android.os.*;
import org.json.JSONObject;

public final class RestAlarm extends BroadcastReceiver {
    static final String CHANNEL="rest_complete_silent";
    private static final Handler countdown = new Handler(Looper.getMainLooper());
    private static Runnable nextCue;
    private static long cueDeadline;
    private static int cueSeconds;
    private static int cueDuration;
    private static boolean cueAudio;

    private static void scheduleCountdown(Context context, JSONObject state) {
        JSONObject settings = state.optJSONObject("settings");
        long end = state.isNull("active") ? 0 : state.optLong("restEndAt");
        int seconds = settings == null ? 0 : settings.optInt("countdownSeconds",3);
        int duration = settings == null ? 0 : settings.optInt("countdownVibrationMs", 80);
        boolean audio = settings != null && settings.optBoolean("headphoneCues", true);
        if (seconds != 3 && seconds != 5) seconds = 0;
        if (cueDeadline == end && cueSeconds == seconds && cueDuration == duration && cueAudio == audio) return;
        if (nextCue != null) countdown.removeCallbacks(nextCue);
        cueDeadline = end;
        cueSeconds = seconds;
        cueDuration = duration;
        cueAudio = audio;
        if (seconds == 0 || end <= System.currentTimeMillis()) return;
        postCue(context.getApplicationContext(), end, seconds, duration, audio);
    }

    private static void postCue(Context context, long end, int seconds, int duration, boolean audio) {
        long now = System.currentTimeMillis();
        long when = nextCountdownCue(now, end, seconds);
        if (when >= end) return;
        nextCue = () -> {
            if (new WorkoutStore(context).read().optLong("restEndAt") != end) return;
            long time = System.currentTimeMillis();
            if (time < end && time - when < 500) {
                vibrate(context, duration);
                if (audio) HeadphoneCue.play(context, end, false);
            }
            postCue(context, end, seconds, duration, audio);
        };
        countdown.postDelayed(nextCue, Math.max(1, when - now + 1));
    }

    static long nextCountdownCue(long now, long end, int seconds) {
        long remaining = end - now;
        if (remaining <= 0 || seconds <= 0) return end;
        return end - 1000 * Math.min(seconds, (remaining - 1) / 1000);
    }

    private static void vibrate(Context context, int ms) {
        if (ms <= 0) return;
        Vibrator vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) vibrator.vibrate(VibrationEffect.createOneShot(ms,VibrationEffect.DEFAULT_AMPLITUDE));
    }
    static PendingIntent intent(Context context) {
        return PendingIntent.getBroadcast(context,1,new Intent(context,RestAlarm.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
    static boolean precise(Context context) {
        return Build.VERSION.SDK_INT<31||context.getSystemService(AlarmManager.class).canScheduleExactAlarms();
    }
    static void schedule(Context context) {
        WorkoutStore store=new WorkoutStore(context);
        JSONObject state=store.read();
        scheduleCountdown(context,state);
        long end=state.optLong("restEndAt");
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
            store.prefs.edit().putLong("announcedRest",end).commit();
            vibrate(context,settings.optInt("vibrationMs",350));
            if (now-end<1000 && settings.optBoolean("headphoneCues",true)) HeadphoneCue.play(context,end,true);
            if(!settings.optBoolean("notifications")||MainActivity.visible)return;
            NotificationManager manager=context.getSystemService(NotificationManager.class);
            NotificationChannel channel=new NotificationChannel(CHANNEL,"Rest timer",NotificationManager.IMPORTANCE_LOW);
            channel.setSound(null,null);channel.enableVibration(false);manager.createNotificationChannel(channel);
            PendingIntent open=PendingIntent.getActivity(context,2,new Intent(context,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
            try {
                manager.notify(1,new Notification.Builder(context,CHANNEL).setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(RestTimerService.title(0)).setShowWhen(false)
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
