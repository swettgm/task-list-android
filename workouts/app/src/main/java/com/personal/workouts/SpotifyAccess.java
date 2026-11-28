package com.personal.workouts;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.*;
import android.media.MediaMetadata;
import android.graphics.Bitmap;
import android.util.Base64;
import java.io.ByteArrayOutputStream;
import android.media.session.*;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import org.json.JSONObject;

/** Uses Spotify's active media session. Notification contents are never read or stored. */
public final class SpotifyAccess extends NotificationListenerService {
    static final String PACKAGE_NAME="com.spotify.music";
    private static Bitmap lastArt;
    private static String artData="";
    private static synchronized String artwork(MediaMetadata metadata) {
        Bitmap bitmap=metadata==null?null:metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);
        if(bitmap==null&&metadata!=null)bitmap=metadata.getBitmap(MediaMetadata.METADATA_KEY_ART);
        if(bitmap==null&&metadata!=null)bitmap=metadata.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON);
        if(bitmap==null||bitmap.isRecycled()){lastArt=null;artData="";return "";}
        if(bitmap==lastArt)return artData;
        double scale=Math.min(96.0/bitmap.getWidth(),96.0/bitmap.getHeight());
        Bitmap thumb=Bitmap.createScaledBitmap(bitmap,Math.max(1,(int)(bitmap.getWidth()*scale)),Math.max(1,(int)(bitmap.getHeight()*scale)),true);
        ByteArrayOutputStream bytes=new ByteArrayOutputStream();
        thumb.compress(Bitmap.CompressFormat.PNG,100,bytes);
        artData="data:image/png;base64,"+Base64.encodeToString(bytes.toByteArray(),Base64.NO_WRAP);
        lastArt=bitmap;
        if(thumb!=bitmap)thumb.recycle();
        return artData;
    }
    static boolean allowed(Context context) {
        ComponentName component=new ComponentName(context,SpotifyAccess.class);
        if(Build.VERSION.SDK_INT>=27)return context.getSystemService(NotificationManager.class).isNotificationListenerAccessGranted(component);
        String enabled=android.provider.Settings.Secure.getString(context.getContentResolver(),"enabled_notification_listeners");
        if(enabled==null)return false;
        for(String name:enabled.split(":"))if(component.equals(ComponentName.unflattenFromString(name)))return true;
        return false;
    }
    private static MediaController controller(Context context) {
        if(!allowed(context))return null;
        try {
            for(MediaController controller:context.getSystemService(MediaSessionManager.class).getActiveSessions(new ComponentName(context,SpotifyAccess.class))) {
                if(PACKAGE_NAME.equals(controller.getPackageName()))return controller;
            }
        } catch(SecurityException denied) { }
        return null;
    }
    static boolean openSession(Context context) {
        MediaController controller=controller(context);
        if(controller==null)return false;
        PendingIntent activity=controller.getSessionActivity();
        if(activity==null||!PACKAGE_NAME.equals(activity.getCreatorPackage()))return false;
        try {
            activity.send();
            return true;
        } catch(PendingIntent.CanceledException|SecurityException unavailable) {
            return false;
        }
    }
    static JSONObject state(Context context) {
        JSONObject value=new JSONObject();
        try {
            MediaController controller=controller(context);
            value.put("connected",allowed(context)).put("available",controller!=null).put("title","Open Spotify").put("artist","").put("playing",false).put("art","");
            if(controller!=null) {
                MediaMetadata metadata=controller.getMetadata();PlaybackState playback=controller.getPlaybackState();
                if(metadata!=null) {
                    value.put("art",artwork(metadata));
                    String title=metadata.getString(MediaMetadata.METADATA_KEY_TITLE),artist=metadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
                    value.put("title",title==null?"Spotify":title).put("artist",artist==null?"":artist);
                }
                if(metadata!=null&&playback!=null){
                    long duration=metadata.getLong(MediaMetadata.METADATA_KEY_DURATION);
                    float speed=playback.getState()==PlaybackState.STATE_PLAYING?playback.getPlaybackSpeed():0;
                    long position=playback.getPosition();
                    if(duration>0&&position>=0){
                        long timestamp=playback.getLastPositionUpdateTime();
                        if(timestamp>0)position+=(long)(Math.max(0,android.os.SystemClock.elapsedRealtime()-timestamp)*speed);
                        value.put("duration",duration).put("position",Math.min(duration,Math.max(0,position))).put("speed",speed);
                    }
                }
                value.put("playing",playback!=null&&(playback.getState()==PlaybackState.STATE_PLAYING||playback.getState()==PlaybackState.STATE_BUFFERING));
            }
        } catch(org.json.JSONException ignored) { }
        return value;
    }
    static void command(Context context,String command) {
        MediaController controller=controller(context);
        if(controller==null)return;
        MediaController.TransportControls controls=controller.getTransportControls();
        switch(command) {
            case "prev":controls.skipToPrevious();break;
            case "next":controls.skipToNext();break;
            case "play":
                PlaybackState playback=controller.getPlaybackState();
                if(playback!=null&&(playback.getState()==PlaybackState.STATE_PLAYING||playback.getState()==PlaybackState.STATE_BUFFERING))controls.pause();
                else controls.play();
                break;
            default:break;
        }
    }
}
