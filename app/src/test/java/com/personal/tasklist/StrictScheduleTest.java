package com.personal.tasklist;
import static org.junit.Assert.*;
import android.content.Context;
import java.time.*;
import java.util.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
@RunWith(RobolectricTestRunner.class) @Config(sdk=28)
public class StrictScheduleTest {
 @Test public void selectedDaysOverrideStaleDueDatesAndHonorAlternateWeeks()throws Exception{
  JSONObject sunday=new JSONObject("{\"id\":\"groceries\",\"days\":[0],\"due\":\"2026-11-06\"}"),oil=new JSONObject("{\"repeat\":{\"amount\":2,\"unit\":\"week\",\"start\":\"2026-10-25\",\"weekdays\":[0,6]}}"),truck=new JSONObject("{\"days\":[0,1,2,3,4]}");
  Set<String> oilDays=new HashSet<>(Arrays.asList("2026-11-07","2026-11-08","2026-11-21","2026-11-22"));
  oil.put("id","oil").put("notify",true).put("time","09:00");JSONObject movedState=new JSONObject().put("movedTo",new JSONObject().put("oil:2026-11-07","2026-11-06"));
  assertEquals("2026-11-07",ReminderPlanner.chosenDate(oil,"2026-11-07",movedState));
  assertEquals("2026-11-07",ReminderPlanner.next(new JSONArray().put(oil),movedState,Instant.parse("2026-11-06T12:00:00Z").toEpochMilli()).getJSONObject(0).getString("date"));
  for(int n=0;n<28;n++){LocalDate date=LocalDate.of(2026,11,1).plusDays(n);assertEquals(date.getDayOfWeek()==DayOfWeek.SUNDAY,ReminderPlanner.scheduled(sunday,date));assertEquals(oilDays.contains(date.toString()),ReminderPlanner.scheduled(oil,date));assertEquals(date.getDayOfWeek()!=DayOfWeek.FRIDAY&&date.getDayOfWeek()!=DayOfWeek.SATURDAY,ReminderPlanner.scheduled(truck,date));}
 }
 @Test public void widgetRejectsEarlyOccurrencesAndCarriesUnfinishedOriginalDates()throws Exception{
  Context context=RuntimeEnvironment.getApplication();context.getSharedPreferences("tasks",0).edit().clear().commit();context.getSharedPreferences("reference_state",0).edit().clear().commit();context.getSharedPreferences("task_reminders",0).edit().clear().commit();
  ReferenceStore store=new ReferenceStore(context);
  JSONObject oil=new JSONObject("{\"id\":\"oil\",\"title\":\"Check oil\",\"priority\":\"LOW\",\"days\":[0,6]}");
  JSONObject groceries=new JSONObject("{\"id\":\"groceries\",\"title\":\"Groceries\",\"priority\":\"LOW\",\"days\":[0]}");
  JSONObject moved=new JSONObject("{\"id\":\"moved\",\"title\":\"Explicit move\",\"priority\":\"LOW\",\"due\":\"2026-11-05\"}");
  store.writeState(new JSONObject().put("customTasks",new JSONArray().put(oil).put(groceries).put(moved)).put("movedTo",new JSONObject().put("moved:2026-11-05","2026-11-06").put("oil:2026-11-07","2026-11-06").put("groceries:2026-11-08","2026-11-06")).put("reminders",new JSONObject().put("daily",false).put("scheduled",false)).toString());
  store.updateSnapshot(new JSONArray().put(row("oil","2026-11-07")).put(row("oil","2026-11-08")).put(row("oil","2026-11-06")).put(row("groceries","2026-11-01")).put(row("groceries","2026-11-08")).put(row("moved","2026-11-05")).toString());
  assertEquals(Arrays.asList("html:groceries:2026-11-01","html:moved:2026-11-05"),ids(context,"2026-11-06"));
  assertEquals(Arrays.asList("html:groceries:2026-11-01","html:moved:2026-11-05","html:oil:2026-11-07"),ids(context,"2026-11-07"));
  assertEquals(Arrays.asList("html:groceries:2026-11-01","html:groceries:2026-11-08","html:moved:2026-11-05","html:oil:2026-11-07","html:oil:2026-11-08"),ids(context,"2026-11-08"));
  assertEquals(5,ids(context,"2026-11-09").size());assertTrue(store.completeTask("html:groceries:2026-11-01"));assertEquals(4,ids(context,"2026-11-09").size());assertTrue(ids(context,"2026-11-09").contains("html:groceries:2026-11-08"));
 }
 private static List<String> ids(Context context,String date){List<String> result=new ArrayList<>();long now=LocalDate.parse(date).atTime(19,0).atZone(ReminderPlanner.ZONE).toInstant().toEpochMilli();for(TaskStore.Task task:TaskWidgetProvider.todayTasks(context,"","",now))result.add(task.id);Collections.sort(result);return result;}
 private static JSONObject row(String id,String date)throws Exception{return new JSONObject().put("id","html:"+id+":"+date).put("title",id).put("listName","My tasks").put("labels",new JSONArray()).put("priority","LOW").put("minutes",0).put("done",false).put("status",TaskStore.NEXT).put("dueDate",date).put("plannedDate","2026-11-06").put("stateDate",date).put("progressKey",id+":"+date).put("completionKeys",new JSONArray().put(id+":"+date)).put("createdAt",1).put("onToday",true).put("snapshotDate","2026-11-06").put("weekendOpening","2026-11-06").put("source","html");}
}
