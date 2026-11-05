package com.personal.tasklist;

import static org.junit.Assert.*;
import android.content.Context;
import android.os.Looper;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class SyncEngineTest {
    private Context context;
    private ReferenceStore reference;
    private static final String TOKEN = "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

    @Before public void reset() {
        context = RuntimeEnvironment.getApplication();
        context.getSharedPreferences("desktop_sync", Context.MODE_PRIVATE).edit().clear().commit();
        context.getSharedPreferences("reference_state", Context.MODE_PRIVATE).edit().clear().commit();
        context.getSharedPreferences("tasks", Context.MODE_PRIVATE).edit().clear().commit();
        reference = new ReferenceStore(context);
    }

    @Test public void onlyExplicitLocalNetworkEndpointsAreAccepted() {
        assertEquals("http://192.168.1.4:8765", SyncEngine.validateEndpoint("http://192.168.1.4:8765/"));
        assertEquals("https://10.1.2.3:8443", SyncEngine.validateEndpoint("https://10.1.2.3:8443"));
        assertEquals("http://127.0.0.1:8765", SyncEngine.validateEndpoint("http://127.0.0.1:8765"));
        for (String endpoint : new String[] {"http://example.com:8765", "https://8.8.8.8:8765", "http://192.168.1.4",
                "http://user:pass@192.168.1.4:8765", "http://192.168.1.4:8765/path", "http://192.168.1.4:8765?x=y",
                "http://192.168.1.4:8765#token", "file:///tmp/tasks", "http://192.168.1.4:99999", "http://010.1.2.3:8765"})
            assertThrows(IllegalArgumentException.class, () -> SyncEngine.validateEndpoint(endpoint));
    }

    @Test public void pairingSecretValidationAndStatusNeverExposeTheToken() throws Exception {
        SyncEngine sync = new SyncEngine(context, () -> {}, (url, token, path, body) -> new JSONObject());
        try {
            assertThrows(IllegalArgumentException.class, () -> sync.pair("http://192.168.1.2:8765", "short"));
            assertFalse(new JSONObject(sync.status()).getBoolean("paired"));
            sync.pair("http://192.168.1.2:8765", TOKEN);
            JSONObject status = new JSONObject(sync.status());
            assertTrue(status.getBoolean("paired")); assertFalse(status.getBoolean("connected"));
            assertEquals("http://192.168.1.2:8765", status.getString("url"));
            assertFalse(sync.status().contains(TOKEN));
        } finally { sync.close(); }
    }

    @Test public void syncPreservesPhoneEditsMadeWhileTheRequestIsInFlight() throws Exception {
        reference.writeState("{\"completed\":{\"first\":false},\"uiContext\":\"At home\"}");
        AtomicInteger updates = new AtomicInteger();
        SyncEngine sync = new SyncEngine(context, updates::incrementAndGet, (url, token, path, body) -> {
            assertEquals(TOKEN, token); assertEquals("/api/sync", path);
            assertEquals(0, body.getJSONObject("base").length());
            assertFalse(body.getJSONObject("state").getJSONObject("completed").getBoolean("first"));
            reference.writeState("{\"completed\":{\"first\":true,\"phone\":true},\"uiContext\":\"On the phone\"}");
            return new JSONObject("{\"revision\":2,\"state\":{\"completed\":{\"first\":false,\"pc\":true},\"uiContext\":\"On the computer\"}}");
        });
        try {
            sync.pair("http://127.0.0.1:8765", TOKEN); sync.runOneSync(); Shadows.shadowOf(Looper.getMainLooper()).idle();
            JSONObject saved = new JSONObject(reference.readState());
            assertTrue(saved.getJSONObject("completed").getBoolean("first"));
            assertTrue(saved.getJSONObject("completed").getBoolean("phone"));
            assertTrue(saved.getJSONObject("completed").getBoolean("pc"));
            assertEquals("On the phone", saved.getString("uiContext"));
            assertTrue(new JSONObject(sync.status()).getBoolean("connected"));
            assertEquals(1, updates.get());
        } finally { sync.close(); }
    }

    @Test public void offlineAndUnpairKeepEverySavedTask() throws Exception {
        String state = "{\"completed\":{\"keep\":true},\"customTasks\":[{\"id\":\"a\",\"title\":\"Keep me\"}]}";
        reference.writeState(state);
        SyncEngine sync = new SyncEngine(context, () -> {}, (url, token, path, body) -> { throw new java.io.IOException("Offline"); });
        try {
            sync.pair("http://127.0.0.1:8765", TOKEN); sync.runOneSync();
            assertTrue(StateMerge.same(new JSONObject(state), new JSONObject(reference.readState())));
            assertFalse(new JSONObject(sync.status()).getBoolean("connected"));
            assertTrue(new JSONObject(sync.status()).getBoolean("paired"));
            sync.unpair();
            assertFalse(new JSONObject(sync.status()).getBoolean("paired"));
            assertTrue(StateMerge.same(new JSONObject(state), new JSONObject(reference.readState())));
        } finally { sync.close(); }
    }

    @Test public void compareAndWriteRejectsAStaleSyncWithoutChangingUserData() throws Exception {
        reference.writeState("{\"completed\":{\"a\":false}}");
        long revision = reference.revision();
        reference.writeState("{\"completed\":{\"a\":true}}");
        assertFalse(reference.compareAndWriteState(revision, "{\"completed\":{\"a\":false}}"));
        assertTrue(new JSONObject(reference.readState()).getJSONObject("completed").getBoolean("a"));
    }

    @Test public void desktopCompletionReopenAndProgressRefreshTheWidgetBeforeThePageReloads() throws Exception {
        String id=reference.addCustomTask("PC mirror",java.util.Collections.emptyList(),"LOW",false),date=java.time.LocalDate.now(ReminderPlanner.ZONE).toString(),key=id+":"+date;AtomicInteger phase=new AtomicInteger();
        SyncEngine sync=new SyncEngine(context,()->{},(url,token,path,body)->{JSONObject state=new JSONObject(body.getJSONObject("state").toString());boolean done=phase.getAndIncrement()==0;state.put("completed",new JSONObject().put(key,done)).put("inProgressTasks",done?new org.json.JSONArray():new org.json.JSONArray().put(key)).put("inProgress",done?JSONObject.NULL:key);return new JSONObject().put("revision",phase.get()).put("state",state);});
        try{sync.pair("http://127.0.0.1:8765",TOKEN);sync.runOneSync();assertTrue(new TaskStore(context).all().get(0).done);sync.runOneSync();TaskStore.Task row=new TaskStore(context).all().get(0);assertFalse(row.done);assertEquals(TaskStore.IN_PROGRESS,row.status);}
        finally{sync.close();}
    }

    @Test public void malformedRemoteDataCannotReplaceSavedTasks() throws Exception {
        reference.writeState("{\"completed\":{\"keep\":true}}");
        String before = reference.readState();
        SyncEngine sync = new SyncEngine(context, () -> {}, (url, token, path, body) ->
                new JSONObject("{\"revision\":2,\"state\":{\"completed\":\"damaged\"}}"));
        try {
            sync.pair("http://127.0.0.1:8765", TOKEN); sync.runOneSync();
            assertEquals(before, reference.readState());
            assertFalse(new JSONObject(sync.status()).getBoolean("connected"));
        } finally { sync.close(); }
    }
}
