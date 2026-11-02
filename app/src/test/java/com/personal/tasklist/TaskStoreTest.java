package com.personal.tasklist;

import static org.junit.Assert.*;

import android.content.Context;
import android.content.SharedPreferences;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class TaskStoreTest {
    private Context context;
    private TaskStore store;

    @Before public void setUp() {
        context = RuntimeEnvironment.getApplication();
        context.getSharedPreferences("tasks", Context.MODE_PRIVATE).edit().clear().commit();
        store = new TaskStore(context);
    }

    @Test public void freshInstallHasNoInventedTasks() {
        assertTrue(store.all().isEmpty());
        assertEquals(java.util.Collections.singletonList(TaskStore.DEFAULT_LIST), store.lists());
    }

    @Test public void editAndReopenPersistAcrossStoreInstances() {
        store.add("  Buy groceries  ", "  Personal  ", "2026-11-03");
        TaskStore.Task initial = store.all().get(0);
        assertEquals("Buy groceries", initial.title);
        assertEquals("Personal", initial.listName);
        assertFalse(initial.done);

        store.complete(initial.id);
        store.update(initial.id, "Review project notes", "Work checklist", "2026-11-04");
        TaskStore.Task edited = new TaskStore(context).all().get(0);
        assertEquals(initial.id, edited.id);
        assertEquals(initial.createdAt, edited.createdAt);
        assertEquals("Review project notes", edited.title);
        assertEquals("Work checklist", edited.listName);
        assertEquals("2026-11-04", edited.dueDate);
        assertTrue("Editing a completed task must preserve its completion", edited.done);

        store.toggle(initial.id);
        assertFalse(new TaskStore(context).all().get(0).done);
    }

    @Test public void repeatedWidgetCompletionDoesNotReopenTask() {
        store.add("Plan the week", "Work", "");
        String id = store.all().get(0).id;
        store.complete(id);
        store.complete(id);
        assertTrue(store.all().get(0).done);
        store.delete(id);
        store.complete(id);
        assertTrue("A stale widget tap must not recreate a deleted task", store.all().isEmpty());
    }

    @Test public void listNamesAreIndependentAndBlankNamesUseDefault() {
        store.add("One", "", "");
        store.add("Two", "Work", "");
        store.add("Three", "Work", "");
        store.add("Four", "Personal", "");
        assertEquals(new HashSet<>(java.util.Arrays.asList("My tasks", "Work", "Personal")), new HashSet<>(store.lists()));
        assertEquals(4, store.all().size());
        HashSet<String> ids = new HashSet<>();
        for (TaskStore.Task task : store.all()) ids.add(task.id);
        assertEquals("Every task needs a separate widget action identity", 4, ids.size());
    }

    @Test public void invalidInputNeverChangesExistingTasks() {
        store.add("Keep this task", "Personal", "2026-11-01");
        String id = store.all().get(0).id;
        assertThrows(IllegalArgumentException.class, () -> store.add("  ", "Work", ""));
        assertThrows(DateTimeParseException.class, () -> store.add("Invalid date", "Work", "2026-02-30"));
        assertThrows(DateTimeParseException.class, () -> store.update(id, "Changed", "Work", "tomorrow"));
        TaskStore.Task preserved = new TaskStore(context).all().get(0);
        assertEquals(1, store.all().size());
        assertEquals("Keep this task", preserved.title);
        assertEquals("Personal", preserved.listName);
        assertEquals("2026-11-01", preserved.dueDate);
    }

    @Test public void damagedStorageIsNotSilentlyOverwritten() {
        context.getSharedPreferences("tasks", Context.MODE_PRIVATE).edit().putString("items", "not-json").commit();
        assertThrows(IllegalStateException.class, () -> store.add("A new task", "Personal", ""));
        assertEquals("not-json", context.getSharedPreferences("tasks", Context.MODE_PRIVATE).getString("items", ""));
    }

    @Test public void legacyRecordsGainWorkflowFieldsWithoutLosingIdentityOrUnknownData() throws Exception {
        String legacy = "[{\"id\":\"legacy-open\",\"title\":\"Existing task\",\"listName\":\"Personal\",\"dueDate\":\"2026-11-03\",\"done\":false,\"createdAt\":42,\"futureField\":{\"value\":\"keep\"}},{\"id\":\"legacy-done\",\"title\":\"Finished task\",\"done\":true,\"createdAt\":43}]";
        preferences().edit().putString("items", legacy).commit();
        TaskStore.Task open = find("legacy-open");
        assertEquals(TaskStore.NEXT, open.status);
        assertEquals("UNSET", open.priority);
        assertEquals("", open.plannedDate);
        assertEquals(0, open.minutes);
        assertTrue(open.labels.isEmpty());
        assertEquals(TaskStore.DONE, find("legacy-done").status);
        assertEquals("Reading legacy data must not rewrite storage", legacy, preferences().getString("items", ""));

        store.doTomorrow(open.id);
        TaskStore.Task migrated = new TaskStore(context).all().stream().filter(t -> t.id.equals(open.id)).findFirst().get();
        assertEquals(42, migrated.createdAt);
        assertEquals("2026-11-03", migrated.dueDate);
        assertEquals("Personal", migrated.listName);
        JSONArray records = new JSONArray(preferences().getString("items", "[]"));
        assertEquals("keep", records.getJSONObject(0).getJSONObject("futureField").getString("value"));
        assertTrue(find("legacy-done").done);
    }

    @Test public void contextsAreNormalizedImmutableAndRetainedByCompatibilityEdits() {
        store.add("A task", "Personal", "2026-12-01", Arrays.asList(" At home ", "On the phone", "At home", "", " Custom context "),
            TaskStore.WAITING, "", "HIGH", 25);
        TaskStore.Task initial = store.all().get(0);
        assertEquals(Arrays.asList("At home", "On the phone", "Custom context"), initial.labels);
        assertThrows(UnsupportedOperationException.class, () -> initial.labels.add("Other"));
        assertEquals(Arrays.asList("At home", "On the road", "On the phone", "On the computer", "Guild", "Custom context"), store.labels());
        store.update(initial.id, "Edited task", "Work", "2026-12-02");
        TaskStore.Task edited = find(initial.id);
        assertEquals(initial.id, edited.id);
        assertEquals(initial.createdAt, edited.createdAt);
        assertEquals(initial.labels, edited.labels);
        assertEquals(TaskStore.WAITING, edited.status);
        assertEquals("HIGH", edited.priority);
        assertEquals(25, edited.minutes);
    }

    @Test public void quickActionsScheduleWorkWithoutMovingItsDeadlineOrLosingMetadata() {
        LocalDate today = LocalDate.now();
        store.add("An open task", "Work", today.minusDays(1).toString(), Collections.singletonList("On the computer"),
            TaskStore.NEXT, "", "MEDIUM", 15);
        TaskStore.Task original = store.all().get(0);
        store.doNow(original.id);
        TaskStore.Task active = find(original.id);
        assertEquals(TaskStore.IN_PROGRESS, active.status);
        assertEquals(today.toString(), active.plannedDate);
        assertTrue(TaskStore.isForToday(active, today));
        store.doTomorrow(original.id);
        TaskStore.Task scheduled = new TaskStore(context).all().get(0);
        assertEquals(TaskStore.NEXT, scheduled.status);
        assertEquals(today.plusDays(1).toString(), scheduled.plannedDate);
        assertEquals(original.dueDate, scheduled.dueDate);
        assertEquals(original.id, scheduled.id);
        assertEquals(original.createdAt, scheduled.createdAt);
        assertEquals(original.labels, scheduled.labels);
        assertEquals(original.priority, scheduled.priority);
        assertEquals(original.minutes, scheduled.minutes);
        assertFalse("An explicit tomorrow plan wins over an overdue deadline", TaskStore.isForToday(scheduled, today));
        assertTrue(TaskStore.isForToday(scheduled, today.plusDays(1)));
    }

    @Test public void startingAnotherTaskDemotesOnlyThePreviousActiveTask() {
        LocalDate today = LocalDate.now();
        store.add("First task", "Work", today.plusDays(5).toString(), Collections.singletonList("At home"), TaskStore.NEXT, "", "LOW", 10);
        String first = store.all().get(0).id;
        store.doNow(first);
        store.add("Second task", "Personal", "");
        String second = store.all().stream().filter(t -> t.title.equals("Second task")).findFirst().get().id;
        store.setStatus(second, TaskStore.IN_PROGRESS);
        TaskStore.Task previous = find(first);
        assertEquals(TaskStore.NEXT, previous.status);
        assertEquals(today.toString(), previous.plannedDate);
        assertEquals(today.plusDays(5).toString(), previous.dueDate);
        assertEquals(Collections.singletonList("At home"), previous.labels);
        assertEquals("LOW", previous.priority);
        assertEquals(10, previous.minutes);
        assertEquals(TaskStore.IN_PROGRESS, find(second).status);
        assertEquals(1, store.all().stream().filter(t -> TaskStore.IN_PROGRESS.equals(t.status)).count());
        store.add("Third task", "Work", "", Collections.emptyList(), TaskStore.IN_PROGRESS, "", "HIGH", 0);
        assertEquals(TaskStore.NEXT, find(second).status);
        assertEquals(1, store.all().stream().filter(t -> TaskStore.IN_PROGRESS.equals(t.status)).count());
    }

    @Test public void statusesAndCompletionRemainConsistentAndKeepTaskDetails() {
        store.add("A task", "Work", "2026-12-01", Collections.singletonList("On the road"), TaskStore.NEXT, "", "HIGHEST", 30);
        String id = store.all().get(0).id;
        store.doTomorrow(id);
        store.setStatus(id, TaskStore.WAITING);
        assertEquals("", find(id).plannedDate);
        assertFalse(TaskStore.isForToday(find(id), LocalDate.of(2027,1,1)));
        store.setStatus(id, TaskStore.SOMEDAY);
        assertFalse(TaskStore.isForToday(find(id), LocalDate.of(2027,1,1)));
        store.setStatus(id, TaskStore.NEXT);
        assertEquals("", find(id).plannedDate);
        store.setStatus(id, TaskStore.DONE);
        store.complete(id);
        assertTrue(find(id).done);
        assertEquals(TaskStore.DONE, find(id).status);
        store.toggle(id);
        TaskStore.Task reopened = find(id);
        assertFalse(reopened.done);
        assertEquals(TaskStore.NEXT, reopened.status);
        assertEquals("2026-12-01", reopened.dueDate);
        assertEquals(Collections.singletonList("On the road"), reopened.labels);
        assertEquals("HIGHEST", reopened.priority);
        assertEquals(30, reopened.minutes);
    }

    @Test public void fullEditorUpdateKeepsIdentityAndEnforcesOneActiveTask() {
        store.add("First", "Personal", "");
        String first = store.all().get(0).id;
        store.doNow(first);
        store.add("Second", "Personal", "");
        TaskStore.Task second = store.all().stream().filter(t -> t.title.equals("Second")).findFirst().get();
        store.update(second.id, "Updated", "Work", "2026-12-31", Collections.singletonList("On the computer"),
            TaskStore.IN_PROGRESS, LocalDate.now().toString(), "HIGH", 40);
        TaskStore.Task edited = find(second.id);
        assertEquals(second.createdAt, edited.createdAt);
        assertEquals(TaskStore.NEXT, find(first).status);
        assertEquals(TaskStore.IN_PROGRESS, edited.status);
        assertEquals("HIGH", edited.priority);
        assertEquals(40, edited.minutes);
        assertEquals(Collections.singletonList("On the computer"), edited.labels);
    }

    @Test public void priorityGroupsSortHighestFirstAndCompletedTasksStayLast() {
        for (String priority : Arrays.asList("UNSET", "LOWEST", "LOW", "MEDIUM", "HIGH", "HIGHEST"))
            store.add(priority, "Work", "", Collections.emptyList(), TaskStore.NEXT, "", priority, 0);
        assertEquals(TaskStore.PRIORITIES, store.all().stream().map(t -> t.priority).collect(java.util.stream.Collectors.toList()));
        String highest = store.all().get(0).id;
        store.complete(highest);
        assertEquals("HIGH", store.all().get(0).priority);
        assertEquals(highest, store.all().get(store.all().size()-1).id);
    }

    @Test public void invalidNewFieldsDoNotOverwriteExistingData() {
        store.add("Keep", "Personal", "");
        String id = store.all().get(0).id;
        String before = preferences().getString("items", "");
        assertThrows(IllegalArgumentException.class, () -> store.add("Bad", "Work", "", null, TaskStore.NEXT, "", "UNSET", 0));
        assertThrows(IllegalArgumentException.class, () -> store.add("Bad", "Work", "", Arrays.asList("At home", null), TaskStore.NEXT, "", "UNSET", 0));
        assertThrows(IllegalArgumentException.class, () -> store.update(id, "Bad", "Work", "", Collections.emptyList(), "Unknown", "", "UNSET", 0));
        assertThrows(IllegalArgumentException.class, () -> store.update(id, "Bad", "Work", "", Collections.emptyList(), TaskStore.NEXT, "", "urgent", 0));
        assertThrows(IllegalArgumentException.class, () -> store.update(id, "Bad", "Work", "", Collections.emptyList(), TaskStore.NEXT, "", "UNSET", -1));
        assertThrows(DateTimeParseException.class, () -> store.update(id, "Bad", "Work", "", Collections.emptyList(), TaskStore.NEXT, "tomorrow", "UNSET", 0));
        assertThrows(IllegalArgumentException.class, () -> store.setStatus(id, "Unknown"));
        assertEquals(before, preferences().getString("items", ""));
    }

    @Test public void malformedJsonFieldsBlockWritesInsteadOfDroppingOrCoercingTasks() throws Exception {
        String[] fields = {"labels", "status", "priority", "minutes", "plannedDate", "done", "createdAt", "title", "dueDate"};
        Object[] invalid = {"not-an-array", "Unknown", "URGENT", -1, "not-a-date", "false", 1.5, 12, "2026-02-30"};
        for (int i = 0; i < fields.length; i++) {
            JSONObject record = new JSONObject().put("id", "existing").put("title", "Keep").put("done", false).put(fields[i], invalid[i]);
            String damaged = new JSONArray().put(record).toString();
            preferences().edit().putString("items", damaged).commit();
            assertThrows("Invalid field: " + fields[i], IllegalStateException.class, () -> store.add("New", "Work", ""));
            assertEquals(damaged, preferences().getString("items", ""));
        }
    }

    @Test public void savedCompletionBatchIsAtomicAndPreservesWorkflowMetadata() {
        store.add("First", "Personal", "", Collections.singletonList("At home"), TaskStore.IN_PROGRESS, LocalDate.now().toString(), "HIGH", 20);
        String first = store.all().get(0).id;
        store.add("Second", "Work", "");
        String second = store.all().stream().filter(t -> t.title.equals("Second")).findFirst().get().id;
        Map<String,Boolean> changes = new LinkedHashMap<>(); changes.put(first, true); changes.put(second, true);
        store.setCompleted(changes);
        assertEquals(TaskStore.DONE, find(first).status);
        assertTrue(find(second).done);
        assertEquals("HIGH", find(first).priority);
        assertEquals(20, find(first).minutes);
        assertEquals(Collections.singletonList("At home"), find(first).labels);
        String saved = preferences().getString("items", "");
        changes.put(first, false); changes.put(second, null);
        assertThrows(IllegalArgumentException.class, () -> store.setCompleted(changes));
        assertEquals(saved, preferences().getString("items", ""));
        store.setCompleted(Collections.singletonMap(first, false));
        assertEquals(TaskStore.NEXT, find(first).status);
        assertFalse(find(first).done);
    }

    private SharedPreferences preferences() { return context.getSharedPreferences("tasks", Context.MODE_PRIVATE); }
    private TaskStore.Task find(String id) {
        return store.all().stream().filter(task -> task.id.equals(id)).findFirst().orElseThrow(() -> new AssertionError("Missing task: " + id));
    }

    @Test public void chosenLabelEmojiAndUnusedLabelReachNativePickers() throws Exception {
        new ReferenceStore(context).writeState("{\"contextOptions\":[\"Guild\"],\"contextIcons\":{\"Guild\":\"⚔️\"}}");
        assertTrue(store.labels().contains("Guild"));
        assertEquals("⚔️",TaskStore.contextEmoji(context,"Guild"));
        assertEquals("🏠",TaskStore.contextEmoji(context,"At home"));
    }
    @Test public void iconFallbackDoesNotResetUnreadableTaskState() {
        SharedPreferences reference=context.getSharedPreferences("reference_state",Context.MODE_PRIVATE);
        reference.edit().putString("state","[").commit();
        assertEquals("🏠",TaskStore.contextEmoji(context,"At home"));
        assertEquals("[",reference.getString("state",""));
    }

    @Test public void guildImageUsesAStaticDrawableAtEmojiSize() {
        CharSequence text=TaskStore.contextDisplay(context,"Guild");
        assertTrue(text instanceof android.text.Spanned);
        android.text.style.ImageSpan[] images=((android.text.Spanned)text).getSpans(0,text.length(),android.text.style.ImageSpan.class);
        assertEquals(1,images.length);assertEquals(images[0].getDrawable().getBounds().width(),images[0].getDrawable().getBounds().height());
        assertTrue(images[0].getDrawable().getBounds().width()>0);
    }
    @Test public void guildImageCanBeSelectedForAnotherLabel() throws Exception {
        new ReferenceStore(context).writeState("{\"contextIcons\":{\"Raid\":\"image:guild\"}}");
        assertTrue(TaskStore.usesGuildImage(context,"Raid"));
        assertTrue(TaskStore.contextDisplay(context,"Raid") instanceof android.text.Spanned);
        assertEquals("Raid",TaskStore.contextEmoji(context,"Raid"));
    }
}
