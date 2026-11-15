package com.personal.tasklist;
import org.json.*;
import java.util.*;

/** Per-device registers: immutable phone seed, compact changes and tombstones. */
final class CloudState {
 static final long MAX_CLOCK=9007199254740991L;
 static JSONObject copy(JSONObject v)throws JSONException{return new JSONObject(v.toString());}
 static JSONObject normalized(JSONObject input)throws JSONException{
  JSONObject out=copy(input);out.remove("uiContext");out.remove("updatedAt");out.remove("inProgress");
  JSONArray tasks=out.optJSONArray("customTasks");if(tasks!=null){JSONObject map=new JSONObject();for(int i=0;i<tasks.length();i++){JSONObject t=tasks.getJSONObject(i);map.put(t.getString("id"),t);}out.put("customTasks",map);}
  JSONArray active=out.optJSONArray("inProgressTasks");if(active!=null)out.put("inProgressTasks",members(active));
  JSONObject links=out.optJSONObject("manualLinks");if(links!=null){JSONObject map=new JSONObject();for(String k:keys(links)){map.put(k,members(links.getJSONArray(k)));}out.put("manualLinks",map);}
  return out;
 }
 static JSONObject members(JSONArray array)throws JSONException{JSONObject out=new JSONObject();for(int i=0;i<array.length();i++)out.put(array.getString(i),true);return out;}
 static List<String> keys(JSONObject value){List<String> out=new ArrayList<>();value.keys().forEachRemaining(out::add);Collections.sort(out);return out;}
 static JSONObject restored(JSONObject state)throws JSONException{
  JSONObject out=copy(state),tasks=out.optJSONObject("customTasks");if(tasks!=null){JSONArray list=new JSONArray();for(String key:keys(tasks))list.put(tasks.getJSONObject(key));out.put("customTasks",list);}
  JSONObject links=out.optJSONObject("manualLinks");if(links!=null){JSONObject map=new JSONObject();for(String key:keys(links)){JSONArray list=new JSONArray();JSONObject peers=links.getJSONObject(key);for(String peer:keys(peers))if(peers.optBoolean(peer))list.put(peer);map.put(key,list);}out.put("manualLinks",map);}
  JSONObject active=out.optJSONObject("inProgressTasks");if(active!=null){JSONArray list=new JSONArray();for(String key:keys(active))if(active.optBoolean(key))list.put(key);out.put("inProgressTasks",list).put("inProgress",list.length()>0?list.getString(list.length()-1):JSONObject.NULL);}
  return out.put("cloudManaged",true);
 }
 static String escape(String v){return v.replace("~","~0").replace("/","~1");}
 static String[] segments(String path){
  if(path==null||!path.startsWith("/")||path.length()>4000)throw new IllegalArgumentException("Invalid cloud path");
  String[] parts=path.substring(1).split("/",-1);if(parts.length>32)throw new IllegalArgumentException("Invalid cloud path");
  for(int i=0;i<parts.length;i++){parts[i]=parts[i].replace("~1","/").replace("~0","~");if(Arrays.asList("__proto__","prototype","constructor").contains(parts[i]))throw new IllegalArgumentException("Invalid cloud path");}return parts;
 }
 static void diff(Object before,Object after,String path,JSONObject patch)throws JSONException{
  if(after instanceof JSONObject){JSONObject next=(JSONObject)after,old=before instanceof JSONObject?(JSONObject)before:new JSONObject();TreeSet<String> names=new TreeSet<>(keys(old));names.addAll(keys(next));
   for(String key:names){String child=path+"/"+escape(key);if(!next.has(key))patch.put(child,new JSONObject().put("deleted",true));else if(!old.has(key)||!StateMerge.same(old.opt(key),next.opt(key)))diff(old.opt(key),next.opt(key),child,patch);}
  }else if(!StateMerge.same(before,after))patch.put(path,new JSONObject().put("value",after));
 }
 static JSONObject capture(JSONObject journal,JSONObject state)throws JSONException{
  JSONObject result=copy(journal),patch=new JSONObject();diff(normalized(result.optJSONObject("localHead")==null?new JSONObject():result.getJSONObject("localHead")),normalized(state),"",patch);
  if(patch.length()>0){long clock=result.optLong("counter")+1;if(clock>MAX_CLOCK)throw new IllegalArgumentException("Cloud clock overflow");JSONObject regs=result.optJSONObject("registers");if(regs==null)regs=new JSONObject();for(String key:keys(patch))regs.put(key,patch.getJSONObject(key).put("clock",clock));result.put("counter",clock).put("registers",regs);}
  return result.put("localHead",state);
 }
 static final class Winner {final String owner,path;final long clock;final JSONObject op;Winner(String o,String p,long c,JSONObject v){owner=o;path=p;clock=c;op=v;}}
 static final Comparator<Winner> ORDER=Comparator.comparingLong((Winner v)->v.clock).thenComparing(v->v.owner).thenComparing(v->v.path);
 static final class Result {final JSONObject state;final long counter;Result(JSONObject s,long c){state=s;counter=c;}}
 static Result resolve(JSONObject seed,List<JSONObject> documents)throws JSONException{
  JSONObject state=normalized(seed);Map<String,Winner> winners=new HashMap<>();long maximum=0;
  for(JSONObject doc:documents){String owner=doc.getString("owner");JSONObject regs=doc.getJSONObject("registers");if(owner.length()>80||regs.length()>20000)throw new IllegalArgumentException("Invalid cloud document");
   for(String path:keys(regs)){segments(path);JSONObject op=regs.getJSONObject(path);Object rawClock=op.get("clock");if(!(rawClock instanceof Integer||rawClock instanceof Long))throw new IllegalArgumentException("Invalid cloud clock");long clock=op.getLong("clock");if(clock<=0||clock>MAX_CLOCK||!(op.optBoolean("deleted")||op.has("value")))throw new IllegalArgumentException("Invalid cloud change");maximum=Math.max(maximum,clock);Winner candidate=new Winner(owner,path,clock,op),old=winners.get(path);if(old==null||ORDER.compare(candidate,old)>0)winners.put(path,candidate);}
  }
  List<Winner> changes=new ArrayList<>(winners.values());changes.sort(ORDER);
  for(Winner change:changes){String[] parts=segments(change.path);JSONObject target=state;for(int i=0;i<parts.length-1;i++){JSONObject child=target.optJSONObject(parts[i]);if(child==null){child=new JSONObject();target.put(parts[i],child);}target=child;}if(change.op.optBoolean("deleted"))target.remove(parts[parts.length-1]);else target.put(parts[parts.length-1],change.op.get("value"));}
  return new Result(restored(state),maximum);
 }
}
