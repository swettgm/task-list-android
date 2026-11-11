package com.personal.tasklist;

import static org.junit.Assert.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class StateMergeTest {
    private JSONObject json(String value) throws Exception { return new JSONObject(value); }

    @Test public void concurrentProgressUsesSetChangesAndKeepsLegacyPointerAligned() throws Exception {
        JSONObject base=json("{\"inProgressTasks\":[\"a\",\"b\"],\"inProgress\":\"b\"}"),local=json("{\"inProgressTasks\":[],\"inProgress\":null}"),remote=json("{\"inProgressTasks\":[\"a\",\"b\",\"other\"],\"inProgress\":\"other\"}");
        JSONObject merged=StateMerge.merge(base,local,remote);assertEquals(1,merged.getJSONArray("inProgressTasks").length());assertEquals("other",merged.getString("inProgress"));
    }

    @Test public void unchangedLocalCompletionCannotUndoANewerRemoteCompletion() throws Exception {
        JSONObject base = json("{\"completed\":{\"a\":false},\"inProgress\":null}");
        JSONObject remote = json("{\"completed\":{\"a\":true,\"b\":true},\"inProgress\":\"b\"}");
        JSONObject result = StateMerge.merge(base, json(base.toString()), remote);
        assertTrue(result.getJSONObject("completed").getBoolean("a"));
        assertTrue(result.getJSONObject("completed").getBoolean("b"));
        assertEquals("b", result.getString("inProgress"));
    }

    @Test public void explicitLocalReopenAndMetadataDeletionSurviveOtherRemoteEdits() throws Exception {
        JSONObject base = json("{\"completed\":{\"a\":true},\"completedOn\":{\"a\":\"2026-11-01\"},\"manualLinks\":{\"a\":[\"b\"]}}");
        JSONObject local = json("{\"completed\":{\"a\":false},\"completedOn\":{},\"manualLinks\":{\"a\":[]}}");
        JSONObject remote = json("{\"completed\":{\"a\":true,\"c\":true},\"completedOn\":{\"a\":\"2026-11-01\",\"c\":\"2026-11-01\"},\"manualLinks\":{\"a\":[\"b\"],\"d\":[\"e\"]}}");
        JSONObject result = StateMerge.merge(base, local, remote);
        assertFalse(result.getJSONObject("completed").getBoolean("a"));
        assertTrue(result.getJSONObject("completed").getBoolean("c"));
        assertFalse(result.getJSONObject("completedOn").has("a"));
        assertTrue(result.getJSONObject("completedOn").has("c"));
        assertEquals(0, result.getJSONObject("manualLinks").getJSONArray("a").length());
        assertEquals("e", result.getJSONObject("manualLinks").getJSONArray("d").getString(0));
    }

    @Test public void customTasksMergeByIdentityIncludingConcurrentFieldsAndRemoval() throws Exception {
        JSONObject base = json("{\"customTasks\":[{\"id\":\"a\",\"title\":\"Old\",\"notes\":\"old\"},{\"id\":\"remove\",\"title\":\"Delete me\"}]}");
        JSONObject local = json("{\"customTasks\":[{\"id\":\"a\",\"title\":\"Phone title\",\"notes\":\"old\"},{\"id\":\"phone\",\"title\":\"Phone addition\"}]}");
        JSONObject remote = json("{\"customTasks\":[{\"id\":\"a\",\"title\":\"Old\",\"notes\":\"PC note\"},{\"id\":\"remove\",\"title\":\"Delete me\"},{\"id\":\"pc\",\"title\":\"PC addition\"}]}");
        JSONArray tasks = StateMerge.merge(base, local, remote).getJSONArray("customTasks");
        assertEquals(3, tasks.length());
        assertEquals("a", tasks.getJSONObject(0).getString("id"));
        assertEquals("Phone title", tasks.getJSONObject(0).getString("title"));
        assertEquals("PC note", tasks.getJSONObject(0).getString("notes"));
        assertEquals("pc", tasks.getJSONObject(1).getString("id"));
        assertEquals("phone", tasks.getJSONObject(2).getString("id"));
    }

    @Test public void filtersAndClockTimestampsRemainDeviceLocal() throws Exception {
        JSONObject base = json("{\"uiContext\":\"At home\",\"updatedAt\":\"old\",\"uiTheme\":{\"accent\":\"#123456\"}}");
        JSONObject local = json("{\"uiContext\":\"On the phone\",\"updatedAt\":\"phone time\",\"uiTheme\":{\"accent\":\"#123456\"}}");
        JSONObject remote = json("{\"uiContext\":\"On the computer\",\"updatedAt\":\"pc time\",\"uiTheme\":{\"accent\":\"#abcdef\"}}");
        JSONObject result = StateMerge.merge(base, local, remote);
        assertEquals("On the phone", result.getString("uiContext"));
        assertEquals("phone time", result.getString("updatedAt"));
        assertEquals("#abcdef", result.getJSONObject("uiTheme").getString("accent"));
    }

    @Test public void initialPhoneSnapshotPreservesUnknownRemoteFieldsAndDoesNotMutateInputs() throws Exception {
        JSONObject local = json("{\"completed\":{\"a\":true},\"uiTheme\":{\"accent\":\"#123456\"}}");
        JSONObject remote = json("{\"completed\":{\"b\":true},\"futureExtension\":{\"keep\":true}}");
        String beforeLocal = local.toString(), beforeRemote = remote.toString();
        JSONObject result = StateMerge.merge(new JSONObject(), local, remote);
        assertTrue(result.getJSONObject("completed").getBoolean("a"));
        assertTrue(result.getJSONObject("completed").getBoolean("b"));
        assertTrue(result.getJSONObject("futureExtension").getBoolean("keep"));
        assertEquals(beforeLocal, local.toString()); assertEquals(beforeRemote, remote.toString());
    }

    @Test public void manualLinkSetDeltasPreserveConcurrentPeerAdditionsAndApplyExplicitRemovals() throws Exception {
        JSONObject base = json("{\"manualLinks\":{\"a\":[\"b\",\"old\"]}}");
        JSONObject local = json("{\"manualLinks\":{\"a\":[\"b\",\"phone\"]}}");
        JSONObject remote = json("{\"manualLinks\":{\"a\":[\"b\",\"old\",\"pc\"],\"elsewhere\":[\"keep\"]}}");
        JSONObject links = StateMerge.merge(base, local, remote).getJSONObject("manualLinks");
        assertEquals("[\"b\",\"pc\",\"phone\"]", links.getJSONArray("a").toString());
        assertEquals("keep", links.getJSONArray("elsewhere").getString(0));
    }
}
