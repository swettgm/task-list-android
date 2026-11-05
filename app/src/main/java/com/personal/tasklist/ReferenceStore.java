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
        return addCustomTask(title,labels,priority,LocalDate.now(REFERENCE_ZONE).plusDays(tomorrow?1:0).toString(),null,false);
    }
    public String addCustomTask(String title,List<String> labels,String priority,String date,String time,boolean weekend) {return addCustomTask(title,labels,priority,date,time,weekend,false);}
    public String addCustomTask(String title,List<String> labels,String priority,String date,String time,boolean weekend,boolean urgent) {
        try{LocalDate.parse(date);}catch(Exception invalid){throw new IllegalArgumentException("Choose a valid date.");}
        if(time!=null&&!time.isEmpty()&&ReminderPlanner.time(time)==null)throw new IllegalArgumentException("Choose a valid time.");
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("Enter a task title.");
        priority=TaskStore.normalizePriority(priority);
        if (!TaskStore.PRIORITIES.contains(priority)) throw new IllegalArgumentException("Choose a valid priority.");
        if (labels == null) throw new IllegalArgumentException("Choose valid context labels.");
        LinkedHashSet<String> contexts = new LinkedHashSet<>();
        for (String label : labels) {
            if (label == null) throw new IllegalArgumentException("A context label must be text.");
            if (!label.trim().isEmpty()) contexts.add(label.trim());
        }
        String priorityBeforeUrgent=priority;
        if(urgent)priority="HIGH";
        String id = "custom-" + UUID.randomUUID();
        synchronized (LOCK) {
            try {
                JSONObject current = state();
                JSONArray custom = customTasks(current);
                JSONObject definition = new JSONObject().put("id", id).put("title", title.trim())
                    .put("priority", priority).put("labels", new JSONArray(contexts))
                    .put("due", date).put("when", "Not set");
                if(time!=null&&!time.isEmpty())definition.put("time",time);if(weekend)definition.put("weekend",true);if(urgent)definition.put("urgent",true).put("priorityBeforeUrgent",priorityBeforeUrgent);
                JSONArray records = storedSnapshot();
                records.put(customMirror(definition,!LocalDate.parse(date).isAfter(LocalDate.now(REFERENCE_ZONE))));
                custom.put(definition);
                current.put("customTasks", custom).put("updatedAt", Instant.now().toString());
                appendMissingCustomMirrors(records, current);
                removePurgedRecords(records,current);
            applyPersistedCompletions(records, current);
            applyPersistedProgress(records,current);
                String stateJson = current.toString(), snapshotJson = records.toString();
                checkSize(stateJson); snapshot(snapshotJson);
                taskStore.all(); // Reject damaged native storage before committing either addition.
                commit(prefs.edit().putString("state", stateJson).putString("snapshot", snapshotJson)
                    .putLong("revision", nextRevision()));
            } catch (JSONException invalid) {
                throw new IllegalStateException("Your new task could not be saved.", invalid);
            }
        }
        replaceLatestMirror();TaskReminders.reschedule(appContext);
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
            removePurgedRecords(records,current);
            applyPersistedCompletions(records, current);
            applyPersistedProgress(records,current);
            String normalized = records.toString();
            checkSize(normalized);
            TaskStore.validateReferenceTasks(normalized);
            taskStore.all();
            commit(prefs.edit().putString("snapshot", normalized));
        }
        replaceLatestMirror();
    }

    private static boolean purgedRecord(JSONObject record,JSONObject state){String key=record.optString("progressKey");int split=key.lastIndexOf(':');return split>0&&ReminderPlanner.removed(key.substring(0,split),key.substring(split+1),state);}
    private static void removePurgedRecords(JSONArray records,JSONObject state){for(int i=records.length()-1;i>=0;i--){JSONObject record=records.optJSONObject(i);if(record!=null&&purgedRecord(record,state))records.remove(i);}}

    /** Complete a reference row using its engine keys, including nested child/step keys. */
    public boolean completeTask(String taskId) { return completeTask(taskId,null); }
    /** Notification actions can complete an occurrence before a widget snapshot exists. */
    boolean completeReminder(JSONObject task,String date){
        try{String key=task.getString("id")+":"+date;JSONObject record=new JSONObject().put("id","html:"+key).put("title",task.getString("title")).put("listName",TaskStore.DEFAULT_LIST).put("dueDate",date).put("plannedDate",LocalDate.now(REFERENCE_ZONE).toString()).put("done",false).put("status",TaskStore.NEXT).put("labels",task.optJSONArray("_contexts")!=null?task.optJSONArray("_contexts"):task.optJSONArray("labels")!=null?task.optJSONArray("labels"):new JSONArray()).put("priority",TaskStore.normalizePriority(task.optString("priority"))).put("minutes",task.optInt("minutes")).put("createdAt",System.currentTimeMillis()).put("completionKeys",ReminderPlanner.completionKeys(task,date)).put("progressKey",key).put("stateDate",date).put("onToday",true).put("source","html").put("snapshotDate",LocalDate.now(REFERENCE_ZONE).toString());return completeTask("html:"+key,record);}catch(JSONException invalid){throw new IllegalStateException("The reminder task could not be read.",invalid);}
    }
    private boolean completeTask(String taskId,JSONObject reminderRecord) {
        if (taskId == null || !taskId.startsWith("html:") || taskId.length() == 5) return false;
        synchronized (LOCK) {
            JSONArray records = storedSnapshot();
            JSONObject target = null;
            try {
                for (int i = 0; i < records.length(); i++) {
                    JSONObject record = records.getJSONObject(i);
                    if (taskId.equals(record.getString("id"))) { target = record; break; }
                }
                if(reminderRecord!=null){if(target==null){target=reminderRecord;records.put(target);}else{for(java.util.Iterator<String> it=reminderRecord.keys();it.hasNext();){String name=it.next();target.put(name,reminderRecord.get(name));}}}
                if (target == null) return false;
                JSONObject current = state();if(purgedRecord(target,current))return false;
                JSONObject completed = current.has("completed") ? objectField(current, "completed") : new JSONObject();
                JSONObject completedOn = current.has("completedOn") ? objectField(current, "completedOn") : new JSONObject();
                JSONObject completedAt = current.has("completedAt") ? objectField(current, "completedAt") : new JSONObject();
                long finishedAt=System.currentTimeMillis();for(java.util.Iterator<String> it=completedAt.keys();it.hasNext();){Object value=completedAt.opt(it.next());if(value instanceof Number){long previous=((Number)value).longValue();if(previous>=finishedAt&&previous<Long.MAX_VALUE)finishedAt=previous+1;}}

                JSONObject rollover = current.has("rolloverCompletedOn") ? objectField(current, "rolloverCompletedOn") : new JSONObject();
                java.util.List<String> active=activeKeys(current);
                String progress = target.getString("progressKey");
                JSONArray keys = target.getJSONArray("completionKeys");
                boolean changed = false;
                boolean becameComplete = false;
                String today = LocalDate.now(REFERENCE_ZONE).toString();
                boolean clearActive = active.stream().anyMatch(value->matches(value,progress));
                for (int i = 0; i < keys.length(); i++) {
                    String key = keys.getString(i);
                    if (!Boolean.TRUE.equals(completed.opt(key))) {
                        completed.put(key, true);
                        completedOn.put(key, today);completedAt.put(key,finishedAt);
                        changed = true; becameComplete = true;
                    }
                    if (active.stream().anyMatch(value->matches(value,key))) clearActive = true;
                }
                if (becameComplete) {
                    completedOn.put(progress, today);completedAt.put(progress,finishedAt);
                    current.put("completedAt",completedAt);
                    current.put("completedOn", completedOn);
                }
                if (clearActive) { active.removeIf(value->{if(matches(value,progress))return true;for(int i=0;i<keys.length();i++)if(matches(value,keys.optString(i)))return true;return false;});writeActiveKeys(current,active);changed = true; }
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

    /** Persist widget Do now/Stop in the same occurrence state read by the app. */
    private static java.util.List<String> activeKeys(JSONObject state){
        Object raw=state.opt("inProgress");if(raw!=null&&raw!=JSONObject.NULL&&!(raw instanceof String))throw new IllegalStateException("The saved active task could not be read.");
        java.util.LinkedHashSet<String> keys=new java.util.LinkedHashSet<>();Object stored=state.opt("inProgressTasks");if(stored!=null&&!(stored instanceof JSONArray))throw new IllegalStateException("The saved active tasks could not be read.");
        if(stored instanceof JSONArray){JSONArray values=(JSONArray)stored;for(int i=0;i<values.length();i++){Object value=values.opt(i);if(!(value instanceof String))throw new IllegalStateException("The saved active tasks could not be read.");if(!((String)value).isEmpty())keys.add((String)value);}}
        if(raw instanceof String){if(!keys.contains(raw)){keys.clear();keys.add((String)raw);}}else if(state.has("inProgress"))keys.clear();
        return new java.util.ArrayList<>(keys);
    }
    private static void writeActiveKeys(JSONObject state,java.util.List<String> keys)throws JSONException{state.put("inProgressTasks",new JSONArray(keys)).put("inProgress",keys.isEmpty()?JSONObject.NULL:keys.get(keys.size()-1));}
    public boolean setTaskInProgress(String taskId, boolean start) {
        if(taskId==null||!taskId.startsWith("html:")||taskId.length()==5)return false;
        synchronized(LOCK){JSONArray records=storedSnapshot();String beforeSnapshot=records.toString();JSONObject target=null;
            try{
                for(int i=0;i<records.length();i++)if(taskId.equals(records.getJSONObject(i).getString("id"))){target=records.getJSONObject(i);break;}
                if(target==null)return false;JSONObject current=state();if(purgedRecord(target,current))return false;applyPersistedCompletions(records,current);java.util.List<String> active=activeKeys(current),next=new java.util.ArrayList<>(active);String progress=target.getString("progressKey");boolean done=target.optBoolean("done")||TaskStore.DONE.equals(target.optString("status"));
                if(!done&&start){if(!next.contains(progress))next.add(progress);startLinked(current,records,progress,next);}else next.removeIf(value->matches(value,progress));
                boolean changed=!active.equals(next)||(!done&&!current.optBoolean("progressStateInitialized"));if(changed){writeActiveKeys(current,next);current.put("progressStateInitialized",true).put("updatedAt",Instant.now().toString());}
                applyPersistedProgress(records,current);String serialized=current.toString(),snapshotJson=records.toString();checkSize(serialized);checkSize(snapshotJson);TaskStore.validateReferenceTasks(snapshotJson);
                if(changed||!beforeSnapshot.equals(snapshotJson)){SharedPreferences.Editor editor=prefs.edit().putString("snapshot",snapshotJson);if(changed)editor.putString("state",serialized).putLong("revision",nextRevision());commit(editor);}
            }catch(JSONException invalid){throw new IllegalStateException("The saved widget task could not be read.",invalid);}
        }replaceLatestMirror();TaskReminders.reschedule(appContext);return true;
    }
    private void startLinked(JSONObject state,JSONArray records,String progress,java.util.List<String> active)throws JSONException{
        JSONObject links=state.optJSONObject("manualLinks");if(links==null)return;String[] source=progress.split(":",3);if(source.length<2)return;
        JSONArray definitions=ReminderPlanner.effective(new JSONArray(TaskReminders.prefs(appContext).getString("definitions","[]")),state);
        java.util.Set<String> seen=new java.util.HashSet<>();java.util.ArrayDeque<String> queue=new java.util.ArrayDeque<>();queue.add(source[0]);
        while(!queue.isEmpty()){String id=queue.remove();if(!seen.add(id))continue;java.util.Set<String> peers=new java.util.LinkedHashSet<>();JSONArray direct=links.optJSONArray(id);if(direct!=null)for(int i=0;i<direct.length();i++)peers.add(direct.optString(i));for(java.util.Iterator<String> it=links.keys();it.hasNext();){String candidate=it.next();JSONArray reverse=links.optJSONArray(candidate);if(reverse!=null)for(int i=0;i<reverse.length();i++)if(id.equals(reverse.optString(i)))peers.add(candidate);}
            for(String peer:peers){String key=null;JSONObject definition=null;for(int i=0;i<definitions.length();i++)if(peer.equals(definitions.getJSONObject(i).optString("id")))definition=definitions.getJSONObject(i);
                if(definition!=null&&definition.optBoolean("retired"))continue;
                for(int i=0;i<records.length();i++){JSONObject row=records.getJSONObject(i);String value=row.optString("progressKey");if(!value.startsWith(peer+":")||row.optBoolean("done")||TaskStore.DONE.equals(row.optString("status"))||purgedRecord(row,state))continue;if(value.equals(peer+":"+source[1])){key=value;break;}if(key==null&&row.optBoolean("onToday"))key=value;}
                if(key==null&&definition!=null){String date=definition.optString("due",source[1]);if(!ReminderPlanner.done(definition,date,state))key=peer+":"+date;}
                if(key!=null){if(!active.contains(key))active.add(key);if(!seen.contains(peer))queue.add(peer);}
            }
        }
    }
    private static void applyPersistedProgress(JSONArray records,JSONObject state){
        if(!state.has("inProgress")&&!state.has("inProgressTasks")&&!state.optBoolean("progressStateInitialized"))return;java.util.List<String> active=activeKeys(state);
        try{for(int i=0;i<records.length();i++){JSONObject record=records.getJSONObject(i);if(record.optBoolean("done")||TaskStore.DONE.equals(record.optString("status")))continue;String progress=record.getString("progressKey");if(active.stream().anyMatch(value->matches(value,progress)))record.put("status",TaskStore.IN_PROGRESS);else if(TaskStore.IN_PROGRESS.equals(record.optString("status")))record.put("status",TaskStore.NEXT);}}catch(JSONException invalid){throw invalidSnapshot(invalid);}
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
        JSONObject row=new JSONObject().put("id", "html:" + key).put("title", requiredString(definition, "title"))
            .put("listName", TaskStore.DEFAULT_LIST).put("dueDate", due).put("plannedDate", due)
            .put("done", false).put("status", TaskStore.NEXT).put("labels", definition.getJSONArray("labels"))
            .put("priority", requiredString(definition, "priority")).put("minutes", 0)
            .put("urgent",definition.optBoolean("urgent")).put("createdAt", System.currentTimeMillis()).put("completionKeys", new JSONArray().put(key))
            .put("progressKey", key).put("stateDate", due).put("onToday", onToday).put("source", "html").put("snapshotDate",LocalDate.now(REFERENCE_ZONE).toString()).put("time",definition.optString("time"));
        row.put("weekendOpening",WeekendSchedule.opening(definition,due));
        if(definition.has("showBeforeMinutes"))row.put("showBeforeMinutes",definition.opt("showBeforeMinutes"));
        return row;
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
