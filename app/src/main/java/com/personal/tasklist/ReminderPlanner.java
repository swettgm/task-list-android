package com.personal.tasklist;

import org.json.*;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.*;

/** Calendar-only reminder planning; all durable task state remains in ReferenceStore. */
final class ReminderPlanner {
    static final ZoneId ZONE=ZoneId.of("America/New_York");
    static LocalTime time(String value) {
        try {
            java.util.regex.Matcher m=java.util.regex.Pattern.compile("^(\\d{1,2}):(\\d{2})(?:\\s*(AM|PM))?$",java.util.regex.Pattern.CASE_INSENSITIVE).matcher(value.trim());
            if(!m.matches())return null;int h=Integer.parseInt(m.group(1)),minute=Integer.parseInt(m.group(2));
            if(m.group(3)!=null){if(h<1||h>12)return null;h=h%12+(m.group(3).equalsIgnoreCase("PM")?12:0);}return LocalTime.of(h,minute);
        } catch(RuntimeException e){return null;}
    }
    static boolean selected(JSONArray days,LocalDate date){if(days==null)return false;int day=date.getDayOfWeek().getValue()%7;for(int i=0;i<days.length();i++)if(days.optInt(i,-1)==day)return true;return false;}
    static boolean scheduled(JSONObject task,LocalDate date){
        try{
            if(task.optBoolean("retired"))return false;
            if(task.has("recurrenceResetOn")&&date.toString().compareTo(task.optString("recurrenceResetOn"))<0)return false;
            if(task.has("startsOn")&&date.isBefore(LocalDate.parse(task.getString("startsOn"))))return false;
            JSONObject repeat=task.optJSONObject("repeat");
            if(repeat!=null){LocalDate start=LocalDate.parse(repeat.getString("start"));int amount=repeat.getInt("amount");if(amount<1||date.isBefore(start))return false;JSONArray days=repeat.optJSONArray("weekdays");String unit=repeat.getString("unit");long difference;
                switch(unit){
                    case "day":return ChronoUnit.DAYS.between(start,date)%amount==0&&(days==null||days.length()==0||selected(days,date));
                    case "week":difference=ChronoUnit.WEEKS.between(start.minusDays(start.getDayOfWeek().getValue()-1),date.minusDays(date.getDayOfWeek().getValue()-1));return difference%amount==0&&(days!=null&&days.length()>0?selected(days,date):date.getDayOfWeek()==start.getDayOfWeek());
                    case "month":difference=(date.getYear()-start.getYear())*12L+date.getMonthValue()-start.getMonthValue();return difference%amount==0&&date.getDayOfMonth()==Math.min(start.getDayOfMonth(),date.lengthOfMonth());
                    case "year":return (date.getYear()-start.getYear())%amount==0&&date.getMonth()==start.getMonth()&&date.getDayOfMonth()==Math.min(start.getDayOfMonth(),date.lengthOfMonth());
                    default:return false;
                }
            }
            if(task.has("weekendCycleStart")){LocalDate start=LocalDate.parse(task.getString("weekendCycleStart"));long diff=ChronoUnit.DAYS.between(start,date);return (diff==0&&date.getDayOfWeek()==DayOfWeek.SUNDAY)||(diff>=13&&(diff%14==0||diff%14==13));}
            if(task.has("intervalDays")){LocalDate start=LocalDate.parse(task.getString("intervalStart"));int amount=task.getInt("intervalDays");return amount>0&&!date.isBefore(start)&&ChronoUnit.DAYS.between(start,date)%amount==0;}
            JSONArray days=task.optJSONArray("days");if(days!=null&&days.length()>0)return selected(days,date);
            JSONArray dates=task.optJSONArray("dates");if(dates!=null&&dates.length()>0){for(int i=0;i<dates.length();i++)if(date.toString().equals(dates.optString(i)))return true;return false;}
            return task.has("due")&&date.equals(LocalDate.parse(task.getString("due")));
        }catch(JSONException|RuntimeException ignored){}return false;
    }
    static JSONArray checklist(JSONObject task,String date){JSONArray revisions=task.optJSONArray("checklistRevisions"),chosen=null;String latest="";if(revisions!=null)for(int i=0;i<revisions.length();i++){JSONObject rev=revisions.optJSONObject(i);if(rev!=null&&rev.optString("from").compareTo(date)<=0&&rev.optString("from").compareTo(latest)>=0){latest=rev.optString("from");chosen=rev.optJSONArray("items");}}return chosen;}
    static boolean removed(String id,String date,JSONObject state){JSONObject tasks=state.optJSONObject("taskPurged"),completions=state.optJSONObject("completionPurged");return tasks!=null&&tasks.has(id)||completions!=null&&completions.has(id+":"+date);}
    static JSONArray completionKeys(JSONObject task,String date){
        JSONArray keys=new JSONArray();String prefix=task.optString("id")+":"+date;JSONArray edited=checklist(task,date);
        if(edited!=null){for(int i=0;i<edited.length();i++)keys.put(prefix+edited.optJSONObject(i).optString("key"));}
        else if(task.optBoolean("children")){int[] indexes={0,1,2,3,5,6,7,8,9,10,13,14,15,19,16,17,18};for(int index:indexes){if(index==19&&task.has("childrenChangedOn")&&date.compareTo(task.optString("childrenChangedOn"))<0)continue;keys.put(prefix+":child:"+index);}}
        else{JSONArray steps=task.optJSONArray("steps");if(task.has("stepsChangedOn")&&date.compareTo(task.optString("stepsChangedOn"))<0&&task.optJSONArray("previousSteps")!=null)steps=task.optJSONArray("previousSteps");int count=steps==null?0:steps.length();JSONObject extras=task.optJSONObject("extraStepsByDate");if(extras!=null&&extras.optJSONArray(date)!=null)count+=extras.optJSONArray(date).length();for(int i=0;i<count;i++)keys.put(prefix+":step:"+i);}
        if(keys.length()==0)keys.put(prefix);return keys;
    }
    static boolean done(JSONObject task,String date,JSONObject state){
        if(removed(task.optString("id"),date,state))return true;JSONObject completed=state.optJSONObject("completed");if(completed==null)return false;JSONArray keys=completionKeys(task,date);for(int i=0;i<keys.length();i++)if(!completed.optBoolean(keys.optString(i)))return false;return true;
    }
    static JSONArray effective(JSONArray definitions,JSONObject state){Map<String,JSONObject> tasks=new LinkedHashMap<>();for(int i=0;i<definitions.length();i++){JSONObject task=definitions.optJSONObject(i);if(task!=null)tasks.put(task.optString("id"),task);}JSONObject overrides=state.optJSONObject("taskOverrides");if(overrides!=null){Iterator<String> ids=overrides.keys();while(ids.hasNext()){String id=ids.next();JSONObject task=overrides.optJSONObject(id);if(task!=null)tasks.put(id,task);}}JSONArray custom=state.optJSONArray("customTasks");if(custom!=null)for(int i=0;i<custom.length();i++){JSONObject task=custom.optJSONObject(i);if(task!=null)tasks.put(task.optString("id"),task);}JSONObject purged=state.optJSONObject("taskPurged");if(purged!=null){Iterator<String> ids=purged.keys();while(ids.hasNext())tasks.remove(ids.next());}return new JSONArray(tasks.values());}
    /** A previously armed alarm must survive unrelated saves while Android delays delivery. */
    static JSONArray keepDue(JSONArray previous,JSONArray upcoming,JSONArray tasks,JSONObject state,long now) throws JSONException {
        JSONArray result=upcoming;long earliest=upcoming.length()==0?Long.MAX_VALUE:upcoming.getJSONObject(0).getLong("at");
        for(int i=0;i<previous.length();i++){
            JSONObject reminder=previous.optJSONObject(i);if(reminder==null)continue;long at=reminder.optLong("at",Long.MIN_VALUE);if(at>now||at<now-86400000L||at>earliest)continue;
            for(int j=0;j<tasks.length();j++){
                JSONObject task=tasks.getJSONObject(j);if(!task.optString("id").equals(reminder.optString("id"))||task.optBoolean("retired")||!task.optBoolean("notify"))continue;
                try{
                    String date=reminder.getString("date"),key=task.getString("id")+":"+date;if(!key.equals(reminder.getString("key"))||done(task,date,state)||(task.has("recurrenceResetOn")&&date.compareTo(task.optString("recurrenceResetOn"))<0))continue;
                    LocalDate original=LocalDate.parse(date);JSONObject moved=state.optJSONObject("movedTo");boolean relocated=moved!=null&&moved.has(key);if(!scheduled(task,original))continue;
                    LocalDate display=relocated?LocalDate.parse(moved.getString(key)):original;LocalTime time=time(task.optString("time"));if(time==null||display.atTime(time).atZone(ZONE).toInstant().toEpochMilli()!=at)continue;
                    if(at<earliest){result=new JSONArray();earliest=at;}result.put(reminder);break;
                }catch(java.time.DateTimeException ignored){}
            }
        }
        return result;
    }
    static JSONArray next(JSONArray definitions,JSONObject state,long now){
        JSONArray result=new JSONArray();long earliest=Long.MAX_VALUE;LocalDate today=Instant.ofEpochMilli(now).atZone(ZONE).toLocalDate();JSONObject moved=state.optJSONObject("movedTo");
        for(int i=0;i<definitions.length();i++){
            JSONObject task=definitions.optJSONObject(i);if(task==null||task.optBoolean("retired")||!task.optBoolean("notify"))continue;LocalTime at=time(task.optString("time"));if(at==null)continue;String id=task.optString("id");Set<LocalDate> dates=new LinkedHashSet<>();
            for(int day=-1;day<=366;day++){LocalDate date=today.plusDays(day);if(scheduled(task,date))dates.add(date);}
            if(moved!=null){Iterator<String> keys=moved.keys();while(keys.hasNext()){String key=keys.next();if(key.startsWith(id+":")){try{dates.add(LocalDate.parse(key.substring(id.length()+1)));}catch(RuntimeException ignored){}}}}
            for(LocalDate date:dates){String stateDate=date.toString(),key=id+":"+stateDate;if(!scheduled(task,date))continue;if(task.has("recurrenceResetOn")&&stateDate.compareTo(task.optString("recurrenceResetOn"))<0)continue;LocalDate display=date;try{if(moved!=null&&moved.has(key))display=LocalDate.parse(moved.getString(key));if(done(task,stateDate,state))continue;long millis=display.atTime(at).atZone(ZONE).toInstant().toEpochMilli();if(millis<=now||millis>earliest)continue;if(millis<earliest){result=new JSONArray();earliest=millis;}result.put(new JSONObject().put("id",id).put("title",task.optString("title")).put("key",key).put("date",stateDate).put("at",millis));}catch(JSONException|RuntimeException ignored){}}
        }return result;
    }
}
