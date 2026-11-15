package com.personal.meditation;
import android.content.Context;import android.media.*;import android.net.Uri;import android.os.*;import android.widget.Toast;
/** A separate looping player: interval bells never replace the background audio. */
final class AmbientPlayer {
 private final Context context;private final Handler handler=new Handler(Looper.getMainLooper());private MediaPlayer player;private boolean prepared,paused;private int generation;private Runnable timeout;
 AmbientPlayer(Context context){this.context=context;}
 void stop(){generation++;if(timeout!=null)handler.removeCallbacks(timeout);timeout=null;prepared=false;paused=false;if(player!=null){player.setOnPreparedListener(null);player.setOnErrorListener(null);player.release();player=null;}}
 void play(String choice,String uri,int volume){stop();if("off".equals(choice))return;final int expected=generation;try{MediaPlayer media=new MediaPlayer();player=media;media.setAudioAttributes(new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());if("custom".equals(choice))media.setDataSource(context,Uri.parse(uri));else{int resource="brown_noise".equals(choice)?R.raw.brown_noise:"white_noise".equals(choice)?R.raw.white_noise:R.raw.rain;try(android.content.res.AssetFileDescriptor fd=context.getResources().openRawResourceFd(resource)){media.setDataSource(fd.getFileDescriptor(),fd.getStartOffset(),fd.getLength());}}media.setLooping(true);float gain=Math.max(0,Math.min(100,volume))/100f;media.setVolume(gain,gain);media.setOnPreparedListener(p->{if(expected!=generation)return;if(timeout!=null)handler.removeCallbacks(timeout);timeout=null;prepared=true;try{if(!paused)p.start();}catch(RuntimeException failed){fail(expected);}});media.setOnErrorListener((p,a,b)->{fail(expected);return true;});timeout=()->fail(expected);handler.postDelayed(timeout,10000);media.prepareAsync();}catch(Exception failed){fail(expected);}}
 private void fail(int expected){if(expected!=generation)return;stop();Toast.makeText(context,"Couldn’t play the background sound. The timer will continue.",Toast.LENGTH_LONG).show();}
 void pause(){paused=true;if(prepared&&player!=null)try{if(player.isPlaying())player.pause();}catch(RuntimeException failed){stop();}}
 void resume(){paused=false;if(prepared&&player!=null)try{player.start();}catch(RuntimeException failed){stop();}}
 MediaPlayer playerForTests(){return player;}
}
