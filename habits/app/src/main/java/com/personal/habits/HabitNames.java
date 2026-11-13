package com.personal.habits;
import android.content.Context;
/** Per-habit widget reveal state, separate from the habit's daily marks. */
final class HabitNames {
 private HabitNames(){}
 static boolean revealed(Context c,String id){return c.getSharedPreferences("revealed_names",0).getBoolean(id,false);}
 static synchronized int toggle(Context c,String id){if(new HabitStore(c).habits().stream().noneMatch(h->h.id.equals(id)))return -1;boolean next=!revealed(c,id);if(!c.getSharedPreferences("revealed_names",0).edit().putBoolean(id,next).commit())throw new IllegalStateException("Unable to save name visibility");return next?1:0;}
}
