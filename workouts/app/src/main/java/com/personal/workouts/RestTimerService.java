package com.personal.workouts;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.os.*;
import android.provider.Settings;
import org.json.JSONObject;

/** A user-visible foreground countdown, backed by the saved timer deadline. */
public final class RestTimerService extends Service {
    static final int NOTIFICATION=2;
    static final String CHANNEL="rest_countdown";
    static final String MORE="com.personal.workouts.REST_MORE";
    static final String SKIP="com.personal.workouts.REST_SKIP";
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Runnable update=this::tick;
    private static final long GO_DISPLAY_MS=4000;
    private FloatingRestTimer overlay;
    private String previousTitle;

    static boolean canFloat(Context context,JSONObject settings) {
        return settings!=null&&settings.optBoolean("floatOverApps")&&Settings.canDrawOverlays(context);
    }

    private static boolean isCounting(JSONObject state,long now) {
        return !state.isNull("active")&&state.optLong("restEndAt")>now;
    }

    static void sync(Context context) {
        JSONObject state=new WorkoutStore(context).read(),settings=state.optJSONObject("settings");
        if(!isCounting(state,System.currentTimeMillis())||settings==null
            ||(!settings.optBoolean("notifications")&&!canFloat(context,settings))){
            context.stopService(new Intent(context,RestTimerService.class));
            return;
        }
        try { context.startForegroundService(new Intent(context,RestTimerService.class)); }
        catch(RuntimeException unavailable) { /* The scheduled completion alarm remains available. */ }
    }
    private PendingIntent action(String name,int code) {
        return PendingIntent.getBroadcast(this,code,new Intent(this,RestAlarm.class).setAction(name),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
    static String title(long remaining) {
        return "Rest · "+(remaining>0?FloatingRestTimer.formatTime(remaining):"Go!");
    }
    Notification notification(JSONObject state) {
        long end=state.optLong("restEndAt"),now=System.currentTimeMillis();
        long remaining=Math.max(0,end-now),length=Math.max(1,state.optLong("restDurationMs",Math.max(remaining,90000)));
        PendingIntent open=PendingIntent.getActivity(this,5,new Intent(this,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        return new Notification.Builder(this,CHANNEL).setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title(remaining)).setShowWhen(false)
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
        previousTitle=null;
        tick();
        return START_NOT_STICKY;
    }
    private void tick() {
        JSONObject state=new WorkoutStore(this).read(),settings=state.optJSONObject("settings");
        long now=System.currentTimeMillis(),end=state.optLong("restEndAt"),remaining=Math.max(0,end-now);
        boolean allowed=canFloat(this,settings);
        boolean showOverApps=allowed&&!MainActivity.visible;
        boolean done=remaining==0;
        boolean showGo=showOverApps&&end>0&&now-end<GO_DISPLAY_MS;
        if(state.isNull("active")||settings==null||(!allowed&&!settings.optBoolean("notifications"))||(done&&!showGo)){
            if(done&&end>0)RestAlarm.complete(this);
            stopSelf();
            clearOverlay();
            return;
        }

        if(done)RestAlarm.complete(this);
        if(showOverApps){
            if(overlay==null){
                try {
                    FloatingRestTimer timer=new FloatingRestTimer(this);
                    if(timer.show())overlay=timer;
                }catch(RuntimeException unavailable){ /* The notification is still available. */ }
            }
            if(overlay!=null)overlay.update(state,remaining);
        }else clearOverlay();

        String title=title(remaining);
        if(!title.equals(previousTitle)){
            getSystemService(NotificationManager.class).notify(NOTIFICATION,notification(state));
            previousTitle=title;
        }
        // Poll foreground changes and animate the ring separately from notification updates.
        handler.postDelayed(update,allowed?200:Math.max(50,Math.min(1000,(remaining-1)%1000+1)));
    }

    private void clearOverlay() {
        if(overlay!=null){
            overlay.remove();
            overlay=null;
        }
    }

    @Override public void onDestroy(){
        handler.removeCallbacks(update);
        clearOverlay();
        stopForeground(STOP_FOREGROUND_REMOVE);
        super.onDestroy();
    }
    @Override public IBinder onBind(Intent intent){return null;}
}
