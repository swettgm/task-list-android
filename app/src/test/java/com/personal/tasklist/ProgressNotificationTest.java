package com.personal.tasklist;

import static org.junit.Assert.*;
import android.app.*;
import android.content.*;
import android.os.*;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlertDialog;
import java.time.LocalDate;

@RunWith(RobolectricTestRunner.class) @Config(sdk={28,34})
public class ProgressNotificationTest {
    Context context;ReferenceStore store;String date;
    @Before public void reset(){context=RuntimeEnvironment.getApplication();context.getSharedPreferences("reference_state",0).edit().clear().commit();context.getSharedPreferences("tasks",0).edit().clear().commit();context.getSharedPreferences("desktop_sync",0).edit().clear().commit();TaskReminders.prefs(context).edit().clear().commit();if(Build.VERSION.SDK_INT>=33)Shadows.shadowOf((Application)context).grantPermissions(android.Manifest.permission.POST_NOTIFICATIONS);store=new ReferenceStore(context);date=LocalDate.now(ReminderPlanner.ZONE).toString();}
    JSONObject task(String id)throws Exception{return new JSONObject().put("id",id).put("title",id.equals("a")?"Alpha":"Beta").put("due",date).put("priority","LOW");}
    JSONObject active(boolean multiple)throws Exception{JSONArray keys=new JSONArray().put("a:"+date);if(multiple)keys.put("b:"+date);return new JSONObject().put("customTasks",new JSONArray().put(task("a")).put(task("b"))).put("completed",new JSONObject()).put("inProgressTasks",keys).put("inProgress",keys.getString(keys.length()-1)).put("progressStateInitialized",true).put("reminders",new JSONObject().put("scheduled",false).put("daily",false));}
    Notification notification(){return Shadows.shadowOf(context.getSystemService(NotificationManager.class)).getNotification("in-progress",0);}
    void send(PendingIntent action)throws Exception{action.send();Shadows.shadowOf(Looper.getMainLooper()).idle();}
    JSONObject saved()throws Exception{return new JSONObject(store.readState());}
    @Test public void completeWorksForUntimedNotifyOffTasksAndRemovesTheLastOngoingNotification()throws Exception{
        store.writeState(active(false).toString());Notification n=notification();assertNotNull(n);assertEquals(1,n.actions.length);assertEquals("Complete",n.actions[0].title.toString());assertTrue(n.actions[0].actionIntent.isBroadcast());assertTrue((n.flags&Notification.FLAG_ONGOING_EVENT)!=0);send(n.actions[0].actionIntent);JSONObject state=saved();assertTrue(state.getJSONObject("completed").getBoolean("a:"+date));assertEquals(date,state.getJSONObject("completedOn").getString("a:"+date));assertTrue(state.getJSONObject("completedAt").getLong("a:"+date)>0);assertEquals(0,state.getJSONArray("inProgressTasks").length());assertNull(notification());assertTrue(new TaskStore(context).all().stream().anyMatch(row->row.id.equals("html:a:"+date)&&row.done));
    }
    @Test public void duplicateAndStoppedThenRestartedActionsCannotCompleteANewProgressSession()throws Exception{
        JSONObject state=active(false);store.writeState(state.toString());PendingIntent old=notification().actions[0].actionIntent;state.put("inProgress",JSONObject.NULL).put("inProgressTasks",new JSONArray());store.writeState(state.toString());state.put("inProgress","a:"+date).put("inProgressTasks",new JSONArray().put("a:"+date));store.writeState(state.toString());send(old);assertFalse(saved().getJSONObject("completed").optBoolean("a:"+date));PendingIntent current=notification().actions[0].actionIntent;send(current);long revision=store.revision(),at=saved().getJSONObject("completedAt").getLong("a:"+date);send(current);assertEquals(revision,store.revision());assertEquals(at,saved().getJSONObject("completedAt").getLong("a:"+date));
    }
    @Test public void unrelatedRefreshKeepsTheDisplayedActionUsableButReschedulingOrDeletionInvalidatesIt()throws Exception{
        JSONObject state=active(false);store.writeState(state.toString());PendingIntent before=notification().actions[0].actionIntent;state.put("uiTheme",new JSONObject().put("accent","#80ddaa"));store.writeState(state.toString());send(before);assertTrue(saved().getJSONObject("completed").getBoolean("a:"+date));
        state=active(false);store.writeState(state.toString());PendingIntent moved=notification().actions[0].actionIntent;state.put("inProgress",JSONObject.NULL).put("inProgressTasks",new JSONArray()).put("movedTo",new JSONObject().put("a:"+date,LocalDate.parse(date).plusDays(1).toString()));store.writeState(state.toString());send(moved);assertFalse(saved().getJSONObject("completed").optBoolean("a:"+date));
        state=active(false);store.writeState(state.toString());PendingIntent deleted=notification().actions[0].actionIntent;state.getJSONArray("customTasks").getJSONObject(0).put("retired",true);store.writeState(state.toString());send(deleted);assertFalse(saved().getJSONObject("completed").optBoolean("a:"+date));
    }
    @Test public void multipleTasksOpenADirectActivityPickerAndOnlyTheChosenTaskCompletes()throws Exception{
        JSONObject state=active(true).put("manualLinks",new JSONObject().put("a",new JSONArray().put("b")).put("b",new JSONArray().put("a")));store.writeState(state.toString());Notification n=notification();assertEquals("Complete…",n.actions[0].title.toString());assertTrue(n.actions[0].actionIntent.isActivity());Intent launch=Shadows.shadowOf(n.actions[0].actionIntent).getSavedIntent();assertEquals(MainActivity.class.getName(),launch.getComponent().getClassName());assertTrue(launch.getBooleanExtra("complete_in_progress",false));
        org.robolectric.android.controller.ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class,launch).setup();try{controller.get().taskBridgeForTests().appReady();Shadows.shadowOf(Looper.getMainLooper()).idle();AlertDialog dialog=ShadowAlertDialog.getLatestAlertDialog();assertEquals("Complete task",Shadows.shadowOf(dialog).getTitle());assertEquals(2,dialog.getListView().getAdapter().getCount());dialog.getListView().performItemClick(null,1,1);assertTrue(saved().getJSONObject("completed").getBoolean("b:"+date));assertFalse(saved().getJSONObject("completed").optBoolean("a:"+date));assertEquals("a:"+date,saved().getString("inProgress"));assertNotNull(notification());assertEquals("Complete",notification().actions[0].title.toString());}finally{controller.pause().stop().destroy();}
    }
    @Test public void cancellingThePickerDoesNotChangeTaskState()throws Exception{
        store.writeState(active(true).toString());org.robolectric.android.controller.ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).setup();try{String before=store.readState();controller.get().showProgressCompletionChooser();AlertDialog dialog=ShadowAlertDialog.getLatestAlertDialog();dialog.getButton(AlertDialog.BUTTON_NEGATIVE).performClick();assertEquals(before,store.readState());}finally{controller.pause().stop().destroy();}
    }
    @Test public void checklistStepProgressCompletesItsMovedParentOccurrenceAndPreservesOtherProgressAndFutureRepeats()throws Exception{
        String origin=LocalDate.parse(date).minusDays(1).toString();JSONObject state=active(true);JSONObject parent=state.getJSONArray("customTasks").getJSONObject(0);parent.remove("due");parent.put("repeat",new JSONObject().put("start",origin).put("unit","day").put("amount",1)).put("checklistRevisions",new JSONArray().put(new JSONObject().put("from",origin).put("items",new JSONArray().put(new JSONObject().put("title","One").put("key",":step:first")).put(new JSONObject().put("title","Two").put("key",":step:second")))));state.put("movedTo",new JSONObject().put("a:"+origin,date)).put("inProgressTasks",new JSONArray().put("a:"+origin+":step:first").put("a:"+origin+":step:second").put("b:"+date));store.writeState(state.toString());JSONArray choices=TaskReminders.progressChoices(context);assertEquals(2,choices.length());JSONObject choice=choices.getJSONObject(0);assertTrue(TaskReminders.completeProgress(context,choice.getString("key"),choice.getString("token")));JSONObject complete=saved().getJSONObject("completed");assertTrue(complete.getBoolean("a:"+origin+":step:first"));assertTrue(complete.getBoolean("a:"+origin+":step:second"));assertFalse(complete.optBoolean("a:"+date+":step:first"));assertEquals(1,saved().getJSONArray("inProgressTasks").length());assertEquals("b:"+date,saved().getString("inProgress"));assertNotNull(notification());
    }
    @Test public void snapshotFallbackCompletesExistingTasksBeforeDefinitionRefresh()throws Exception{
        String key="original:"+date;JSONObject state=new JSONObject().put("completed",new JSONObject()).put("inProgress",key).put("inProgressTasks",new JSONArray().put(key)).put("reminders",new JSONObject().put("daily",false).put("scheduled",false));store.writeState(state.toString());JSONObject row=new JSONObject().put("id","html:"+key).put("title","Original task").put("listName","Personal").put("dueDate","").put("done",false).put("createdAt",42).put("labels",new JSONArray()).put("status",TaskStore.NEXT).put("plannedDate",date).put("priority","LOW").put("minutes",0).put("completionKeys",new JSONArray().put(key)).put("progressKey",key).put("stateDate",date).put("onToday",true);store.updateSnapshot(new JSONArray().put(row).toString());TaskReminders.reschedule(context);assertNotNull(notification());send(notification().actions[0].actionIntent);assertTrue(saved().getJSONObject("completed").getBoolean(key));assertNull(notification());
    }
    @Test public void inactiveTasksAndUnknownActionTokensCannotBeCompleted()throws Exception{
        JSONObject state=active(false);store.writeState(state.toString());assertFalse(TaskReminders.completeProgress(context,"a:"+date,"wrong"));assertFalse(saved().getJSONObject("completed").optBoolean("a:"+date));state.put("inProgress",JSONObject.NULL).put("inProgressTasks",new JSONArray());store.writeState(state.toString());long revision=store.revision();assertFalse(store.completeProgressReminder(task("a"),date));assertEquals(revision,store.revision());assertFalse(saved().getJSONObject("completed").optBoolean("a:"+date));
    }
}
