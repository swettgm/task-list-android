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
    private final Runnable changed;
    private final Object lock=new Object();
    private final Handler ui=new Handler(Looper.getMainLooper());
    private final ScheduledExecutorService executor=Executors.newSingleThreadScheduledExecutor(r->{Thread t=new Thread(r,"Tasks Google sync");t.setDaemon(true);return t;});
    private ScheduledFuture<?> periodic;
    private long generation,authGeneration;
    private int authRequest=AUTHORIZE;
    private boolean running,closed,signingIn,queued;
    private String message="Not signed in",accessToken;
    GoogleSync(Context context,Runnable changed){app=context.getApplicationContext();reference=new ReferenceStore(app);prefs=app.getSharedPreferences("google_sync",Context.MODE_PRIVATE);this.changed=changed;if(signedIn())message="Waiting to sync";}
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
    void requestSync(){synchronized(lock){if(closed||!running||!signedIn()||queued)return;queued=true;}executor.execute(()->{synchronized(lock){queued=false;}syncOnce();});}
    String status(){synchronized(lock){try{return new JSONObject().put("signedIn",signedIn()).put("email",prefs.getString("email","")).put("signingIn",signingIn).put("message",message).put("lastSyncAt",prefs.getString("lastSyncAt","")).toString();}catch(Exception invalid){return "{}";}}}
    void signIn(Activity activity){
        synchronized(lock){if(closed||signingIn)return;}
        new AlertDialog.Builder(activity).setTitle("Sync with Google").setMessage("Sign into the same Google account on your phone and PC. Your current tasks and theme will be combined with that account’s saved Tasks data. Your other Drive files stay private.")
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
                    if(!same)edit.remove("base").remove("fileId").remove("lastSyncAt");
                    if(!edit.commit())throw new Exception("Account save failed");
                    accessToken=token;signingIn=false;message="Signed in. Syncing…";
                }
                requestSync();
            }catch(Exception failure){authFailed(expected);}
        });
    }
    void signOut(){synchronized(lock){generation++;signingIn=false;accessToken=null;if(!prefs.edit().clear().commit())throw new IllegalStateException("The Google connection could not be removed");message="Signed out. Tasks remain on this phone.";}}
    private String token(String email)throws Exception{
        synchronized(lock){if(accessToken!=null)return accessToken;}
        AuthorizationResult result=Tasks.await(Identity.getAuthorizationClient(app).authorize(request(email)),30,TimeUnit.SECONDS);
        if(result.hasResolution()||result.getAccessToken()==null)throw new GoogleDrive.Failure(401,"Sign in to Google again to continue syncing.");
        return result.getAccessToken();
    }
    void syncOnce(){
        final long expected;final String account,email;final JSONObject sent,base;final String baseFile;
        synchronized(lock){
            if(closed||!running||!signedIn()||signingIn)return;
            expected=generation;account=prefs.getString("account","");email=prefs.getString("email","");baseFile=prefs.getString("fileId","");
            try{sent=new JSONObject(reference.readState());base=new JSONObject(prefs.getString("base","{}"));}catch(Exception unreadable){message="Saved sync data could not be read. Local tasks are kept.";return;}
        }
        String token=null;
        try{
            token=token(email);GoogleDrive drive=new GoogleDrive(token);
            synchronized(lock){if(closed||expected!=generation)return;accessToken=token;}
            String id=drive.find();
            if(id==null){synchronized(lock){if(closed||expected!=generation)return;}drive.create(sent);id=drive.find();if(id==null)throw new Exception("Waiting for new sync file");}
            for(int attempt=0;attempt<8;attempt++){
                try{
                    GoogleDrive.Version remote=drive.read(id);JSONObject merged=StateMerge.merge(id.equals(baseFile)?base:new JSONObject(),sent,remote.state);
                    synchronized(lock){if(closed||expected!=generation)return;}
                    if(!StateMerge.same(merged,remote.state))drive.write(id,merged,remote.etag);
                    boolean updated=false;
                    synchronized(lock){
                        if(closed||expected!=generation)return;
                        boolean saved=false;
                        for(int retry=0;retry<16;retry++){
                            long revision=reference.revision();JSONObject current=new JSONObject(reference.readState());JSONObject latest=StateMerge.merge(sent,current,merged);
                            boolean differs=!StateMerge.same(latest,current);
                            if(reference.compareAndWriteState(revision,latest.toString())){saved=true;updated=differs;break;}
                        }
                        if(!saved)throw new Exception("Tasks changed while applying sync");
                        if(!prefs.edit().putString("fileId",id).putString("base",merged.toString()).putString("lastSyncAt",java.time.Instant.now().toString()).commit())throw new Exception("Sync baseline save failed");
                        message="Synced with Google";
                    }
                    if(updated){reference.refreshSyncedMirror();TaskReminders.reschedule(app);TaskWidgetProvider.refreshAll(app);ui.post(changed);}
                    return;
                }catch(GoogleDrive.Failure conflict){if(conflict.status!=412)throw conflict;}
            }
            throw new GoogleDrive.Failure(0,"Tasks changed on another device. Retrying shortly; local edits are kept.");
        }catch(Exception error){
            synchronized(lock){if(closed||expected!=generation)return;message=error instanceof GoogleDrive.Failure?error.getMessage():"Google sync is unavailable. Your tasks are saved on this phone.";if(error instanceof GoogleDrive.Failure&&((GoogleDrive.Failure)error).status==401){accessToken=null;if(token!=null)try{Identity.getAuthorizationClient(app).clearToken(ClearTokenRequest.builder().setToken(token).build());}catch(Exception ignored){}}}
        }
    }
}
