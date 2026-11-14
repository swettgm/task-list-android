package com.personal.tasklist;
import static org.junit.Assert.*;
import android.content.Context;
import org.json.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RunWith(RobolectricTestRunner.class)@Config(sdk=28)
public class GoogleDriveTest {
 @Test public void readAndWriteUseTheSameEtagAndPrivateEnvelope()throws Exception{
  List<String> calls=new ArrayList<>();
  GoogleDrive drive=new GoogleDrive("private",(method,path,token,body,type,etag)->{
   calls.add(method+" "+path+" "+etag);
   if(path.contains("fields=id,etag")&&method.equals("GET"))return new JSONObject().put("etag","\"v7\"");
   if(path.contains("alt=media")){assertEquals("\"v7\"",etag);return GoogleDrive.envelope(new JSONObject().put("completed",new JSONObject().put("a",true)));}
   assertEquals("PUT",method);assertEquals("\"v7\"",etag);assertEquals("personal.tasks",new JSONObject(new String(body,StandardCharsets.UTF_8)).getString("application"));return new JSONObject();
  });
  GoogleDrive.Version version=drive.read("file/id");assertTrue(version.state.getJSONObject("completed").getBoolean("a"));drive.write("file/id",version.state,version.etag);assertEquals(3,calls.size());assertTrue(calls.get(0).contains("file%2Fid"));
 }
 @Test public void missingEtagNeverReadsOrWritesUnversionedData()throws Exception{
  int[] calls={0};GoogleDrive drive=new GoogleDrive("private",(m,p,t,b,c,e)->{calls[0]++;return new JSONObject();});
  try{drive.read("id");fail();}catch(GoogleDrive.Failure expected){assertEquals(1,calls[0]);}
 }
 @Test public void corruptEnvelopeIsNotAccepted()throws Exception{
  GoogleDrive drive=new GoogleDrive("private",(m,p,t,b,c,e)->p.contains("fields=id,etag")?new JSONObject().put("etag","v"):new JSONObject().put("state",new JSONObject()));
  try{drive.read("id");fail();}catch(GoogleDrive.Failure expected){assertTrue(expected.getMessage().contains("not overwritten"));}
 }
 @Test public void oldestDuplicateIsUsedOnBothPlatforms()throws Exception{
  GoogleDrive drive=new GoogleDrive("private",(m,p,t,b,c,e)->new JSONObject().put("items",new JSONArray().put(new JSONObject().put("id","new").put("createdDate","2026-11-03")).put(new JSONObject().put("id","old").put("createdDate","2026-11-01"))));assertEquals("old",drive.find());
 }
 @Test public void newFilesAreConfinedToPrivateAppData()throws Exception{
  GoogleDrive drive=new GoogleDrive("private",(m,p,t,b,c,e)->{assertEquals("POST",m);assertTrue(p.contains("uploadType=multipart"));String text=new String(b,StandardCharsets.UTF_8);assertTrue(text.contains("appDataFolder"));assertTrue(text.contains("personal.tasks"));return new JSONObject().put("id","created");});assertEquals("created",drive.create(new JSONObject()));
 }
 @Test public void definitionsExportIsStableAndKeepsConcurrentTaskHistory()throws Exception{
  Context app=RuntimeEnvironment.getApplication();app.getSharedPreferences("reference_state",0).edit().clear().commit();ReferenceStore store=new ReferenceStore(app);
  store.writeState("{\"completed\":{\"keep\":true},\"projects\":{\"p\":{\"title\":\"keep\"}}}");
  assertTrue(store.updateSharedDefinitions("[{\"id\":\"base\",\"title\":\"Base task\",\"priority\":\"HIGH\"}]"));long revision=store.revision();
  assertFalse(store.updateSharedDefinitions("[{\"title\":\"Base task\",\"priority\":\"HIGH\",\"id\":\"base\"}]"));assertEquals(revision,store.revision());
  JSONObject state=new JSONObject(store.readState());assertTrue(state.getJSONObject("completed").getBoolean("keep"));assertEquals("Base task",state.getJSONObject("sharedDefinitions").getJSONObject("base").getString("title"));assertTrue(state.getJSONObject("projects").has("p"));
 }
 @Test public void invalidDefinitionsDoNotReplaceSavedState()throws Exception{
  Context app=RuntimeEnvironment.getApplication();app.getSharedPreferences("reference_state",0).edit().clear().commit();ReferenceStore store=new ReferenceStore(app);String before=store.readState();
  try{store.updateSharedDefinitions("[{\"id\":\"x\",\"title\":\"One\"},{\"id\":\"x\",\"title\":\"Two\"}]");fail();}catch(IllegalArgumentException expected){assertEquals(before,store.readState());}
 }
 @Test public void cloudPayloadKeepsTaskDataButExcludesLocalViewAndVolatileTimestamp()throws Exception{
  JSONObject local=new JSONObject().put("uiContext","Home").put("updatedAt","now").put("completed",new JSONObject().put("task",true));JSONObject shared=GoogleSync.sharedState(local);
  assertFalse(shared.has("uiContext"));assertFalse(shared.has("updatedAt"));assertTrue(shared.getJSONObject("completed").getBoolean("task"));assertTrue(local.has("uiContext"));
 }
 @Test public void signingOutKeepsLocalTasksAndDoesNotExposeCredentials()throws Exception{
  Context app=RuntimeEnvironment.getApplication();app.getSharedPreferences("reference_state",0).edit().clear().commit();ReferenceStore store=new ReferenceStore(app);store.writeState("{\"completed\":{\"keep\":true}}");
  app.getSharedPreferences("google_sync",0).edit().clear().putString("email","test@example.com").putString("account","subject").putString("base","{\"secret\":true}").commit();GoogleSync sync=new GoogleSync(app,()->{});
  try{assertFalse(sync.status().contains("secret"));sync.signOut();assertTrue(new JSONObject(store.readState()).getJSONObject("completed").getBoolean("keep"));assertFalse(new JSONObject(sync.status()).getBoolean("signedIn"));assertFalse(app.getSharedPreferences("google_sync",0).contains("base"));}finally{sync.close();}
 }
}
