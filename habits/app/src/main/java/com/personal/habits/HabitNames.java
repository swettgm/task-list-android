package com.personal.habits;
import android.content.Context;
import android.os.SystemClock;
import java.util.HashMap;
import java.util.Map;
/** Brief in-process reveals; old saved visibility flags are deliberately ignored. */
final class HabitNames {
 static final long REVEAL_MS=5000;
 private static final Map<String,Long> expires=new HashMap<>();
 private HabitNames(){}
 static synchronized boolean revealed(Context c,String id){Long until=expires.get(id);return until!=null&&SystemClock.elapsedRealtime()<until;}
 static synchronized int toggle(Context c,String id){if(new HabitStore(c).habits().stream().noneMatch(h->h.id.equals(id)))return -1;if(revealed(c,id)){expires.remove(id);return 0;}expires.put(id,SystemClock.elapsedRealtime()+REVEAL_MS);return 1;}
 static synchronized long deadline(String id){Long until=expires.get(id);return until==null?0:until;}
 static synchronized boolean expire(String id,long token){if(token!=0&&token==deadline(id)){expires.remove(id);return true;}return false;}
 static synchronized void hideAll(){expires.clear();}
}
