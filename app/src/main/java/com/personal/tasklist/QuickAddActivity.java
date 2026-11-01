package com.personal.tasklist;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.*;
import java.util.*;
import java.io.*;
import java.nio.charset.StandardCharsets;

/** A dialog-sized activity launched directly above the home screen by a widget. */
public final class QuickAddActivity extends Activity {
    private EditText title;
    private Spinner context, priority, when;
    private final List<String> contexts = new ArrayList<>();
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setFinishOnTouchOutside(true);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        LinearLayout form = new LinearLayout(this); form.setOrientation(1);form.setPadding(dp(20),dp(16),dp(20),dp(12));
        form.setBackgroundColor(Color.rgb(24,33,50));
        TextView heading = new TextView(this);heading.setText("Add task");heading.setTextSize(20);heading.setTextColor(Color.rgb(230,237,247));form.addView(heading);
        title = new EditText(this);title.setContentDescription("Task title");title.setHint("What needs doing?");title.setSingleLine(true);title.setImeOptions(EditorInfo.IME_ACTION_DONE);
        form.addView(title,new LinearLayout.LayoutParams(-1,dp(56)));
        contexts.add("No label");contexts.addAll(new TaskStore(this).labels());
        String selected=getIntent().getStringExtra("widget_label");if(selected!=null&&!selected.isEmpty()&&!contexts.contains(selected))contexts.add(selected);
        context=spinner("Context label",contexts);if(selected!=null)context.setSelection(Math.max(0,contexts.indexOf(selected)));form.addView(context);
        LinearLayout options=new LinearLayout(this);options.setOrientation(0);
        priority=spinner("Priority",Arrays.asList("Highest","High","Medium","Low","Lowest","Not set"));priority.setSelection(5);
        when=spinner("When to do it",Arrays.asList("Today","Tomorrow"));
        options.addView(priority,new LinearLayout.LayoutParams(0,dp(48),1));options.addView(when,new LinearLayout.LayoutParams(0,dp(48),1));form.addView(options);
        LinearLayout actions=new LinearLayout(this);actions.setGravity(Gravity.END);
        Button cancel=new Button(this);cancel.setText("Cancel");cancel.setAllCaps(false);cancel.setOnClickListener(v->finish());actions.addView(cancel);
        Button save=new Button(this);save.setText("Add task");save.setAllCaps(false);save.setOnClickListener(v->save());actions.addView(save);form.addView(actions);
        ScrollView scroll=new ScrollView(this);scroll.addView(form);setContentView(scroll);title.requestFocus();
        title.setOnEditorActionListener((v,action,event)->{if(action==EditorInfo.IME_ACTION_DONE){save();return true;}return false;});
    }
    @Override protected void onStart(){super.onStart();getWindow().setLayout((int)(getResources().getDisplayMetrics().widthPixels*.92),WindowManager.LayoutParams.WRAP_CONTENT);}
    private Spinner spinner(String description,List<String> items){Spinner s=new Spinner(this);s.setContentDescription(description);ArrayAdapter<String>a=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,items);s.setAdapter(a);s.setLayoutParams(new LinearLayout.LayoutParams(-1,dp(48)));return s;}
    private void save(){
        if(title.getText().toString().trim().isEmpty()){title.setError("Enter a task title");return;}
        try{
            ReferenceStore store=new ReferenceStore(this);
            if(store.readState().equals("{}"))try(InputStream in=getAssets().open("www/initial-state.json");ByteArrayOutputStream out=new ByteArrayOutputStream()){
                byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);String seed=out.toString(StandardCharsets.UTF_8.name()).trim();if(!seed.isEmpty()&&!seed.equals("{}"))store.writeState(seed);
            }
            List<String> labels=context.getSelectedItemPosition()==0?Collections.emptyList():Collections.singletonList(context.getSelectedItem().toString());
            store.addCustomTask(title.getText().toString(),labels,TaskStore.PRIORITIES.get(priority.getSelectedItemPosition()),when.getSelectedItemPosition()==1);
            finish();
        }catch(Exception error){title.setError("Couldn’t save. Please try again.");}
    }
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
}
