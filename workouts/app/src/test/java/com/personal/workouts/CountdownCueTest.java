package com.personal.workouts;

import android.media.AudioDeviceInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=28)
public class CountdownCueTest {
    @Test public void countdownPulsesLandOnWholeSecondsAndStopAtGo() {
        long end=100_000L;
        assertEquals(97_000L,RestAlarm.nextCountdownCue(90_000L,end,3));
        assertEquals(95_000L,RestAlarm.nextCountdownCue(90_000L,end,5));
        assertEquals(98_000L,RestAlarm.nextCountdownCue(97_010L,end,5));
        assertEquals(end,RestAlarm.nextCountdownCue(99_995L,end,5));
        assertEquals(end,RestAlarm.nextCountdownCue(90_000L,end,0));
    }

    @Test public void previewContainsEveryBeepAndALongerHigherFinish() {
        int rate=24000;
        for(int seconds:new int[]{3,5}){
            short[] sequence=HeadphoneCue.previewSamples(seconds);
            for(int second=0;second<seconds;second++){
                assertTrue(peak(sequence,second*rate,second*rate+rate/8)>4000);
                assertEquals(0,peak(sequence,second*rate+rate/4,(second+1)*rate));
            }
            int finish=seconds*rate;
            assertTrue(peak(sequence,finish+rate/4,sequence.length)>4000);
            assertTrue(risingCrossings(sequence,finish+rate/100,rate/10)
                >risingCrossings(sequence,rate/100,rate/10));
        }
    }

    private static int risingCrossings(short[] samples,int start,int length){
        int count=0;
        for(int i=start+1;i<start+length;i++){
            if(samples[i-1]<=0&&samples[i]>0)count++;
        }
        return count;
    }

    private static int peak(short[] samples,int start,int end){
        int peak=0;
        for(int i=start;i<end;i++)peak=Math.max(peak,Math.abs(samples[i]));
        return peak;
    }

    @Test public void speakersAndAmbiguousBluetoothOutputsAreRejected() {
        assertFalse(HeadphoneCue.isHeadphoneOutput(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,"Phone speaker"));
        assertFalse(HeadphoneCue.isHeadphoneOutput(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,"Car stereo"));
        assertFalse(HeadphoneCue.isHeadphoneOutput(AudioDeviceInfo.TYPE_BLUETOOTH_SCO,"Bluetooth speaker"));
        assertTrue(HeadphoneCue.isHeadphoneOutput(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,"AirPods Pro"));
        assertTrue(HeadphoneCue.isHeadphoneOutput(AudioDeviceInfo.TYPE_WIRED_HEADPHONES,"Wired audio"));
    }
}
