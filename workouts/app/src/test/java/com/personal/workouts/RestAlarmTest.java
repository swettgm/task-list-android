package com.personal.workouts;

import android.app.*;
import android.content.Context;
import org.json.JSONObject;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class) @Config(sdk=28)
public class RestAlarmTest {
    private Context context;private WorkoutStore store;
    @Before public void setup(){context=RuntimeEnvironment.getApplication();store=new WorkoutStore(context);store.prefs.edit().clear().commit();MainActivity.visible=false;}
    @Test public void aFutureRestIsScheduledForItsSavedDeadline()throws Exception {
        long end=System.currentTimeMillis()+90000;JSONObject data=WorkoutStoreTest.fixture().put("restEndAt",end);
        store.write(data.toString());RestAlarm.schedule(context);
        assertEquals(end,shadowOf(context.getSystemService(AlarmManager.class)).getNextScheduledAlarm().triggerAtTime);
    }
    @Test public void skippingRestCancelsItsAlarm()throws Exception {
        JSONObject data=WorkoutStoreTest.fixture().put("restEndAt",System.currentTimeMillis()+90000);
        store.write(data.toString());RestAlarm.schedule(context);
        data=store.read().put("restEndAt",0);store.write(data.toString());RestAlarm.schedule(context);
        assertNull(shadowOf(context.getSystemService(AlarmManager.class)).getNextScheduledAlarm());
    }
    @Test public void restCompletionIsDeduplicatedAndNotificationsDefaultOff()throws Exception {
        long end=System.currentTimeMillis()-1;store.write(WorkoutStoreTest.fixture().put("restEndAt",end).toString());
        RestAlarm.complete(context);assertEquals(end,store.prefs.getLong("announcedRest",0));
        RestAlarm.complete(context);assertEquals(0,shadowOf(context.getSystemService(NotificationManager.class)).size());
    }
    @Test public void anEarlyOrCancelledAlarmCannotAnnounceCompletion()throws Exception {
        store.write(WorkoutStoreTest.fixture().put("restEndAt",System.currentTimeMillis()+90000).toString());
        RestAlarm.complete(context);assertEquals(0,store.prefs.getLong("announcedRest",0));
        JSONObject data=store.read().put("restEndAt",0);store.write(data.toString());
        RestAlarm.complete(context);assertEquals(0,store.prefs.getLong("announcedRest",0));
    }
    @Test public void spotifyControlsHandleMissingAccessWithoutReadingSessions()throws Exception {
        JSONObject music=SpotifyAccess.state(context);assertFalse(music.getBoolean("connected"));assertFalse(music.getBoolean("available"));
        SpotifyAccess.command(context,"play");SpotifyAccess.command(context,"unrecognized");
    }
}
