package com.personal.tasklist;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.JsonReader;
import android.util.JsonToken;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Private persistence for the reference engine and its native home-screen mirror. */
public final class ReferenceStore {
    private static final Object LOCK = new Object();
    private static final Object MIRROR_LOCK = new Object();
    private static final int MAX_BYTES = 2 * 1024 * 1024;
    private static final ZoneId REFERENCE_ZONE = ZoneId.of("America/New_York");
    private final SharedPreferences prefs;
    private final Context appContext;
    private final TaskStore taskStore;

    public ReferenceStore(Context context) {
        Context app = context.getApplicationContext();
        appContext=app;
        prefs = app.getSharedPreferences("reference_state", Context.MODE_PRIVATE);
        taskStore = new TaskStore(app);
    }

    public String readState() {
        synchronized (LOCK) { return state().toString(); }
    }

    /** Persist the engine's whole state; unrecognized extension properties are retained. */
    public void writeState(String json) {
        String normalized = object(json).toString();
        synchronized (LOCK) {
            String previous = state().toString();
            if (normalized.equals(previous) && prefs.contains("state")) return;
            commit(prefs.edit().putString("state", normalized).putLong("revision", nextRevision()));
        }
        TaskReminders.reschedule(appContext);
    }

    /** Apply a sync result only if no task or widget edit happened after it was read. */
    boolean compareAndWriteState(long expectedRevision, String json) {
        String normalized = object(json).toString();
        synchronized (LOCK) {
            if (revision() != expectedRevision) return false;
            if (normalized.equals(state().toString()) && prefs.contains("state")) return true;
            commit(prefs.edit().putString("state", normalized).putLong("revision", nextRevision()));
            return true;
        }
    }

    public long revision() {
        synchronized (LOCK) {
            try {
                long value = prefs.getLong("revision", 0);
                if (value < 0) throw new IllegalStateException("The saved task revision is invalid.");
                return value;
            } catch (ClassCastException invalid) {
                throw new IllegalStateException("The saved task revision could not be read.", invalid);
            }
        }
    }

    /** Add a one-time task from the small native home-screen popup. */
    public String addCustomTask(String title, List<String> labels, String priority, boolean tomorrow) {
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("Enter a task title.");
        priority=TaskStore.normalizePriority(priority);
        if (!TaskStore.PRIORITIES.contains(priority)) throw new IllegalArgumentException("Choose a valid priority.");
        if (labels == null) throw new IllegalArgumentException("Choose valid context labels.");
        LinkedHashSet<String> contexts = new LinkedHashSet<>();
        for (String label : labels) {
            if (label == null) throw new IllegalArgumentException("A context label must be text.");
            if (!label.trim().isEmpty()) contexts.add(label.trim());
        }
        String id = "custom-" + UUID.randomUUID();
        String date = LocalDate.now(REFERENCE_ZONE).plusDays(tomorrow ? 1 : 0).toString();
        synchronized (LOCK) {
            try {
                JSONObject current = state();
                JSONArray custom = customTasks(current);
                JSONObject definition = new JSONObject().put("id", id).put("title", title.trim())
                    .put("priority", priority).put("labels", new JSONArray(contexts))
                    .put("due", date).put("when", "Not set");
                JSONArray records = storedSnapshot();
                records.put(customMirror(definition, !tomorrow));
                custom.put(definition);
                current.put("customTasks", custom).put("updatedAt", Instant.now().toString());
                appendMissingCustomMirrors(records, current);
                applyPersistedCompletions(records, current);
                String stateJson = current.toString(), snapshotJson = records.toString();
                checkSize(stateJson); snapshot(snapshotJson);
                taskStore.all(); // Reject damaged native storage before committing either addition.
                commit(prefs.edit().putString("state", stateJson).putString("snapshot", snapshotJson)
                    .putLong("revision", nextRevision()));
            } catch (JSONException invalid) {
                throw new IllegalStateException("Your new task could not be saved.", invalid);
            }
        }
        replaceLatestMirror();
        return id;
    }

    /** Atomically replaces html: mirror rows while retaining every native task. */
    public void updateSnapshot(String json) {
        JSONArray records = snapshot(json);
        synchronized (LOCK) {
            // A widget may have completed a row after the page calculated this snapshot.
            // Persisted completion always wins over an older open-row snapshot.
            JSONObject current = state();
            appendMissingCustomMirrors(records, current);
            applyPersistedCompletions(records, current);
            String normalized = records.toString();
            checkSize(normalized);
            TaskStore.validateReferenceTasks(normalized);
            taskStore.all();
            commit(prefs.edit().putString("snapshot", normalized));
        }
        replaceLatestMirror();
    }

    /** Complete a reference row using its engine keys, including nested child/step keys. */
    public boolean completeTask(String taskId) {
        if (taskId == null || !taskId.startsWith("html:") || taskId.length() == 5) return false;
        synchronized (LOCK) {
            JSONArray records = storedSnapshot();
            JSONObject target = null;
            try {
                for (int i = 0; i < records.length(); i++) {
                    JSONObject record = records.getJSONObject(i);
                    if (taskId.equals(record.getString("id"))) { target = record; break; }
                }
                if (target == null) return false;
                JSONObject current = state();
                JSONObject completed = current.has("completed") ? objectField(current, "completed") : new JSONObject();
                JSONObject completedOn = current.has("completedOn") ? objectField(current, "completedOn") : new JSONObject();
                JSONObject rollover = current.has("rolloverCompletedOn") ? objectField(current, "rolloverCompletedOn") : new JSONObject();
                Object rawActive = current.opt("inProgress");
                if (rawActive != null && rawActive != JSONObject.NULL && !(rawActive instanceof String))
                    throw new IllegalStateException("The saved active task could not be read.");
                String active = rawActive instanceof String ? (String) rawActive : null;
                String progress = target.getString("progressKey");
                JSONArray keys = target.getJSONArray("completionKeys");
                boolean changed = false;
                boolean becameComplete = false;
                String today = LocalDate.now(REFERENCE_ZONE).toString();
                boolean clearActive = matches(active, progress);
                for (int i = 0; i < keys.length(); i++) {
                    String key = keys.getString(i);
                    if (!Boolean.TRUE.equals(completed.opt(key))) {
                        completed.put(key, true);
                        completedOn.put(key, today);
                        changed = true; becameComplete = true;
                    }
                    if (matches(active, key)) clearActive = true;
                }
                if (becameComplete) {
                    completedOn.put(progress, today);
                    current.put("completedOn", completedOn);
                }
                if (clearActive) { current.put("inProgress", JSONObject.NULL); changed = true; }
                if (becameComplete && target.getBoolean("onToday") && !today.equals(target.getString("stateDate"))) {
                    if (!today.equals(rollover.opt(progress))) {
                        rollover.put(progress, today); changed = true;
                    }
                    current.put("rolloverCompletedOn", rollover);
                }
                current.put("completed", completed);
                boolean mirrorChanged = !Boolean.TRUE.equals(target.opt("done")) || !TaskStore.DONE.equals(target.opt("status"));
                target.put("done", true).put("status", TaskStore.DONE);
                if (changed || mirrorChanged) {
                    SharedPreferences.Editor editor = prefs.edit().putString("snapshot", records.toString());
                    if (changed) {
                        current.put("updatedAt", Instant.now().toString());
                        String serialized = current.toString();
                        checkSize(serialized);
                        editor.putString("state", serialized).putLong("revision", nextRevision());
                    }
                    commit(editor);
                }
            } catch (JSONException invalid) {
                throw new IllegalStateException("The saved widget task could not be read.", invalid);
            }
        }
        // Avoid holding both store locks during widget refreshes or launcher callbacks.
        synchronized (MIRROR_LOCK) { taskStore.complete(taskId); }
        TaskReminders.reschedule(appContext);
        return true;
    }

    private void replaceLatestMirror() {
        synchronized (MIRROR_LOCK) {
            String latest;
            synchronized (LOCK) { latest = storedSnapshot().toString(); }
            // Never hold the reference-state lock across widget/launcher callbacks.
            taskStore.replaceReferenceTasks(latest);
        }
    }

    private static JSONArray customTasks(JSONObject current) {
        Object value = current.opt("customTasks");
        if (value == null) return new JSONArray();
        if (!(value instanceof JSONArray)) throw new IllegalStateException("Saved custom tasks must be a list.");
        return (JSONArray) value;
    }

    private static void applyPersistedCompletions(JSONArray records, JSONObject state) {
        if (!state.has("completed")) return;
        JSONObject completed = objectField(state, "completed");
        for (int i = 0; i < records.length(); i++) {
            try {
                JSONObject record = records.getJSONObject(i);
                JSONArray keys = record.getJSONArray("completionKeys");
                boolean allDone = true;
                for (int j = 0; j < keys.length(); j++)
                    if (!Boolean.TRUE.equals(completed.opt(keys.getString(j)))) { allDone = false; break; }
                if (allDone) record.put("done", true).put("status", TaskStore.DONE);
            } catch (JSONException invalid) { throw invalidSnapshot(invalid); }
        }
    }

    private static void appendMissingCustomMirrors(JSONArray records, JSONObject state) {
        JSONArray definitions = customTasks(state);
        Set<String> present = new HashSet<>();
        try {
            for (int i = 0; i < records.length(); i++) present.add(records.getJSONObject(i).getString("id"));
            for (int i = 0; i < definitions.length(); i++) {
                JSONObject definition = definitions.getJSONObject(i);
                String id = requiredString(definition, "id");
                // Only the native quick-add form needs an immediate fallback row.
                // App-defined recurring/checklist occurrences come from the engine snapshot.
                if (!id.startsWith("custom-") || !definition.has("due") || definition.has("repeat")
                        || definition.has("days") || definition.has("intervalDays") || definition.has("weekendCycleStart")) continue;
                String due = requiredString(definition, "due");
                String mirrorId = "html:" + id + ":" + due;
                if (!present.contains(mirrorId)) {
                    boolean today = !LocalDate.parse(due).isAfter(LocalDate.now(REFERENCE_ZONE));
                    records.put(customMirror(definition, today)); present.add(mirrorId);
                }
            }
        } catch (JSONException invalid) { throw new IllegalStateException("Saved custom tasks could not be read.", invalid); }
    }

    private static JSONObject customMirror(JSONObject definition, boolean onToday) throws JSONException {
        String id = requiredString(definition, "id"), due = requiredString(definition, "due");
        String key = id + ":" + due;
        return new JSONObject().put("id", "html:" + key).put("title", requiredString(definition, "title"))
            .put("listName", TaskStore.DEFAULT_LIST).put("dueDate", due).put("plannedDate", due)
            .put("done", false).put("status", TaskStore.NEXT).put("labels", definition.getJSONArray("labels"))
            .put("priority", requiredString(definition, "priority")).put("minutes", 0)
            .put("createdAt", System.currentTimeMillis()).put("completionKeys", new JSONArray().put(key))
            .put("progressKey", key).put("stateDate", due).put("onToday", onToday).put("source", "html");
    }

    private JSONObject state() {
        try { return object(prefs.getString("state", "{}")); }
        catch (RuntimeException invalid) { throw new IllegalStateException("Your saved task state could not be read.", invalid); }
    }

    private JSONArray storedSnapshot() {
        try { return snapshot(prefs.getString("snapshot", "[]")); }
        catch (RuntimeException invalid) { throw new IllegalStateException("Your saved widget tasks could not be read.", invalid); }
    }

    private long nextRevision() {
        long current = revision();
        if (current == Long.MAX_VALUE) throw new IllegalStateException("The saved task revision is full.");
        return current + 1;
    }

    private static boolean matches(String active, String key) {
        return active != null && (active.equals(key) || active.startsWith(key + ":"));
    }

    private static JSONObject objectField(JSONObject value, String key) {
        Object nested = value.opt(key);
        if (!(nested instanceof JSONObject)) throw new IllegalStateException("The saved " + key + " field must be an object.");
        return (JSONObject) nested;
    }

    private static JSONObject object(String json) {
        strictJson(json, JsonToken.BEGIN_OBJECT);
        try { return new JSONObject(json); }
        catch (JSONException invalid) { throw new IllegalArgumentException("Choose a valid JSON task-state object.", invalid); }
    }

    private static JSONArray snapshot(String json) {
        strictJson(json, JsonToken.BEGIN_ARRAY);
        try {
            JSONArray records = new JSONArray(json);
            Set<String> ids = new HashSet<>();
            for (int i = 0; i < records.length(); i++) {
                JSONObject record = records.getJSONObject(i);
                String id = requiredString(record, "id"), progress = requiredString(record, "progressKey");
                if (!id.equals("html:" + progress) || !ids.add(id)) throw new JSONException("Invalid or duplicate reference task identity.");
                String date = requiredString(record, "stateDate"); LocalDate.parse(date);
                if (!(record.opt("onToday") instanceof Boolean)) throw new JSONException("Task visibility must be true or false.");
                JSONArray keys = record.getJSONArray("completionKeys");
                if (keys.length() == 0 && !Boolean.TRUE.equals(record.opt("done")) && !TaskStore.DONE.equals(record.opt("status")))
                    throw new JSONException("An open reference task needs completion keys.");
                for (int j = 0; j < keys.length(); j++)
                    if (!(keys.get(j) instanceof String) || keys.getString(j).trim().isEmpty())
                        throw new JSONException("A completion key must be nonempty text.");
            }
            TaskStore.validateReferenceTasks(records.toString());
            return records;
        } catch (JSONException | RuntimeException invalid) { throw invalidSnapshot(invalid); }
    }

    private static String requiredString(JSONObject object, String key) throws JSONException {
        Object value = object.opt(key);
        if (!(value instanceof String) || ((String) value).trim().isEmpty()) throw new JSONException("Missing text field: " + key);
        return (String) value;
    }

    private static IllegalArgumentException invalidSnapshot(Exception error) {
        return new IllegalArgumentException("Choose a valid widget task snapshot.", error);
    }

    private static void checkSize(String json) {
        if (json == null || json.length() > MAX_BYTES || json.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES)
            throw new IllegalArgumentException("The task state must be no larger than 2 MiB.");
    }

    private static void strictJson(String json, JsonToken expected) {
        checkSize(json);
        try (JsonReader reader = new JsonReader(new StringReader(json))) {
            reader.setLenient(false);
            if (reader.peek() != expected) throw new IllegalArgumentException("Choose a valid JSON " + (expected == JsonToken.BEGIN_OBJECT ? "object." : "array."));
            reader.skipValue();
            if (reader.peek() != JsonToken.END_DOCUMENT) throw new IllegalArgumentException("The JSON contains extra data.");
        } catch (IOException invalid) { throw new IllegalArgumentException("Choose valid JSON task data.", invalid); }
    }

    private static void commit(SharedPreferences.Editor editor) {
        if (!editor.commit()) throw new IllegalStateException("Your task state could not be saved. Check device storage.");
    }
}
