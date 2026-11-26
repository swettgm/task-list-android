package com.personal.tasklist;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import java.util.HashMap;
import java.util.Map;

/** Android owns periodic wakeups when the task screen is not running. */
public final class GoogleSyncJob extends JobService {
    private static final int PERIODIC=28731, EDIT=28732;
    private final Map<Integer,GoogleSync> jobs=new HashMap<>();

    static void schedulePeriodic(Context context) {
        JobScheduler scheduler=context.getSystemService(JobScheduler.class);
        if(scheduler==null||scheduler.getPendingJob(PERIODIC)!=null)return;
        scheduler.schedule(new JobInfo.Builder(PERIODIC,new ComponentName(context,GoogleSyncJob.class))
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).setPeriodic(15*60_000L).setPersisted(true).build());
    }

    static void scheduleSoon(Context context) {
        if(context.getSharedPreferences("google_sync",Context.MODE_PRIVATE).getString("account","").isEmpty())return;
        JobScheduler scheduler=context.getSystemService(JobScheduler.class);
        if(scheduler==null||scheduler.getPendingJob(EDIT)!=null)return;
        scheduler.schedule(new JobInfo.Builder(EDIT,new ComponentName(context,GoogleSyncJob.class))
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY).build());
    }

    @Override public boolean onStartJob(JobParameters parameters) {
        if(getSharedPreferences("google_sync",Context.MODE_PRIVATE).getString("account","").isEmpty())return false;
        GoogleSync sync=new GoogleSync(this,()->{});
        jobs.put(parameters.getJobId(),sync);
        sync.runBackgroundOnce(()->{
            if(jobs.get(parameters.getJobId())!=sync)return;
            jobs.remove(parameters.getJobId());
            sync.close();
            jobFinished(parameters,false);
        });
        return true;
    }

    @Override public boolean onStopJob(JobParameters parameters) {
        GoogleSync sync=jobs.remove(parameters.getJobId());
        if(sync!=null)sync.close();
        return true;
    }

    @Override public void onDestroy(){
        for(GoogleSync sync:jobs.values())sync.close();
        jobs.clear();
        super.onDestroy();
    }
}
