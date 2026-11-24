package com.personal.workouts;

import android.app.*;
import android.content.*;
import org.json.JSONObject;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.android.controller.ServiceController;
import org.robolectric.annotation.Config;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class) @Config(sdk=28)
public class RestTimerServiceTest {
    private Context context;private WorkoutStore store;
    @Before public void setup(){context=RuntimeEnvironment.getApplication();store=new WorkoutStore(context);store.prefs.edit().clear().commit();}
    private long runningTimer()throws Exception {
        long end=System.currentTimeMillis()+90000;
        JSONObject data=WorkoutStoreTest.fixture();data.put("active",WorkoutStoreTest.session(data))
            .put("restEndAt",end).put("restDurationMs",90000).put("restStartedAt",end-90000);
        data.getJSONObject("settings").put("notifications",true);
        assertNotNull(store.write(data.toString()));return end;
    }
    @Test public void foregroundNotificationHasCountdownProgressAndActions()throws Exception {
        runningTimer();
        ServiceController<RestTimerService> controller=Robolectric.buildService(RestTimerService.class).create();
        controller.startCommand(0,1);
        Notification notification=shadowOf(controller.get()).getLastForegroundNotification();
        assertNotNull(notification);
        assertTrue(notification.extras.getBoolean(Notification.EXTRA_SHOW_CHRONOMETER));
        assertTrue(notification.extras.getBoolean(Notification.EXTRA_CHRONOMETER_COUNT_DOWN));
        assertEquals(1000,notification.extras.getInt(Notification.EXTRA_PROGRESS_MAX));
        assertEquals(2,notification.actions.length);
        assertEquals("+30s",notification.actions[0].title.toString());
        assertEquals("Skip",notification.actions[1].title.toString());
        assertTrue((notification.flags&Notification.FLAG_ONGOING_EVENT)!=0);
        controller.destroy();
    }
    @Test public void notificationMoreAddsTimeWithoutChangingWorkoutSets()throws Exception {
        long end=runningTimer();String active=store.read().getJSONObject("active").toString();
        new RestAlarm().onReceive(context,new Intent(context,RestAlarm.class).setAction(RestTimerService.MORE));
        assertEquals(end+30000,store.read().getLong("restEndAt"));
        assertEquals(120000,store.read().getLong("restDurationMs"));
        assertEquals(active,store.read().getJSONObject("active").toString());
    }
    @Test public void notificationSkipClearsOnlyTheRestDeadline()throws Exception {
        runningTimer();String active=store.read().getJSONObject("active").toString();
        new RestAlarm().onReceive(context,new Intent(context,RestAlarm.class).setAction(RestTimerService.SKIP));
        assertEquals(0,store.read().getLong("restEndAt"));
        assertEquals(active,store.read().getJSONObject("active").toString());
    }
    @Test public void oldActionsCannotCreateATimerWithoutAWorkout()throws Exception {
        store.write(WorkoutStoreTest.fixture().toString());
        new RestAlarm().onReceive(context,new Intent(context,RestAlarm.class).setAction(RestTimerService.MORE));
        assertEquals(0,store.read().getLong("restEndAt"));
    }
}
