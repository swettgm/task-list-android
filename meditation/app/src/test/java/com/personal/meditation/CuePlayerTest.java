package com.personal.meditation;
import static org.junit.Assert.*;
import android.content.Context;import android.os.Looper;import java.util.concurrent.atomic.AtomicInteger;
import org.junit.*;import org.junit.runner.RunWith;import org.robolectric.*;import org.robolectric.annotation.Config;import org.robolectric.shadows.ShadowMediaPlayer;
@RunWith(RobolectricTestRunner.class)@Config(sdk=28)public class CuePlayerTest {
 private CuePlayer player;private Context context;
 @Before public void setup(){context=RuntimeEnvironment.getApplication();ShadowMediaPlayer.setMediaInfoProvider(source->new ShadowMediaPlayer.MediaInfo(20000,0));player=new CuePlayer(context);}
 @After public void stop(){player.stop();}
 @Test public void builtInAudioContinuesBeyondTenSecondsAndCompletesOnce(){AtomicInteger ended=new AtomicInteger();player.play("soft_bell","",ended::incrementAndGet);Shadows.shadowOf(Looper.getMainLooper()).idleFor(java.time.Duration.ofSeconds(11));assertEquals(0,ended.get());Shadows.shadowOf(Looper.getMainLooper()).idleFor(java.time.Duration.ofSeconds(9));assertEquals(1,ended.get());Shadows.shadowOf(Looper.getMainLooper()).idleFor(java.time.Duration.ofSeconds(5));assertEquals(1,ended.get());}
 @Test public void suppliedZenTonePlaysItsCompleteTail(){AtomicInteger ended=new AtomicInteger();player.play("zen_tone","",ended::incrementAndGet);Shadows.shadowOf(Looper.getMainLooper()).idleFor(java.time.Duration.ofSeconds(19));assertEquals(0,ended.get());Shadows.shadowOf(Looper.getMainLooper()).idleFor(java.time.Duration.ofSeconds(1));assertEquals(1,ended.get());}
 @Test public void stoppingOrReplacingALongCueCancelsItsCompletion(){AtomicInteger old=new AtomicInteger(),next=new AtomicInteger();player.play("soft_bell","",old::incrementAndGet);Shadows.shadowOf(Looper.getMainLooper()).idleFor(java.time.Duration.ofSeconds(3));player.play("zen_tone","",next::incrementAndGet);Shadows.shadowOf(Looper.getMainLooper()).idleFor(java.time.Duration.ofSeconds(18));assertEquals(0,old.get());assertEquals(0,next.get());player.stop();Shadows.shadowOf(Looper.getMainLooper()).idleFor(java.time.Duration.ofSeconds(5));assertEquals(0,next.get());}
 @Test public void customAudioKeepsItsExistingTenSecondLimit(){AtomicInteger ended=new AtomicInteger();player.play("custom","content://test/bell",ended::incrementAndGet);Shadows.shadowOf(Looper.getMainLooper()).idleFor(java.time.Duration.ofSeconds(10));assertEquals(1,ended.get());}
 @Test public void silentChoiceCompletesImmediately(){AtomicInteger ended=new AtomicInteger();player.play("silent","",ended::incrementAndGet);assertEquals(1,ended.get());}
}
