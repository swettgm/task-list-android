package com.personal.workouts;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.*;
import android.media.MediaMetadata;
import android.media.session.*;
import org.json.JSONObject;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class) @Config(sdk=28)
public class SpotifyAccessTest {
    private Context context;private MediaSession session;private MediaController spotify;
    @Before public void setup() {
        context=RuntimeEnvironment.getApplication();session=new MediaSession(context,"test");
        spotify=session.getController();shadowOf(spotify).setPackageName("com.spotify.music");
        shadowOf(context.getSystemService(MediaSessionManager.class)).addController(spotify);
        shadowOf(context.getSystemService(NotificationManager.class)).setNotificationListenerAccessGranted(new ComponentName(context,SpotifyAccess.class),true);
    }
    @After public void cleanup(){session.release();}
    @Test public void connectedPlayerReadsOnlySpotifyMetadata()throws Exception {
        shadowOf(spotify).setMetadata(new MediaMetadata.Builder().putString(MediaMetadata.METADATA_KEY_TITLE,"Track")
            .putString(MediaMetadata.METADATA_KEY_ARTIST,"Artist").build());
        shadowOf(spotify).setPlaybackState(new PlaybackState.Builder().setState(PlaybackState.STATE_PLAYING,0,1).build());
        JSONObject value=SpotifyAccess.state(context);
        assertTrue(value.getBoolean("connected"));assertTrue(value.getBoolean("available"));
        assertEquals("Track",value.getString("title"));assertEquals("Artist",value.getString("artist"));
        assertTrue(value.getBoolean("playing"));
    }
    @Test public void availableAlbumArtworkIsReturnedAsALocalImage()throws Exception {
        android.graphics.Bitmap bitmap=android.graphics.Bitmap.createBitmap(200,200,android.graphics.Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(android.graphics.Color.BLUE);
        shadowOf(spotify).setMetadata(new MediaMetadata.Builder().putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART,bitmap).build());
        String art=SpotifyAccess.state(context).getString("art");
        assertTrue(art.startsWith("data:image/png;base64,"));
        assertTrue(art.length()>30);
        shadowOf(spotify).setMetadata(new MediaMetadata.Builder().putString(MediaMetadata.METADATA_KEY_TITLE,"No cover").build());
        assertEquals("",SpotifyAccess.state(context).getString("art"));
    }
    @Test public void controlsPauseResumeAndSkipTheSpotifySession() {
        shadowOf(spotify).setPlaybackState(new PlaybackState.Builder().setState(PlaybackState.STATE_PLAYING,0,1).build());
        SpotifyAccess.command(context,"play");assertEquals(PlaybackState.ACTION_PAUSE,shadowOf(spotify.getTransportControls()).getLastPerformedAction());
        shadowOf(spotify).setPlaybackState(new PlaybackState.Builder().setState(PlaybackState.STATE_PAUSED,0,0).build());
        SpotifyAccess.command(context,"play");assertEquals(PlaybackState.ACTION_PLAY,shadowOf(spotify.getTransportControls()).getLastPerformedAction());
        SpotifyAccess.command(context,"next");assertEquals(PlaybackState.ACTION_SKIP_TO_NEXT,shadowOf(spotify.getTransportControls()).getLastPerformedAction());
        SpotifyAccess.command(context,"prev");assertEquals(PlaybackState.ACTION_SKIP_TO_PREVIOUS,shadowOf(spotify.getTransportControls()).getLastPerformedAction());
        SpotifyAccess.command(context,"pause");assertEquals(PlaybackState.ACTION_PAUSE,shadowOf(spotify.getTransportControls()).getLastPerformedAction());
        SpotifyAccess.command(context,"resume");assertEquals(PlaybackState.ACTION_PLAY,shadowOf(spotify.getTransportControls()).getLastPerformedAction());
    }
    @Test public void openingPlaybackUsesTheActivityProvidedBySpotify() {
        Intent playback=new Intent("test.spotify.playback").setPackage(SpotifyAccess.PACKAGE_NAME);
        PendingIntent action=PendingIntent.getActivity(context,1,playback,PendingIntent.FLAG_IMMUTABLE);
        shadowOf(action).setCreatorPackage(SpotifyAccess.PACKAGE_NAME);
        shadowOf(spotify).setSessionActivity(action);
        assertTrue(SpotifyAccess.openSession(context));
        Intent started=shadowOf(RuntimeEnvironment.getApplication()).getNextStartedActivity();
        assertNotNull(started);
        assertTrue(started.filterEquals(playback));
    }
    @Test public void missingCanceledAndUnrelatedPlaybackActivitiesFallBack() {
        assertFalse(SpotifyAccess.openSession(context));
        PendingIntent action=PendingIntent.getActivity(context,2,new Intent("test.spotify.playback"),PendingIntent.FLAG_IMMUTABLE);
        shadowOf(spotify).setSessionActivity(action);
        assertFalse(SpotifyAccess.openSession(context));
        shadowOf(action).setCreatorPackage(SpotifyAccess.PACKAGE_NAME);
        action.cancel();
        assertFalse(SpotifyAccess.openSession(context));
    }
    @Test public void otherPlayersAreNotUsed()throws Exception {
        shadowOf(spotify).setPackageName("another.player");
        assertFalse(SpotifyAccess.state(context).getBoolean("available"));
        SpotifyAccess.command(context,"next");assertEquals(0,shadowOf(spotify.getTransportControls()).getLastPerformedAction());
    }
    @Test public void revokingAccessDisablesThePlayer()throws Exception {
        shadowOf(context.getSystemService(NotificationManager.class)).setNotificationListenerAccessGranted(new ComponentName(context,SpotifyAccess.class),false);
        assertFalse(SpotifyAccess.state(context).getBoolean("connected"));
        assertFalse(SpotifyAccess.state(context).getBoolean("available"));
        SpotifyAccess.command(context,"play");assertEquals(0,shadowOf(spotify.getTransportControls()).getLastPerformedAction());
    }
}
