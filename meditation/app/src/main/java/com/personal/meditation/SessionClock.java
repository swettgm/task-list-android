package com.personal.meditation;
/** Monotonic timer. UI refreshes never move any deadline. */
final class SessionClock {
 static final long PREP=30000;enum Cue{SHORT_ONE,SHORT_TWO,LONG,START,QUARTER_ONE,QUARTER_TWO,QUARTER_THREE,END}
 private long total,anchor,accumulated;private int next;private boolean started,paused;
 synchronized void start(long now,long duration){if(duration<60000||duration>14400000)throw new IllegalArgumentException("Choose 1–240 minutes");total=duration;anchor=now;accumulated=0;next=0;started=true;paused=false;}
 synchronized long elapsed(long now){return !started?0:Math.min(PREP+total,accumulated+(paused?0:Math.max(0,now-anchor)));}
 synchronized String phase(long now){if(!started)return "idle";if(elapsed(now)>=PREP+total)return "finished";if(paused)return "paused";return elapsed(now)<PREP?"preparing":"meditating";}
 synchronized String period(long now){return elapsed(now)<PREP?"preparing":"meditating";}
 synchronized boolean active(long now){return started&&elapsed(now)<PREP+total;}
 synchronized long remaining(long now){long e=elapsed(now);return !started?0:e<PREP?PREP-e:Math.max(0,PREP+total-e);}
 synchronized long duration(){return total;}
 synchronized int quarter(long now){return (int)Math.min(4,Math.max(0,elapsed(now)-PREP)*4/Math.max(1,total)+1);}
 private long deadline(int i){if(i<3)return 27000+i*1000;if(i==3)return PREP;return PREP+total*(i-3)/4;}
 synchronized Cue poll(long now){if(!started||paused)return null;long e=elapsed(now);Cue latest=null;while(next<8&&e>=deadline(next)){latest=Cue.values()[next++];}return latest;}
 synchronized void pause(long now){if(!active(now)||paused)return;accumulated=elapsed(now);paused=true;}
 synchronized void resume(long now){if(!paused)return;anchor=now;paused=false;}
 synchronized void stop(){started=false;paused=false;next=0;accumulated=0;}
}
