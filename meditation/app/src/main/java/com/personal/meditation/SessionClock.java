package com.personal.meditation;
import org.json.*;
/** Monotonic timer. UI refreshes never move any deadline. */
final class SessionClock {
 static final long PREP=30000;enum Cue{SHORT_ONE,SHORT_TWO,LONG,START,INTERVAL,END}
 private long total,anchor,accumulated,prep=PREP;private int next,intervals=4;private boolean started,paused;
 synchronized void start(long now,long duration){start(now,duration,4);}
 synchronized void start(long now,long duration,int divisions){start(now,duration,divisions,PREP);}
 synchronized void start(long now,long duration,int divisions,long countdown){if(countdown<0||countdown>300000)throw new IllegalArgumentException("Choose 0–300 countdown seconds");if(divisions<1||divisions>6)throw new IllegalArgumentException("Choose 0–5 intermediate bells");if(duration<60000||duration>14400000)throw new IllegalArgumentException("Choose 1–240 minutes");prep=countdown;intervals=divisions;total=duration;anchor=now;accumulated=0;next=0;started=true;paused=false;}
 synchronized long elapsed(long now){return !started?0:Math.min(prep+total,accumulated+(paused?0:Math.max(0,now-anchor)));}
 synchronized String phase(long now){if(!started)return "idle";if(elapsed(now)>=prep+total)return "finished";if(paused)return "paused";return elapsed(now)<prep?"preparing":"meditating";}
 synchronized String period(long now){return elapsed(now)<prep?"preparing":"meditating";}
 synchronized boolean active(long now){return started&&elapsed(now)<prep+total;}
 synchronized long remaining(long now){long e=elapsed(now);return !started?0:e<prep?prep-e:Math.max(0,prep+total-e);}
 synchronized long duration(){return total;}
 synchronized int interval(long now){return (int)Math.min(intervals,Math.max(0,elapsed(now)-prep)*intervals/Math.max(1,total)+1);}
 private long deadline(int i){if(i<3)return prep-3000+i*1000;if(i==3)return prep;return prep+total*(i-3)/intervals;}
 synchronized Cue poll(long now){if(!started||paused)return null;long e=elapsed(now);Cue latest=null;while(next<4+intervals&&e>=deadline(next)){int index=next++;latest=index<4?Cue.values()[index]:index==3+intervals?Cue.END:Cue.INTERVAL;}return latest;}
 synchronized long nextDelay(long now){if(!started||paused||next>=4+intervals)return 1000;return Math.max(1,Math.min(1000,deadline(next)-elapsed(now)));}
 synchronized JSONObject state(long now){try{return new JSONObject().put("phase",phase(now)).put("period",period(now)).put("remainingMs",remaining(now)).put("durationMs",duration()).put("interval",interval(now)).put("intervals",intervals).put("countdownMs",prep);}catch(JSONException invalid){throw new IllegalStateException(invalid);}}
 synchronized void pause(long now){if(!active(now)||paused)return;accumulated=elapsed(now);paused=true;}
 synchronized void resume(long now){if(!paused)return;anchor=now;paused=false;}
 synchronized void stop(){started=false;paused=false;next=0;accumulated=0;}
}
