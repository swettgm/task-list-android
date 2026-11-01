package com.personal.tasklist;

import static org.junit.Assert.*;

import android.content.Context;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
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
}
