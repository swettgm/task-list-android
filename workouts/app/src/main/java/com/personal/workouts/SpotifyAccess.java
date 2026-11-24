package com.personal.workouts;

import android.app.NotificationManager;
import android.content.*;
import android.media.MediaMetadata;
import android.media.session.*;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import org.json.JSONObject;

/** Uses Spotify's active media session. Notification contents are never read or stored. */
public final class SpotifyAccess extends NotificationListenerService {
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
                if("com.spotify.music".equals(controller.getPackageName()))return controller;
            }
        } catch(SecurityException denied) { }
        return null;
    }
    static JSONObject state(Context context) {
        JSONObject value=new JSONObject();
        try {
            MediaController controller=controller(context);
            value.put("connected",allowed(context)).put("available",controller!=null).put("title","Open Spotify").put("artist","").put("playing",false);
            if(controller!=null) {
                MediaMetadata metadata=controller.getMetadata();PlaybackState playback=controller.getPlaybackState();
                if(metadata!=null) {
                    String title=metadata.getString(MediaMetadata.METADATA_KEY_TITLE),artist=metadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
                    value.put("title",title==null?"Spotify":title).put("artist",artist==null?"":artist);
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
