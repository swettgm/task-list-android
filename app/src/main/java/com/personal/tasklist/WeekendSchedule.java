package com.personal.tasklist;
import java.time.*;
import org.json.*;
/** Date shortcut and legacy snapshot compatibility; no early-weekend visibility. */
final class WeekendSchedule {
    static LocalDate friday(LocalDate date){int day=date.getDayOfWeek().getValue();return date.plusDays(5-day);}
    static String opening(JSONObject task,String occurrence){return "";}
    static boolean visible(String opening,String occurrence,ZonedDateTime now,LocalTime cutoff){return false;}
}
