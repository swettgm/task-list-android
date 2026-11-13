package com.personal.habits;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.*;
import java.time.*;
import java.util.*;

/** Calendar keys survive rolling windows, DST, process death and widget refreshes. */
public final class HabitStore {
 private static final Object LOCK=new Object();
 private final SharedPreferences prefs;
 public HabitStore(Context c){prefs=c.getSharedPreferences("habits",Context.MODE_PRIVATE);}
 public static LocalDate today(){return LocalDate.now(ZoneId.systemDefault());}
 public static List<LocalDate> window(LocalDate today){List<LocalDate> days=new ArrayList<>();for(int i=-2;i<=4;i++)days.add(today.plusDays(i));return days;}
 public static long nextMidnight(Clock clock){return LocalDate.now(clock).plusDays(1).atStartOfDay(clock.getZone()).toInstant().toEpochMilli()+1000;}
 public static final class Habit {
  public final String id,name;
  Habit(String id,String name){this.id=id;this.name=name;}
 }
 private JSONObject read(){try{return new JSONObject(prefs.getString("data","{\"habits\":[],\"marks\":{}}"));}catch(JSONException e){throw new IllegalStateException("Unable to read saved habits",e);}}
 private void write(JSONObject data){if(!prefs.edit().putString("data",data.toString()).commit())throw new IllegalStateException("Unable to save habits");}
 public List<Habit> habits(){synchronized(LOCK){List<Habit> list=new ArrayList<>();JSONArray a=read().optJSONArray("habits");if(a!=null)for(int i=0;i<a.length();i++){JSONObject h=a.optJSONObject(i);if(h!=null)list.add(new Habit(h.optString("id"),h.optString("name")));}return list;}}
 public String save(String id,String name){name=name==null?"":name.trim();if(name.isEmpty()||name.length()>120)throw new IllegalArgumentException("Enter a habit name (up to 120 characters)");synchronized(LOCK){try{JSONObject data=read();JSONArray a=data.getJSONArray("habits");if(id==null){if(a.length()>=200)throw new IllegalArgumentException("You can track up to 200 habits");id=UUID.randomUUID().toString();a.put(new JSONObject().put("id",id).put("name",name));}else{boolean found=false;for(int i=0;i<a.length();i++)if(a.getJSONObject(i).getString("id").equals(id)){a.getJSONObject(i).put("name",name);found=true;break;}if(!found)throw new IllegalArgumentException("Habit no longer exists");}write(data);return id;}catch(JSONException e){throw new IllegalStateException(e);}}}
 public void delete(String id){synchronized(LOCK){try{JSONObject data=read();JSONArray a=data.getJSONArray("habits");for(int i=a.length()-1;i>=0;i--)if(a.getJSONObject(i).getString("id").equals(id))a.remove(i);data.getJSONObject("marks").remove(id);write(data);}catch(JSONException e){throw new IllegalStateException(e);}}}
 public int state(String id,LocalDate day){synchronized(LOCK){JSONObject marks=read().optJSONObject("marks");JSONObject days=marks==null?null:marks.optJSONObject(id);return days==null?0:days.optInt(day.toString(),0);}}
 public int cycle(String id,LocalDate day){if(day==null)throw new IllegalArgumentException("Missing date");synchronized(LOCK){if(habits().stream().noneMatch(h->h.id.equals(id)))return -1;try{JSONObject data=read(),marks=data.getJSONObject("marks"),days=marks.optJSONObject(id);if(days==null){days=new JSONObject();marks.put(id,days);}int next=(days.optInt(day.toString(),0)+1)%3;if(next==0)days.remove(day.toString());else days.put(day.toString(),next);write(data);return next;}catch(JSONException e){throw new IllegalStateException(e);}}}
}
