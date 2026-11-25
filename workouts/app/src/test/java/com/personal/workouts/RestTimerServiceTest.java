package com.personal.workouts;

import android.app.*;
import android.content.*;
import android.os.Looper;
import android.view.WindowManager;
import java.time.Duration;
import org.json.JSONObject;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.android.controller.ServiceController;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowSettings;
import org.robolectric.shadows.ShadowWindowManagerImpl;
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
    @Test public void foregroundNotificationHasOneLineCountdownProgressAndActions()throws Exception {
        runningTimer();
        ServiceController<RestTimerService> controller=Robolectric.buildService(RestTimerService.class).create();
        controller.startCommand(0,1);
        Notification notification=shadowOf(controller.get()).getLastForegroundNotification();
        assertNotNull(notification);
        assertEquals("Rest · 1:30",notification.extras.getCharSequence(Notification.EXTRA_TITLE));
        assertNull(notification.extras.getCharSequence(Notification.EXTRA_TEXT));
        assertFalse(notification.extras.getBoolean(Notification.EXTRA_SHOW_CHRONOMETER));
        assertEquals(1000,notification.extras.getInt(Notification.EXTRA_PROGRESS_MAX));
        assertEquals(2,notification.actions.length);
        assertEquals("+30s",notification.actions[0].title.toString());
        assertEquals("Skip",notification.actions[1].title.toString());
        assertTrue((notification.flags&Notification.FLAG_ONGOING_EVENT)!=0);
        controller.destroy();
    }
    @Test public void overlayFollowsRestOnlyOutsideTheAppAndTearsDown()throws Exception {
        runningTimer();
        JSONObject inProgress=store.read();
        inProgress.getJSONObject("settings").put("notifications",false).put("floatOverApps",true);
        assertNotNull(store.write(inProgress.toString()));
        ShadowSettings.setCanDrawOverlays(true);
        MainActivity.visible=true;
        ServiceController<RestTimerService> controller=Robolectric.buildService(RestTimerService.class).create();
        try {
            controller.startCommand(0,1);
            ShadowWindowManagerImpl windows=Shadow.extract(controller.get().getSystemService(WindowManager.class));
            assertTrue(windows.getViews().isEmpty());
            MainActivity.visible=false;
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(250));
            assertEquals(1,windows.getViews().size());
            assertTrue(windows.getViews().get(0).getContentDescription().toString().startsWith("Rest · "));
            MainActivity.visible=true;
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(250));
            assertTrue(windows.getViews().isEmpty());
            MainActivity.visible=false;
            ShadowSettings.setCanDrawOverlays(false);
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(250));
            assertTrue(windows.getViews().isEmpty());
        }finally{
            controller.destroy();
            MainActivity.visible=false;
            ShadowSettings.setCanDrawOverlays(false);
        }
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
