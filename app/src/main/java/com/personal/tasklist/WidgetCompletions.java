package com.personal.tasklist;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Durable two-second confirmation window, separate from canonical completion history. */
final class WidgetCompletions {
    static final long DELAY_MS=2000;
    private static final Object LOCK=new Object();
    private static final Set<String> finishing=new HashSet<>();
    private static SharedPreferences prefs(Context c){return c.getSharedPreferences("widget_completions",Context.MODE_PRIVATE);}
    static String token(Context c,String id){return prefs(c).getString(id,"");}
    private static long deadline(String value){try{return Long.parseLong(value.substring(value.indexOf('|')+1));}catch(RuntimeException invalid){return 0;}}
    static String begin(Context c,String id){synchronized(LOCK){String current=token(c,id);if(!current.isEmpty())return current;String value=UUID.randomUUID()+"|"+(System.currentTimeMillis()+DELAY_MS);if(!prefs(c).edit().putString(id,value).commit())throw new IllegalStateException("Could not save widget completion");return value;}}
    static boolean cancel(Context c,String id,String expected){synchronized(LOCK){String current=token(c,id);if(current.isEmpty()||!current.equals(expected)||deadline(current)<=System.currentTimeMillis()||finishing.contains(id))return false;if(!prefs(c).edit().remove(id).commit())throw new IllegalStateException("Could not cancel widget completion");return true;}}
    static void settleExpired(Context c){
        for(Map.Entry<String,?> entry:prefs(c).getAll().entrySet()){
            String id=entry.getKey();if(!(entry.getValue() instanceof String))continue;String expected=(String)entry.getValue();
            synchronized(LOCK){if(!expected.equals(token(c,id))||deadline(expected)>System.currentTimeMillis()||!finishing.add(id))continue;}
            boolean saved=false;
            try{if(!new ReferenceStore(c).completeTask(id))new TaskStore(c).complete(id);saved=true;}
            catch(RuntimeException unavailable){/* Retain the durable request and retry on the next refresh. */}
            finally{synchronized(LOCK){if(saved&&expected.equals(token(c,id)))prefs(c).edit().remove(id).commit();finishing.remove(id);}}
        }
    }
}
