package com.personal.workouts;

import android.content.Context;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=28)
public class WorkoutStoreTest {
    private WorkoutStore store;
    static JSONObject fixture()throws Exception {
        return new JSONObject("{\"version\":1,\"revision\":0,\"selected\":\"r1\",\"routines\":[{\"id\":\"r1\",\"name\":\"Workout A\",\"exercises\":[{\"id\":\"e1\",\"name\":\"Squat\",\"weight\":45,\"unit\":\"lb\",\"rest\":90,\"notes\":\"\",\"sets\":[{\"target\":8,\"weight\":45,\"reps\":null}]}]}],\"history\":[],\"active\":null,\"restEndAt\":0,\"settings\":{\"top\":\"#203747\",\"bottom\":\"#5a5c61\",\"text\":\"#f4f7fa\",\"unit\":\"lb\",\"rest\":90,\"vibrationMs\":350,\"notifications\":false,\"gradient\":true,\"spotify\":true}}");
    }
    static JSONObject session(JSONObject state)throws Exception {
        return new JSONObject().put("id","s1").put("routineId","r1").put("name","Workout A")
            .put("startedAt",1000).put("finishedAt",0).put("notes","")
            .put("exercises",new JSONArray(state.getJSONArray("routines").getJSONObject(0).getJSONArray("exercises").toString()));
    }
    @Before public void setup() { store=new WorkoutStore(RuntimeEnvironment.getApplication());store.prefs.edit().clear().commit(); }
    @Test public void freshNativeStoreHasNoInventedWorkoutData() { assertEquals("{}",store.read().toString()); }
    @Test public void snapshotsRoundTripWithRevision()throws Exception {
        String saved=store.write(fixture().toString());assertNotNull(saved);assertEquals(1,new JSONObject(saved).getLong("revision"));
        assertEquals(saved,store.read().toString());
    }
    @Test public void staleSaveCannotReplaceNewerData()throws Exception {
        JSONObject original=fixture();assertNotNull(store.write(original.toString()));String before=store.read().toString();
        original.put("selected","different");assertNull(store.write(original.toString()));assertEquals(before,store.read().toString());
    }
    @Test public void malformedSnapshotCannotEraseData()throws Exception {
        store.write(fixture().toString());String before=store.read().toString();assertNull(store.write("{invalid"));assertEquals(before,store.read().toString());
    }
    @Test public void invalidSetTargetCannotReplaceData()throws Exception {
        JSONObject data=fixture();data.getJSONArray("routines").getJSONObject(0).getJSONArray("exercises").getJSONObject(0).getJSONArray("sets").getJSONObject(0).put("target",0);
        assertNull(store.write(data.toString()));assertEquals("{}",store.read().toString());
    }
    @Test public void duplicateExerciseIdsAreRejected()throws Exception {
        JSONObject data=fixture();JSONArray exercises=data.getJSONArray("routines").getJSONObject(0).getJSONArray("exercises");
        exercises.put(new JSONObject(exercises.getJSONObject(0).toString()));assertNull(store.write(data.toString()));
    }
    @Test public void activeWorkoutPreservesActualRepsAndWeights()throws Exception {
        JSONObject data=fixture(),active=session(data);
        active.getJSONArray("exercises").getJSONObject(0).getJSONArray("sets").getJSONObject(0).put("reps",6).put("weight",40);
        data.put("active",active);assertNotNull(store.write(data.toString()));
        JSONObject set=store.read().getJSONObject("active").getJSONArray("exercises").getJSONObject(0).getJSONArray("sets").getJSONObject(0);
        assertEquals(6,set.getInt("reps"));assertEquals(40,set.getInt("weight"));
    }
    @Test public void completedHistoryIsIndependentFromFutureRoutineChanges()throws Exception {
        JSONObject data=fixture(),session=session(data);session.put("finishedAt",9000);
        session.getJSONArray("exercises").getJSONObject(0).getJSONArray("sets").getJSONObject(0).put("reps",8);
        data.getJSONArray("history").put(session);
        data.getJSONArray("routines").getJSONObject(0).getJSONArray("exercises").getJSONObject(0).put("weight",55);
        assertNotNull(store.write(data.toString()));
        assertEquals(45,store.read().getJSONArray("history").getJSONObject(0).getJSONArray("exercises").getJSONObject(0).getInt("weight"));
    }
    @Test public void corruptStoredJsonIsReportedWithoutReplacingIt() {
        store.prefs.edit().putString("state","bad-json").commit();
        try{store.read();fail();}catch(IllegalStateException expected){assertEquals("bad-json",store.prefs.getString("state",""));}
    }
    @Test public void emptyRoutinesAreValid()throws Exception {
        JSONObject state=fixture();state.getJSONArray("routines").getJSONObject(0).put("exercises",new JSONArray());
        assertNotNull(store.write(state.toString()));
    }
    @Test public void amrapTargetsAndZeroRepsSurviveSaving()throws Exception {
        JSONObject data=fixture(),active=session(data);
        JSONObject set=active.getJSONArray("exercises").getJSONObject(0).getJSONArray("sets").getJSONObject(0);
        set.put("target",5).put("amrap",true).put("reps",0);data.put("active",active);
        assertNotNull(store.write(data.toString()));
        set=store.read().getJSONObject("active").getJSONArray("exercises").getJSONObject(0).getJSONArray("sets").getJSONObject(0);
        assertTrue(set.getBoolean("amrap"));assertEquals(0,set.getInt("reps"));
    }
    @Test public void invalidAmrapDoesNotReplaceSavedData()throws Exception {
        JSONObject data=fixture();data.getJSONArray("routines").getJSONObject(0).getJSONArray("exercises").getJSONObject(0).getJSONArray("sets").getJSONObject(0).put("amrap","yes");
        assertNull(store.write(data.toString()));
    }

}
