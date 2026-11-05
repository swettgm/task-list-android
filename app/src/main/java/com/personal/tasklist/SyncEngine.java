package com.personal.tasklist;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/** Small foreground-only LAN sync client. Task data remains local when its PC is unavailable. */
public final class SyncEngine {
    static final int MAX_MESSAGE = 2 * 1024 * 1024;
    interface Transport { JSONObject request(String endpoint, String token, String path, JSONObject body) throws Exception; }
    private final SharedPreferences prefs;
    private final ReferenceStore reference;
    private final Context appContext;
    private final Runnable changed;
    private final Transport transport;
    private final Handler ui = new Handler(Looper.getMainLooper());
    private final Object lock = new Object();
    private final AtomicBoolean queued = new AtomicBoolean();
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Tasks LAN sync"); thread.setDaemon(true); return thread;
    });
    private ScheduledFuture<?> periodic;
    private boolean running, closed;
    private long generation;
    private boolean connected;
    private String message = "Not linked to a PC";

    public SyncEngine(Context context, Runnable changed) { this(context, changed, SyncEngine::request); }
    SyncEngine(Context context, Runnable changed, Transport transport) {
        Context app = context.getApplicationContext();
        appContext=app;
        prefs = app.getSharedPreferences("desktop_sync", Context.MODE_PRIVATE);
        reference = new ReferenceStore(app); this.changed = changed; this.transport = transport;
        if (paired()) message = "Open Tasks on your PC to connect";
    }

    public void start() {
        synchronized (lock) {
            if (closed || running) return;
            running = true;
            periodic = executor.scheduleWithFixedDelay(this::runOneSync, 0, 5, TimeUnit.SECONDS);
        }
    }
    public void stop() {
        synchronized (lock) { running = false; if (periodic != null) { periodic.cancel(false); periodic = null; } }
    }
    public void close() {
        synchronized (lock) { closed = true; running = false; generation++; if (periodic != null) periodic.cancel(false); }
        executor.shutdownNow(); ui.removeCallbacksAndMessages(null);
    }
    private boolean paired() { return prefs.contains("url") && prefs.contains("token"); }

    public void pair(String endpoint, String token) {
        String normalized = validateEndpoint(endpoint), secret = validateToken(token);
        synchronized (lock) {
            if (closed) return;
            if (!prefs.edit().putString("url", normalized).putString("token", secret).putString("base", "{}")
                    .remove("lastSyncAt").commit()) throw new IllegalStateException("The PC connection could not be saved.");
            generation++; connected = false; message = "Connecting to your PC…";
        }
        requestSync();
    }
    public void unpair() {
        synchronized (lock) {
            generation++; connected = false; message = "Not linked to a PC";
            if (!prefs.edit().clear().commit()) throw new IllegalStateException("The PC connection could not be removed.");
        }
    }
    public void requestSync() {
        synchronized (lock) { if (closed || !running || !paired() || !queued.compareAndSet(false, true)) return; }
        try { executor.execute(() -> { queued.set(false); runOneSync(); }); }
        catch (java.util.concurrent.RejectedExecutionException stopped) { queued.set(false); }
    }
    public String status() {
        synchronized (lock) {
            try { return new JSONObject().put("paired", paired()).put("connected", connected)
                    .put("message", message).put("url", prefs.getString("url", ""))
                    .put("lastSyncAt", prefs.getString("lastSyncAt", "")).toString(); }
            catch (Exception invalid) { return "{\"paired\":false,\"connected\":false,\"message\":\"Sync status unavailable\"}"; }
        }
    }

    void runOneSync() {
        String endpoint, secret, baseText; long attempt;
        synchronized (lock) {
            if (closed || !paired()) return;
            endpoint = prefs.getString("url", ""); secret = prefs.getString("token", "");
            baseText = prefs.getString("base", "{}"); attempt = generation;
        }
        try {
            validateEndpoint(endpoint); validateToken(secret);
            JSONObject sent = new JSONObject(reference.readState());
            JSONObject response = transport.request(endpoint, secret, "/api/sync",
                    new JSONObject().put("base", new JSONObject(baseText)).put("state", sent));
            JSONObject remote = response.getJSONObject("state");
            validateState(remote);
            String remoteText = remote.toString();
            if (remoteText.getBytes(StandardCharsets.UTF_8).length > MAX_MESSAGE) throw new IOException("State too large");
            boolean updated = false;
            synchronized (lock) {
                if (closed || attempt != generation) return;
                // A widget or form may save after the request was sent. Merge those edits,
                // then CAS under ReferenceStore's own lock so the last edit cannot be lost.
                boolean applied = false;
                for (int retry = 0; retry < 16; retry++) {
                    long revision = reference.revision();
                    JSONObject current = new JSONObject(reference.readState());
                    JSONObject merged = StateMerge.merge(sent, current, remote);
                    if (StateMerge.same(current, merged)) { applied = true; break; }
                    if (reference.compareAndWriteState(revision, merged.toString())) { applied = true; updated = true; break; }
                }
                if (!applied) throw new IOException("Task list changed during sync");
                if (!prefs.edit().putString("base", remoteText).putString("lastSyncAt", Instant.now().toString()).commit())
                    throw new IOException("Sync status could not be saved");
                connected = true; message = "Connected to your PC";
            }
            if (updated) { TaskReminders.reschedule(appContext); TaskWidgetProvider.refreshAll(appContext); ui.post(changed); }
        } catch (Exception unavailable) {
            synchronized (lock) {
                if (closed || attempt != generation) return;
                connected = false;
                message = unavailable instanceof PairingRejectedException ? "Pairing code did not match. Pair this PC again."
                        : "PC unavailable. Keep Tasks open on your PC on the same Wi-Fi.";
            }
        }
    }

    public static String validateEndpoint(String value) {
        try {
            URI uri = new URI(value == null ? "" : value.trim());
            String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.ROOT);
            if ((!scheme.equals("http") && !scheme.equals("https")) || uri.getRawUserInfo() != null
                    || uri.getRawQuery() != null || uri.getRawFragment() != null || uri.getPort() < 1 || uri.getPort() > 65535
                    || (uri.getRawPath() != null && !uri.getRawPath().isEmpty() && !uri.getRawPath().equals("/")))
                throw new IllegalArgumentException();
            String host = uri.getHost();
            if (host == null || !privateHost(host)) throw new IllegalArgumentException();
            return new URI(scheme, null, host, uri.getPort(), null, null, null).toASCIIString();
        } catch (Exception invalid) { throw new IllegalArgumentException("Use the local PC address shown in Tasks, including its port."); }
    }
    private static boolean privateHost(String host) throws Exception {
        if (host.equalsIgnoreCase("localhost")) return true;
        if (host.startsWith("[") && host.endsWith("]")) host = host.substring(1, host.length() - 1);
        if (host.indexOf(':') >= 0) {
            if (host.contains("%")) return false;
            byte[] bytes = InetAddress.getByName(host).getAddress();
            return bytes.length == 16 && (((bytes[0] & 0xfe) == 0xfc) || InetAddress.getByAddress(bytes).isLoopbackAddress());
        }
        String[] parts = host.split("\\.", -1);
        if (parts.length != 4) return false;
        int[] numbers = new int[4];
        for (int i = 0; i < 4; i++) {
            if (!parts[i].matches("0|[1-9][0-9]{0,2}")) return false;
            numbers[i] = Integer.parseInt(parts[i]); if (numbers[i] > 255) return false;
        }
        return numbers[0] == 10 || numbers[0] == 127 || (numbers[0] == 192 && numbers[1] == 168)
                || (numbers[0] == 172 && numbers[1] >= 16 && numbers[1] <= 31);
    }
    public static String validateToken(String token) {
        if (token == null || !token.matches("[0-9a-fA-F]{64}")) throw new IllegalArgumentException("Use the pairing code shown on your PC.");
        return token.toLowerCase(Locale.ROOT);
    }

    private static void validateState(JSONObject state) throws Exception {
        for (String field : new String[] {"completed", "completedOn", "movedTo", "rolloverCompletedOn", "taskOverrides", "manualLinks"}) {
            if (state.has(field) && !(state.get(field) instanceof JSONObject)) throw new IOException("Invalid task state");
        }
        if (state.has("completed")) {
            JSONObject values = state.getJSONObject("completed");
            java.util.Iterator<String> keys = values.keys();
            while (keys.hasNext()) if (!(values.get(keys.next()) instanceof Boolean)) throw new IOException("Invalid completion state");
        }
        if (state.has("customTasks")) {
            org.json.JSONArray tasks = state.getJSONArray("customTasks");
            java.util.Set<String> ids = new java.util.HashSet<>();
            for (int i = 0; i < tasks.length(); i++) {
                JSONObject task = tasks.getJSONObject(i);
                Object id = task.get("id"), title = task.get("title");
                if (!(id instanceof String) || ((String) id).isEmpty() || !ids.add((String) id)
                        || !(title instanceof String) || ((String) title).trim().isEmpty()) throw new IOException("Invalid custom task");
            }
        }
        if (state.has("inProgress") && state.get("inProgress") != JSONObject.NULL && !(state.get("inProgress") instanceof String))
            throw new IOException("Invalid active task");
    }

    private static JSONObject request(String endpoint, String token, String path, JSONObject body) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(endpoint + path).openConnection();
        connection.setInstanceFollowRedirects(false); connection.setConnectTimeout(4000); connection.setReadTimeout(5000);
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("X-Tasks-Client", "android");
        try {
            if (body != null) {
                byte[] data = body.toString().getBytes(StandardCharsets.UTF_8);
                if (data.length > MAX_MESSAGE) throw new IOException("Sync payload too large");
                connection.setRequestMethod("POST"); connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                connection.setFixedLengthStreamingMode(data.length);
                try (OutputStream output = connection.getOutputStream()) { output.write(data); }
            }
            int code = connection.getResponseCode();
            if (code == 401 || code == 403) throw new PairingRejectedException();
            if (code != 200) throw new IOException("PC returned HTTP " + code);
            try (InputStream input = connection.getInputStream(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192]; int count, total = 0;
                while ((count = input.read(buffer)) != -1) {
                    total += count; if (total > MAX_MESSAGE) throw new IOException("Sync response too large");
                    output.write(buffer, 0, count);
                }
                return new JSONObject(output.toString(StandardCharsets.UTF_8.name()));
            }
        } finally { connection.disconnect(); }
    }
    private static final class PairingRejectedException extends IOException { }
}
