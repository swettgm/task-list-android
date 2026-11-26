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
    @Test public void foregroundNotificationHasOneLineCountdownAndEndTimer()throws Exception {
        runningTimer();
        ServiceController<RestTimerService> controller=Robolectric.buildService(RestTimerService.class).create();
        controller.startCommand(0,1);
        Notification notification=shadowOf(controller.get()).getLastForegroundNotification();
        assertNotNull(notification);
        assertEquals("Rest · 1:30",notification.extras.getCharSequence(Notification.EXTRA_TITLE));
        assertNull(notification.extras.getCharSequence(Notification.EXTRA_TEXT));
        assertFalse(notification.extras.getBoolean(Notification.EXTRA_SHOW_CHRONOMETER));
        assertEquals(1000,notification.extras.getInt(Notification.EXTRA_PROGRESS_MAX));
        assertEquals(1,notification.actions.length);
        assertEquals("End timer",notification.actions[0].title.toString());
        assertTrue((notification.flags&Notification.FLAG_ONGOING_EVENT)!=0);
        controller.destroy();
    }
    @Test public void notificationActionEndsOnlyTheMatchingRestAndPreservesTheWorkout()throws Exception {
        long firstEnd=runningTimer();
        Intent stale=shadowOf(RestAlarm.endTimerAction(context,store.read()).actionIntent).getSavedIntent();
        JSONObject next=store.read().put("restStartedAt",firstEnd-89000).put("restEndAt",firstEnd+1000);
        assertNotNull(store.write(next.toString()));
        RestAlarm receiver=new RestAlarm();
        receiver.onReceive(context,stale);
        JSONObject current=store.read();
        assertEquals(firstEnd+1000,current.getLong("restEndAt"));
        String workout=current.getJSONObject("active").toString();
        Intent matching=shadowOf(RestAlarm.endTimerAction(context,current).actionIntent).getSavedIntent();
        receiver.onReceive(context,matching);
        JSONObject ended=store.read();
        assertEquals(0,ended.getLong("restEndAt"));
        assertEquals(workout,ended.getJSONObject("active").toString());
    }
    @Test public void oneOverlayStaysAttachedAcrossAppsAndOvertime()throws Exception {
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
            assertEquals(1,windows.getViews().size());
            android.view.View timer=windows.getViews().get(0);
            assertTrue(RestTimerService.isFloatingVisible());
            MainActivity.visible=false;
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(250));
            assertSame(timer,windows.getViews().get(0));
            MainActivity.visible=true;
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(250));
            assertSame(timer,windows.getViews().get(0));

            JSONObject overdue=store.read().put("restEndAt",System.currentTimeMillis()-3000);
            assertNotNull(store.write(overdue.toString()));
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(6));
            assertSame(timer,windows.getViews().get(0));
            assertTrue(timer.getContentDescription().toString().startsWith("Rest · -0:"));
            Notification notification=shadowOf(context.getSystemService(NotificationManager.class)).getNotification(RestTimerService.NOTIFICATION);
            assertEquals("Rest complete",notification.extras.getCharSequence(Notification.EXTRA_TITLE));
            assertEquals("Ready for your next set",notification.extras.getCharSequence(Notification.EXTRA_TEXT));

            assertNotNull(store.write(store.read().put("restEndAt",0).toString()));
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(250));
            assertTrue(windows.getViews().isEmpty());
            assertFalse(RestTimerService.isFloatingVisible());
        }finally{
            controller.destroy();
            MainActivity.visible=false;
            ShadowSettings.setCanDrawOverlays(false);
        }
    }
    @Test public void savedDragPositionAppliesOnlyToItsOriginalRest()throws Exception{
        JSONObject state=new JSONObject().put("restStartedAt",10)
            .put("settings",new JSONObject().put("timerPosition",new JSONObject().put("x",.5).put("y",1)));
        assertNull(FloatingRestTimer.positionForRest(state));
        JSONObject position=new JSONObject().put("started",10).put("x",.5).put("y",1);
        state.put("restPosition",position);
        assertSame(position,FloatingRestTimer.positionForRest(state));
        state.put("restStartedAt",11);
        assertNull(FloatingRestTimer.positionForRest(state));
    }
    @Test public void dismissZoneMustBeEnteredAndThenExpands(){
        FloatingRestTimer.DropHover hover=new FloatingRestTimer.DropHover();
        assertFalse(hover.update(8,16,38,50));
        assertFalse(hover.update(8,44,38,50));
        assertFalse(hover.update(8,44,38,50));
        assertTrue(hover.update(8,35,38,50));
        assertTrue(hover.update(8,48,38,50));
        assertFalse(hover.update(8,51,38,50));
        assertFalse(hover.update(8,48,38,50));
        hover.reset();
        assertTrue(hover.update(90,35,38,50));
    }
}
