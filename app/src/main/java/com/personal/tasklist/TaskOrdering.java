package com.personal.tasklist;
import org.json.*;
import java.time.Instant;

/** Shared widget ordering: priority, urgency, saved manual order, then actual creation time. */
final class TaskOrdering {
    static String identity(TaskStore.Task task){String key=task.sourceRecord().optString("progressKey");if(key.isEmpty()&&task.id.startsWith("html:"))key=task.id.substring(5);int end=key.lastIndexOf(':');return end>0?key.substring(0,end):task.id;}
    static long legacyCreation(String id){try{if(!id.matches("app-[0-9a-z]+-[0-9a-z]+"))return 0;long value=Long.parseLong(id.split("-")[1],36);return value>=946684800000L&&value<=System.currentTimeMillis()+86400000L?value:0;}catch(RuntimeException invalid){return 0;}}
    static long creation(JSONObject definition){Object value=definition.opt("createdAt");try{if(value instanceof Number)return Math.max(0,((Number)value).longValue());if(value instanceof String)return Math.max(0,Instant.parse((String)value).toEpochMilli());}catch(RuntimeException invalid){}return legacyCreation(definition.optString("id"));}
    private static int rank(JSONObject state,String priority,String id){JSONObject order=state.optJSONObject("taskOrder");JSONArray values=order==null?null:order.optJSONArray(priority);if(values!=null)for(int i=0;i<values.length();i++)if(id.equals(values.optString(i)))return i;return Integer.MAX_VALUE;}
    private static long creationFor(TaskStore.Task task,JSONObject source,JSONObject state){
        if(source.has("taskCreatedAt"))return Math.max(0,source.optLong("taskCreatedAt"));
        if(!task.id.startsWith("html:"))return task.createdAt;
        String id=identity(task);JSONObject overrides=state.optJSONObject("taskOverrides"),definition=overrides==null?null:overrides.optJSONObject(id);JSONArray custom=state.optJSONArray("customTasks");
        if(custom!=null)for(int i=0;i<custom.length();i++){JSONObject value=custom.optJSONObject(i);if(value!=null&&id.equals(value.optString("id"))){definition=value;break;}}
        return definition==null?legacyCreation(id):creation(definition);
    }
    static int compare(TaskStore.Task a,TaskStore.Task b,JSONObject state){
        int result=Integer.compare(TaskStore.PRIORITIES.indexOf(a.priority),TaskStore.PRIORITIES.indexOf(b.priority));if(result!=0)return result;
        JSONObject x=a.sourceRecord(),y=b.sourceRecord();result=Boolean.compare(y.optBoolean("urgent"),x.optBoolean("urgent"));if(result!=0)return result;
        result=Integer.compare(rank(state,a.priority,identity(a)),rank(state,b.priority,identity(b)));if(result!=0)return result;
        long first=creationFor(a,x,state);
        long second=creationFor(b,y,state);
        result=Long.compare(first,second);if(result!=0)return result;
        return Integer.compare(x.optInt("taskDefinitionOrder",Integer.MAX_VALUE),y.optInt("taskDefinitionOrder",Integer.MAX_VALUE));
    }
}
