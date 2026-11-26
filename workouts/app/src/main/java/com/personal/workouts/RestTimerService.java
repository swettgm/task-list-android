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
    private final Handler handler=new Handler(Looper.getMainLooper());
    private final Runnable update=this::tick;
    static final String OPEN_CONTROLS="com.personal.workouts.OPEN_REST_CONTROLS";
    private static volatile boolean floatingVisible;
    private static final String COMPLETE_TITLE="Rest complete";
    private static final String COMPLETE_TEXT="Ready for your next set";

    static boolean isFloatingVisible(){return floatingVisible;}
    private FloatingRestTimer overlay;
    private String previousTitle;
    private long previousRest;

    static boolean canFloat(Context context,JSONObject settings) {
        return settings!=null&&settings.optBoolean("floatingTimer",true)&&settings.optBoolean("floatOverApps")&&Settings.canDrawOverlays(context);
    }

    private static boolean shouldRun(Context context,JSONObject state,long now) {
        JSONObject settings=state.optJSONObject("settings");
        long end=state.optLong("restEndAt");
        if(state.isNull("active")||settings==null||end==0)return false;
        return (canFloat(context,settings)&&!state.optBoolean("restDismissed"))||(end>now&&settings.optBoolean("notifications"));
    }

    static void sync(Context context) {
        JSONObject state=new WorkoutStore(context).read();
        if(!shouldRun(context,state,System.currentTimeMillis())){
            context.stopService(new Intent(context,RestTimerService.class));
            return;
        }
        try { context.startForegroundService(new Intent(context,RestTimerService.class)); }
        catch(RuntimeException unavailable) { /* The scheduled completion alarm remains available. */ }
    }
    static String title(long remaining) {
        return remaining>0?"Rest · "+FloatingRestTimer.formatTime(remaining):COMPLETE_TITLE;
    }
    Notification notification(JSONObject state) {
        long end=state.optLong("restEndAt"),now=System.currentTimeMillis();
        long remaining=Math.max(0,end-now),length=Math.max(1,state.optLong("restDurationMs",Math.max(remaining,90000)));
        PendingIntent open=PendingIntent.getActivity(this,5,new Intent(this,MainActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification.Builder builder=new Notification.Builder(this,CHANNEL).setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title(remaining)).setShowWhen(false)
            .setOngoing(true).setOnlyAlertOnce(true).setContentIntent(open)
            .addAction(RestAlarm.endTimerAction(this,state));
        if(remaining>0)builder.setProgress(1000,(int)Math.min(1000,remaining*1000/length),false);
        else builder.setContentText(COMPLETE_TEXT);
        return builder.build();
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
        if(!shouldRun(this,state,System.currentTimeMillis())){
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
        long now=System.currentTimeMillis(),end=state.optLong("restEndAt"),remaining=end-now;
        boolean allowed=canFloat(this,settings)&&!state.optBoolean("restDismissed");
        if(!shouldRun(this,state,now)){
            if(remaining<=0&&end>0&&!state.isNull("active"))RestAlarm.complete(this);
            stopSelf();
            clearOverlay();
            return;
        }

        if(allowed){
            if(overlay!=null&&!overlay.isShowing())clearOverlay();
            if(overlay==null){
                try {
                    FloatingRestTimer timer=new FloatingRestTimer(this);
                    if(timer.show()){
                        overlay=timer;
                        floatingVisible=true;
                    }
                }catch(RuntimeException unavailable){ /* The in-app timer and notification remain available. */ }
            }
            if(overlay!=null)overlay.update(state,remaining);
        }else clearOverlay();

        if(remaining<=0)RestAlarm.complete(this);
        String title=title(remaining);
        long rest=RestAlarm.restId(state);
        if(!title.equals(previousTitle)||rest!=previousRest){
            getSystemService(NotificationManager.class).notify(NOTIFICATION,notification(state));
            previousTitle=title;
            previousRest=rest;
        }
        handler.postDelayed(update,allowed?200:Math.max(50,Math.min(1000,(remaining-1)%1000+1)));
    }

    private void clearOverlay() {
        floatingVisible=false;
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
