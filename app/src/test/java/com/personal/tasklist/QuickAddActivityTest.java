package com.personal.tasklist;

import static org.junit.Assert.*;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import java.time.LocalDate;
import java.time.ZoneId;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.android.controller.ActivityController;

/** The home-screen popup must save through the canonical store without opening the app. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class QuickAddActivityTest {
    private Context context;
    private ReferenceStore reference;

    @Before public void setUp() {
        context = RuntimeEnvironment.getApplication();
        context.getSharedPreferences("reference_state", Context.MODE_PRIVATE).edit().clear().commit();
        context.getSharedPreferences("tasks", Context.MODE_PRIVATE).edit().clear().commit();
        reference = new ReferenceStore(context);
    }

    @Test public void saveFromWidgetKeepsContextAndSchedulesTomorrowWithoutOpeningMain() throws Exception {
        reference.writeState(new JSONObject().put("customTasks", new JSONArray())
                .put("completed", new JSONObject().put("earlier:2026-11-01", true))
                .put("futureField", "keep").toString());
        long previousRevision = reference.revision();
        Intent shortcut = new Intent(context, QuickAddActivity.class)
                .putExtra("widget_label", "On the phone").putExtra("widget_list", "Personal");
        ActivityController<QuickAddActivity> controller = Robolectric.buildActivity(QuickAddActivity.class, shortcut).setup();
        QuickAddActivity activity = controller.get();
        idleMainLooper();
        View decor = activity.getWindow().getDecorView();
        EditText title = findDescription(decor, "Task title", EditText.class);
        Spinner label = findDescription(decor, "Context label", Spinner.class);
        Spinner priority = findDescription(decor, "Priority", Spinner.class);
        Spinner when = findDescription(decor, "When to do it", Spinner.class);
        assertNotNull(title); assertNotNull(label); assertNotNull(priority); assertNotNull(when);
        assertEquals("📱", label.getSelectedItem().toString());
        title.setText("  Call the office  ");
        select(priority, "High");
        select(when, "Tomorrow");
        idleMainLooper();
        Button save = findButton(decor, "Add task");
        assertNotNull(save);
        save.performClick();
        idleMainLooper();

        assertTrue(activity.isFinishing());
        assertNull("Quick add must not navigate to MainActivity", Shadows.shadowOf(activity).getNextStartedActivity());
        JSONObject state = new JSONObject(reference.readState());
        assertEquals("keep", state.getString("futureField"));
        assertTrue(state.getJSONObject("completed").getBoolean("earlier:2026-11-01"));
        assertTrue(reference.revision() > previousRevision);
        JSONArray custom = state.getJSONArray("customTasks");
        assertEquals(1, custom.length());
        JSONObject added = custom.getJSONObject(0);
        String tomorrow = LocalDate.now(ZoneId.of("America/New_York")).plusDays(1).toString();
        assertEquals("Call the office", added.getString("title"));
        assertEquals("HIGH", added.getString("priority"));
        assertEquals(tomorrow, added.getString("due"));
        assertEquals(1, added.getJSONArray("labels").length());
        assertEquals("On the phone", added.getJSONArray("labels").getString(0));
        TaskStore.Task mirrored = null;
        for (TaskStore.Task task : new TaskStore(context).all())
            if (task.title.equals("Call the office")) mirrored = task;
        assertNotNull("The widget must see the task before the full app is opened", mirrored);
        assertTrue(mirrored.id.startsWith("html:"));
        assertEquals("HIGH", mirrored.priority);
        assertEquals(tomorrow, mirrored.plannedDate);
        assertTrue(mirrored.labels.contains("On the phone"));
        controller.pause().stop().destroy();
    }

    @Test public void cancelLeavesCanonicalStateUntouched() {
        reference.writeState("{\"customTasks\":[],\"futureField\":\"keep\"}");
        String before = reference.readState();
        long previousRevision = reference.revision();
        ActivityController<QuickAddActivity> controller = Robolectric.buildActivity(QuickAddActivity.class).setup();
        QuickAddActivity activity = controller.get();
        idleMainLooper();
        EditText title = findDescription(activity.getWindow().getDecorView(), "Task title", EditText.class);
        assertNotNull(title);
        title.setText("A draft that is not saved");
        Button cancel = findButton(activity.getWindow().getDecorView(), "Cancel");
        assertNotNull(cancel);
        cancel.performClick();
        idleMainLooper();
        assertTrue(activity.isFinishing());
        assertEquals(before, reference.readState());
        assertEquals(previousRevision, reference.revision());
        assertTrue(new TaskStore(context).all().isEmpty());
        assertNull(Shadows.shadowOf(activity).getNextStartedActivity());
        controller.pause().stop().destroy();
    }

    @Test public void blankTitleShowsValidationAndKeepsPopupOpen() {
        String before = reference.readState();
        long previousRevision = reference.revision();
        ActivityController<QuickAddActivity> controller = Robolectric.buildActivity(QuickAddActivity.class).setup();
        QuickAddActivity activity = controller.get();
        idleMainLooper();
        EditText title = findDescription(activity.getWindow().getDecorView(), "Task title", EditText.class);
        assertNotNull(title);
        title.setText("   ");
        Button save = findButton(activity.getWindow().getDecorView(), "Add task");
        assertNotNull(save);
        save.performClick();
        idleMainLooper();
        assertFalse(activity.isFinishing());
        assertNotNull(title.getError());
        assertEquals(before, reference.readState());
        assertEquals(previousRevision, reference.revision());
        assertTrue(new TaskStore(context).all().isEmpty());
        assertNull(Shadows.shadowOf(activity).getNextStartedActivity());
        controller.pause().stop().destroy();
    }

    private static void select(Spinner spinner, String wanted) {
        for (int i = 0; i < spinner.getCount(); i++) {
            String label = spinner.getItemAtPosition(i).toString().replace(" priority", "");
            if (label.equalsIgnoreCase(wanted)) { spinner.setSelection(i); return; }
        }
        fail("Missing popup option: " + wanted);
    }

    private static void idleMainLooper() { Shadows.shadowOf(Looper.getMainLooper()).idle(); }

    private static Button findButton(View view, String text) {
        if (view instanceof Button && text.contentEquals(((Button) view).getText())) return (Button) view;
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                Button found = findButton(group.getChildAt(i), text);
                if (found != null) return found;
            }
        }
        return null;
    }

    private static <T extends View> T findDescription(View view, String description, Class<T> kind) {
        CharSequence actual = view.getContentDescription();
        if (kind.isInstance(view) && actual != null && description.contentEquals(actual)) return kind.cast(view);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                T found = findDescription(group.getChildAt(i), description, kind);
                if (found != null) return found;
            }
        }
        return null;
    }

    @Test public void widgetPopupUsesSavedGradientAndRoundedClippedPanel() throws Exception {
        reference.writeState("{\"uiTheme\":{\"accent\":\"#ffffff\",\"background\":\"#123456\",\"gradient\":true,\"gradientEnd\":\"#654321\",\"gradientAngle\":135}}");
        ActivityController<QuickAddActivity> controller=Robolectric.buildActivity(QuickAddActivity.class).setup();QuickAddActivity activity=controller.get();
        android.view.ViewGroup content=activity.findViewById(android.R.id.content);android.widget.ScrollView panel=(android.widget.ScrollView)content.getChildAt(0);
        assertTrue(panel.getClipToOutline());assertTrue(panel.getBackground() instanceof PopupAppearance.Background);
        PopupAppearance.Background background=(PopupAppearance.Background)panel.getBackground();assertTrue(background.appearance.gradient);assertEquals(android.graphics.Color.parseColor("#123456"),background.appearance.start);assertEquals(android.graphics.Color.parseColor("#654321"),background.appearance.end);assertEquals(135,background.appearance.angle);
        background.setBounds(0,0,400,300);android.graphics.Outline outline=new android.graphics.Outline();background.getOutline(outline);assertTrue(outline.getRadius()>0);
        android.widget.EditText title=findDescription(activity.getWindow().getDecorView(),"Task title",android.widget.EditText.class);assertEquals(background.appearance.foreground,title.getCurrentTextColor());assertNull(title.getBackgroundTintList());assertTrue(title.getBackground() instanceof android.graphics.drawable.GradientDrawable);assertTrue(((android.graphics.drawable.GradientDrawable)title.getBackground()).getCornerRadius()>0);Button add=findButton(activity.getWindow().getDecorView(),"Add task");assertNull(add.getBackgroundTintList());assertEquals(background.appearance.accent,((android.graphics.drawable.GradientDrawable)add.getBackground()).getColor().getDefaultColor());assertTrue(title.hasFocus());controller.pause().stop().destroy();
    }
    @Test public void popupFieldsHaveVisibleGapsWithoutGrowingThePanel() {
        ActivityController<QuickAddActivity> controller=Robolectric.buildActivity(QuickAddActivity.class).setup();QuickAddActivity activity=controller.get();View decor=activity.getWindow().getDecorView();
        EditText title=findDescription(decor,"Task title",EditText.class);Spinner label=findDescription(decor,"Context label",Spinner.class),priority=findDescription(decor,"Priority",Spinner.class),when=findDescription(decor,"When to do it",Spinner.class);Button cancel=findButton(decor,"Cancel"),save=findButton(decor,"Add task");
        ViewGroup form=(ViewGroup)title.getParent();float density=activity.getResources().getDisplayMetrics().density;int width=Math.round(320*density),gap=Math.round(8*density);
        form.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED));form.layout(0,0,width,form.getMeasuredHeight());
        assertEquals(Math.round(40*density),title.getHeight());assertEquals(title.getHeight(),label.getHeight());assertEquals(title.getHeight(),priority.getHeight());assertEquals(title.getHeight(),when.getHeight());
        assertEquals(gap,label.getTop()-title.getBottom());assertEquals(gap,((View)priority.getParent()).getTop()-label.getBottom());assertEquals(gap,when.getLeft()-priority.getRight());assertEquals(gap,save.getLeft()-cancel.getRight());
        assertEquals(gap,((View)cancel.getParent()).getTop()-((View)priority.getParent()).getBottom());assertTrue(form.getMeasuredHeight()<=Math.round(260*density));assertEquals(Math.round(20*density),form.getPaddingLeft());assertEquals(Math.round(20*density),form.getPaddingRight());controller.pause().stop().destroy();
    }
    @Test public void widgetPopupMatchesSolidLightThemeWhenGradientIsOff() throws Exception {
        reference.writeState("{\"uiTheme\":{\"background\":\"#ffffff\",\"accent\":\"#123\",\"gradient\":false}}");
        ActivityController<QuickAddActivity> controller=Robolectric.buildActivity(QuickAddActivity.class).setup();QuickAddActivity activity=controller.get();
        android.view.ViewGroup content=activity.findViewById(android.R.id.content);PopupAppearance.Background background=(PopupAppearance.Background)content.getChildAt(0).getBackground();assertFalse(background.appearance.gradient);assertEquals(android.graphics.Color.WHITE,background.appearance.start);assertEquals(android.graphics.Color.parseColor("#112233"),background.appearance.accent);assertEquals(android.graphics.Color.parseColor("#10151f"),background.appearance.foreground);controller.pause().stop().destroy();
    }
}
