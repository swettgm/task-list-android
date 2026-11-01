package com.personal.tasklist;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

/** One local source of truth for the app and every widget instance. */
public final class TaskStore {
    private static final Object LOCK = new Object();
    private final SharedPreferences prefs;
    private final Context context;
    public static final String DEFAULT_LIST = "My tasks";

    public static final class Task {
        public final String id;
        public final String title;
        public final String listName;
        public final String dueDate;
        public final boolean done;
        public final long createdAt;

        Task(String id, String title, String listName, String dueDate, boolean done, long createdAt) {
            this.id = id;
            this.title = title;
            this.listName = listName;
            this.dueDate = dueDate;
            this.done = done;
            this.createdAt = createdAt;
        }
        Task withDone(boolean value) { return new Task(id, title, listName, dueDate, value, createdAt); }
        JSONObject json() throws JSONException {
            return new JSONObject().put("id", id).put("title", title).put("listName", listName)
                .put("dueDate", dueDate).put("done", done).put("createdAt", createdAt);
        }
    }

    public TaskStore(Context context) {
        this.context = context.getApplicationContext();
        prefs = this.context.getSharedPreferences("tasks", Context.MODE_PRIVATE);
    }

    public List<Task> all() {
        synchronized (LOCK) {
            List<Task> tasks = read();
            tasks.sort(Comparator.comparing((Task t) -> t.done)
                .thenComparing(t -> t.dueDate.isEmpty() ? "9999-12-31" : t.dueDate)
                .thenComparing(Comparator.comparingLong((Task t) -> t.createdAt).reversed()));
            return tasks;
        }
    }

    public List<String> lists() {
        LinkedHashSet<String> lists = new LinkedHashSet<>();
        lists.add(DEFAULT_LIST);
        for (Task task : all()) lists.add(task.listName);
        return new ArrayList<>(lists);
    }

    public void add(String title, String list, String due) {
        requireTitle(title);
        requireDate(due);
        synchronized (LOCK) {
            List<Task> tasks = read();
            tasks.add(new Task(UUID.randomUUID().toString(), title.trim(), listName(list), due, false,
                System.currentTimeMillis()));
            write(tasks);
        }
        TaskWidgetProvider.refreshAll(context);
    }

    public void update(String id, String title, String list, String due) {
        requireTitle(title);
        requireDate(due);
        synchronized (LOCK) {
            List<Task> tasks = read();
            for (int i = 0; i < tasks.size(); i++) {
                Task old = tasks.get(i);
                if (old.id.equals(id)) {
                    tasks.set(i, new Task(id, title.trim(), listName(list), due, old.done, old.createdAt));
                    break;
                }
            }
            write(tasks);
        }
        TaskWidgetProvider.refreshAll(context);
    }

    public void toggle(String id) {
        synchronized (LOCK) {
            List<Task> tasks = read();
            for (int i = 0; i < tasks.size(); i++) {
                Task task = tasks.get(i);
                if (task.id.equals(id)) { tasks.set(i, task.withDone(!task.done)); break; }
            }
            write(tasks);
        }
        TaskWidgetProvider.refreshAll(context);
    }

    /** Widget taps are idempotent even when a stale row is tapped twice. */
    public void complete(String id) {
        synchronized (LOCK) {
            List<Task> tasks = read();
            for (int i = 0; i < tasks.size(); i++) {
                Task task = tasks.get(i);
                if (task.id.equals(id) && !task.done) {
                    tasks.set(i, task.withDone(true));
                    write(tasks);
                    break;
                }
            }
        }
        TaskWidgetProvider.refreshAll(context);
    }

    public void delete(String id) {
        synchronized (LOCK) {
            List<Task> tasks = read();
            tasks.removeIf(task -> task.id.equals(id));
            write(tasks);
        }
        TaskWidgetProvider.refreshAll(context);
    }

    private List<Task> read() {
        List<Task> tasks = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(prefs.getString("items", "[]"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                tasks.add(new Task(item.getString("id"), item.getString("title"),
                    item.optString("listName", DEFAULT_LIST), item.optString("dueDate", ""),
                    item.optBoolean("done", false), item.optLong("createdAt", 0)));
            }
        } catch (JSONException error) {
            // Never overwrite damaged storage with an empty list during an edit.
            throw new IllegalStateException("Your task data could not be read. Restart the app and try again.", error);
        }
        return tasks;
    }

    private void write(List<Task> tasks) {
        JSONArray array = new JSONArray();
        try { for (Task task : tasks) array.put(task.json()); }
        catch (JSONException error) { throw new IllegalStateException("The task could not be saved.", error); }
        if (!prefs.edit().putString("items", array.toString()).commit())
            throw new IllegalStateException("Your task could not be saved. Check your device storage.");
    }

    private static String listName(String list) {
        return list == null || list.trim().isEmpty() ? DEFAULT_LIST : list.trim();
    }
    private static void requireTitle(String title) {
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("Enter a task title.");
    }
    private static void requireDate(String due) {
        if (due == null) throw new IllegalArgumentException("Choose a valid date.");
        if (!due.isEmpty()) LocalDate.parse(due);
    }
}
