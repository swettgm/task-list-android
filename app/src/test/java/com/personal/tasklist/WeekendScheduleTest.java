package com.personal.tasklist;
import static org.junit.Assert.*;
import java.time.*;
import org.json.*;
import org.junit.Test;
@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner.class) @org.robolectric.annotation.Config(sdk=28)
public class WeekendScheduleTest {
 @Test public void weekendNeverOpensBeforeItsChosenOccurrence()throws Exception{
  JSONObject task=new JSONObject("{\"due\":\"2026-11-07\",\"weekend\":true}");assertEquals("",WeekendSchedule.opening(task,"2026-11-07"));LocalTime cutoff=LocalTime.of(18,0);ZoneId zone=ZoneId.of("America/New_York");assertFalse(WeekendSchedule.visible("2026-11-06","2026-11-07",ZonedDateTime.of(2026,11,6,17,59,0,0,zone),cutoff));assertFalse(WeekendSchedule.visible("2026-11-06","2026-11-07",ZonedDateTime.of(2026,11,6,18,0,0,0,zone),cutoff));assertEquals(LocalDate.of(2026,11,13),WeekendSchedule.friday(LocalDate.of(2026,11,8)));
 }
 @Test public void weekendShortcutChoosesFridayThroughoutTheWeek(){
  for(int day=2;day<=8;day++)assertEquals(LocalDate.of(2026,11,day<6?6:13),WeekendSchedule.friday(LocalDate.of(2026,11,day)));
  assertEquals(LocalDate.of(2027,1,1),WeekendSchedule.friday(LocalDate.of(2026,12,31)));
  assertEquals(LocalDate.of(2027,1,8),WeekendSchedule.friday(LocalDate.of(2027,1,1)));
  assertEquals(LocalDate.of(2026,12,4),WeekendSchedule.friday(LocalDate.of(2026,11,30)));
 }
 @Test public void recurringWeekendShowsItsSaturdayFirstAndSpecificSundayStaysOnSunday()throws Exception{
  JSONObject repeat=new JSONObject("{\"days\":[0,6]}");assertEquals("",WeekendSchedule.opening(repeat,"2026-11-07"));assertEquals("",WeekendSchedule.opening(repeat,"2026-11-08"));assertEquals("",WeekendSchedule.opening(new JSONObject("{\"days\":[0]}"),"2026-11-08"));assertEquals("",WeekendSchedule.opening(new JSONObject("{\"days\":[0,1,2,3,4,5,6]}"),"2026-11-07"));
 }
 @Test public void weeklySundayAnchorsAreNotGenericWeekendTasks()throws Exception{
  assertEquals("",WeekendSchedule.opening(new JSONObject("{\"repeat\":{\"amount\":1,\"unit\":\"week\",\"start\":\"2026-11-08\"}}"),"2026-11-08"));
  assertEquals("",WeekendSchedule.opening(new JSONObject("{\"repeat\":{\"amount\":1,\"unit\":\"week\",\"start\":\"2026-11-08\",\"weekdays\":[0]}}"),"2026-11-08"));
 }
}
