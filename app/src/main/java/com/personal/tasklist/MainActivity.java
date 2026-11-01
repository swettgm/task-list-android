package com.personal.tasklist;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.List;

/** Native Android task lists with local storage and a customizable home-screen widget. */
public final class MainActivity extends Activity {
    private static final int NAVY = Color.rgb(31, 43, 64);
    private static final int BLUE = Color.rgb(25, 91, 126);
    private static final int MUTED = Color.rgb(91, 111, 139);
    private TaskStore store;
    private LinearLayout rows;
    private TextView summary;
    private Spinner listFilter;
    private Switch completedSwitch;
    // Empty is the all-lists selection; a real checklist may be named "All lists".
    private String selectedList = "";
    private boolean refreshing;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        store = new TaskStore(this);
        if (state != null) selectedList = state.getString("list", "");
        String widgetList = getIntent().getStringExtra("widget_list");
        if (state == null && widgetList != null && !widgetList.isEmpty()) selectedList = widgetList;
        buildScreen();
        if (state != null) completedSwitch.setChecked(state.getBoolean("completed", false));
        if (state == null && getIntent().getBooleanExtra("add_task", false))
            rows.post(() -> showTaskEditor(null));
    }

    @Override protected void onResume() { super.onResume(); refresh(); }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent.hasExtra("widget_list")) {
            String list = intent.getStringExtra("widget_list");
            selectedList = list == null ? "" : list;
        }
        refresh();
        if (intent.getBooleanExtra("add_task", false)) showTaskEditor(null);
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        out.putString("list", selectedList);
        out.putBoolean("completed", completedSwitch.isChecked());
        super.onSaveInstanceState(out);
    }

    private void buildScreen() {
        LinearLayout root = column();
        root.setBackgroundColor(Color.WHITE);
        root.setPadding(dp(24), 0, dp(24), 0);
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            view.setPadding(dp(24) + insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop() + dp(14),
                dp(24) + insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom() + dp(12));
            return insets;
        });
        setContentView(root);
        root.requestApplyInsets();
        TextView eyebrow = text("YOUR TASKS", 11, MUTED, false);
        eyebrow.setLetterSpacing(0.06f);
        root.addView(eyebrow);
        addSpace(root, 12);
        root.addView(line());
        addSpace(root, 19);
        root.addView(text("Task List", 29, NAVY, true));
        root.addView(text("Tasks & checklists", 20, BLUE, true));
        addSpace(root, 8);
        root.addView(text(LocalDate.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL)), 13, MUTED, false));
        summary = text("", 13, MUTED, false);
        summary.setPadding(0, dp(5), 0, dp(13));
        root.addView(summary);

        LinearLayout controls = row();
        listFilter = new Spinner(this);
        listFilter.setContentDescription("Filter by checklist");
        controls.addView(listFilter, new LinearLayout.LayoutParams(0, dp(48), 1));
        completedSwitch = new Switch(this);
        completedSwitch.setText("Done ");
        completedSwitch.setTextSize(13);
        completedSwitch.setTextColor(MUTED);
        completedSwitch.setContentDescription("Show completed tasks");
        completedSwitch.setOnCheckedChangeListener((button, value) -> renderTasks());
        controls.addView(completedSwitch, new LinearLayout.LayoutParams(-2, dp(48)));
        root.addView(controls);
        listFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!refreshing) { selectedList = position == 0 ? "" : parent.getItemAtPosition(position).toString(); renderTasks(); }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) { }
        });
        addSpace(root, 8);
        Button add = button("+  Add task", true);
        add.setOnClickListener(view -> showTaskEditor(null));
        root.addView(add, new LinearLayout.LayoutParams(-1, dp(50)));
        addSpace(root, 12);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        rows = column();
        scroll.addView(rows);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));
        addSpace(root, 12);
        root.addView(line());
        addSpace(root, 9);
        Button widget = button("Home widget", false);
        widget.setOnClickListener(view -> showWidgetHelp());
        root.addView(widget, new LinearLayout.LayoutParams(-1, dp(48)));
    }

    private void refresh() {
        if (listFilter == null) return;
        try {
            refreshing = true;
            List<String> filters = new ArrayList<>();
            List<String> knownLists = store.lists();
            // Widgets retain a chosen checklist even when its last task is deleted.
            if (!selectedList.isEmpty() && !knownLists.contains(selectedList)) knownLists.add(selectedList);
            filters.add("All lists"); filters.addAll(knownLists);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, filters);
            listFilter.setAdapter(adapter);
            listFilter.setSelection(selectedList.isEmpty() ? 0 : filters.subList(1, filters.size()).indexOf(selectedList) + 1);
            refreshing = false;
            renderTasks();
        } catch (RuntimeException error) {
            refreshing = false;
            showError(error);
        }
    }

    private void renderTasks() {
        if (rows == null) return;
        rows.removeAllViews();
        List<TaskStore.Task> all;
        try { all = store.all(); }
        catch (RuntimeException error) { showError(error); return; }
        int done = 0, total = 0, shown = 0;
        for (TaskStore.Task task : all) {
            if (!selectedList.isEmpty() && !selectedList.equals(task.listName)) continue;
            total++;
            if (task.done) done++;
            if (task.done && !completedSwitch.isChecked()) continue;
            shown++;
            addTaskRow(task);
        }
        summary.setText(total == 0 ? "Your lists, at your own pace." : (total - done) + " open · " + done + " complete");
        if (shown == 0) {
            LinearLayout empty = column();
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(dp(16), dp(40), dp(16), dp(32));
            TextView title = text(total == 0 ? "A fresh checklist" : "All caught up", 22, NAVY, true);
            title.setGravity(Gravity.CENTER);
            empty.addView(title);
            TextView hint = text(total == 0 ? "Add your first task, then keep your list close with a home-screen widget."
                : "Add another task, or turn on Done to see what you’ve completed.", 15, MUTED, false);
            hint.setGravity(Gravity.CENTER);
            hint.setPadding(0, dp(12), 0, 0);
            hint.setLineSpacing(dp(4), 1);
            empty.addView(hint);
            rows.addView(empty, new LinearLayout.LayoutParams(-1, -1));
        }
    }

    private void addTaskRow(TaskStore.Task task) {
        LinearLayout item = row();
        item.setPadding(0, dp(10), 0, dp(10));
        CheckBox check = new CheckBox(this);
        check.setChecked(task.done);
        check.setContentDescription((task.done ? "Reopen " : "Complete ") + task.title);
        check.setOnCheckedChangeListener((view, value) -> {
            try { store.toggle(task.id); refresh(); } catch (RuntimeException error) { showError(error); }
        });
        item.addView(check, new LinearLayout.LayoutParams(dp(48), dp(48)));
        LinearLayout details = column();
        TextView title = text(task.title, 16, task.done ? MUTED : NAVY, false);
        title.setMaxLines(4);
        if (task.done) title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        details.addView(title);
        String detail = task.listName;
        if (!task.dueDate.isEmpty()) {
            LocalDate due = LocalDate.parse(task.dueDate);
            String date = due.equals(LocalDate.now()) ? "Today" : due.format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
            detail += " · " + date;
        }
        TextView meta = text(detail, 12, MUTED, false);
        meta.setPadding(0, dp(5), 0, 0);
        details.addView(meta);
        details.setContentDescription("Edit " + task.title);
        details.setOnClickListener(view -> showTaskEditor(task));
        item.addView(details, new LinearLayout.LayoutParams(0, -2, 1));
        Button edit = button("Edit", false);
        edit.setTextSize(12);
        edit.setMinWidth(0); edit.setMinimumWidth(0);
        edit.setPadding(dp(8), 0, dp(8), 0);
        edit.setContentDescription("Edit " + task.title);
        edit.setOnClickListener(view -> showTaskEditor(task));
        item.addView(edit, new LinearLayout.LayoutParams(dp(56), dp(48)));
        rows.addView(item);
        rows.addView(line());
    }

    private void showTaskEditor(TaskStore.Task task) {
        final List<String> editorLists;
        try { editorLists = store.lists(); }
        catch (RuntimeException error) { showError(error); return; }
        LinearLayout content = column();
        content.setPadding(dp(24), dp(8), dp(24), dp(8));
        EditText title = new EditText(this);
        title.setHint("What needs doing?");
        title.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        title.setMaxLines(3);
        if (task != null) title.setText(task.title);
        content.addView(title, new LinearLayout.LayoutParams(-1, -2));
        addSpace(content, 12);
        content.addView(text("Checklist", 12, MUTED, true));
        AutoCompleteTextView list = new AutoCompleteTextView(this);
        list.setHint(TaskStore.DEFAULT_LIST);
        list.setSingleLine(true);
        list.setThreshold(0);
        list.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, editorLists));
        list.setText(task != null ? task.listName : (selectedList.isEmpty() ? TaskStore.DEFAULT_LIST : selectedList));
        list.setOnClickListener(view -> list.showDropDown());
        content.addView(list, new LinearLayout.LayoutParams(-1, -2));
        content.addView(text("Type a new name to create another checklist.", 12, MUTED, false));
        addSpace(content, 16);
        final String[] due = { task == null ? "" : task.dueDate };
        LinearLayout dates = row();
        Button date = button(due[0].isEmpty() ? "Set due date" : due[0], false);
        date.setOnClickListener(view -> {
            LocalDate initial = due[0].isEmpty() ? LocalDate.now() : LocalDate.parse(due[0]);
            new DatePickerDialog(this, (picker, year, month, day) -> {
                due[0] = LocalDate.of(year, month + 1, day).toString();
                date.setText(due[0]);
            }, initial.getYear(), initial.getMonthValue() - 1, initial.getDayOfMonth()).show();
        });
        dates.addView(date, new LinearLayout.LayoutParams(0, dp(48), 1));
        Button clearDate = button("Clear date", false);
        clearDate.setOnClickListener(view -> { due[0] = ""; date.setText("Set due date"); });
        dates.addView(clearDate, new LinearLayout.LayoutParams(0, dp(48), 1));
        content.addView(dates);
        ScrollView scroller = new ScrollView(this);
        scroller.addView(content);
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setTitle(task == null ? "Add task" : "Edit task")
            .setView(scroller).setNegativeButton("Cancel", null).setPositiveButton("Save", null);
        if (task != null) builder.setNeutralButton("Delete", (dialog, which) -> new AlertDialog.Builder(this)
            .setTitle("Delete this task?").setMessage(task.title).setNegativeButton("Keep", null)
            .setPositiveButton("Delete", (confirm, choice) -> {
                try { store.delete(task.id); refresh(); } catch (RuntimeException error) { showError(error); }
            }).show());
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
            if (title.getText().toString().trim().isEmpty()) { title.setError("Enter a task title"); return; }
            try {
                if (task == null) store.add(title.getText().toString(), list.getText().toString(), due[0]);
                else store.update(task.id, title.getText().toString(), list.getText().toString(), due[0]);
                dialog.dismiss();
                refresh();
            } catch (RuntimeException error) { showError(error); }
        }));
        dialog.show();
    }

    private void showWidgetHelp() {
        AppWidgetManager manager = AppWidgetManager.getInstance(this);
        String help = "Touch and hold an empty space on your home screen, choose Widgets, then add Task List checklist.\n\n"
            + "Choose its title, checklist, view, theme, and background transparency. Resize it on your home screen. "
            + "Tap the circle beside a task to complete it; use the settings button on the widget to customize it again.";
        AlertDialog.Builder dialog = new AlertDialog.Builder(this).setTitle("Your home-screen checklist")
            .setMessage(help).setNegativeButton("Close", null);
        if (manager.isRequestPinAppWidgetSupported()) dialog.setPositiveButton("Add widget", (choice, which) -> {
            try {
                boolean requested = manager.requestPinAppWidget(new ComponentName(this, TaskWidgetProvider.class), null, null);
                if (!requested) Toast.makeText(this, "Add the widget from your home screen’s Widgets menu.", Toast.LENGTH_LONG).show();
            } catch (RuntimeException error) {
                Toast.makeText(this, "Add the widget from your home screen’s Widgets menu.", Toast.LENGTH_LONG).show();
            }
        });
        dialog.show();
    }

    private void showError(RuntimeException error) {
        new AlertDialog.Builder(this).setTitle("Couldn’t update your tasks")
            .setMessage(error.getMessage() == null ? "Please try again." : error.getMessage())
            .setPositiveButton("OK", null).show();
    }

    private LinearLayout column() {
        LinearLayout view = new LinearLayout(this); view.setOrientation(LinearLayout.VERTICAL); return view;
    }
    private LinearLayout row() {
        LinearLayout view = new LinearLayout(this); view.setOrientation(LinearLayout.HORIZONTAL);
        view.setGravity(Gravity.CENTER_VERTICAL); return view;
    }
    private TextView text(String value, int size, int color, boolean bold) {
        TextView view = new TextView(this); view.setText(value); view.setTextSize(size); view.setTextColor(color);
        if (bold) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }
    private Button button(String value, boolean primary) {
        Button view = new Button(this); view.setText(value); view.setAllCaps(false); view.setTextSize(14);
        view.setTextColor(primary ? Color.WHITE : BLUE);
        if (primary) {
            GradientDrawable background = new GradientDrawable(); background.setColor(NAVY);
            background.setCornerRadius(dp(7)); view.setBackground(background);
        } else view.setBackgroundColor(Color.TRANSPARENT);
        return view;
    }
    private View line() {
        View view = new View(this); view.setBackgroundColor(Color.rgb(216, 226, 236));
        view.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(1))); return view;
    }
    private void addSpace(LinearLayout parent, int height) { parent.addView(new View(this), new LinearLayout.LayoutParams(1, dp(height))); }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
