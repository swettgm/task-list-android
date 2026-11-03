package com.personal.tasklist;

import static org.junit.Assert.*;

import android.content.Context;
import android.content.SharedPreferences;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/** Synthetic fixtures only: none of these tasks come from the user's reference. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class ReferenceStoreTest {
    private Context context;
    private ReferenceStore reference;
    private TaskStore tasks;

    @Before public void setUp() {
        context = RuntimeEnvironment.getApplication();
        preferences().edit().clear().commit();
        context.getSharedPreferences("tasks", Context.MODE_PRIVATE).edit().clear().commit();
        reference = new ReferenceStore(context);
        tasks = new TaskStore(context);
    }

    @Test public void projectsStaySeparateAndSurviveNativeTaskWrites() throws Exception {
        JSONObject projects=new JSONObject().put("project-test",new JSONObject().put("id","project-test").put("title","Assess future idea").put("notes","Private project notes").put("date","2026-12-01").put("minutes",75).put("completed",true));
        reference.writeState(new JSONObject().put("projects",projects).put("projectDeleted",new JSONObject().put("deleted-project","2026-11-02")).put("customTasks",new JSONArray()).put("completed",new JSONObject()).toString());
        assertTrue(tasks.all().isEmpty());String id=reference.addCustomTask("Actual task",Collections.emptyList(),"UNSET",false);String day=LocalDate.now(ZoneId.of("America/New_York")).toString();assertTrue(reference.completeTask("html:"+id+":"+day));
        JSONObject reopened=new JSONObject(new ReferenceStore(context).readState());assertEquals(projects.toString(),reopened.getJSONObject("projects").toString());assertEquals("2026-11-02",reopened.getJSONObject("projectDeleted").getString("deleted-project"));assertEquals(1,tasks.all().size());assertEquals("Actual task",tasks.all().get(0).title);
    }

    @Test public void widgetProgressIsSharedIdempotentAndWinsOverStaleSnapshots() throws Exception {
        String day=LocalDate.now(ZoneId.of("America/New_York")).toString(),a="widget-a:"+day,b="widget-b:"+day;
        JSONObject projects=new JSONObject().put("keep",new JSONObject().put("id","keep").put("title","Keep project"));
        reference.writeState(new JSONObject().put("completed",new JSONObject().put("keep:old",true)).put("projects",projects).put("inProgress",JSONObject.NULL).put("progressStateInitialized",true).toString());
        JSONArray rows=new JSONArray().put(snapshot(a,day,true,a)).put(snapshot(b,day,true,b));String stale=rows.toString();reference.updateSnapshot(stale);
        assertTrue(reference.setTaskInProgress("html:"+a,true));assertEquals(a,new JSONObject(new ReferenceStore(context).readState()).getString("inProgress"));assertEquals(TaskStore.IN_PROGRESS,find("html:"+a).status);long revision=reference.revision();
        reference.setTaskInProgress("html:"+a,true);assertEquals(revision,reference.revision());assertEquals(a,new JSONObject(reference.readState()).getString("inProgress"));
        reference.setTaskInProgress("html:"+b,true);assertEquals(TaskStore.IN_PROGRESS,find("html:"+a).status);assertEquals(b,new JSONObject(reference.readState()).getString("inProgress"));reference.setTaskInProgress("html:"+a,false);assertEquals(b,new JSONObject(reference.readState()).getString("inProgress"));
        reference.updateSnapshot(stale);assertEquals(TaskStore.IN_PROGRESS,find("html:"+b).status);reference.setTaskInProgress("html:"+b,false);rows.getJSONObject(1).put("status",TaskStore.IN_PROGRESS);reference.updateSnapshot(rows.toString());assertTrue(new JSONObject(reference.readState()).isNull("inProgress"));assertEquals(TaskStore.NEXT,find("html:"+b).status);
        reference.completeTask("html:"+a);reference.setTaskInProgress("html:"+a,true);JSONObject saved=new JSONObject(reference.readState());assertTrue(saved.isNull("inProgress"));assertTrue(saved.getJSONObject("completed").getBoolean(a));assertTrue(saved.getJSONObject("completed").getBoolean("keep:old"));assertEquals(projects.toString(),saved.getJSONObject("projects").toString());
    }

    @Test public void widgetStopClearsAnActiveChildWithoutChangingItsChecklist() throws Exception {
        String day=LocalDate.now(ZoneId.of("America/New_York")).toString(),key="widget-parent:"+day;
        reference.writeState(new JSONObject().put("completed",new JSONObject().put(key+":step:0",true)).put("inProgress",key+":step:1").put("progressStateInitialized",true).toString());reference.updateSnapshot(new JSONArray().put(snapshot(key,day,true,key+":step:0",key+":step:1")).toString());assertEquals(TaskStore.IN_PROGRESS,find("html:"+key).status);
        reference.setTaskInProgress("html:"+key,false);JSONObject saved=new JSONObject(reference.readState());assertTrue(saved.isNull("inProgress"));assertTrue(saved.getJSONObject("completed").getBoolean(key+":step:0"));assertFalse(saved.getJSONObject("completed").optBoolean(key+":step:1"));assertEquals(TaskStore.NEXT,find("html:"+key).status);
    }

    @Test public void emptyStoreDoesNotInventTasksOrState() {
        assertEquals("{}", reference.readState());
        assertEquals(0, reference.revision());
        assertTrue(tasks.all().isEmpty());
        assertFalse(reference.completeTask("html:missing:2026-11-01"));
    }

    @Test public void canonicalStatePersistsUnknownNestedFieldsAcrossInstances() throws Exception {
        JSONObject state = new JSONObject()
            .put("version", 7)
            .put("completed", new JSONObject().put("alpha:2026-11-01", false))
            .put("extensionTasks", new JSONArray().put(new JSONObject().put("id", "custom-alpha").put("notes", "Keep this field")))
            .put("futureSettings", new JSONObject().put("layout", "compact").put("values", new JSONArray(Arrays.asList(1, 2, 3))));
        reference.writeState(state.toString());
        ReferenceStore reopened = new ReferenceStore(context);
        JSONObject saved = new JSONObject(reopened.readState());
        assertEquals(7, saved.getInt("version"));
        assertEquals("Keep this field", saved.getJSONArray("extensionTasks").getJSONObject(0).getString("notes"));
        assertEquals("compact", saved.getJSONObject("futureSettings").getString("layout"));
        assertEquals(3, saved.getJSONObject("futureSettings").getJSONArray("values").length());
        assertTrue(reopened.revision() > 0);
    }

    @Test public void invalidOrOversizedStateCannotReplaceSavedData() throws Exception {
        reference.writeState("{\"completed\":{},\"future\":\"keep\"}");
        String saved = reference.readState();
        long revision = reference.revision();
        for (String invalid : new String[] {"[]", "null", "\"text\"", "{broken", "{} trailing-data", "{'a':1}", "{\"a\":1,}"}) {
            assertThrows(IllegalArgumentException.class, () -> reference.writeState(invalid));
            assertEquals(saved, reference.readState());
            assertEquals(revision, reference.revision());
        }
        char[] padding = new char[2 * 1024 * 1024 + 1]; Arrays.fill(padding, 'x');
        String oversized = new JSONObject().put("padding", new String(padding)).toString();
        assertThrows(IllegalArgumentException.class, () -> reference.writeState(oversized));
        assertEquals(saved, reference.readState());
        assertEquals(revision, reference.revision());
    }

    @Test public void snapshotReplacementPreservesNativeTasksAndUnknownMirrorMetadata() throws Exception {
        tasks.add("Native task", "Personal", "2026-12-01", Collections.singletonList("At home"),
            TaskStore.IN_PROGRESS, "2026-11-01", "HIGH", 35);
        TaskStore.Task nativeTask = tasks.all().get(0);
        String day = today();
        JSONObject alpha = snapshot("alpha:" + day, day, true, "alpha:" + day)
            .put("status", TaskStore.IN_PROGRESS)
            .put("futureMetadata", new JSONObject().put("retain", true));
        reference.updateSnapshot(new JSONArray().put(alpha).toString());
        assertEquals(2, tasks.all().size());
        assertEquals(TaskStore.IN_PROGRESS, find(nativeTask.id).status);
        assertEquals(nativeTask.createdAt, find(nativeTask.id).createdAt);
        assertEquals(nativeTask.dueDate, find(nativeTask.id).dueDate);
        assertEquals(nativeTask.labels, find(nativeTask.id).labels);
        assertEquals(nativeTask.minutes, find(nativeTask.id).minutes);
        assertTrue(rawTask(alpha.getString("id")).getJSONObject("futureMetadata").getBoolean("retain"));

        JSONObject beta = snapshot("beta:" + day, day, true, "beta:" + day);
        reference.updateSnapshot(new JSONArray().put(beta).toString());
        assertEquals(2, tasks.all().size());
        assertTrue(tasks.all().stream().noneMatch(task -> task.id.equals(alpha.optString("id"))));
        assertEquals(nativeTask.status, find(nativeTask.id).status);
        assertEquals("Generic task", find(beta.getString("id")).title);

        reference.updateSnapshot("[]");
        assertEquals(1, tasks.all().size());
        assertEquals(nativeTask.id, tasks.all().get(0).id);
    }

    @Test public void malformedSnapshotCannotReplaceExistingMirrorOrStoredSnapshot() throws Exception {
        String day = today();
        JSONObject valid = snapshot("alpha:" + day, day, true, "alpha:" + day);
        reference.updateSnapshot(new JSONArray().put(valid).toString());
        String storedSnapshot = preferences().getString("snapshot", "");
        String storedTasks = taskPreferences().getString("items", "");
        String[] fields = {"id", "status", "minutes", "completionKeys", "stateDate", "onToday"};
        Object[] values = {"native-id", "Unknown", -1, "not-an-array", "not-a-date", "true"};
        for (int i = 0; i < fields.length; i++) {
            JSONObject invalid = new JSONObject(valid.toString()).put(fields[i], values[i]);
            String json = new JSONArray().put(invalid).toString();
            assertThrows("Invalid snapshot field: " + fields[i], IllegalArgumentException.class, () -> reference.updateSnapshot(json));
            assertEquals(storedSnapshot, preferences().getString("snapshot", ""));
            assertEquals(storedTasks, taskPreferences().getString("items", ""));
        }
        assertThrows(IllegalArgumentException.class, () -> reference.updateSnapshot("{}"));
        assertEquals(storedSnapshot, preferences().getString("snapshot", ""));
        assertEquals(storedTasks, taskPreferences().getString("items", ""));
    }

    @Test public void widgetCompletionUpdatesCanonicalLeafAndMirrorWithoutDroppingData() throws Exception {
        String day = today(), key = "alpha:" + day;
        reference.writeState(new JSONObject()
            .put("completed", new JSONObject().put("unrelated:2026-10-01", true))
            .put("inProgress", key)
            .put("futureSettings", new JSONObject().put("retain", "yes")).toString());
        JSONObject record = snapshot(key, day, true, key).put("priority", "HIGH").put("minutes", 25)
            .put("labels", new JSONArray().put("On the computer")).put("dueDate", "2026-12-01");
        reference.updateSnapshot(new JSONArray().put(record).toString());
        long revision = reference.revision();

        assertTrue(reference.completeTask("html:" + key));
        JSONObject saved = new JSONObject(new ReferenceStore(context).readState());
        assertTrue(saved.getJSONObject("completed").getBoolean(key));
        assertEquals(day, saved.getJSONObject("completedOn").getString(key));
        assertTrue(saved.getJSONObject("completed").getBoolean("unrelated:2026-10-01"));
        assertTrue(saved.isNull("inProgress"));
        assertEquals("yes", saved.getJSONObject("futureSettings").getString("retain"));
        Instant.parse(saved.getString("updatedAt"));
        assertTrue(reference.revision() > revision);
        TaskStore.Task mirror = find("html:" + key);
        assertTrue(mirror.done);
        assertEquals(TaskStore.DONE, mirror.status);
        assertEquals("2026-12-01", mirror.dueDate);
        assertEquals(Collections.singletonList("On the computer"), mirror.labels);
        assertEquals("HIGH", mirror.priority);
        assertEquals(25, mirror.minutes);
    }

    @Test public void completingParentChecksEveryChildAndClearsNestedActiveStep() throws Exception {
        String day = today(), parent = "parent:" + day;
        String first = parent + ":child:0", second = parent + ":child:1";
        reference.writeState(new JSONObject().put("completed", new JSONObject().put(first, false))
            .put("inProgress", second + ":step:2").toString());
        reference.updateSnapshot(new JSONArray().put(snapshot(parent, day, true, first, second)).toString());
        assertTrue(reference.completeTask("html:" + parent));
        JSONObject saved = new JSONObject(reference.readState());
        assertTrue(saved.getJSONObject("completed").getBoolean(first));
        assertTrue(saved.getJSONObject("completed").getBoolean(second));
        assertEquals(day, saved.getJSONObject("completedOn").getString(first));
        assertEquals(day, saved.getJSONObject("completedOn").getString(second));
        assertEquals(day, saved.getJSONObject("completedOn").getString(parent));
        assertTrue(saved.isNull("inProgress"));
    }

    @Test public void completionClearsMatchingOccurrenceKeyEvenWhenProgressDateDiffers() throws Exception {
        String day = today(), progress = "alpha:" + day;
        String occurrence = "alpha:2026-10-01";
        reference.writeState(new JSONObject().put("completed", new JSONObject()).put("inProgress", occurrence).toString());
        reference.updateSnapshot(new JSONArray().put(snapshot(progress, day, true, occurrence)).toString());
        assertTrue(reference.completeTask("html:" + progress));
        JSONObject saved = new JSONObject(reference.readState());
        assertTrue(saved.getJSONObject("completed").getBoolean(occurrence));
        assertEquals(day, saved.getJSONObject("completedOn").getString(occurrence));
        assertEquals(day, saved.getJSONObject("completedOn").getString(progress));
        assertTrue(saved.isNull("inProgress"));
    }

    @Test public void unrelatedActiveMarkerAndRolloverHistoryArePreserved() throws Exception {
        String day = today(), key = "alpha:" + day, active = key + "-different";
        reference.writeState(new JSONObject().put("completed", new JSONObject()).put("inProgress", active)
            .put("rolloverCompletedOn", new JSONObject().put("other:2026-10-01", "2026-10-02")).toString());
        reference.updateSnapshot(new JSONArray().put(snapshot(key, day, true, key)).toString());
        reference.completeTask("html:" + key);
        JSONObject saved = new JSONObject(reference.readState());
        assertEquals(active, saved.getString("inProgress"));
        assertEquals("2026-10-02", saved.getJSONObject("rolloverCompletedOn").getString("other:2026-10-01"));
        assertFalse("A same-day completion does not create a rollover record", saved.getJSONObject("rolloverCompletedOn").has(key));
    }

    @Test public void carriedTodayOccurrenceRecordsCompletionUsingNewYorkDate() throws Exception {
        String day = today(), oldDate = LocalDate.parse(day).minusDays(2).toString();
        String key = "carried:" + oldDate;
        reference.writeState("{\"completed\":{}}");
        reference.updateSnapshot(new JSONArray().put(snapshot(key, oldDate, true, key)).toString());
        reference.completeTask("html:" + key);
        JSONObject saved = new JSONObject(reference.readState());
        assertEquals(day, saved.getJSONObject("rolloverCompletedOn").getString(key));
    }

    @Test public void futureOrNonTodayOccurrenceDoesNotCreateRolloverCompletionMarker() throws Exception {
        String previousDate = LocalDate.parse(today()).minusDays(2).toString(), key = "other:" + previousDate;
        reference.writeState("{\"completed\":{},\"rolloverCompletedOn\":{}}");
        reference.updateSnapshot(new JSONArray().put(snapshot(key, previousDate, false, key)).toString());
        reference.completeTask("html:" + key);
        JSONObject saved = new JSONObject(reference.readState());
        assertFalse(saved.getJSONObject("rolloverCompletedOn").has(key));
    }

    @Test public void repeatedWidgetCompletionIsIdempotentAndNativeOrUnknownIdsReturnFalse() throws Exception {
        String day = today(), key = "alpha:" + day;
        tasks.add("Native task", "Personal", "");
        String nativeId = tasks.all().get(0).id;
        reference.writeState("{\"completed\":{}}");
        reference.updateSnapshot(new JSONArray().put(snapshot(key, day, true, key)).toString());
        assertTrue(reference.completeTask("html:" + key));
        long savedRevision = reference.revision();
        String savedState = reference.readState();
        assertTrue(reference.completeTask("html:" + key));
        assertEquals(savedRevision, reference.revision());
        assertEquals(savedState, reference.readState());
        assertTrue(find("html:" + key).done);
        assertFalse(reference.completeTask(nativeId));
        assertFalse(reference.completeTask("html:unknown:" + day));
        assertFalse(find(nativeId).done);
        assertEquals(savedRevision, reference.revision());
    }

    @Test public void overdueWidgetCompletionRecordsActualDateWithoutRewritingCompletedChildHistory() throws Exception {
        String day = today(), oldDate = LocalDate.parse(day).minusDays(3).toString();
        String parent = "overdue:" + oldDate, first = parent + ":step:0", second = parent + ":step:1";
        String earlierCompletion = LocalDate.parse(day).minusDays(1).toString();
        reference.writeState(new JSONObject()
            .put("completed", new JSONObject().put(first, true).put(second, false))
            .put("completedOn", new JSONObject().put(first, earlierCompletion).put("unrelated:2026-10-01", "2026-10-02"))
            .put("futureSettings", new JSONObject().put("retain", true)).toString());
        reference.updateSnapshot(new JSONArray().put(snapshot(parent, oldDate, false, first, second)).toString());

        assertTrue(reference.completeTask("html:" + parent));
        JSONObject saved = new JSONObject(reference.readState());
        JSONObject dates = saved.getJSONObject("completedOn");
        assertEquals(earlierCompletion, dates.getString(first));
        assertEquals(day, dates.getString(second));
        assertEquals(day, dates.getString(parent));
        assertEquals("2026-10-02", dates.getString("unrelated:2026-10-01"));
        assertTrue(saved.getJSONObject("futureSettings").getBoolean("retain"));
        assertFalse(saved.has("rolloverCompletedOn"));
        assertTrue(find("html:" + parent).done);
    }

    @Test public void repeatedCompletionKeepsPriorDatesForOccurrenceProgressAndRollover() throws Exception {
        String day = today(), originalDate = LocalDate.parse(day).minusDays(4).toString();
        String priorCompletion = LocalDate.parse(day).minusDays(1).toString();
        String occurrence = "prior:" + originalDate, progress = "prior:" + day;
        reference.writeState(new JSONObject()
            .put("completed", new JSONObject().put(occurrence, true))
            .put("completedOn", new JSONObject().put(occurrence, priorCompletion).put(progress, priorCompletion))
            .put("rolloverCompletedOn", new JSONObject().put(progress, priorCompletion)).toString());
        reference.updateSnapshot(new JSONArray().put(snapshot(progress, originalDate, true, occurrence)).toString());
        String before = reference.readState();
        long revision = reference.revision();

        assertTrue(reference.completeTask("html:" + progress));
        assertEquals(before, reference.readState());
        assertEquals(revision, reference.revision());
        assertEquals(priorCompletion, new JSONObject(reference.readState()).getJSONObject("completedOn").getString(progress));
    }

    @Test public void malformedCompletionDatesCannotPartiallySaveStateOrMirror() throws Exception {
        String day = today(), key = "invalid-dates:" + day;
        reference.writeState("{\"completed\":{}}");
        reference.updateSnapshot(new JSONArray().put(snapshot(key, day, true, key)).toString());
        String state = new JSONObject().put("completed", new JSONObject()).put("completedOn", new JSONArray()).toString();
        preferences().edit().putString("state", state).commit();
        String mirror = taskPreferences().getString("items", "");
        String snapshotBefore = preferences().getString("snapshot", "");
        long revision = reference.revision();

        assertThrows(IllegalStateException.class, () -> reference.completeTask("html:" + key));
        assertEquals(state, preferences().getString("state", ""));
        assertEquals(mirror, taskPreferences().getString("items", ""));
        assertEquals(snapshotBefore, preferences().getString("snapshot", ""));
        assertEquals(revision, reference.revision());
        assertFalse(find("html:" + key).done);
    }

    @Test public void malformedCompletionMapCannotPartiallySaveOrCompleteMirror() throws Exception {
        String day = today(), key = "alpha:" + day;
        reference.writeState("{\"completed\":{}}");
        reference.updateSnapshot(new JSONArray().put(snapshot(key, day, true, key)).toString());
        String savedState = new JSONObject().put("completed", new JSONArray()).put("inProgress", key).toString();
        preferences().edit().putString("state", savedState).commit();
        String savedTasks = taskPreferences().getString("items", "");
        long savedRevision = reference.revision();
        assertThrows(RuntimeException.class, () -> reference.completeTask("html:" + key));
        assertEquals(savedState, preferences().getString("state", ""));
        assertEquals(savedTasks, taskPreferences().getString("items", ""));
        assertEquals(savedRevision, reference.revision());
    }

    @Test public void damagedStoredSnapshotCannotOverwriteCanonicalState() throws Exception {
        String day = today(), key = "alpha:" + day;
        reference.writeState("{\"completed\":{},\"futureField\":\"keep\"}");
        reference.updateSnapshot(new JSONArray().put(snapshot(key, day, true, key)).toString());
        String savedState = reference.readState();
        long savedRevision = reference.revision();
        preferences().edit().putString("snapshot", "not-json").commit();
        assertThrows(RuntimeException.class, () -> reference.completeTask("html:" + key));
        assertEquals(savedState, reference.readState());
        assertEquals(savedRevision, reference.revision());
        assertFalse(find("html:" + key).done);
    }

    @Test public void staleSnapshotCannotReopenTaskCompletedFromWidget() throws Exception {
        String day = today(), key = "alpha:" + day;
        JSONObject original = snapshot(key, day, true, key);
        reference.writeState("{\"completed\":{}}");
        reference.updateSnapshot(new JSONArray().put(original).toString());
        assertTrue(reference.completeTask("html:" + key));
        long savedRevision = reference.revision();

        // A queued page snapshot may still describe the checkbox before the widget tap.
        reference.updateSnapshot(new JSONArray().put(original).toString());
        assertTrue(find("html:" + key).done);
        assertEquals(TaskStore.DONE, find("html:" + key).status);
        assertTrue(new JSONObject(reference.readState()).getJSONObject("completed").getBoolean(key));
        assertEquals(savedRevision, reference.revision());
    }

    @Test public void explicitPageSaveCanReopenCompletedTaskBeforeFreshSnapshot() throws Exception {
        String day = today(), key = "alpha:" + day;
        JSONObject original = snapshot(key, day, true, key);
        reference.writeState("{\"completed\":{}}");
        reference.updateSnapshot(new JSONArray().put(original).toString());
        reference.completeTask("html:" + key);

        JSONObject edited = new JSONObject(reference.readState());
        edited.getJSONObject("completed").put(key, false);
        reference.writeState(edited.toString());
        reference.updateSnapshot(new JSONArray().put(original).toString());
        assertFalse(find("html:" + key).done);
        assertEquals(TaskStore.NEXT, find("html:" + key).status);
        assertFalse(new JSONObject(reference.readState()).getJSONObject("completed").getBoolean(key));
    }

    @Test public void quickAddTodayNormalizesContextsAndImmediatelyMirrorsWithoutLosingHistory() throws Exception {
        String day = today(), existingKey = "existing:" + day;
        tasks.add("Native task", "Personal", "2026-12-01");
        TaskStore.Task nativeTask = tasks.all().get(0);
        reference.writeState(new JSONObject().put("completed", new JSONObject().put("history:2026-10-01", true))
            .put("futureSettings", new JSONObject().put("retain", "yes")).toString());
        JSONObject existing = snapshot(existingKey, day, true, existingKey)
            .put("futureMetadata", new JSONObject().put("retain", true));
        reference.updateSnapshot(new JSONArray().put(existing).toString());
        long revision = reference.revision();

        String id = reference.addCustomTask("  A new task  ",
            Arrays.asList(" At home ", "On the phone", "At home", " ", " Custom context "), "HIGH", false);
        assertTrue(id.startsWith("custom-"));
        java.util.UUID.fromString(id.substring("custom-".length()));
        JSONObject state = new JSONObject(new ReferenceStore(context).readState());
        JSONObject definition = state.getJSONArray("customTasks").getJSONObject(0);
        assertEquals(id, definition.getString("id"));
        assertEquals("A new task", definition.getString("title"));
        assertEquals("HIGH", definition.getString("priority"));
        assertEquals("Not set", definition.getString("when"));
        assertEquals(day, definition.getString("due"));
        assertEquals(new JSONArray(Arrays.asList("At home", "On the phone", "Custom context")).toString(), definition.getJSONArray("labels").toString());
        assertTrue(state.getJSONObject("completed").getBoolean("history:2026-10-01"));
        assertEquals("yes", state.getJSONObject("futureSettings").getString("retain"));
        assertEquals(revision + 1, reference.revision());

        String key = id + ":" + day;
        TaskStore.Task mirror = find("html:" + key);
        assertEquals("A new task", mirror.title);
        assertEquals(TaskStore.DEFAULT_LIST, mirror.listName);
        assertEquals(day, mirror.dueDate);
        assertEquals(day, mirror.plannedDate);
        assertEquals(TaskStore.NEXT, mirror.status);
        assertEquals("HIGH", mirror.priority);
        assertEquals(0, mirror.minutes);
        assertEquals(Arrays.asList("At home", "On the phone", "Custom context"), mirror.labels);
        assertFalse(mirror.done);
        JSONObject metadata = rawTask(mirror.id);
        assertEquals(key, metadata.getString("progressKey"));
        assertEquals(new JSONArray().put(key).toString(), metadata.getJSONArray("completionKeys").toString());
        assertTrue(metadata.getBoolean("onToday"));
        assertTrue(rawTask("html:" + existingKey).getJSONObject("futureMetadata").getBoolean("retain"));
        assertEquals(nativeTask.id, find(nativeTask.id).id);
        assertEquals(nativeTask.createdAt, find(nativeTask.id).createdAt);
        assertEquals(nativeTask.dueDate, find(nativeTask.id).dueDate);
        assertFalse(find(nativeTask.id).done);
    }

    @Test public void quickAddTomorrowUsesNewYorkDateAndDistinctStableIds() throws Exception {
        String tomorrow = LocalDate.parse(today()).plusDays(1).toString();
        String first = reference.addCustomTask("First task", Collections.singletonList("On the computer"), "MEDIUM", true);
        String second = reference.addCustomTask("Second task", Collections.emptyList(), "UNSET", true);
        assertNotEquals(first, second);
        JSONObject state = new JSONObject(new ReferenceStore(context).readState());
        JSONArray definitions = state.getJSONArray("customTasks");
        assertEquals(2, definitions.length());
        assertEquals(first, definitions.getJSONObject(0).getString("id"));
        assertEquals(second, definitions.getJSONObject(1).getString("id"));
        assertEquals(tomorrow, definitions.getJSONObject(0).getString("due"));
        assertEquals(tomorrow, definitions.getJSONObject(1).getString("due"));
        for (String id : Arrays.asList(first, second)) {
            TaskStore.Task mirror = find("html:" + id + ":" + tomorrow);
            assertEquals(tomorrow, mirror.plannedDate);
            assertEquals(tomorrow, mirror.dueDate);
            assertEquals(TaskStore.NEXT, mirror.status);
            assertFalse(rawTask(mirror.id).getBoolean("onToday"));
        }
    }

    @Test public void invalidQuickAddInputCannotChangeStateMirrorOrRevision() {
        reference.addCustomTask("Keep this task", Collections.singletonList("At home"), "UNSET", false);
        String state = reference.readState(), mirror = taskPreferences().getString("items", "");
        String snapshot = preferences().getString("snapshot", "");
        long revision = reference.revision();
        assertThrows(IllegalArgumentException.class, () -> reference.addCustomTask(null, Collections.emptyList(), "UNSET", false));
        assertThrows(IllegalArgumentException.class, () -> reference.addCustomTask("  ", Collections.emptyList(), "UNSET", false));
        assertThrows(IllegalArgumentException.class, () -> reference.addCustomTask("Invalid", null, "UNSET", false));
        assertThrows(IllegalArgumentException.class, () -> reference.addCustomTask("Invalid", Arrays.asList("At home", null), "UNSET", false));
        assertThrows(IllegalArgumentException.class, () -> reference.addCustomTask("Invalid", Collections.emptyList(), "URGENT", false));
        assertEquals(state, reference.readState());
        assertEquals(mirror, taskPreferences().getString("items", ""));
        assertEquals(snapshot, preferences().getString("snapshot", ""));
        assertEquals(revision, reference.revision());
    }

    @Test public void staleEmptySnapshotKeepsCanonicalQuickAddedTaskAndCompletionRoundTrips() throws Exception {
        String day = today();
        String id = reference.addCustomTask("A new task", Collections.singletonList("On the road"), "LOW", false);
        String key = id + ":" + day, mirrorId = "html:" + key;
        String definitions = new JSONObject(reference.readState()).getJSONArray("customTasks").toString();
        reference.updateSnapshot("[]");
        assertFalse(find(mirrorId).done);
        assertEquals(definitions, new JSONObject(reference.readState()).getJSONArray("customTasks").toString());

        assertTrue(reference.completeTask(mirrorId));
        assertTrue(new JSONObject(reference.readState()).getJSONObject("completed").getBoolean(key));
        reference.updateSnapshot("[]");
        assertTrue(find(mirrorId).done);
        assertEquals(TaskStore.DONE, find(mirrorId).status);
        assertEquals(definitions, new JSONObject(reference.readState()).getJSONArray("customTasks").toString());
        assertTrue(new JSONObject(new ReferenceStore(context).readState()).getJSONObject("completed").getBoolean(key));
    }

    @Test public void appRecurringAndChecklistDefinitionsDoNotBlockNativeQuickAddOrInventMirrorRows() throws Exception {
        JSONObject repeat = new JSONObject().put("id", "app-recurring").put("title", "Monthly task")
            .put("repeat", new JSONObject().put("amount", 1).put("unit", "month").put("start", today()));
        JSONObject checklist = new JSONObject().put("id", "app-checklist").put("title", "Past checklist")
            .put("due", "2026-01-01").put("steps", new JSONArray().put("Step"));
        reference.writeState(new JSONObject().put("customTasks", new JSONArray().put(repeat).put(checklist)).toString());
        reference.updateSnapshot("[]");
        assertTrue(tasks.all().isEmpty());
        String id = reference.addCustomTask("Popup task", Collections.singletonList("At home"), "UNSET", false);
        assertEquals(1, tasks.all().size());
        assertTrue(tasks.all().get(0).id.startsWith("html:" + id));
        assertEquals(3, new JSONObject(reference.readState()).getJSONArray("customTasks").length());
    }

    private static JSONObject snapshot(String progress, String stateDate, boolean onToday, String... completionKeys) throws Exception {
        return new JSONObject().put("id", "html:" + progress).put("title", "Generic task").put("listName", "Personal")
            .put("dueDate", "").put("done", false).put("createdAt", 42)
            .put("labels", new JSONArray()).put("status", TaskStore.NEXT).put("plannedDate", stateDate)
            .put("priority", "UNSET").put("minutes", 0).put("completionKeys", new JSONArray(Arrays.asList(completionKeys)))
            .put("progressKey", progress).put("stateDate", stateDate).put("onToday", onToday);
    }
    @Test public void widgetCompletionRecordsStrictRecencyWithoutRefreshingDuplicateTaps() throws Exception {
        String day=today(),a=reference.addCustomTask("Alpha",Collections.emptyList(),"HIGH",false),b=reference.addCustomTask("Zulu",Collections.emptyList(),"LOW",false);reference.completeTask("html:"+a+":"+day);long first=new JSONObject(reference.readState()).getJSONObject("completedAt").getLong(a+":"+day);reference.completeTask("html:"+b+":"+day);JSONObject times=new JSONObject(reference.readState()).getJSONObject("completedAt");assertTrue(times.getLong(b+":"+day)>first);reference.completeTask("html:"+a+":"+day);assertEquals(first,new JSONObject(reference.readState()).getJSONObject("completedAt").getLong(a+":"+day));assertEquals(day,new JSONObject(reference.readState()).getJSONObject("completedOn").getString(a+":"+day));
    }
    private static String today() { return LocalDate.now(ZoneId.of("America/New_York")).toString(); }
    private SharedPreferences preferences() { return context.getSharedPreferences("reference_state", Context.MODE_PRIVATE); }
    private SharedPreferences taskPreferences() { return context.getSharedPreferences("tasks", Context.MODE_PRIVATE); }
    private TaskStore.Task find(String id) {
        return tasks.all().stream().filter(task -> task.id.equals(id)).findFirst().orElseThrow(() -> new AssertionError("Missing task: " + id));
    }
    private JSONObject rawTask(String id) throws Exception {
        JSONArray records = new JSONArray(taskPreferences().getString("items", "[]"));
        for (int i = 0; i < records.length(); i++) if (id.equals(records.getJSONObject(i).getString("id"))) return records.getJSONObject(i);
        throw new AssertionError("Missing raw task: " + id);
    }
}
