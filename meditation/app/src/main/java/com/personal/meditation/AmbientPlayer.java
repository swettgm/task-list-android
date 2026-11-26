package com.personal.meditation;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/** Built-in backgrounds loop at exact PCM frame boundaries. Imported audio uses MediaPlayer. */
final class AmbientPlayer {
    private static final int SAMPLE_RATE=24000, FRAMES=SAMPLE_RATE*24;
    private static final Executor AUDIO_IO=Executors.newSingleThreadExecutor();
    private static final AudioAttributes ATTRIBUTES=new AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build();

    private final Context context;
    private final Executor reader;
    private final Handler handler=new Handler(Looper.getMainLooper());
    private MediaPlayer player;
    private AudioTrack track;
    private boolean prepared,paused;
    private int generation;
    private Runnable timeout;

    AmbientPlayer(Context context){this(context,AUDIO_IO);}
    AmbientPlayer(Context context,Executor reader){this.context=context;this.reader=reader;}

    void stop(){
        generation++;
        if(timeout!=null)handler.removeCallbacks(timeout);
        timeout=null;prepared=false;paused=false;
        if(track!=null){track.release();track=null;}
        if(player!=null){
            player.setOnPreparedListener(null);
            player.setOnErrorListener(null);
            player.release();
            player=null;
        }
    }

    void play(String choice,String uri,int volume){
        stop();
        if("off".equals(choice))return;
        int expected=generation;
        float gain=Math.max(0,Math.min(100,volume))/100f;
        if("custom".equals(choice)){playCustom(uri,gain,expected);return;}

        int resource;
        switch(choice){
            case "brown_noise":resource=R.raw.brown_noise;break;
            case "white_noise":resource=R.raw.white_noise;break;
            case "bass_hum":resource=R.raw.bass_hum;break;
            default:resource=R.raw.rain;break;
        }
        int channels=resource==R.raw.rain?2:1;
        reader.execute(()->{
            try{
                byte[] pcm=new byte[FRAMES*channels*2];
                // The bundled assets are generated as canonical 16-bit PCM WAV.
                try(DataInputStream audio=new DataInputStream(context.getResources().openRawResource(resource))){
                    byte[] header=new byte[44];
                    audio.readFully(header);
                    audio.readFully(pcm);
                    if(audio.read()!=-1)throw new IOException("Unexpected audio length");
                }
                handler.post(()->playLoop(pcm,channels,gain,expected));
            }catch(IOException|RuntimeException invalid){handler.post(()->fail(expected));}
        });
    }

    private void playLoop(byte[] pcm,int channels,float gain,int expected){
        if(expected!=generation)return;
        try{
            AudioFormat format=new AudioFormat.Builder().setSampleRate(SAMPLE_RATE)
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setChannelMask(channels==2?AudioFormat.CHANNEL_OUT_STEREO:AudioFormat.CHANNEL_OUT_MONO)
                .build();
            track=new AudioTrack.Builder().setAudioAttributes(ATTRIBUTES).setAudioFormat(format)
                .setTransferMode(AudioTrack.MODE_STATIC).setBufferSizeInBytes(pcm.length).build();
            if(track.write(pcm,0,pcm.length)!=pcm.length||track.setLoopPoints(0,FRAMES,-1)!=AudioTrack.SUCCESS)
                throw new IllegalStateException("Audio loop could not be loaded");
            track.setVolume(gain);
            if(!paused)track.play();
        }catch(RuntimeException failed){fail(expected);}
    }

    private void playCustom(String uri,float gain,int expected){
        try{
            MediaPlayer media=new MediaPlayer();
            player=media;
            media.setAudioAttributes(ATTRIBUTES);
            media.setDataSource(context,Uri.parse(uri));
            media.setLooping(true);
            media.setVolume(gain,gain);
            media.setOnPreparedListener(p->{
                if(expected!=generation)return;
                if(timeout!=null)handler.removeCallbacks(timeout);
                timeout=null;prepared=true;
                try{if(!paused)p.start();}catch(RuntimeException failed){fail(expected);}
            });
            media.setOnErrorListener((p,a,b)->{fail(expected);return true;});
            timeout=()->fail(expected);
            handler.postDelayed(timeout,10000);
            media.prepareAsync();
        }catch(Exception failed){fail(expected);}
    }

    private void fail(int expected){
        if(expected!=generation)return;
        stop();
        Toast.makeText(context,"Couldn’t play the background sound. The timer will continue.",Toast.LENGTH_LONG).show();
    }

    void pause(){
        paused=true;
        try{
            if(track!=null&&track.getPlayState()==AudioTrack.PLAYSTATE_PLAYING)track.pause();
            if(prepared&&player!=null&&player.isPlaying())player.pause();
        }catch(RuntimeException failed){stop();}
    }

    void resume(){
        paused=false;
        try{
            if(track!=null)track.play();
            if(prepared&&player!=null)player.start();
        }catch(RuntimeException failed){stop();}
    }

    MediaPlayer playerForTests(){return player;}
    AudioTrack trackForTests(){return track;}
}
