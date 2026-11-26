package com.personal.meditation;
import static org.junit.Assert.*;import android.content.Context;import android.os.Looper;import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.Config;import org.robolectric.shadows.ShadowMediaPlayer;
@RunWith(RobolectricTestRunner.class)@Config(sdk=28)public class AmbientPlayerTest{
 private AmbientPlayer player;private Context context;
 @Before public void setup(){context=RuntimeEnvironment.getApplication();ShadowMediaPlayer.setMediaInfoProvider(source->new ShadowMediaPlayer.MediaInfo(24000,0));player=new AmbientPlayer(context,Runnable::run);}
 @After public void stop(){player.stop();}
 @Test public void backgroundLoopsAcrossSeveralCyclesAndPausesWithoutRestarting(){player.play("custom","content://test/background",25);Shadows.shadowOf(Looper.getMainLooper()).idleFor(java.time.Duration.ofMillis(1));android.media.MediaPlayer media=player.playerForTests();assertNotNull(media);assertTrue(media.isLooping());Shadows.shadowOf(Looper.getMainLooper()).idleFor(java.time.Duration.ofSeconds(61));assertTrue(media.isPlaying());player.pause();assertFalse(media.isPlaying());player.resume();assertSame(media,player.playerForTests());assertTrue(media.isPlaying());player.stop();assertNull(player.playerForTests());}
 @Test public void customBackgroundIsLoopedInsteadOfUsingTheTenSecondBellLimit(){player.play("custom","content://test/background",35);Shadows.shadowOf(Looper.getMainLooper()).idleFor(java.time.Duration.ofSeconds(30));assertTrue(player.playerForTests().isPlaying());assertTrue(player.playerForTests().isLooping());player.play("off","",25);assertNull(player.playerForTests());}
 @Test public void cuePlaybackCannotReplaceTheBackgroundPlayer(){player.play("custom","content://test/background",30);Shadows.shadowOf(Looper.getMainLooper()).idleFor(java.time.Duration.ofMillis(1));android.media.MediaPlayer loop=player.playerForTests();CuePlayer cue=new CuePlayer(context);try{cue.play("soft_bell","",()->{});Shadows.shadowOf(Looper.getMainLooper()).idleFor(java.time.Duration.ofSeconds(2));assertSame(loop,player.playerForTests());assertTrue(loop.isPlaying());}finally{cue.stop();}}
 @Test public void pauseWhilePreparingDoesNotStartAudioUntilResumeAndTimeoutReleasesIt(){ShadowMediaPlayer.setMediaInfoProvider(source->new ShadowMediaPlayer.MediaInfo(24000,1000));player.play("custom","content://test/background",25);player.pause();Shadows.shadowOf(Looper.getMainLooper()).idleFor(java.time.Duration.ofSeconds(2));assertFalse(player.playerForTests().isPlaying());player.resume();assertTrue(player.playerForTests().isPlaying());ShadowMediaPlayer.setMediaInfoProvider(source->new ShadowMediaPlayer.MediaInfo(24000,50000));player.play("custom","content://test/background",25);Shadows.shadowOf(Looper.getMainLooper()).idleFor(java.time.Duration.ofSeconds(10));assertNull(player.playerForTests());}
 @Test public void packagedBackgroundsUseAnExactLoopAndResumeTheSameTrack(){
  for(String choice:new String[]{"brown_noise","white_noise","rain","bass_hum"}){
   player.play(choice,"",25);
   Shadows.shadowOf(Looper.getMainLooper()).idle();
   android.media.AudioTrack track=player.trackForTests();
   assertNotNull(choice,track);
   assertEquals(24000,track.getSampleRate());
   assertEquals(android.media.AudioTrack.PLAYSTATE_PLAYING,track.getPlayState());
   player.pause();
   assertEquals(android.media.AudioTrack.PLAYSTATE_PAUSED,track.getPlayState());
   player.resume();
   assertSame(track,player.trackForTests());
   player.stop();
   assertNull(player.trackForTests());
  }
 }
}
