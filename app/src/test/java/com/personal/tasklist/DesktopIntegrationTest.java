package com.personal.tasklist;
import static org.junit.Assert.*;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Looper;
import org.json.JSONObject;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlertDialog;

@RunWith(RobolectricTestRunner.class) @Config(sdk=28)
public class DesktopIntegrationTest {
    Context context;
    @Before public void reset(){context=RuntimeEnvironment.getApplication();context.getSharedPreferences("desktop_sync",0).edit().clear().commit();context.getSharedPreferences("reference_state",0).edit().clear().commit();}
    @Test public void lastTabIsRememberedOutsideTheSyncedTaskState()throws Exception{
        context.getSharedPreferences("task_ui",0).edit().clear().commit();
        org.robolectric.android.controller.ActivityController<MainActivity> first=Robolectric.buildActivity(MainActivity.class).setup();String before=new ReferenceStore(context).readState();first.get().taskBridgeForTests().setLastView("upcoming");assertEquals("upcoming",first.get().taskBridgeForTests().getLastView());first.pause().stop().destroy();
        org.robolectric.android.controller.ActivityController<MainActivity> second=Robolectric.buildActivity(MainActivity.class).setup();try{assertEquals("upcoming",second.get().taskBridgeForTests().getLastView());second.get().taskBridgeForTests().setLastView("invalid");assertEquals("upcoming",second.get().taskBridgeForTests().getLastView());assertEquals(before,new ReferenceStore(context).readState());}finally{second.pause().stop().destroy();context.getSharedPreferences("task_ui",0).edit().clear().commit();}
    }
    @Test public void phoneSaveMergesOverAConcurrentDesktopCompletion()throws Exception{
        ReferenceStore store=new ReferenceStore(context);store.writeState("{\"completed\":{\"phone\":false,\"pc\":false}}");
        org.robolectric.android.controller.ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class).setup();
        try{MainActivity.TaskBridge bridge=controller.get().taskBridgeForTests();JSONObject local=new JSONObject(bridge.readState());store.writeState("{\"completed\":{\"phone\":false,\"pc\":true}}");local.getJSONObject("completed").put("phone",true);bridge.writeState(local.toString());JSONObject saved=new JSONObject(store.readState());assertTrue(saved.getJSONObject("completed").getBoolean("pc"));assertTrue(saved.getJSONObject("completed").getBoolean("phone"));}
        finally{controller.pause().stop().destroy();}
    }
    @Test public void scanningAPairingLinkRequiresConfirmationAndCancelKeepsPhoneUnpaired()throws Exception{
        String token="0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";
        Intent intent=new Intent(Intent.ACTION_VIEW,Uri.parse("tasks://pair?url=http%3A%2F%2F192.168.1.4%3A8765&token="+token));
        org.robolectric.android.controller.ActivityController<MainActivity> controller=Robolectric.buildActivity(MainActivity.class,intent).setup();
        try{controller.get().taskBridgeForTests().appReady();Shadows.shadowOf(Looper.getMainLooper()).idle();AlertDialog dialog=ShadowAlertDialog.getLatestAlertDialog();assertNotNull(dialog);assertEquals("Link this PC?",Shadows.shadowOf(dialog).getTitle());assertFalse(Shadows.shadowOf(dialog).getMessage().toString().contains(token));assertFalse(new JSONObject(controller.get().taskBridgeForTests().getSyncStatus()).getBoolean("paired"));dialog.getButton(AlertDialog.BUTTON_NEGATIVE).performClick();assertFalse(new JSONObject(controller.get().taskBridgeForTests().getSyncStatus()).getBoolean("paired"));}
        finally{controller.pause().stop().destroy();}
    }
}
