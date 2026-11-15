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
 @Test public void mediaRequestsCarryNoMetadataEtag()throws Exception{
  List<String> calls=new ArrayList<>();GoogleDrive drive=new GoogleDrive("private",(method,path,token,body,type,etag)->{assertNull(etag);calls.add(method+" "+path);return new JSONObject().put("schema",2).put("application","personal.tasks");});
  drive.readDocument("file/id");drive.writeDocument("file/id",new JSONObject().put("schema",2));assertEquals(2,calls.size());assertTrue(calls.get(0).contains("file%2Fid?alt=media"));assertTrue(calls.get(1).startsWith("PUT"));
 }
 @Test public void filesArePrivateAndLegacyCloudFileIsNotUsed()throws Exception{
  GoogleDrive drive=new GoogleDrive("private",(m,p,t,b,c,e)->{assertTrue(p.contains("spaces=appDataFolder"));assertFalse(p.contains("tasks-state-v1"));return new JSONObject().put("files",new JSONArray());});assertTrue(drive.files().isEmpty());
 }
 @Test public void invalidCloudEnvelopeIsRejected()throws Exception{
  GoogleDrive drive=new GoogleDrive("private",(m,p,t,b,c,e)->new JSONObject().put("schema",1));try{drive.readDocument("id");fail();}catch(GoogleDrive.Failure expected){assertTrue(expected.getMessage().contains("not been overwritten"));}
 }
 @Test public void newDocumentsUsePrivateAppDataFolder()throws Exception{
  GoogleDrive drive=new GoogleDrive("private",(m,p,t,b,c,e)->{assertEquals("POST",m);assertNull(e);assertTrue(new String(b,StandardCharsets.UTF_8).contains("appDataFolder"));return new JSONObject().put("id","created");});assertEquals("created",drive.createDocument(GoogleDrive.SEED,GoogleDrive.document("seed","epoch").put("state",new JSONObject())));
 }
 @Test public void crossPlatformRegisterFixturesConvergeInEitherOrder()throws Exception{
  try(java.io.InputStream input=getClass().getResourceAsStream("/cloud-v2-fixtures.json")){
   assertNotNull(input);java.io.ByteArrayOutputStream bytes=new java.io.ByteArrayOutputStream();byte[] buffer=new byte[4096];int n;while((n=input.read(buffer))!=-1)bytes.write(buffer,0,n);JSONArray cases=new JSONArray(bytes.toString("UTF-8"));
   for(int i=0;i<cases.length();i++){JSONObject sample=cases.getJSONObject(i);List<JSONObject> docs=new ArrayList<>();JSONArray values=sample.getJSONArray("documents");for(int j=0;j<values.length();j++)docs.add(values.getJSONObject(j));assertTrue(StateMerge.same(sample.getJSONObject("expected"),CloudState.resolve(sample.getJSONObject("seed"),docs).state));Collections.reverse(docs);assertTrue(StateMerge.same(sample.getJSONObject("expected"),CloudState.resolve(sample.getJSONObject("seed"),docs).state));}
  }
 }
 @Test public void retryDoesNotReclockAnUnchangedOfflineDeletion()throws Exception{
  JSONObject before=new JSONObject("{\"customTasks\":[{\"id\":\"a\",\"title\":\"Delete\"}],\"inProgressTasks\":[\"a:date\"]}"),after=new JSONObject("{\"customTasks\":[],\"inProgressTasks\":[]}"),journal=new JSONObject().put("counter",0).put("registers",new JSONObject()).put("localHead",before);
  JSONObject captured=CloudState.capture(journal,after),again=CloudState.capture(captured,after);assertEquals(1,captured.getLong("counter"));assertEquals(1,again.getLong("counter"));JSONObject doc=new JSONObject().put("owner","phone").put("registers",again.getJSONObject("registers"));CloudState.Result result=CloudState.resolve(before,Collections.singletonList(doc));assertEquals(0,result.state.getJSONArray("customTasks").length());assertEquals(0,result.state.getJSONArray("inProgressTasks").length());
 }
 @Test public void firstRestoreReplacesOldTasksAndCreatesPrivateBackup()throws Exception{
  Context app=RuntimeEnvironment.getApplication();app.getSharedPreferences("reference_state",0).edit().clear().commit();ReferenceStore store=new ReferenceStore(app);JSONObject old=new JSONObject("{\"customTasks\":[{\"id\":\"old\",\"title\":\"Old\"}]}"),remote=new JSONObject("{\"customTasks\":[{\"id\":\"phone\",\"title\":\"Phone\"}],\"cloudManaged\":true}");store.writeState(old.toString());store.restoreCloud(old,remote,"account");assertEquals("phone",new JSONObject(store.readState()).getJSONArray("customTasks").getJSONObject(0).getString("id"));assertEquals("account",store.cloudAccount());assertTrue(new java.io.File(app.getFilesDir(),"cloud-backups").listFiles().length>0);
 }
 @Test public void restoringCloudRemovesOldWidgetMirrorBeforeTheUiPublishesNewRows()throws Exception{
  Context app=RuntimeEnvironment.getApplication();app.getSharedPreferences("reference_state",0).edit().clear().commit();app.getSharedPreferences("tasks",0).edit().clear().commit();ReferenceStore store=new ReferenceStore(app);
  String oldId=store.addCustomTask("Old local task",Collections.emptyList(),"LOW",false);JSONObject sent=new JSONObject(store.readState());assertTrue(app.getSharedPreferences("reference_state",0).getString("snapshot","[]").contains(oldId));
  store.restoreCloud(sent,new JSONObject().put("cloudManaged",true).put("customTasks",new JSONArray()),"account");store.refreshSyncedMirror();assertFalse(app.getSharedPreferences("reference_state",0).getString("snapshot","[]").contains(oldId));for(TaskStore.Task task:new TaskStore(app).all())assertFalse(task.id.contains(oldId));
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
