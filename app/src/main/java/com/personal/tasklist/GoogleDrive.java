package com.personal.tasklist;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Private app-data only, with ETag compare-and-set on every Drive update. */
final class GoogleDrive {
    static final int LIMIT=2*1024*1024;
    static final String NAME="tasks-state-v1.json";
    interface Transport { JSONObject send(String method,String path,String token,byte[] body,String contentType,String etag)throws Exception; }
    static final class Failure extends Exception {
        final int status;
        Failure(int status,String message){super(message);this.status=status;}
    }
    static final class Version {
        final JSONObject state;final String etag;
        Version(JSONObject state,String etag){this.state=state;this.etag=etag;}
    }
    private final String token;private final Transport transport;
    GoogleDrive(String token){this(token,GoogleDrive::http);}
    GoogleDrive(String token,Transport transport){this.token=token;this.transport=transport;}
    static String encode(String value){try{return URLEncoder.encode(value,StandardCharsets.UTF_8.name()).replace("+","%20");}catch(java.io.UnsupportedEncodingException impossible){throw new IllegalStateException(impossible);}}
    private JSONObject call(String method,String path,JSONObject value,String etag)throws Exception{
        byte[] body=value==null?null:value.toString().getBytes(StandardCharsets.UTF_8);
        if(body!=null&&body.length>LIMIT)throw new Failure(0,"Tasks sync data is too large");
        return transport.send(method,path,token,body,"application/json; charset=UTF-8",etag);
    }
    String find()throws Exception{
        List<JSONObject> files=new ArrayList<>();String page="";int pages=0;
        do{
            if(++pages>20)throw new Failure(0,"Too many Tasks sync pages");
            String query="/drive/v2/files?spaces=appDataFolder&maxResults=100&fields=items(id,createdDate),nextPageToken&q="+encode("title = '"+NAME+"' and trashed = false");
            if(!page.isEmpty())query+="&pageToken="+encode(page);
            JSONObject result=call("GET",query,null,null);JSONArray items=result.optJSONArray("items");
            if(items!=null)for(int i=0;i<items.length();i++){JSONObject file=items.optJSONObject(i);if(file!=null&&!file.optString("id").isEmpty())files.add(file);}
            page=result.optString("nextPageToken","");if(files.size()>1000)throw new Failure(0,"Too many Tasks sync files");
        }while(!page.isEmpty());
        files.sort(Comparator.comparing((JSONObject v)->v.optString("createdDate")).thenComparing(v->v.optString("id")));
        return files.isEmpty()?null:files.get(0).getString("id");
    }
    static JSONObject envelope(JSONObject state)throws Exception{return new JSONObject().put("schema",1).put("application","personal.tasks").put("state",state);}
    String create(JSONObject state)throws Exception{
        String boundary="tasks-"+java.util.UUID.randomUUID();
        JSONObject metadata=new JSONObject().put("name",NAME).put("parents",new JSONArray().put("appDataFolder")).put("mimeType","application/json");
        String body="--"+boundary+"\r\nContent-Type: application/json; charset=UTF-8\r\n\r\n"+metadata+"\r\n--"+boundary+"\r\nContent-Type: application/json\r\n\r\n"+envelope(state)+"\r\n--"+boundary+"--\r\n";
        byte[] bytes=body.getBytes(StandardCharsets.UTF_8);if(bytes.length>LIMIT+8192)throw new Failure(0,"Tasks sync data is too large");
        JSONObject result=transport.send("POST","/upload/drive/v3/files?uploadType=multipart&fields=id",token,bytes,"multipart/related; boundary="+boundary,null);
        String id=result.optString("id","");if(id.isEmpty())throw new Failure(0,"Google did not create the sync file");return id;
    }
    Version read(String id)throws Exception{
        String path="/drive/v2/files/"+encode(id);String etag=call("GET",path+"?fields=id,etag",null,null).optString("etag","");
        if(etag.isEmpty())throw new Failure(0,"Google did not provide a safe sync version");
        JSONObject data=call("GET",path+"?alt=media",null,etag);
        if(data.optInt("schema")!=1||!"personal.tasks".equals(data.optString("application"))||data.optJSONObject("state")==null)throw new Failure(0,"The Google sync file could not be read. It was not overwritten.");
        return new Version(data.getJSONObject("state"),etag);
    }
    void write(String id,JSONObject state,String etag)throws Exception{
        if(etag==null||etag.isEmpty())throw new Failure(0,"Missing sync version");
        call("PUT","/upload/drive/v2/files/"+encode(id)+"?uploadType=media&fields=id,etag",envelope(state),etag);
    }
    static JSONObject http(String method,String path,String token,byte[] body,String contentType,String etag)throws Exception{
        if(!path.startsWith("/drive/")&&!path.startsWith("/upload/drive/")&&!path.equals("/oauth2/v3/userinfo"))throw new Failure(0,"Invalid Google endpoint");
        HttpURLConnection connection=(HttpURLConnection)new URL("https://www.googleapis.com"+path).openConnection();
        try{
            connection.setConnectTimeout(15000);connection.setReadTimeout(25000);connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod(method);connection.setRequestProperty("Authorization","Bearer "+token);
            connection.setRequestProperty("Accept","application/json");
            if(etag!=null)connection.setRequestProperty("If-Match",etag);
            if(body!=null){connection.setDoOutput(true);connection.setRequestProperty("Content-Type",contentType);connection.setFixedLengthStreamingMode(body.length);try(java.io.OutputStream out=connection.getOutputStream()){out.write(body);}}
            int status=connection.getResponseCode();
            if(status<200||status>=300)throw new Failure(status,status==401?"Google access expired. Sign in again.":"Google Drive is unavailable. Local tasks are kept.");
            ByteArrayOutputStream bytes=new ByteArrayOutputStream();try(InputStream in=connection.getInputStream()){byte[] buffer=new byte[8192];int n;while((n=in.read(buffer))!=-1){if(bytes.size()+n>LIMIT+8192)throw new Failure(0,"Google sync data is too large");bytes.write(buffer,0,n);}}
            String json=bytes.toString(StandardCharsets.UTF_8.name());return new JSONObject(json.isEmpty()?"{}":json);
        }finally{connection.disconnect();}
    }
}
