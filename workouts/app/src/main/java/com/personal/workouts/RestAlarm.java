package com.personal.workouts;

import android.app.*;
import android.content.*;
import android.os.*;
import org.json.JSONObject;

public final class RestAlarm extends BroadcastReceiver {
    static final String CHANNEL="rest_complete_silent";
    private static final String END_TIMER="com.personal.workouts.END_REST_TIMER";
    private static final Handler countdown = new Handler(Looper.getMainLooper());
    private static Runnable nextCue;
    private static long cueDeadline;
    private static int cueSeconds;
    private static int cueDuration;
    private static boolean cueAudio;

    private static void scheduleCountdown(Context context, JSONObject state) {
        JSONObject settings = state.optJSONObject("settings");
        long end = RestTimerService.hasRest(state) ? state.optLong("restEndAt") : 0;
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

    static void vibrate(Context context, int ms) {
        if (ms <= 0) return;
        Vibrator vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) vibrator.vibrate(VibrationEffect.createOneShot(ms,VibrationEffect.DEFAULT_AMPLITUDE));
    }
    static PendingIntent intent(Context context) {
        return PendingIntent.getBroadcast(context,1,new Intent(context,RestAlarm.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
    static long restId(JSONObject state){
        long started=state.optLong("restStartedAt");
        return started>0?started:state.optLong("restEndAt");
    }

    static Notification.Action endTimerAction(Context context,JSONObject state){
        long id=restId(state);
        Intent stop=new Intent(context,RestAlarm.class).setAction(END_TIMER)
            .setData(android.net.Uri.fromParts("exercise-rest",Long.toString(id),null))
            .putExtra("restId",id);
        PendingIntent pending=PendingIntent.getBroadcast(context,3,stop,PendingIntent.FLAG_IMMUTABLE);
        return new Notification.Action.Builder(null,"End timer",pending).build();
    }

    private static void endTimer(Context context,long expected){
        synchronized(WorkoutStore.LOCK){
            WorkoutStore store=new WorkoutStore(context);
            JSONObject state=store.read();
            if(expected<=0||!RestTimerService.hasRest(state)||restId(state)!=expected)return;
            try{
                state.put("restEndAt",0).put("restStartedAt",0).put("restDurationMs",0);
                state.remove("restSource");
                state.remove("restPosition");
                if(store.write(state.toString())==null)return;
            }catch(org.json.JSONException invalid){return;}
        }
        schedule(context);
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
            if(!settings.optBoolean("notifications")||MainActivity.visible||RestTimerService.isFloatingVisible())return;
            NotificationManager manager=context.getSystemService(NotificationManager.class);
            NotificationChannel channel=new NotificationChannel(CHANNEL,"Rest timer",NotificationManager.IMPORTANCE_LOW);
            channel.setSound(null,null);channel.enableVibration(false);manager.createNotificationChannel(channel);
            PendingIntent open=PendingIntent.getActivity(context,2,new Intent(context,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
            try {
                manager.notify(1,new Notification.Builder(context,CHANNEL).setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("Rest complete").setContentText("Ready for your next set").setShowWhen(false)
                    .setContentIntent(open).setAutoCancel(true).addAction(endTimerAction(context,state)).build());
            } catch(SecurityException denied) { /* The saved workout remains available without notification permission. */ }
        }
    }
    @Override public void onReceive(Context context,Intent intent) {
        if(END_TIMER.equals(intent.getAction()))endTimer(context,intent.getLongExtra("restId",0));
        else if(intent.getAction()==null)complete(context);
    }
}
