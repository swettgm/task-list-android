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

    @Test public void speakersAndAmbiguousBluetoothOutputsAreRejected() {
        assertFalse(HeadphoneCue.isHeadphoneOutput(AudioDeviceInfo.TYPE_BUILTIN_SPEAKER,"Phone speaker"));
        assertFalse(HeadphoneCue.isHeadphoneOutput(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,"Car stereo"));
        assertFalse(HeadphoneCue.isHeadphoneOutput(AudioDeviceInfo.TYPE_BLUETOOTH_SCO,"Bluetooth speaker"));
        assertTrue(HeadphoneCue.isHeadphoneOutput(AudioDeviceInfo.TYPE_BLUETOOTH_A2DP,"AirPods Pro"));
        assertTrue(HeadphoneCue.isHeadphoneOutput(AudioDeviceInfo.TYPE_WIRED_HEADPHONES,"Wired audio"));
    }
}
