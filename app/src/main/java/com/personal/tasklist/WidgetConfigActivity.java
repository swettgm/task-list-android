package com.personal.tasklist;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/** Standard launcher configuration activity; edits are committed only with Save. */
public final class WidgetConfigActivity extends Activity {
    private int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private Spinner listInput, labelInput, colorInput;
    private SeekBar opacityInput;
    private TextView opacityLabel, previewTitle, previewTask;
    private LinearLayout preview;
    private final List<String> lists = new ArrayList<>();
    private final List<String> contexts = new ArrayList<>();
    private boolean ready;
    private static final int NAVY = Color.rgb(31, 43, 64);
    private static final int BLUE = Color.rgb(25, 91, 126);
    private static final int BACKGROUND = Color.rgb(16, 21, 31);
    private static final int SURFACE = Color.rgb(24, 33, 50);
    private static final int FOREGROUND = Color.rgb(230, 237, 247);
    private static final int MUTED = Color.rgb(167, 182, 200);
    private static final int ACCENT = Color.rgb(104, 183, 255);

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setResult(RESULT_CANCELED);
        widgetId = getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) { finish(); return; }
        android.appwidget.AppWidgetProviderInfo widgetInfo = AppWidgetManager.getInstance(this).getAppWidgetInfo(widgetId);
        android.content.ComponentName expectedProvider = new android.content.ComponentName(this, TaskWidgetProvider.class);
        if (widgetInfo == null || !expectedProvider.equals(widgetInfo.provider)) { finish(); return; }
        SharedPreferences prefs = TaskWidgetProvider.preferences(this, widgetId);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BACKGROUND);
        root.setPadding(dp(22), dp(12), dp(22), dp(12));
        root.setOnApplyWindowInsetsListener((v, insets) -> {
            int top, bottom;
            if (android.os.Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets bars = insets.getInsets(WindowInsets.Type.systemBars() | WindowInsets.Type.ime());
                top = bars.top; bottom = bars.bottom;
            } else { top = insets.getSystemWindowInsetTop(); bottom = insets.getSystemWindowInsetBottom(); }
            v.setPadding(dp(22), dp(12) + top, dp(22), dp(12) + bottom);
            return insets;
        });
        setContentView(root);

        LinearLayout heading = new LinearLayout(this);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = text("Customize widget", 24, FOREGROUND);
        title.setTypeface(null, Typeface.BOLD);
        heading.addView(title, new LinearLayout.LayoutParams(0, dp(52), 1));
        Button cancel = new Button(this);cancel.setBackground(new PopupAppearance(null).control(this,false)); cancel.setText("Cancel"); cancel.setTextColor(ACCENT); cancel.setOnClickListener(v -> finish());
        heading.addView(cancel);
        root.addView(heading);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        LinearLayout content = new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(content);
        TextView intro = text("Shows only today’s open tasks. Choose a checklist, context, colors and transparency.", 14, MUTED);
        intro.setPadding(0, dp(8), 0, dp(14)); content.addView(intro);

        preview = new LinearLayout(this); preview.setOrientation(LinearLayout.VERTICAL); preview.setPadding(dp(18), dp(14), dp(18), dp(14));
        previewTitle = text("", 18, NAVY); previewTitle.setTypeface(null, Typeface.BOLD);
        previewTask = text("", 14, NAVY); previewTask.setPadding(0, dp(6), 0, dp(6));
        preview.addView(previewTitle); preview.addView(previewTask);
        LinearLayout previewBackdrop = new LinearLayout(this);
        previewBackdrop.setPadding(dp(12), dp(12), dp(12), dp(12));
        previewBackdrop.setBackgroundColor(SURFACE);
        previewBackdrop.addView(preview, new LinearLayout.LayoutParams(-1, -2));
        content.addView(previewBackdrop);
        TextView previewNote = text("Preview · Scroll the widget to see all of today’s tasks.", 11, MUTED);
        previewNote.setPadding(0,dp(6),0,dp(8)); content.addView(previewNote);

        lists.add(""); contexts.add("");
        try {
            TaskStore store = new TaskStore(this);
            lists.addAll(store.lists());
            contexts.addAll(store.labels());
        }
        catch (RuntimeException unavailable) {
            TextView warning = text("Your saved checklists are unavailable. Open the app to check your data.", 13, ACCENT);
            warning.setPadding(0,dp(12),0,dp(4)); content.addView(warning);
        }
        String selectedList = prefs.getString("list", "");
        if (!selectedList.isEmpty() && !lists.contains(selectedList)) lists.add(selectedList);
        List<String> displayedLists = new ArrayList<>(lists); displayedLists.set(0, "All lists");
        label(content, "Checklist"); listInput = spinner(displayedLists); listInput.setSelection(Math.max(0, lists.indexOf(selectedList))); content.addView(listInput);
        listInput.setContentDescription("Choose the widget checklist");
        String selectedContext = prefs.getString("label", "");
        if (!selectedContext.isEmpty() && !contexts.contains(selectedContext)) contexts.add(selectedContext);
        List<CharSequence> displayedContexts = new ArrayList<>();for(String value:contexts)displayedContexts.add(TaskStore.contextDisplay(this,value)); displayedContexts.set(0, "All contexts");
        label(content, "Context label"); labelInput = spinner(displayedContexts);
        labelInput.setSelection(Math.max(0, contexts.indexOf(selectedContext)));
        labelInput.setContentDescription("Filter widget tasks by context label");
        content.addView(labelInput);
        label(content, "Colors"); colorInput = spinner(java.util.Arrays.asList("Paper — white and navy", "Night — navy and light blue")); colorInput.setSelection(prefs.getBoolean("dark", true) ? 1 : 0); content.addView(colorInput);
        opacityLabel = text("", 14, FOREGROUND); opacityLabel.setPadding(0, dp(20), 0, dp(5)); content.addView(opacityLabel);
        opacityInput = new SeekBar(this); opacityInput.setMax(100); opacityInput.setProgress(prefs.getInt("opacity",100)); content.addView(opacityInput, new LinearLayout.LayoutParams(-1, dp(48)));
        opacityInput.setContentDescription("Background opacity");
        opacityInput.setProgressTintList(android.content.res.ColorStateList.valueOf(ACCENT));
        opacityInput.setThumbTintList(android.content.res.ColorStateList.valueOf(ACCENT));
        TextView transparencyNote = text("Text stays readable while the background becomes more transparent.", 12, MUTED);
        transparencyNote.setPadding(0,0,0,dp(20)); content.addView(transparencyNote);

        Button save = new Button(this);save.setBackground(new PopupAppearance(null).control(this,true)); save.setText("Save widget"); save.setTextColor(BACKGROUND); save.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ACCENT));
        root.addView(save, new LinearLayout.LayoutParams(-1, dp(54)));
        save.setOnClickListener(v -> save());
        AdapterView.OnItemSelectedListener changed = new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) { updatePreview(); }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        };
        listInput.setOnItemSelectedListener(changed); labelInput.setOnItemSelectedListener(changed); colorInput.setOnItemSelectedListener(changed);
        opacityInput.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int value, boolean user) { updatePreview(); }
            @Override public void onStartTrackingTouch(SeekBar bar) { }
            @Override public void onStopTrackingTouch(SeekBar bar) { }
        });
        ready = true;
        if (state != null) {
            String savedList = state.getString("list", "");
            listInput.setSelection(Math.max(0, lists.indexOf(savedList)));
            labelInput.setSelection(Math.max(0, contexts.indexOf(state.getString("label", ""))));
            colorInput.setSelection(state.getBoolean("dark",true) ? 1 : 0);
            opacityInput.setProgress(state.getInt("opacity",100));
        }
        updatePreview();
    }

    private void updatePreview() {
        if (!ready) return;
        boolean dark = colorInput.getSelectedItemPosition() == 1;
        int opacity = opacityInput.getProgress();
        opacityLabel.setText("Background opacity · " + opacity + "%" + (opacity == 0 ? " · Fully transparent" : ""));
        previewTitle.setText("Today");
        previewTitle.setTextColor(dark ? Color.rgb(243,247,252) : NAVY);
        previewTask.setTextColor(dark ? Color.rgb(243,247,252) : NAVY);
        String list = lists.get(listInput.getSelectedItemPosition());
        String context = contexts.get(labelInput.getSelectedItemPosition());
        try {
            List<TaskStore.Task> tasks = TaskWidgetProvider.todayTasks(this, list, context);
            previewTask.setText(tasks.isEmpty() ? "No open tasks. Add one in the app."
                    : (TaskStore.IN_PROGRESS.equals(tasks.get(0).status) ? "▶  " : "○  ") + tasks.get(0).title);
        } catch (RuntimeException unavailable) {
            previewTask.setText("Open the app to check your saved tasks.");
        }
        GradientDrawable background = new GradientDrawable(); background.setCornerRadius(dp(18));
        int color = dark ? NAVY : Color.WHITE;
        background.setColor((Math.round(255 * opacity / 100f) << 24) | (color & 0x00FFFFFF));
        background.setStroke(dp(1), (Math.round(255 * opacity / 100f) << 24) | ((dark ? Color.rgb(53,70,95) : Color.rgb(220,229,237)) & 0x00FFFFFF));
        preview.setBackground(background);
    }

    private void save() {
        TaskWidgetProvider.preferences(this, widgetId).edit()
                .putString("title", "Today")
                .putString("list", lists.get(listInput.getSelectedItemPosition()))
                .putString("label", contexts.get(labelInput.getSelectedItemPosition()))
                .putBoolean("today", true)
                .putBoolean("dark", colorInput.getSelectedItemPosition() == 1)
                .putInt("opacity", opacityInput.getProgress())
                .putBoolean("configured", true).apply();
        TaskWidgetProvider.update(this, AppWidgetManager.getInstance(this), widgetId);
        setResult(RESULT_OK, new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId));
        finish();
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        if (!ready) return;
        state.putString("title", "Today");
        state.putString("list", lists.get(listInput.getSelectedItemPosition()));
        state.putString("label", contexts.get(labelInput.getSelectedItemPosition()));
        state.putBoolean("today", true);
        state.putBoolean("dark", colorInput.getSelectedItemPosition() == 1);
        state.putInt("opacity", opacityInput.getProgress());
    }

    private Spinner spinner(List<? extends CharSequence> values) {
        Spinner spinner = new Spinner(this);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, new ArrayList<CharSequence>(values)) {
            @Override public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                if (view instanceof TextView) ((TextView) view).setTextColor(FOREGROUND);
                return view;
            }
            @Override public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                view.setBackgroundColor(Color.TRANSPARENT);
                if (view instanceof TextView) ((TextView) view).setTextColor(FOREGROUND);
                view.setMinimumHeight(dp(48));
                return view;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);spinner.setBackground(new PopupAppearance(null).dropdown(this));spinner.setPadding(dp(12),0,dp(36),0);
        GradientDrawable popup = new GradientDrawable(); popup.setColor(SURFACE);popup.setCornerRadius(dp(10));popup.setStroke(dp(1),Color.rgb(53,70,95));
        spinner.setPopupBackgroundDrawable(popup);
        spinner.setMinimumHeight(dp(50));
        spinner.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(50)));
        return spinner;
    }

    private void label(LinearLayout parent, String value) {
        TextView label = text(value, 14, FOREGROUND); label.setTypeface(null, Typeface.BOLD);
        label.setPadding(0, dp(18), 0, dp(2)); parent.addView(label);
    }

    private TextView text(String value, int size, int color) {
        TextView view = new TextView(this); view.setText(value); view.setTextSize(size); view.setTextColor(color);
        view.setGravity(Gravity.CENTER_VERTICAL);
        view.setLayoutParams(new LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT));
        return view;
    }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
