package com.personal.workouts;

import android.content.Context;
import android.media.*;
import android.os.*;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;

/** Workout cues require headphones. The explicit Settings preview can use the speaker. */
final class HeadphoneCue {
    private static final int RATE = 24000;
    private static final int CHUNK = RATE / 100;
    private static final AudioAttributes ATTRIBUTES = new AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build();
    private static final short[] BEEP=synthesizeBeep(698,160);
    private static final short[] FINISH_BEEP=synthesizeBeep(932,460);
    private static final int DEFAULT_VOLUME=40;
    private static Handler worker;

    static void play(Context context, long deadline, boolean finishing) {
        Context app=context.getApplicationContext();
        WorkoutStore store=new WorkoutStore(app);
        org.json.JSONObject settings=store.read().optJSONObject("settings");
        int volume=settings==null?DEFAULT_VOLUME:settings.optInt("restCueVolume",DEFAULT_VOLUME);
        enqueue(app,finishing?FINISH_BEEP:BEEP,false,volume,()->store.read().optLong("restEndAt")==deadline,()->true);
    }

    static void preview(Context context,int seconds,int volume,BooleanSupplier active) {
        if(volume>0)enqueue(context.getApplicationContext(),previewSamples(seconds),true,volume,active,active);
    }

    static short[] previewSamples(int seconds) {
        short[] sequence=new short[seconds*RATE+FINISH_BEEP.length];
        for(int second=0;second<seconds;second++)
            System.arraycopy(BEEP,0,sequence,second*RATE,BEEP.length);
        System.arraycopy(FINISH_BEEP,0,sequence,seconds*RATE,FINISH_BEEP.length);
        return sequence;
    }

    private static synchronized void enqueue(Context app,short[] samples,boolean allowSpeaker,int volume,
            BooleanSupplier validAtStart,BooleanSupplier active) {
        if(volume<=0)return;
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
                playOnRoute(audio,samples,allowSpeaker,Math.min(100,volume)/100f,active);
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

    private static void playOnRoute(AudioManager audio,short[] samples,boolean allowSpeaker,float gain,BooleanSupplier active) {
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
                .setBufferSizeInBytes(Math.max(minBytes, CHUNK * 12))
                .setTransferMode(AudioTrack.MODE_STREAM).build();
            if (track.getState() != AudioTrack.STATE_INITIALIZED || !track.setPreferredDevice(preferred)) return;
            track.setVolume(0f);
            track.play();
            // Keep the track fed with silence until Android reports the selected route.
            short[] silence=new short[CHUNK];
            int primed=0;
            long routeTimeout=SystemClock.elapsedRealtime()+240;
            do {
                int written=track.write(silence,0,silence.length,AudioTrack.WRITE_NON_BLOCKING);
                if(written<0||!active.getAsBoolean())return;
                primed+=written;
                if(onPreferredOutput(track,preferred))break;
                SystemClock.sleep(8);
            }while(SystemClock.elapsedRealtime()<routeTimeout);
            if(!onPreferredOutput(track,preferred))return;
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
            track.setVolume(gain);
            streamSamples(track,preferred,samples,primed,interrupted,active);
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

    private static void streamSamples(AudioTrack track,AudioDeviceInfo preferred,short[] samples,
            int primed,AtomicBoolean interrupted,BooleanSupplier active) {
        int offset=0;
        long start=SystemClock.elapsedRealtime();
        while(offset<samples.length){
            int end=Math.min(offset+CHUNK,samples.length);
            long writeDeadline=SystemClock.elapsedRealtime()+150;
            while(offset<end){
                if(!active.getAsBoolean()||interrupted.get()||!onPreferredOutput(track,preferred))return;
                int result=track.write(samples,offset,end-offset,AudioTrack.WRITE_NON_BLOCKING);
                if(result<0||SystemClock.elapsedRealtime()>=writeDeadline)return;
                offset+=result;
                if(offset<end)SystemClock.sleep(2);
            }
            SystemClock.sleep(Math.max(0,start+1000L*offset/RATE-SystemClock.elapsedRealtime()));
        }
        long drainDeadline=SystemClock.elapsedRealtime()+180;
        while(track.getPlaybackHeadPosition()<primed+samples.length&&active.getAsBoolean()
                &&!interrupted.get()&&onPreferredOutput(track,preferred)
                &&SystemClock.elapsedRealtime()<drainDeadline){
            SystemClock.sleep(5);
        }
    }

    private static short[] synthesizeBeep(int pitch,int durationMs) {
        short[] samples=new short[RATE*durationMs/1000];
        double duration=durationMs/1000.0;
        for(int i=0;i<samples.length;i++){
            double t=(double)i/RATE;
            double phase=2*Math.PI*pitch*t;
            double edge=Math.min(1,Math.min(t/.005,(duration-t)/.024));
            double tone=Math.sin(phase)+.27*Math.sin(3*phase)+.08*Math.sin(5*phase);
            samples[i]=(short)(8600*edge*tone);
        }
        return samples;
    }
}
