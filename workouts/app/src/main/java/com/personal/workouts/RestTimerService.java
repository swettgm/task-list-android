package com.personal.workouts;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.os.*;
import org.json.JSONObject;

/** A user-visible foreground countdown, backed by the saved timer deadline. */
public final class RestTimerService extends Service {
    static final int NOTIFICATION=2;
    static final String CHANNEL="rest_countdown";
    static final String MORE="com.personal.workouts.REST_MORE";
    static final String SKIP="com.personal.workouts.REST_SKIP";
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Runnable update=new Runnable(){@Override public void run(){tick();}};
    static void sync(Context context) {
        JSONObject state=new WorkoutStore(context).read(),settings=state.optJSONObject("settings");
        if(state.optLong("restEndAt")<=System.currentTimeMillis()||settings==null||!settings.optBoolean("notifications")||state.isNull("active")){
            context.stopService(new Intent(context,RestTimerService.class));return;
        }
        try { context.startForegroundService(new Intent(context,RestTimerService.class)); }
        catch(RuntimeException unavailable) { /* The scheduled completion alarm remains available. */ }
    }
    private PendingIntent action(String name,int code) {
        return PendingIntent.getBroadcast(this,code,new Intent(this,RestAlarm.class).setAction(name),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
    Notification notification(JSONObject state) {
        long end=state.optLong("restEndAt"),now=System.currentTimeMillis();
        long remaining=Math.max(0,end-now),length=Math.max(1,state.optLong("restDurationMs",Math.max(remaining,90000)));
        PendingIntent open=PendingIntent.getActivity(this,5,new Intent(this,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        return new Notification.Builder(this,CHANNEL).setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Rest timer").setContentText("Next set")
            .setWhen(end).setShowWhen(true).setUsesChronometer(true).setChronometerCountDown(true)
            .setProgress(1000,(int)Math.min(1000,remaining*1000/length),false)
            .setOngoing(true).setOnlyAlertOnce(true).setContentIntent(open)
            .addAction(new Notification.Action.Builder(null,"+30s",action(MORE,3)).build())
            .addAction(new Notification.Action.Builder(null,"Skip",action(SKIP,4)).build())
            .build();
    }
    @Override public void onCreate() {
        super.onCreate();
        NotificationChannel channel=new NotificationChannel(CHANNEL,"Rest countdown",NotificationManager.IMPORTANCE_LOW);
        channel.setSound(null,null);channel.enableVibration(false);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }
    @Override public int onStartCommand(Intent intent,int flags,int startId) {
        handler.removeCallbacks(update);
        JSONObject state=new WorkoutStore(this).read();
        if(state.optLong("restEndAt")<=System.currentTimeMillis()||state.isNull("active")){
            stopSelf();return START_NOT_STICKY;
        }
        Notification notification=notification(state);
        if(Build.VERSION.SDK_INT>=34)startForeground(NOTIFICATION,notification,ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        else startForeground(NOTIFICATION,notification);
        handler.postDelayed(update,1000);
        return START_NOT_STICKY;
    }
    private void tick() {
        JSONObject state=new WorkoutStore(this).read(),settings=state.optJSONObject("settings");
        if(state.optLong("restEndAt")<=System.currentTimeMillis()||state.isNull("active")||settings==null||!settings.optBoolean("notifications")){
            stopForeground(true);stopSelf();RestAlarm.complete(this);return;
        }
        getSystemService(NotificationManager.class).notify(NOTIFICATION,notification(state));
        handler.postDelayed(update,1000);
    }
    @Override public void onDestroy(){handler.removeCallbacks(update);stopForeground(true);super.onDestroy();}
    @Override public IBinder onBind(Intent intent){return null;}
}
