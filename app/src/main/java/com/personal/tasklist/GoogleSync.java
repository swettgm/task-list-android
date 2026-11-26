package com.personal.tasklist;

import android.accounts.Account;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import com.google.android.gms.auth.api.identity.AuthorizationRequest;
import com.google.android.gms.auth.api.identity.AuthorizationResult;
import com.google.android.gms.auth.api.identity.ClearTokenRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.tasks.Tasks;
import org.json.JSONObject;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/** Google issues short-lived tokens; only account identity and merge baseline persist. */
final class GoogleSync {
    static final int AUTHORIZE=2301;
    static final String ANDROID_CLIENT_ID="277712575915-pc43c20vte31fk611r6r5ipcqu7uml5f.apps.googleusercontent.com";
    private final Context app;
    private final ReferenceStore reference;
    private final SharedPreferences prefs;
    private final SharedPreferences books;
    private boolean needsInitialization,forceSeed,needsReauth;
    private static final Object CLOUD_IO=new Object();
    private final Runnable changed;
    private final Object lock=new Object();
    private final Handler ui=new Handler(Looper.getMainLooper());
    private final ScheduledExecutorService executor=Executors.newSingleThreadScheduledExecutor(r->{Thread t=new Thread(r,"Tasks Google sync");t.setDaemon(true);return t;});
    private ScheduledFuture<?> periodic;
    private long generation,authGeneration;
    private int authRequest=AUTHORIZE;
    private boolean running,closed,signingIn,queued;
    private String message="Not signed in",accessToken;
    GoogleSync(Context context,Runnable changed){app=context.getApplicationContext();reference=new ReferenceStore(app);prefs=app.getSharedPreferences("google_sync",Context.MODE_PRIVATE);books=app.getSharedPreferences("google_cloud_v2",Context.MODE_PRIVATE);this.changed=changed;if(!prefs.contains("v2Installed"))prefs.edit().putBoolean("v2Installed",true).putBoolean("seedExistingPhone",signedIn()).commit();if(signedIn())message="Restoring cloud tasks…";}
    static JSONObject sharedState(JSONObject value)throws org.json.JSONException{JSONObject result=new JSONObject(value.toString());result.remove("uiContext");result.remove("updatedAt");return result;}
    private boolean signedIn(){return !prefs.getString("email","").isEmpty()&&!prefs.getString("account","").isEmpty();}
    private static AuthorizationRequest request(String email){
        AuthorizationRequest.Builder builder=AuthorizationRequest.builder().setRequestedScopes(Arrays.asList(new Scope("openid"),new Scope("https://www.googleapis.com/auth/userinfo.email"),new Scope("https://www.googleapis.com/auth/drive.appdata")));
        if(email!=null&&!email.isEmpty())builder.setAccount(new Account(email,"com.google"));
        else builder.setPrompt(AuthorizationRequest.Prompt.SELECT_ACCOUNT);
        return builder.build();
    }
    void start(){synchronized(lock){if(closed||running)return;running=true;periodic=executor.scheduleWithFixedDelay(this::syncOnce,0,10,TimeUnit.SECONDS);}}
    void stop(){synchronized(lock){running=false;if(periodic!=null){periodic.cancel(false);periodic=null;}}}
    void close(){synchronized(lock){closed=true;running=false;generation++;if(periodic!=null)periodic.cancel(false);}executor.shutdownNow();ui.removeCallbacksAndMessages(null);}
    void requestSync(){synchronized(lock){if(closed||!signedIn()||queued)return;queued=true;}try{executor.execute(()->{synchronized(lock){queued=false;}syncOnce();});}catch(java.util.concurrent.RejectedExecutionException stopped){synchronized(lock){queued=false;}}}
    void runBackgroundOnce(Runnable finished) {
        executor.execute(()->{try{syncOnce();}finally{ui.post(finished);}});
    }
    String status(){synchronized(lock){try{return new JSONObject().put("signedIn",signedIn()).put("email",prefs.getString("email","")).put("signingIn",signingIn).put("message",message).put("lastSyncAt",prefs.getString("lastSyncAt","")).put("needsInitialization",needsInitialization).put("needsReauth",needsReauth).toString();}catch(Exception invalid){return "{}";}}}
    void signIn(Activity activity){
        synchronized(lock){if(closed||signingIn)return;}
        new AlertDialog.Builder(activity).setTitle("Sync with Google").setMessage("Sign into the same Google account on your phone and PC. Your saved cloud task list and theme will be restored. This phone’s current list is backed up before its first restore. Your other Drive files stay private.")
            .setNegativeButton("Cancel",null).setPositiveButton("Sign in",(dialog,which)->beginSignIn(activity)).show();
    }
    private void beginSignIn(Activity activity){
        final long expected;final int requestCode;
        synchronized(lock){if(closed||signingIn)return;signingIn=true;authGeneration=++generation;expected=generation;authRequest=AUTHORIZE+(int)(generation%1000);requestCode=authRequest;message="Choose your Google account";}
        try{Identity.getAuthorizationClient(activity).authorize(request(null)).addOnSuccessListener(activity,result->{
            synchronized(lock){if(closed||expected!=generation)return;}
            if(result.hasResolution())try{activity.startIntentSenderForResult(result.getPendingIntent().getIntentSender(),requestCode,null,0,0,0);}catch(Exception error){authFailed(expected);}
            else accept(result,expected);
        }).addOnFailureListener(activity,error->authFailed(expected));}catch(Exception unavailable){authFailed(expected);}
    }
    static boolean isAuthorizationRequest(int code){return code>=AUTHORIZE&&code<AUTHORIZE+1000;}
    void result(int requestCode,int resultCode,Intent data){
        final long expected;
        synchronized(lock){if(!signingIn||closed||authGeneration!=generation||requestCode!=authRequest)return;expected=authGeneration;}
        if(resultCode!=Activity.RESULT_OK||data==null){authFailed(expected);return;}
        try{accept(Identity.getAuthorizationClient(app).getAuthorizationResultFromIntent(data),expected);}catch(Exception error){authFailed(expected);}
    }
    private void authFailed(long expected){synchronized(lock){if(closed||expected!=generation)return;signingIn=false;message="Google sign-in was cancelled or unavailable. Local tasks are kept.";}}
    private void accept(AuthorizationResult authorization,final long expected){
        final String token=authorization.getAccessToken();
        if(token==null||token.isEmpty()){authFailed(expected);return;}
        executor.execute(()->{
            try{
                JSONObject identity=GoogleDrive.http("GET","/oauth2/v3/userinfo",token,null,null,null);
                String email=identity.optString("email"),account=identity.optString("sub");
                if(email.isEmpty()||account.isEmpty()||!identity.optBoolean("email_verified"))throw new Exception("Invalid account");
                synchronized(lock){
                    if(closed||generation!=expected)return;
                    boolean same=account.equals(prefs.getString("account",""));
                    SharedPreferences.Editor edit=prefs.edit().putString("account",account).putString("email",email);
                    if(!same)edit.remove("base").remove("fileId").remove("lastSyncAt").putBoolean("seedExistingPhone",false);
                    if(!edit.commit())throw new Exception("Account save failed");
                    accessToken=token;signingIn=false;needsReauth=false;message="Signed in. Syncing…";
                }
                GoogleSyncJob.schedulePeriodic(app);
                requestSync();
            }catch(Exception failure){authFailed(expected);}
        });
    }
    void signOut(){synchronized(lock){generation++;signingIn=false;accessToken=null;if(!prefs.edit().clear().putBoolean("v2Installed",true).commit())throw new IllegalStateException("The Google connection could not be removed");message="Signed out. Tasks remain on this phone.";}}
    private String token(String email)throws Exception{
        synchronized(lock){if(accessToken!=null)return accessToken;}
        AuthorizationResult result=Tasks.await(Identity.getAuthorizationClient(app).authorize(request(email)),30,TimeUnit.SECONDS);
        if(result.hasResolution()||result.getAccessToken()==null)throw new GoogleDrive.Failure(401,"Sign in to Google again to continue syncing.");
        return result.getAccessToken();
    }
    void initializeCloud(){synchronized(lock){forceSeed=true;}requestSync();}
    private String bookKey(String account){return "account-"+account;}
    void syncOnce(){synchronized(CLOUD_IO){syncNotebook();}}
    private void syncNotebook(){
        final long expected;final String account,email;final JSONObject sent;
        synchronized(lock){
            if(closed||!signedIn()||signingIn)return;
            expected=generation;account=prefs.getString("account","");email=prefs.getString("email","");
            try{sent=sharedState(new JSONObject(reference.readState()));}catch(Exception invalid){message="Local tasks could not be read. Nothing was replaced.";return;}
        }
        String token=null;
        try{
            token=token(email);GoogleDrive drive=new GoogleDrive(token);
            synchronized(lock){if(closed||expected!=generation||!account.equals(prefs.getString("account","")))return;accessToken=token;}
            java.util.List<JSONObject> files=drive.files(),seeds=new java.util.ArrayList<>();for(JSONObject file:files)if(GoogleDrive.SEED.equals(file.optString("name")))seeds.add(file);
            if(seeds.isEmpty()){
                boolean initialize;synchronized(lock){initialize=forceSeed||prefs.getBoolean("seedExistingPhone",false);needsInitialization=!initialize;}
                if(!initialize){synchronized(lock){message="Use this phone’s tasks to start your cloud list.";}return;}
                JSONObject initial=CloudState.copy(sent).put("cloudManaged",true);
                synchronized(lock){if(closed||expected!=generation||!account.equals(prefs.getString("account","")))return;}
                drive.createDocument(GoogleDrive.SEED,GoogleDrive.document("seed",java.util.UUID.randomUUID().toString()).put("state",initial));
                files=drive.files();for(JSONObject file:files)if(GoogleDrive.SEED.equals(file.optString("name")))seeds.add(file);
                if(seeds.isEmpty())throw new GoogleDrive.Failure(0,"Waiting for Google to save the initial cloud list.");
            }
            JSONObject seed=drive.readDocument(seeds.get(0).getString("id"));if(!"seed".equals(seed.optString("kind"))||seed.optJSONObject("state")==null)throw new GoogleDrive.Failure(0,"The cloud task list is invalid. Local tasks are kept.");
            String epoch=seed.getString("epoch"),key=bookKey(account);JSONObject journal=books.contains(key)?new JSONObject(books.getString(key,"{}")):null;
            boolean fresh=journal==null||!epoch.equals(journal.optString("epoch"));
            boolean restore=fresh||!account.equals(reference.cloudAccount());
            if(fresh)journal=new JSONObject().put("epoch",epoch).put("owner",java.util.UUID.randomUUID().toString()).put("counter",0).put("registers",new JSONObject()).put("localHead",new JSONObject());
            else if(!restore){journal=CloudState.capture(journal,sent);if(!books.edit().putString(key,journal.toString()).commit())throw new Exception("Could not save pending cloud changes");}
            String owner=journal.getString("owner");JSONObject own=CloudState.copy(journal.getJSONObject("registers")),canonicalOwn=null;String ownId=null;java.util.List<JSONObject> documents=new java.util.ArrayList<>();
            for(JSONObject file:files){
                if(!file.optString("name").startsWith(GoogleDrive.PREFIX+epoch+"-"))continue;
                JSONObject doc=drive.readDocument(file.getString("id"));if(!epoch.equals(doc.optString("epoch"))||!"device".equals(doc.optString("kind")))throw new Exception("Invalid cloud device data");
                if(owner.equals(doc.getString("owner"))){
                    JSONObject regs=doc.getJSONObject("registers");if(ownId==null){ownId=file.getString("id");canonicalOwn=regs;}
                    for(String path:CloudState.keys(regs)){JSONObject op=regs.getJSONObject(path);if(!own.has(path)||op.getLong("clock")>own.getJSONObject(path).getLong("clock"))own.put(path,op);}
                }else documents.add(doc);
            }
            JSONObject ownDoc=GoogleDrive.document("device",epoch).put("owner",owner).put("registers",own);documents.add(ownDoc);
            CloudState.Result result=CloudState.resolve(seed.getJSONObject("state"),documents);
            synchronized(lock){if(closed||expected!=generation||!account.equals(prefs.getString("account","")))return;}
            if(own.length()>0&&!fresh&&!StateMerge.same(own,canonicalOwn)){
                if(ownId!=null)drive.writeDocument(ownId,ownDoc);else drive.createDocument(GoogleDrive.PREFIX+epoch+"-"+owner+".json",ownDoc);
            }
            boolean updated=false;
            synchronized(lock){
                if(closed||expected!=generation||!account.equals(prefs.getString("account","")))return;
                if(restore){reference.restoreCloud(sent,result.state,account);updated=true;}
                else{
                    boolean saved=false;for(int retry=0;retry<16;retry++){long revision=reference.revision();JSONObject current=new JSONObject(reference.readState()),latest=StateMerge.merge(sent,current,result.state);boolean differs=!StateMerge.same(latest,current);if(reference.compareAndWriteState(revision,latest.toString())){saved=true;updated=differs;break;}}
                    if(!saved)throw new Exception("Tasks changed while applying cloud sync");
                }
                journal.put("registers",own).put("counter",Math.max(result.counter,journal.optLong("counter"))).put("localHead",result.state);
                if(!books.edit().putString(key,journal.toString()).commit())throw new Exception("Cloud baseline save failed");
                prefs.edit().putBoolean("seedExistingPhone",false).putString("lastSyncAt",java.time.Instant.now().toString()).commit();forceSeed=false;needsInitialization=false;needsReauth=false;message="Synced with Google";
            }
            if(updated){reference.refreshSyncedMirror();TaskReminders.reschedule(app);TaskWidgetProvider.refreshAll(app);ui.post(changed);}
        }catch(Exception error){
            synchronized(lock){if(closed||expected!=generation||!account.equals(prefs.getString("account","")))return;message=error instanceof GoogleDrive.Failure?error.getMessage():"Google sync is unavailable. Your tasks are saved on this phone.";
                if(error instanceof GoogleDrive.Failure&&((GoogleDrive.Failure)error).status==401){needsReauth=true;accessToken=null;if(token!=null)try{Identity.getAuthorizationClient(app).clearToken(ClearTokenRequest.builder().setToken(token).build());}catch(Exception ignored){}}
            }
        }
    }
}
