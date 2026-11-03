package com.personal.tasklist;

import android.content.Context;
import java.time.*;
import org.json.*;

/** Weekend tasks keep their original occurrence keys; Friday evening only changes visibility. */
final class WeekendSchedule {
    static LocalTime cutoff(JSONObject state){LocalTime time=ReminderPlanner.time(state.optString("weekendStartTime","18:00"));return time==null?LocalTime.of(18,0):time;}
    static LocalDate friday(LocalDate date){int day=date.getDayOfWeek().getValue();return date.plusDays(5-day);}
    static boolean weekendTask(JSONObject task){
        if(task.optBoolean("weekend")||task.has("weekendCycleStart"))return true;
        JSONObject repeat=task.optJSONObject("repeat");JSONArray days=task.optJSONArray("days");
        if(repeat!=null){if(!"week".equals(repeat.optString("unit")))return false;days=repeat.optJSONArray("weekdays");if(days==null||days.length()==0)try{int day=LocalDate.parse(repeat.getString("start")).getDayOfWeek().getValue();return day==6||day==7;}catch(Exception invalid){return false;}}
        if(days!=null&&days.length()>0){for(int i=0;i<days.length();i++){int day=days.optInt(i,-1);if(day!=0&&day!=6)return false;}return true;}
        try{int day=LocalDate.parse(task.getString("due")).getDayOfWeek().getValue();return day==6||day==7;}catch(Exception invalid){return false;}
    }
    static String opening(JSONObject task,String occurrence){
        if(!weekendTask(task))return "";try{LocalDate day=LocalDate.parse(occurrence);if(day.getDayOfWeek()==DayOfWeek.SATURDAY)return day.minusDays(1).toString();if(day.getDayOfWeek()==DayOfWeek.SUNDAY&&!ReminderPlanner.scheduled(task,day.minusDays(1)))return day.minusDays(2).toString();}catch(Exception invalid){}return "";
    }
    static boolean visible(String opening,String occurrence,ZonedDateTime now,LocalTime cutoff){try{LocalDate day=now.toLocalDate(),start=LocalDate.parse(opening),due=LocalDate.parse(occurrence);return !day.isBefore(start)&&day.isBefore(due)&&(!day.equals(start)||!now.toLocalTime().isBefore(cutoff));}catch(Exception invalid){return false;}}
    static ZonedDateTime nextOpening(ZonedDateTime now,LocalTime cutoff){int offset=(5-now.getDayOfWeek().getValue()+7)%7;ZonedDateTime next=now.toLocalDate().plusDays(offset).atTime(cutoff).atZone(now.getZone());return next.isAfter(now)?next:next.plusWeeks(1);}
}
