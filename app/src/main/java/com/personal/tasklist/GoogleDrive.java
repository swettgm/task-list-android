package com.personal.tasklist;

import org.json.JSONArray;
import org.json.JSONException;
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

/** Private app-data documents: one authoritative seed and one record per client. */
final class GoogleDrive {
    static final int LIMIT=2*1024*1024;
    static final String SEED="tasks-cloud-v2-seed.json",PREFIX="tasks-cloud-v2-device-";
    interface Transport { JSONObject send(String method,String path,String token,byte[] body,String contentType,String etag)throws Exception; }
    static final class Failure extends Exception {
        final int status;
        Failure(int status,String message){super(message);this.status=status;}
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
    List<JSONObject> files()throws Exception{
        List<JSONObject> result=new ArrayList<>();String page="";
        for(int pass=0;pass<20;pass++){
            String q="trashed = false and (name = '"+SEED+"' or name contains '"+PREFIX+"')";
            String path="/drive/v3/files?spaces=appDataFolder&pageSize=1000&fields=files(id,name,createdTime),nextPageToken&q="+encode(q);
            if(!page.isEmpty())path+="&pageToken="+encode(page);
            JSONObject response=call("GET",path,null,null);JSONArray values=response.optJSONArray("files");
            if(values!=null)for(int i=0;i<values.length();i++)result.add(values.getJSONObject(i));
            page=response.optString("nextPageToken");if(page.isEmpty()){result.sort(Comparator.comparing((JSONObject f)->f.optString("createdTime")).thenComparing(f->f.optString("id")));return result;}
        }
        throw new Failure(0,"Too many Tasks cloud documents");
    }
    JSONObject readDocument(String id)throws Exception{
        JSONObject value=call("GET","/drive/v3/files/"+encode(id)+"?alt=media",null,null);
        if(value.optInt("schema")!=2||!"personal.tasks".equals(value.optString("application")))throw new Failure(0,"Cloud data is invalid. It has not been overwritten.");return value;
    }
    static JSONObject document(String kind,String epoch)throws JSONException{return new JSONObject().put("schema",2).put("application","personal.tasks").put("kind",kind).put("epoch",epoch);}
    String createDocument(String name,JSONObject value)throws Exception{
        String boundary="tasks-"+java.util.UUID.randomUUID();
        JSONObject metadata=new JSONObject().put("name",name).put("parents",new JSONArray().put("appDataFolder")).put("mimeType","application/json");
        String body="--"+boundary+"\r\nContent-Type: application/json\r\n\r\n"+metadata+"\r\n--"+boundary+"\r\nContent-Type: application/json\r\n\r\n"+value+"\r\n--"+boundary+"--\r\n";
        byte[] bytes=body.getBytes(StandardCharsets.UTF_8);if(bytes.length>LIMIT+8192)throw new Failure(0,"Tasks cloud data is too large");
        return transport.send("POST","/upload/drive/v3/files?uploadType=multipart&fields=id",token,bytes,"multipart/related; boundary="+boundary,null).getString("id");
    }
    void writeDocument(String id,JSONObject value)throws Exception{
        // Each installation owns its document. Media requests carry no metadata ETag.
        call("PUT","/upload/drive/v2/files/"+encode(id)+"?uploadType=media",value,null);
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
