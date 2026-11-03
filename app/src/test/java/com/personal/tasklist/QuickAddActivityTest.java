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

    @Test public void popupButtonsAndDropdownTextAreStartAligned() {
        ActivityController<QuickAddActivity> controller=Robolectric.buildActivity(QuickAddActivity.class).setup();View root=controller.get().getWindow().getDecorView();EditText title=findDescription(root,"Task title",EditText.class);assertEquals(android.view.Gravity.START|android.view.Gravity.CENTER_VERTICAL,title.getGravity());assertEquals(View.TEXT_ALIGNMENT_VIEW_START,title.getTextAlignment());
        for(String text:new String[]{"Labels","Any time","Date","Cancel","Add task"}){Button button=findButton(root,text);assertNotNull(button);assertEquals(android.view.Gravity.START|android.view.Gravity.CENTER_VERTICAL,button.getGravity());assertEquals(View.TEXT_ALIGNMENT_VIEW_START,button.getTextAlignment());}
        for(String description:new String[]{"Priority","When to do it"}){Spinner spinner=findDescription(root,description,Spinner.class);android.widget.TextView selected=(android.widget.TextView)spinner.getAdapter().getView(spinner.getSelectedItemPosition(),null,spinner);android.widget.TextView option=(android.widget.TextView)spinner.getAdapter().getDropDownView(0,null,spinner);assertEquals(android.view.Gravity.START|android.view.Gravity.CENTER_VERTICAL,selected.getGravity());assertEquals(android.view.Gravity.START|android.view.Gravity.CENTER_VERTICAL,option.getGravity());}
        controller.pause().stop().destroy();
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
        Button label = findDescription(decor, "Context labels", Button.class);
        Spinner priority = findDescription(decor, "Priority", Spinner.class);
        Spinner when = findDescription(decor, "When to do it", Spinner.class);
        assertNotNull(title); assertNotNull(label); assertNotNull(priority); assertNotNull(when);
        assertEquals("📱", label.getText().toString());
        title.setText("  Call the office  ");
        select(priority, "High");
        select(when, "Tomorrow");
        idleMainLooper();
        Button save = findButton(decor, "Add task");
        assertNotNull(save);
        save.performClick();
        idleMainLooper();

        assertFalse(activity.isFinishing());assertEquals("",title.getText().toString());assertTrue(title.hasFocus());
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

    @Test public void quickAddKeepsPopupOpenForConsecutiveTasksAndClearsItsSuccessBorder() throws Exception {
        reference.writeState("{\"customTasks\":[],\"completed\":{\"keep:old\":true}}");ActivityController<QuickAddActivity> controller=Robolectric.buildActivity(QuickAddActivity.class).setup();QuickAddActivity activity=controller.get();View decor=activity.getWindow().getDecorView();EditText title=findDescription(decor,"Task title",EditText.class);Button add=findButton(decor,"Add task");
        title.setText("First batch task");add.performClick();idleMainLooper();assertFalse(activity.isFinishing());assertEquals("",title.getText().toString());title.setText("Second batch task");title.onEditorAction(android.view.inputmethod.EditorInfo.IME_ACTION_DONE);idleMainLooper();JSONObject saved=new JSONObject(reference.readState());JSONArray tasks=saved.getJSONArray("customTasks");assertEquals(2,tasks.length());assertNotEquals(tasks.getJSONObject(0).getString("id"),tasks.getJSONObject(1).getString("id"));assertEquals("",title.getText().toString());assertTrue(title.hasFocus());assertTrue(saved.getJSONObject("completed").getBoolean("keep:old"));assertFalse(activity.isFinishing());
        android.widget.ScrollView panel=(android.widget.ScrollView)((ViewGroup)activity.findViewById(android.R.id.content)).getChildAt(0);assertFalse(panel.isVerticalScrollBarEnabled());assertFalse(panel.isHorizontalScrollBarEnabled());Shadows.shadowOf(Looper.getMainLooper()).idleFor(java.time.Duration.ofMillis(1200));assertNull(panel.getForeground());title.setText("Unsaved third task");findButton(decor,"Cancel").performClick();assertTrue(activity.isFinishing());assertEquals(2,new JSONObject(reference.readState()).getJSONArray("customTasks").length());controller.pause().stop().destroy();
    }

    @Test public void multipleLabelsAndTimeAreSavedThenAllFieldsReset() throws Exception {
        reference.writeState("{\"customTasks\":[]}");ActivityController<QuickAddActivity> controller=Robolectric.buildActivity(QuickAddActivity.class).setup();QuickAddActivity activity=controller.get();View root=activity.getWindow().getDecorView();Button labels=findDescription(root,"Context labels",Button.class);labels.performClick();android.app.AlertDialog picker=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();android.widget.ListView list=picker.getListView();layoutLabels(list);for(int index:new int[]{0,2})list.performItemClick(list.getChildAt(index),index,index);assertTrue("Home label checked after choosing",list.isItemChecked(0));assertTrue("Phone label checked after choosing",list.isItemChecked(2));picker.getButton(android.content.DialogInterface.BUTTON_POSITIVE).performClick();labels.performClick();picker=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();layoutLabels(picker.getListView());assertTrue("Home label stays checked when reopened",picker.getListView().isItemChecked(0));assertTrue("Phone label stays checked when reopened",picker.getListView().isItemChecked(2));picker.getButton(android.content.DialogInterface.BUTTON_POSITIVE).performClick();
        EditText title=findDescription(root,"Task title",EditText.class);title.setText("Multi label task");select(findDescription(root,"Priority",Spinner.class),"High");select(findDescription(root,"When to do it",Spinner.class),"Weekend");idleMainLooper();findButton(root,"Add task").performClick();idleMainLooper();JSONObject first=new JSONObject(reference.readState()).getJSONArray("customTasks").getJSONObject(0);assertEquals(2,first.getJSONArray("labels").length());assertTrue(first.getBoolean("weekend"));assertEquals(java.time.DayOfWeek.FRIDAY,java.time.LocalDate.parse(first.getString("due")).getDayOfWeek());assertEquals("HIGH",first.getString("priority"));assertEquals("",title.getText().toString());assertEquals("Labels",labels.getText().toString());assertEquals(3,findDescription(root,"Priority",Spinner.class).getSelectedItemPosition());android.widget.ListAdapter priorityChoices=(android.widget.ListAdapter)findDescription(root,"Priority",Spinner.class).getAdapter();assertFalse(priorityChoices.isEnabled(3));assertEquals("Priority",priorityChoices.getItem(3).toString());for(int p=0;p<3;p++)assertTrue(priorityChoices.isEnabled(p));assertEquals(0,findDescription(root,"When to do it",Spinner.class).getSelectedItemPosition());assertEquals("Any time",findDescription(root,"Task time",Button.class).getText().toString());title.setText("Clean next task");findButton(root,"Add task").performClick();JSONObject next=new JSONObject(reference.readState()).getJSONArray("customTasks").getJSONObject(1);assertEquals(0,next.getJSONArray("labels").length());assertEquals("UNSET",next.getString("priority"));assertFalse(next.has("weekend"));assertFalse(next.has("time"));controller.pause().stop().destroy();
    }

    @Test public void nativeDateAndTimePickersSaveValuesAndClearThemForTheNextTask()throws Exception{
        reference.writeState("{\"customTasks\":[]}");ActivityController<QuickAddActivity> controller=Robolectric.buildActivity(QuickAddActivity.class).setup();QuickAddActivity activity=controller.get();View root=activity.getWindow().getDecorView();findDescription(root,"Task date",Button.class).performClick();android.app.DatePickerDialog date=(android.app.DatePickerDialog)org.robolectric.shadows.ShadowDialog.getLatestDialog();date.updateDate(2026,11,10);date.getButton(android.content.DialogInterface.BUTTON_POSITIVE).performClick();idleMainLooper();assertEquals(android.view.HapticFeedbackConstants.CLOCK_TICK,Shadows.shadowOf(root).lastHapticFeedbackPerformed());findDescription(root,"Task time",Button.class).performClick();android.app.TimePickerDialog time=(android.app.TimePickerDialog)org.robolectric.shadows.ShadowDialog.getLatestDialog();time.updateTime(14,30);time.getButton(android.content.DialogInterface.BUTTON_POSITIVE).performClick();assertEquals(android.view.HapticFeedbackConstants.CLOCK_TICK,Shadows.shadowOf(root).lastHapticFeedbackPerformed());EditText title=findDescription(root,"Task title",EditText.class);title.setText("Picked date and time");findButton(root,"Add task").performClick();idleMainLooper();JSONObject saved=new JSONObject(reference.readState()).getJSONArray("customTasks").getJSONObject(0);assertEquals("2026-12-10",saved.getString("due"));assertEquals("14:30",saved.getString("time"));assertEquals("Any time",findDescription(root,"Task time",Button.class).getText().toString());assertEquals(0,findDescription(root,"When to do it",Spinner.class).getSelectedItemPosition());controller.pause().stop().destroy();
    }

    @Test public void widgetPlusEntranceProvidesLightHapticFeedback(){
        Intent intent=new Intent(context,QuickAddActivity.class).putExtra("add_task",true);ActivityController<QuickAddActivity> controller=Robolectric.buildActivity(QuickAddActivity.class,intent).setup();QuickAddActivity activity=controller.get();activity.onWindowFocusChanged(true);assertEquals(android.view.HapticFeedbackConstants.CLOCK_TICK,Shadows.shadowOf(activity.getWindow().getDecorView()).lastHapticFeedbackPerformed());controller.pause().stop().destroy();
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

    private static void layoutLabels(android.widget.ListView list){idleMainLooper();list.measure(View.MeasureSpec.makeMeasureSpec(400,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(400,View.MeasureSpec.EXACTLY));list.layout(0,0,400,400);}

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
        EditText title=findDescription(decor,"Task title",EditText.class);Button label=findDescription(decor,"Context labels",Button.class);Spinner priority=findDescription(decor,"Priority",Spinner.class),when=findDescription(decor,"When to do it",Spinner.class);Button cancel=findButton(decor,"Cancel"),save=findButton(decor,"Add task");
        ViewGroup form=(ViewGroup)title.getParent();float density=activity.getResources().getDisplayMetrics().density;int width=Math.round(320*density),gap=Math.round(8*density);
        form.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED));form.layout(0,0,width,form.getMeasuredHeight());
        assertEquals(Math.round(40*density),title.getHeight());assertEquals(title.getHeight(),label.getHeight());assertEquals(title.getHeight(),priority.getHeight());assertEquals(title.getHeight(),when.getHeight());
        assertEquals(gap,label.getTop()-title.getBottom());assertEquals(gap,((View)priority.getParent()).getTop()-label.getBottom());assertEquals(gap,when.getLeft()-priority.getRight());assertEquals(gap,save.getLeft()-cancel.getRight());
        View timeRow=(View)findDescription(decor,"Task time",Button.class).getParent();assertEquals(gap,timeRow.getTop()-((View)priority.getParent()).getBottom());assertEquals(gap,((View)cancel.getParent()).getTop()-timeRow.getBottom());int compactHeight=form.getMeasuredHeight();
        // Compare against the previous layout at the same width and font scale.
        android.widget.LinearLayout.LayoutParams titleParams=(android.widget.LinearLayout.LayoutParams)title.getLayoutParams();titleParams.height=Math.round(56*density);titleParams.topMargin=0;title.setLayoutParams(titleParams);
        android.widget.LinearLayout.LayoutParams labelParams=(android.widget.LinearLayout.LayoutParams)label.getLayoutParams();labelParams.height=Math.round(48*density);labelParams.topMargin=0;label.setLayoutParams(labelParams);
        View options=(View)priority.getParent(),actions=(View)cancel.getParent();
        for(View row:new View[]{options,actions}){android.widget.LinearLayout.LayoutParams params=(android.widget.LinearLayout.LayoutParams)row.getLayoutParams();params.height=Math.round(48*density);params.topMargin=0;row.setLayoutParams(params);}
        form.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED));assertTrue("Spaced controls must not enlarge the prior popup",compactHeight<=form.getMeasuredHeight());assertEquals(Math.round(20*density),form.getPaddingLeft());assertEquals(Math.round(20*density),form.getPaddingRight());controller.pause().stop().destroy();
    }
    @Test public void widgetPopupMatchesSolidLightThemeWhenGradientIsOff() throws Exception {
        reference.writeState("{\"uiTheme\":{\"background\":\"#ffffff\",\"accent\":\"#123\",\"gradient\":false}}");
        ActivityController<QuickAddActivity> controller=Robolectric.buildActivity(QuickAddActivity.class).setup();QuickAddActivity activity=controller.get();
        android.view.ViewGroup content=activity.findViewById(android.R.id.content);PopupAppearance.Background background=(PopupAppearance.Background)content.getChildAt(0).getBackground();assertFalse(background.appearance.gradient);assertEquals(android.graphics.Color.WHITE,background.appearance.start);assertEquals(android.graphics.Color.parseColor("#112233"),background.appearance.accent);assertEquals(android.graphics.Color.parseColor("#10151f"),background.appearance.foreground);controller.pause().stop().destroy();
    }
}
