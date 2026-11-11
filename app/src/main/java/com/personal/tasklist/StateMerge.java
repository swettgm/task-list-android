package com.personal.tasklist;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/** Three-way field merge: apply only local changes made since base onto the remote state. */
public final class StateMerge {
    private static final Object MISSING = new Object();
    private StateMerge() { }

    public static JSONObject merge(JSONObject base, JSONObject local, JSONObject remote) {
        try {
            JSONObject result = mergeObject(base, local, remote, true, false);
            // Views are independent on the phone and PC. Timestamps are not task edits.
            for (String key : new String[] {"uiContext", "updatedAt"}) {
                if (local.has(key)) result.put(key, copy(local.get(key)));
                else result.remove(key);
            }
            if(result.optJSONArray("inProgressTasks")!=null){JSONArray active=result.getJSONArray("inProgressTasks");result.put("inProgress",active.length()>0?active.getString(active.length()-1):JSONObject.NULL);}
            return result;
        } catch (JSONException invalid) { throw new IllegalArgumentException("Task state could not be merged.", invalid); }
    }

    private static JSONObject mergeObject(JSONObject base, JSONObject local, JSONObject remote, boolean root, boolean linkMap) throws JSONException {
        JSONObject result = new JSONObject(remote.toString());
        Set<String> keys = keys(base, local);
        for (String key : keys) {
            if (root && (key.equals("uiContext") || key.equals("updatedAt"))) continue;
            Object before = value(base, key), after = value(local, key), current = value(remote, key);
            if (same(before, after)) continue;
            if (after == MISSING) { result.remove(key); continue; }
            if (after instanceof JSONObject) {
                result.put(key, mergeObject(before instanceof JSONObject ? (JSONObject) before : new JSONObject(),
                        (JSONObject) after, current instanceof JSONObject ? (JSONObject) current : new JSONObject(), false,
                        root && key.equals("manualLinks")));
            } else if (root && key.equals("customTasks") && after instanceof JSONArray) {
                result.put(key, mergeTasks(before instanceof JSONArray ? (JSONArray) before : new JSONArray(),
                        (JSONArray) after, current instanceof JSONArray ? (JSONArray) current : new JSONArray()));
            } else if ((linkMap || root && key.equals("inProgressTasks")) && after instanceof JSONArray) {
                result.put(key, mergeLinkPeers(before instanceof JSONArray ? (JSONArray) before : new JSONArray(),
                        (JSONArray) after, current instanceof JSONArray ? (JSONArray) current : new JSONArray()));
            } else result.put(key, copy(after));
        }
        return result;
    }

    private static JSONArray mergeTasks(JSONArray base, JSONArray local, JSONArray remote) throws JSONException {
        Map<String, JSONObject> before = tasks(base), after = tasks(local), result = tasks(remote);
        Set<String> ids = new LinkedHashSet<>(before.keySet()); ids.addAll(after.keySet());
        for (String id : ids) {
            JSONObject old = before.get(id), changed = after.get(id);
            if (same(old == null ? MISSING : old, changed == null ? MISSING : changed)) continue;
            if (changed == null) result.remove(id);
            else result.put(id, mergeObject(old == null ? new JSONObject() : old, changed,
                    result.containsKey(id) ? result.get(id) : new JSONObject(), false, false));
        }
        JSONArray array = new JSONArray();
        for (JSONObject task : result.values()) array.put(task);
        return array;
    }

    private static JSONArray mergeLinkPeers(JSONArray base, JSONArray local, JSONArray remote) throws JSONException {
        Set<String> before = strings(base), after = strings(local), current = strings(remote);
        Set<String> removed = new LinkedHashSet<>(before); removed.removeAll(after);
        current.removeAll(removed);
        for (String peer : after) if (!before.contains(peer)) current.add(peer);
        JSONArray result = new JSONArray();
        for (String peer : current) result.put(peer);
        return result;
    }

    private static Set<String> strings(JSONArray values) throws JSONException {
        Set<String> result = new LinkedHashSet<>();
        for (int i = 0; i < values.length(); i++) {
            Object value = values.get(i);
            if (!(value instanceof String)) throw new JSONException("A linked task identity must be text.");
            result.add((String) value);
        }
        return result;
    }

    private static Map<String, JSONObject> tasks(JSONArray array) throws JSONException {
        Map<String, JSONObject> result = new LinkedHashMap<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject task = array.getJSONObject(i);
            String id = task.getString("id");
            if (id.isEmpty() || result.containsKey(id)) throw new JSONException("Custom task identities must be unique.");
            result.put(id, new JSONObject(task.toString()));
        }
        return result;
    }

    public static boolean same(Object left, Object right) {
        if (left == right) return true;
        if (left == MISSING || right == MISSING || left == null || right == null) return false;
        if (left instanceof JSONObject && right instanceof JSONObject) {
            JSONObject a = (JSONObject) left, b = (JSONObject) right;
            if (a.length() != b.length()) return false;
            Iterator<String> names = a.keys();
            while (names.hasNext()) { String key = names.next(); if (!b.has(key) || !same(a.opt(key), b.opt(key))) return false; }
            return true;
        }
        if (left instanceof JSONArray && right instanceof JSONArray) {
            JSONArray a = (JSONArray) left, b = (JSONArray) right;
            if (a.length() != b.length()) return false;
            for (int i = 0; i < a.length(); i++) if (!same(a.opt(i), b.opt(i))) return false;
            return true;
        }
        if (left instanceof Number && right instanceof Number)
            return Double.compare(((Number) left).doubleValue(), ((Number) right).doubleValue()) == 0;
        return left.equals(right);
    }

    private static Object value(JSONObject object, String key) throws JSONException { return object.has(key) ? object.get(key) : MISSING; }
    private static Object copy(Object value) throws JSONException {
        if (value instanceof JSONObject) return new JSONObject(value.toString());
        if (value instanceof JSONArray) return new JSONArray(value.toString());
        return value;
    }
    private static Set<String> keys(JSONObject a, JSONObject b) {
        Set<String> keys = new LinkedHashSet<>();
        Iterator<String> first = a.keys(), second = b.keys();
        while (first.hasNext()) keys.add(first.next());
        while (second.hasNext()) keys.add(second.next());
        return keys;
    }
}
