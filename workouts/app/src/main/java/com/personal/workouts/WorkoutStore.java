package com.personal.workouts;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.*;
import java.util.*;

final class WorkoutStore {
    static final Object LOCK=new Object();
    final SharedPreferences prefs;
    WorkoutStore(Context context) { prefs=context.getSharedPreferences("workouts",Context.MODE_PRIVATE); }
    JSONObject read() {
        synchronized(LOCK) {
            try { return new JSONObject(prefs.getString("state","{}")); }
            catch(JSONException invalid) { throw new IllegalStateException("Unreadable workout data",invalid); }
        }
    }
    String write(String raw) {
        synchronized(LOCK) {
            try {
                JSONObject next=new JSONObject(raw);
                validate(next);
                if(next.optLong("revision")!=read().optLong("revision"))return null;
                next.put("revision",next.optLong("revision")+1);
                String value=next.toString();
                if(!prefs.edit().putString("state",value).commit())return null;
                return value;
            } catch(JSONException|IllegalArgumentException invalid) { return null; }
        }
    }
    static void validate(JSONObject data) {
        try {
            if(data.toString().length()>6000000||data.getInt("version")!=1)throw new JSONException("Invalid data");
            JSONArray routines=data.getJSONArray("routines"),history=data.getJSONArray("history");
            if(routines.length()>100||history.length()>10000)throw new JSONException("Too many entries");
            Set<String> ids=new HashSet<>();
            for(int i=0;i<routines.length();i++) {
                JSONObject routine=routines.getJSONObject(i);
                if(!ids.add(text(routine,"id",160)))throw new JSONException("Duplicate routine");
                text(routine,"name",160);exercises(routine.getJSONArray("exercises"),false);
            }
            ids.clear();
            for(int i=0;i<history.length();i++) {
                JSONObject session=history.getJSONObject(i);
                if(!ids.add(text(session,"id",160)))throw new JSONException("Duplicate session");
                session(session);
            }
            if(!data.isNull("active"))session(data.getJSONObject("active"));
            if(data.getLong("restEndAt")<0)throw new JSONException("Invalid timer");
            JSONObject settings=data.getJSONObject("settings");
            for(String key:new String[]{"top","bottom","text"})if(!settings.getString(key).matches("#[a-fA-F0-9]{6}"))throw new JSONException("Invalid color");
            if(!Arrays.asList("lb","kg").contains(settings.getString("unit")))throw new JSONException("Invalid unit");
            if(settings.getInt("rest")<0||settings.getInt("rest")>1800)throw new JSONException("Invalid rest");
            if(settings.getInt("vibrationMs")<0||settings.getInt("vibrationMs")>2000)throw new JSONException("Invalid vibration");
            int countdown=settings.optInt("countdownSeconds");
            if(countdown!=0&&countdown!=3&&countdown!=5)throw new JSONException("Invalid countdown");
            if(settings.has("countdownVibrationMs"))number(settings,"countdownVibrationMs",0,500);
            if(settings.has("headphoneCues"))settings.getBoolean("headphoneCues");
            if(settings.has("restCueVolume"))number(settings,"restCueVolume",0,100);
            if(settings.has("schedule"))validateSchedule(settings.getJSONObject("schedule"));
            settings.getBoolean("notifications");settings.getBoolean("gradient");settings.getBoolean("spotify");
        } catch(JSONException invalid) { throw new IllegalArgumentException("Invalid workout data",invalid); }
    }
    private static void validateSchedule(JSONObject schedule)throws JSONException {
        switch(schedule.getString("mode")) {
            case "off": return;
            case "interval":
                number(schedule,"every",1,30);
                number(schedule,"anchor",0,200000);
                return;
            case "weekdays":
                JSONArray days=schedule.getJSONArray("days");
                if(days.length()<1||days.length()>7)throw new JSONException("Invalid training days");
                for(int i=0;i<days.length();i++){
                    int day=days.getInt(i);
                    if(day<0||day>6)throw new JSONException("Invalid training day");
                }
                return;
            default: throw new JSONException("Invalid training schedule");
        }
    }
    private static void session(JSONObject value)throws JSONException {
        text(value,"id",160);text(value,"name",160);
        if(value.getLong("startedAt")<=0||value.optLong("finishedAt")<0)throw new JSONException("Invalid session time");
        exercises(value.getJSONArray("exercises"),true);
        if(value.optString("notes").length()>20000)throw new JSONException("Notes too long");
    }
    private static void exercises(JSONArray rows,boolean logged)throws JSONException {
        if(rows.length()>100)throw new JSONException("Too many exercises");
        Set<String> ids=new HashSet<>();
        for(int i=0;i<rows.length();i++) {
            JSONObject row=rows.getJSONObject(i);
            if(!ids.add(text(row,"id",160)))throw new JSONException("Duplicate exercise");
            text(row,"name",160);
            if(!Arrays.asList("lb","kg").contains(row.getString("unit")))throw new JSONException("Invalid unit");
            number(row,"weight",0,5000);number(row,"rest",0,1800);
            if(row.optString("notes").length()>20000)throw new JSONException("Notes too long");
            JSONArray sets=row.getJSONArray("sets");
            if(sets.length()<1||sets.length()>20)throw new JSONException("Invalid set count");
            for(int j=0;j<sets.length();j++) {
                JSONObject set=sets.getJSONObject(j);
                number(set,"target",1,100);number(set,"weight",0,5000);
                if(set.has("amrap")&&!(set.get("amrap") instanceof Boolean))throw new JSONException("Invalid AMRAP");
                if(logged&&!set.isNull("reps"))number(set,"reps",0,100);
            }
        }
    }
    private static void number(JSONObject value,String key,double min,double max)throws JSONException {
        double number=value.getDouble(key);
        if(!Double.isFinite(number)||number<min||number>max)throw new JSONException("Invalid "+key);
    }
    private static String text(JSONObject value,String key,int max)throws JSONException {
        String text=value.getString(key);
        if(text.trim().isEmpty()||text.length()>max)throw new JSONException("Invalid "+key);
        return text;
    }
}
