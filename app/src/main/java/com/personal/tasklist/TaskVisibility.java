package com.personal.tasklist;

import org.json.*;
import java.time.*;
import java.util.*;

/** A task's reveal time is relative to its actual due occurrence, never its carryover display date. */
final class TaskVisibility {
    private TaskVisibility(){}
    static long showAt(JSONObject task,String date){
        Object raw=task.opt("showBeforeMinutes");if(!(raw instanceof Number))return 0;double value=((Number)raw).doubleValue();if(!Double.isFinite(value)||value<0||value>Integer.MAX_VALUE||value!=Math.floor(value))return 0;
        LocalTime time=ReminderPlanner.time(task.optString("time"));if(time==null)return 0;
        try{return Math.subtractExact(LocalDate.parse(date).atTime(time).atZone(ReminderPlanner.ZONE).toInstant().toEpochMilli(),Math.multiplyExact((long)value,60000L));}catch(RuntimeException invalid){return 0;}
    }
    static boolean visible(JSONObject task,String date,long now){long at=showAt(task,date);return at==0||now>=at;}
    static long next(JSONArray tasks,JSONObject state,long now){
        long earliest=Long.MAX_VALUE;LocalDate today=Instant.ofEpochMilli(now).atZone(ReminderPlanner.ZONE).toLocalDate();JSONObject moves=state.optJSONObject("movedTo");
        for(int i=0;i<tasks.length();i++){JSONObject task=tasks.optJSONObject(i);if(task==null||!task.has("showBeforeMinutes")||task.optBoolean("retired"))continue;String id=task.optString("id");Set<String> dates=new LinkedHashSet<>();for(int day=0;day<=366;day++)dates.add(today.plusDays(day).toString());if(moves!=null)for(Iterator<String> keys=moves.keys();keys.hasNext();){String key=keys.next();if(key.startsWith(id+":"))dates.add(key.substring(id.length()+1));}
            for(String date:dates)try{if(!ReminderPlanner.scheduled(task,LocalDate.parse(date))||ReminderPlanner.done(task,date,state))continue;long at=showAt(task,ReminderPlanner.chosenDate(task,date,state));if(at>now)earliest=Math.min(earliest,at);}catch(RuntimeException ignored){}
        }return earliest==Long.MAX_VALUE?0:earliest;
    }
}
