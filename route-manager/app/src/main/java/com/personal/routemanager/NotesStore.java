package com.personal.routemanager;
import android.content.Context;
import android.content.SharedPreferences;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.json.*;
/** This app owns one atomic, offline store, separate from Tasks. */
final class NotesStore {
    private final SharedPreferences prefs;
    NotesStore(Context context){prefs=context.getSharedPreferences("route_manager",Context.MODE_PRIVATE);}
    synchronized String read(){String raw=prefs.getString("state",null);if(raw==null)return "{}";validate(raw);return raw;}
    synchronized void write(String raw){validate(raw);if(!prefs.edit().putString("state",raw).commit())throw new IllegalStateException("Could not save notes");}
    static void validate(String raw){
        if(raw==null||raw.getBytes(StandardCharsets.UTF_8).length>2*1024*1024)throw new IllegalArgumentException("Invalid notes size");
        try{
            JSONObject data=new JSONObject(raw);if(data.getInt("version")!=1)throw new JSONException("Unknown notes version");
            if(data.has("routeOffset")){Object offset=data.get("routeOffset");if(!(offset instanceof Integer)||((Integer)offset)<0||((Integer)offset)>2)throw new JSONException("Invalid route offset");}JSONObject routes=data.getJSONObject("routes");if(routes.length()!=5)throw new JSONException("Five routes required");
            for(String route:new String[]{"g2","g3","g4","g5","g6"}){
                JSONObject page=routes.getJSONObject(route);JSONArray stops=page.getJSONArray("stops"),notes=page.getJSONArray("notes");Set<String> ids=new HashSet<>(),names=new HashSet<>(),noteIds=new HashSet<>();
                for(int i=0;i<stops.length();i++){JSONObject stop=stops.getJSONObject(i);String id=text(stop,"id",160),name=text(stop,"name",120),cadence=stop.getString("cadence"),account="";if(stop.has("accountNumber")){Object rawAccount=stop.get("accountNumber");if(!(rawAccount instanceof String)||((String)rawAccount).length()>80||((String)rawAccount).chars().anyMatch(ch->ch==0||ch==10||ch==13))throw new JSONException("Invalid account number");account=((String)rawAccount).trim().replaceAll("\s+"," ");}if(!ids.add(id)||!names.add(new JSONArray().put(name.toLowerCase(Locale.ROOT)).put(account.toLowerCase(Locale.ROOT)).toString())||!Arrays.asList("weekly","biweekly","monthly").contains(cadence))throw new JSONException("Invalid stop");}
                for(int i=0;i<notes.length();i++){JSONObject note=notes.getJSONObject(i);if(!noteIds.add(text(note,"id",160)))throw new JSONException("Duplicate note");text(note,"body",20000);if(note.getLong("createdAt")<=0||note.getLong("updatedAt")<=0)throw new JSONException("Invalid note date");JSONArray links=note.getJSONArray("stops");Set<String> selected=new HashSet<>();for(int j=0;j<links.length();j++){String id=links.getString(j);if(!ids.contains(id)||!selected.add(id))throw new JSONException("Unknown stop");}}
            }
            if(!Arrays.asList("g2","g3","g4","g5","g6").contains(data.getString("current")))throw new JSONException("Invalid current route");
            JSONObject appearance=data.getJSONObject("appearance");for(String key:new String[]{"top","bottom","text","accent"})if(!appearance.getString(key).matches("#[0-9A-Fa-f]{6}"))throw new JSONException("Invalid color");
            if(!(appearance.get("gradient") instanceof Boolean)||!Arrays.asList("small","normal","large").contains(appearance.getString("size")))throw new JSONException("Invalid appearance");
        }catch(JSONException|RuntimeException invalid){throw new IllegalArgumentException("Saved notes are invalid",invalid);}
    }
    private static String text(JSONObject object,String key,int max)throws JSONException{Object value=object.get(key);if(!(value instanceof String))throw new JSONException("Invalid text");String text=((String)value).trim();if(text.isEmpty()||text.length()>max)throw new JSONException("Invalid text length");return text;}
}
