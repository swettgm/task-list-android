package com.personal.workouts;

import android.content.Context;
import android.media.*;
import android.os.*;
import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

/** Sends short cues only to a verified headphone output. */
final class HeadphoneCue {
    private static final int RATE = 24000;
    private static final int CHUNK = RATE / 100;
    private static final AudioAttributes ATTRIBUTES = new AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build();
    private static Handler worker;

    static void play(Context context, long deadline, boolean bell) {
        Context app=context.getApplicationContext();
        enqueue(app,bell,false,()->new WorkoutStore(app).read().optLong("restEndAt")==deadline,()->true);
    }

    static void preview(Context context, boolean bell, BooleanSupplier active) {
        enqueue(context.getApplicationContext(),bell,true,active,active);
    }

    private static synchronized void enqueue(Context app,boolean bell,boolean allowSpeaker,
            BooleanSupplier validAtStart,BooleanSupplier active) {
        AudioManager audio=app.getSystemService(AudioManager.class);
        if(output(audio,allowSpeaker)==null)return;
        if(worker==null){
            HandlerThread thread=new HandlerThread("Workout cues",android.os.Process.THREAD_PRIORITY_AUDIO);
            thread.start();
            worker=new Handler(thread.getLooper());
        }
        long latestStart=SystemClock.elapsedRealtime()+250;
        worker.post(()->{
            if(SystemClock.elapsedRealtime()<=latestStart&&validAtStart.getAsBoolean())
                playOnRoute(audio,bell,allowSpeaker,active);
        });
    }

    static boolean isHeadphoneType(int type) {
        switch (type) {
            case AudioDeviceInfo.TYPE_WIRED_HEADSET:
            case AudioDeviceInfo.TYPE_WIRED_HEADPHONES:
            case AudioDeviceInfo.TYPE_USB_HEADSET:
            case AudioDeviceInfo.TYPE_BLUETOOTH_A2DP:
            case AudioDeviceInfo.TYPE_BLUETOOTH_SCO:
            case AudioDeviceInfo.TYPE_BLE_HEADSET:
            case AudioDeviceInfo.TYPE_HEARING_AID:
                return true;
            default:
                return false;
        }
    }

    static boolean isHeadphoneOutput(int type, String name) {
        if (!isHeadphoneType(type)) return false;
        if (type != AudioDeviceInfo.TYPE_BLUETOOTH_A2DP && type != AudioDeviceInfo.TYPE_BLUETOOTH_SCO) return true;
        // These profiles can also route to speakers or car stereos.
        return name != null && name.toLowerCase(Locale.ROOT)
            .matches(".*(headphone|headset|earbud|airpods?|buds|earphone|beats fit|wh-1000|wf-1000).*");
    }

    private static AudioDeviceInfo output(AudioManager audio,boolean allowSpeaker) {
        AudioDeviceInfo speaker=null;
        for (AudioDeviceInfo device : audio.getDevices(AudioManager.GET_DEVICES_OUTPUTS)) {
            if(isHeadphoneOutput(device.getType(),device.getProductName().toString()))return device;
            if(allowSpeaker&&device.getType()==AudioDeviceInfo.TYPE_BUILTIN_SPEAKER)speaker=device;
        }
        return speaker;
    }

    private static boolean onPreferredOutput(AudioTrack track, AudioDeviceInfo preferred) {
        AudioDeviceInfo route = track.getRoutedDevice();
        return route != null && route.getId() == preferred.getId() && route.getType() == preferred.getType();
    }

    private static void playOnRoute(AudioManager audio,boolean bell,boolean allowSpeaker,BooleanSupplier active) {
        AudioDeviceInfo preferred=output(audio,allowSpeaker);
        if(preferred==null||!active.getAsBoolean())return;
        Handler main = new Handler(Looper.getMainLooper());
        AtomicBoolean interrupted = new AtomicBoolean();
        AudioFocusRequest focus = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(ATTRIBUTES)
            .setOnAudioFocusChangeListener(change -> {if (change < 0) interrupted.set(true);}, main)
            .build();
        AudioTrack track = null;
        boolean focused = false;
        AudioRouting.OnRoutingChangedListener routeListener = null;
        try {
            int minBytes = AudioTrack.getMinBufferSize(RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
            if (minBytes <= 0) return;
            track = new AudioTrack.Builder().setAudioAttributes(ATTRIBUTES)
                .setAudioFormat(new AudioFormat.Builder().setSampleRate(RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).setEncoding(AudioFormat.ENCODING_PCM_16BIT).build())
                .setBufferSizeInBytes(Math.max(minBytes, CHUNK * 4))
                .setTransferMode(AudioTrack.MODE_STREAM).build();
            if (track.getState() != AudioTrack.STATE_INITIALIZED || !track.setPreferredDevice(preferred)) return;
            track.setVolume(0f);
            track.play();
            short[] silence = new short[CHUNK];
            // Prime only with silence, then require the actual route to match before unmuting.
            if (track.write(silence, 0, silence.length, AudioTrack.WRITE_NON_BLOCKING) < 0) return;
            long routeTimeout = SystemClock.elapsedRealtime() + 100;
            while (!onPreferredOutput(track, preferred) && SystemClock.elapsedRealtime() < routeTimeout) SystemClock.sleep(5);
            if (!onPreferredOutput(track, preferred)) return;
            focused = audio.requestAudioFocus(focus) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
            if (!focused) return;
            AudioTrack playing = track;
            routeListener = routing -> {
                if (!onPreferredOutput(playing, preferred)) {
                    interrupted.set(true);
                    playing.setVolume(0f);
                }
            };
            track.addOnRoutingChangedListener(routeListener, main);
            if (!active.getAsBoolean()||!onPreferredOutput(track, preferred)) return;
            track.setVolume(1f);
            streamTone(track, preferred, bell, interrupted, active);
        } catch (RuntimeException ignored) {
            // An unavailable route or focus grant suppresses audio.
        } finally {
            if (track != null) {
                track.setVolume(0f);
                if (routeListener != null) track.removeOnRoutingChangedListener(routeListener);
                track.release();
            }
            if (focused) audio.abandonAudioFocusRequest(focus);
        }
    }

    private static void streamTone(AudioTrack track, AudioDeviceInfo preferred, boolean bell, AtomicBoolean interrupted, BooleanSupplier active) {
        short[] block = new short[CHUNK];
        int chunks = bell ? 32 : 7;
        long start = SystemClock.elapsedRealtime();
        for (int index = 0; index <= chunks; index++) {
            if (!active.getAsBoolean() || interrupted.get() || !onPreferredOutput(track, preferred)) return;
            Arrays.fill(block, (short)0);
            if (index < chunks) {
                for (int sample = 0; sample < CHUNK; sample++) {
                    double time = (index * CHUNK + sample) / (double)RATE;
                    double attack = Math.min(1, time / .008);
                    double phase = 2 * Math.PI * time;
                    double wave = bell
                        ? Math.sin(phase * 740) + .32 * Math.sin(phase * 1480) + .18 * Math.sin(phase * 2110)
                        : Math.sin(phase * 1040);
                    double fade = bell ? Math.exp(-time * 14) : Math.min(1, (.07 - time) / .02);
                    block[sample] = (short)(3600 * wave * attack * fade);
                }
            }
            int written = 0;
            long writeDeadline = SystemClock.elapsedRealtime() + 30;
            while (written < block.length) {
                if (!active.getAsBoolean() || interrupted.get() || !onPreferredOutput(track, preferred)) return;
                int result = track.write(block, written, block.length - written, AudioTrack.WRITE_NON_BLOCKING);
                if (result < 0 || SystemClock.elapsedRealtime() >= writeDeadline) return;
                written += result;
                if (written < block.length) SystemClock.sleep(2);
            }
            SystemClock.sleep(Math.max(0, start + (index + 1) * 10 - SystemClock.elapsedRealtime()));
        }
        long drainDeadline = SystemClock.elapsedRealtime() + 100;
        while (track.getPlaybackHeadPosition() < (chunks + 2) * CHUNK
            && active.getAsBoolean() && !interrupted.get() && onPreferredOutput(track, preferred)
            && SystemClock.elapsedRealtime() < drainDeadline) {
            SystemClock.sleep(5);
        }
    }
}
