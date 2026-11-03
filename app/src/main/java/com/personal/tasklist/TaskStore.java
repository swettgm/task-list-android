package com.personal.tasklist;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** One local source of truth for the app and every widget instance. */
public final class TaskStore {
    private static final Object LOCK = new Object();
    private final SharedPreferences prefs;
    private final Context context;
    public static final String DEFAULT_LIST = "My tasks";
    public static final String NEXT = "Next", IN_PROGRESS = "In progress", WAITING = "Waiting", SOMEDAY = "Someday", DONE = "Done";
    public static final List<String> DEFAULT_LABELS = Collections.unmodifiableList(Arrays.asList(
        "At home", "On the road", "On the phone", "On the computer"));
    public static final List<String> STATUSES = Collections.unmodifiableList(Arrays.asList(NEXT, IN_PROGRESS, WAITING, SOMEDAY, DONE));
    public static final List<String> PRIORITIES = Collections.unmodifiableList(Arrays.asList("HIGH", "MEDIUM", "LOW"));

    static String normalizePriority(String value){if("HIGHEST".equals(value))return "HIGH";if(value==null||value.isEmpty()||"UNSET".equals(value)||"LOWEST".equals(value))return "LOW";return value;}

    public static final class Task {
        public final String id, title, listName, dueDate, status, plannedDate, priority;
        public final boolean done;
        public final long createdAt;
        public final List<String> labels;
        public final int minutes;
        private final String sourceJson;

        Task(String id, String title, String listName, String dueDate, boolean done, long createdAt) {
            this(id, title, listName, dueDate, createdAt, Collections.emptyList(), done ? DONE : NEXT, "", "LOW", 0, "{}");
        }
        private Task(String id, String title, String listName, String dueDate, long createdAt,
                     List<String> labels, String status, String plannedDate, String priority, int minutes, String sourceJson) {
            this.id = id; this.title = title; this.listName = listName; this.dueDate = dueDate;
            this.done = DONE.equals(status); this.createdAt = createdAt;
            this.labels = Collections.unmodifiableList(new ArrayList<>(labels));
            this.status = status; this.plannedDate = plannedDate; this.priority = normalizePriority(priority);
            this.minutes = minutes; this.sourceJson = sourceJson;
        }
        JSONObject sourceRecord() { try{return new JSONObject(sourceJson);}catch(JSONException invalid){throw new IllegalStateException("Invalid saved task metadata",invalid);} }
        private Task withStatus(String value, String planned) {
            return new Task(id, title, listName, dueDate, createdAt, labels, value, planned, priority, minutes, sourceJson);
        }
        private JSONObject json() throws JSONException {
            // Retain unknown fields written by later versions or importers during known-field edits.
            return new JSONObject(sourceJson).put("id", id).put("title", title).put("listName", listName)
                .put("dueDate", dueDate).put("done", done).put("createdAt", createdAt)
                .put("labels", new JSONArray(labels)).put("status", status).put("plannedDate", plannedDate)
                .put("priority", priority).put("minutes", minutes);
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
                .thenComparingInt(t -> PRIORITIES.indexOf(t.priority))
                .thenComparing(t -> t.dueDate.isEmpty() ? "9999-12-31" : t.dueDate)
                .thenComparing(Comparator.comparingLong((Task t) -> t.createdAt).reversed()).thenComparing(t -> t.id));
            return tasks;
        }
    }
    public List<String> lists() {
        LinkedHashSet<String> values = new LinkedHashSet<>(); values.add(DEFAULT_LIST);
        for (Task task : all()) values.add(task.listName);
        return new ArrayList<>(values);
    }
    public List<String> labels() {
        LinkedHashSet<String> values = new LinkedHashSet<>(DEFAULT_LABELS);
        values.add("Guild");
        for (Task task : all()) values.addAll(task.labels);
        try { JSONArray saved=new JSONObject(new ReferenceStore(context).readState()).optJSONArray("contextOptions");if(saved!=null)for(int i=0;i<saved.length();i++)if(!saved.optString(i).trim().isEmpty())values.add(saved.optString(i).trim()); } catch(JSONException|RuntimeException unavailable) { /* Rendering a picker must not overwrite damaged state. */ }
        return new ArrayList<>(values);
    }

    public void add(String title, String list, String due) {
        add(title, list, due, Collections.emptyList(), NEXT, "", "LOW", 0);
    }
    public void add(String title, String list, String due, List<String> labels, String status,
                    String plannedDate, String priority, int minutes) {
        validateInput(title, due, status, plannedDate, priority, minutes);
        List<String> normalized = normalizeLabels(labels);
        synchronized (LOCK) {
            List<Task> tasks = read();
            Task task = new Task(UUID.randomUUID().toString(), title.trim(), listName(list), due,
                System.currentTimeMillis(), normalized, status, plannedDate, priority, minutes, "{}");
            tasks.add(task); write(tasks);
        }
        refreshWidgets();
    }
    /** Compatibility edits preserve contexts, scheduling, status, priority and minutes. */
    public void update(String id, String title, String list, String due) {
        requireTitle(title); requireDate(due);
        synchronized (LOCK) {
            List<Task> tasks = read();
            for (int i = 0; i < tasks.size(); i++) {
                Task old = tasks.get(i);
                if (old.id.equals(id)) {
                    tasks.set(i, new Task(old.id, title.trim(), listName(list), due, old.createdAt,
                        old.labels, old.status, old.plannedDate, old.priority, old.minutes, old.sourceJson));
                    write(tasks); break;
                }
            }
        }
        refreshWidgets();
    }
    public void update(String id, String title, String list, String due, List<String> labels,
                       String status, String plannedDate, String priority, int minutes) {
        validateInput(title, due, status, plannedDate, priority, minutes);
        List<String> normalized = normalizeLabels(labels);
        synchronized (LOCK) {
            List<Task> tasks = read();
            for (int i = 0; i < tasks.size(); i++) {
                Task old = tasks.get(i);
                if (old.id.equals(id)) {
                    tasks.set(i, new Task(old.id, title.trim(), listName(list), due, old.createdAt,
                        normalized, status, plannedDate, priority, minutes, old.sourceJson));
                    write(tasks); break;
                }
            }
        }
        refreshWidgets();
    }

    public void doNow(String id) { changeStatus(id, IN_PROGRESS, LocalDate.now(java.time.ZoneId.of("America/New_York")).toString()); }
    public void doTomorrow(String id) { changeStatus(id, NEXT, LocalDate.now(java.time.ZoneId.of("America/New_York")).plusDays(1).toString()); }
    public void setStatus(String id, String status) {
        requireStatus(status);
        if (IN_PROGRESS.equals(status)) { doNow(id); return; }
        if (DONE.equals(status)) { complete(id); return; }
        changeStatus(id, status, "");
    }
    private void changeStatus(String id, String status, String plannedDate) {
        synchronized (LOCK) {
            List<Task> tasks = read();
            for (int i = 0; i < tasks.size(); i++) {
                Task old = tasks.get(i);
                if (old.id.equals(id)) {
                    tasks.set(i, old.withStatus(status, plannedDate)); write(tasks); break;
                }
            }
        }
        refreshWidgets();
    }
    public void toggle(String id) {
        synchronized (LOCK) {
            List<Task> tasks = read();
            for (int i = 0; i < tasks.size(); i++) {
                Task old = tasks.get(i);
                if (old.id.equals(id)) {
                    tasks.set(i, old.withStatus(old.done ? NEXT : DONE, old.plannedDate)); write(tasks); break;
                }
            }
        }
        refreshWidgets();
    }
    /** Completion is atomic and idempotent, including stale widget taps. */
    public void complete(String id) {
        synchronized (LOCK) {
            List<Task> tasks = read();
            for (int i = 0; i < tasks.size(); i++) {
                Task old = tasks.get(i);
                if (old.id.equals(id) && !old.done) {
                    tasks.set(i, old.withStatus(DONE, old.plannedDate)); write(tasks); break;
                }
            }
        }
        refreshWidgets();
    }
    /** Apply the UI's staged checkboxes as one validated, persistent change. */
    public void setCompleted(Map<String, Boolean> changes) {
        if (changes == null) throw new IllegalArgumentException("Choose valid task completion changes.");
        for (Map.Entry<String, Boolean> change : changes.entrySet()) {
            if (change.getKey() == null || change.getKey().trim().isEmpty() || change.getValue() == null)
                throw new IllegalArgumentException("Choose valid task completion changes.");
        }
        synchronized (LOCK) {
            List<Task> tasks = read();
            boolean changed = false;
            for (int i = 0; i < tasks.size(); i++) {
                Task task = tasks.get(i);
                Boolean value = changes.get(task.id);
                if (value != null && value != task.done) {
                    tasks.set(i, task.withStatus(value ? DONE : NEXT, task.plannedDate)); changed = true;
                }
            }
            if (changed) write(tasks);
        }
        refreshWidgets();
    }
    public void delete(String id) {
        synchronized (LOCK) {
            List<Task> tasks = read();
            if (tasks.removeIf(task -> task.id.equals(id))) write(tasks);
        }
        refreshWidgets();
    }

    /** Replace the WebView's widget mirror without altering older native task records. */
    public void replaceReferenceTasks(String arrayJson) {
        List<Task> referenceTasks = referenceTasks(arrayJson);
        synchronized (LOCK) {
            List<Task> tasks = read();
            tasks.removeIf(task -> task.id.startsWith("html:"));
            tasks.addAll(referenceTasks);
            write(tasks);
        }
        refreshWidgets();
    }

    static void validateReferenceTasks(String arrayJson) { referenceTasks(arrayJson); }

    private static List<Task> referenceTasks(String arrayJson) {
        try {
            List<Task> tasks = parseTasks(arrayJson);
            for (Task task : tasks) if (!task.id.startsWith("html:") || task.id.length() == 5)
                throw new IllegalArgumentException("Reference tasks need an html: identity.");
            return tasks;
        } catch (JSONException | RuntimeException error) {
            throw new IllegalArgumentException("The widget task snapshot is invalid.", error);
        }
    }

    /** Explicit scheduling overrides deadlines; Waiting and Someday stay out of Today. */
    public static boolean isForToday(Task task, LocalDate today) {
        if (task.done || WAITING.equals(task.status) || SOMEDAY.equals(task.status)) return false;
        if (IN_PROGRESS.equals(task.status)) return true;
        String date = task.plannedDate.isEmpty() ? task.dueDate : task.plannedDate;
        return !date.isEmpty() && !LocalDate.parse(date).isAfter(today);
    }
    private List<Task> read() {
        try {
            return parseTasks(prefs.getString("items", "[]"));
        } catch (JSONException | RuntimeException error) {
            // Never replace damaged storage with an empty list as a side effect of editing.
            throw new IllegalStateException("Your task data could not be read. Restart the app and try again.", error);
        }
    }

    private static List<Task> parseTasks(String json) throws JSONException {
            List<Task> tasks = new ArrayList<>();
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                String id = stringField(item, "id", null), title = stringField(item, "title", null);
                String list = stringField(item, "listName", DEFAULT_LIST), due = stringField(item, "dueDate", "");
                boolean done = booleanField(item, "done", false);
                long created = integerField(item, "createdAt", 0);
                // Legacy records had only a done boolean. Migration stays in memory until a write.
                String status = stringField(item, "status", done ? DONE : NEXT);
                if (item.has("status") && item.has("done") && done != DONE.equals(status))
                    throw new JSONException("Task status and completion do not agree.");
                String planned = stringField(item, "plannedDate", ""), priority = normalizePriority(stringField(item, "priority", "LOW"));
                if(item.optBoolean("urgent"))priority="HIGH";
                long minutes = integerField(item, "minutes", 0);
                if (minutes < 0 || minutes > Integer.MAX_VALUE) throw new JSONException("Invalid task minutes.");
                List<String> labels = new ArrayList<>();
                if (item.has("labels")) {
                    Object value = item.get("labels");
                    if (!(value instanceof JSONArray)) throw new JSONException("Task labels must be a list.");
                    JSONArray values = (JSONArray) value;
                    for (int j = 0; j < values.length(); j++) {
                        Object label = values.get(j);
                        if (!(label instanceof String)) throw new JSONException("A context label must be text.");
                        labels.add((String) label);
                    }
                }
                validateInput(title, due, status, planned, priority, (int) minutes);
                if (id.trim().isEmpty() || created < 0) throw new JSONException("Invalid task identity.");
                tasks.add(new Task(id, title.trim(), listName(list), due, created, normalizeLabels(labels),
                    status, planned, priority, (int) minutes, item.toString()));
            }
            validateTasks(tasks);
        return tasks;
    }
    private void write(List<Task> tasks) {
        validateTasks(tasks);
        JSONArray array = new JSONArray();
        try { for (Task task : tasks) array.put(task.json()); }
        catch (JSONException error) { throw new IllegalStateException("The task could not be saved.", error); }
        if (!prefs.edit().putString("items", array.toString()).commit())
            throw new IllegalStateException("Your task could not be saved. Check your device storage.");
    }
    private void refreshWidgets() {
        // A launcher failure must not report a successfully saved task as a failed write.
        try { TaskWidgetProvider.refreshAll(context); }
        catch (RuntimeException launcherUnavailable) { /* The widget's periodic refresh retries. */ }
    }
    private static void validateTasks(List<Task> tasks) {
        Set<String> ids = new HashSet<>();
        for (Task task : tasks) {
            validateInput(task.title, task.dueDate, task.status, task.plannedDate, task.priority, task.minutes);
            if (task.id == null || task.id.trim().isEmpty() || !ids.add(task.id) || task.createdAt < 0)
                throw new IllegalArgumentException("Every task needs a unique valid identity.");
            normalizeLabels(task.labels);
            if (task.done != DONE.equals(task.status)) throw new IllegalArgumentException("Task status is inconsistent.");

        }
    }
    private static void validateInput(String title, String due, String status, String planned, String priority, int minutes) {
        requireTitle(title); requireDate(due); requireDate(planned); requireStatus(status);
        if (!PRIORITIES.contains(normalizePriority(priority))) throw new IllegalArgumentException("Choose a valid priority.");
        if (minutes < 0) throw new IllegalArgumentException("Minutes cannot be negative.");
    }
    private static List<String> normalizeLabels(List<String> labels) {
        if (labels == null) throw new IllegalArgumentException("Choose valid context labels.");
        LinkedHashSet<String> values = new LinkedHashSet<>();
        for (String label : labels) {
            if (label == null) throw new IllegalArgumentException("A context label must be text.");
            if (!label.trim().isEmpty()) values.add(label.trim());
        }
        return new ArrayList<>(values);
    }
    private static String stringField(JSONObject item, String key, String fallback) throws JSONException {
        if (!item.has(key)) { if (fallback == null) throw new JSONException("Missing task field: " + key); return fallback; }
        Object value = item.get(key);
        if (!(value instanceof String)) throw new JSONException("Task field must be text: " + key);
        return (String) value;
    }
    private static boolean booleanField(JSONObject item, String key, boolean fallback) throws JSONException {
        if (!item.has(key)) return fallback;
        Object value = item.get(key);
        if (!(value instanceof Boolean)) throw new JSONException("Task completion must be true or false.");
        return (Boolean) value;
    }
    private static long integerField(JSONObject item, String key, long fallback) throws JSONException {
        if (!item.has(key)) return fallback;
        Object value = item.get(key);
        if (!(value instanceof Integer) && !(value instanceof Long)) throw new JSONException("Task field must be an integer: " + key);
        return ((Number) value).longValue();
    }
    public static boolean usesGuildImage(Context context,String label){
        try{JSONObject icons=new JSONObject(new ReferenceStore(context).readState()).optJSONObject("contextIcons");if(icons!=null&&icons.has(label))return "image:guild".equals(icons.optString(label));}catch(JSONException|RuntimeException unavailable){}
        return "Guild".equals(label);
    }
    private static int contextSymbolResource(Context context,String label){
        try{JSONObject icons=new JSONObject(new ReferenceStore(context).readState()).optJSONObject("contextIcons");if(icons!=null&&!icons.optString(label).isEmpty())return 0;}catch(JSONException|RuntimeException unavailable){}
        switch(label){case "At home":return R.drawable.context_home;case "On the road":return R.drawable.context_car;case "On the phone":return R.drawable.context_phone;case "On the computer":return R.drawable.context_computer;case "Outdoors":return R.drawable.context_tree;default:return 0;}
    }
    public static CharSequence contextDisplay(Context context,String label){
        boolean guild=usesGuildImage(context,label);int resource=guild?R.drawable.guild_label:contextSymbolResource(context,label);if(resource==0)return contextEmoji(context,label);
        android.graphics.drawable.Drawable icon=context.getDrawable(resource);int size=Math.round((guild?20:24)*context.getResources().getDisplayMetrics().density);icon.setBounds(0,0,size,size);
        android.text.SpannableString text=new android.text.SpannableString(label);text.setSpan(new CenteredIconSpan(icon),0,text.length(),android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);return text;
    }
    public static String contextEmoji(Context context,String label) {
        try{JSONObject icons=new JSONObject(new ReferenceStore(context).readState()).optJSONObject("contextIcons");if(icons!=null&&!icons.optString(label).isEmpty())return "image:guild".equals(icons.getString(label))?label:icons.getString(label);}catch(JSONException|RuntimeException unavailable){/* Keep the saved label identity when an icon cannot be read. */}
        return contextEmoji(label);
    }
    public static String contextEmoji(String label) {
        switch(label){
            case "At home":return "🏠";case "On the road":return "🚗";
            case "On the phone":return "📱";case "On the computer":return "💻";
            case "Outdoors":return "🌳";default:return label;
        }
    }
    private static String listName(String list) { return list == null || list.trim().isEmpty() ? DEFAULT_LIST : list.trim(); }
    private static void requireTitle(String title) {
        if (title == null || title.trim().isEmpty()) throw new IllegalArgumentException("Enter a task title.");
    }
    private static void requireStatus(String status) {
        if (!STATUSES.contains(status)) throw new IllegalArgumentException("Choose a valid task status.");
    }
    private static void requireDate(String date) {
        if (date == null) throw new IllegalArgumentException("Choose a valid date.");
        if (!date.isEmpty()) LocalDate.parse(date);
    }
}
