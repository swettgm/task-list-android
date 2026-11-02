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
    private PopupAppearance appearance;
    private EditText title;
    private Spinner context, priority, when;
    private final List<String> contexts = new ArrayList<>();
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setFinishOnTouchOutside(true);
        appearance=PopupAppearance.read(this);
        getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        LinearLayout form = new LinearLayout(this); form.setOrientation(LinearLayout.VERTICAL);form.setPadding(dp(20),dp(16),dp(20),dp(12));
        form.setBackgroundColor(Color.TRANSPARENT);
        TextView heading = new TextView(this);heading.setText("Add task");heading.setTextSize(20);heading.setTextColor(appearance.accent);form.addView(heading);
        title = new EditText(this);title.setContentDescription("Task title");title.setHint("What needs doing?");title.setTextColor(appearance.foreground);title.setHintTextColor(appearance.muted);title.setBackgroundTintList(null);title.setBackground(appearance.control(this,false));title.setPadding(dp(12),dp(8),dp(12),dp(8));title.setSingleLine(true);title.setImeOptions(EditorInfo.IME_ACTION_DONE);
        form.addView(title,new LinearLayout.LayoutParams(-1,dp(56)));
        contexts.add("No label");contexts.addAll(new TaskStore(this).labels());
        String selected=getIntent().getStringExtra("widget_label");if(selected!=null&&!selected.isEmpty()&&!contexts.contains(selected))contexts.add(selected);
        List<CharSequence> displayedContexts=new ArrayList<>();for(String value:contexts)displayedContexts.add(TaskStore.contextDisplay(this,value));
        context=spinner("Context label",displayedContexts);if(selected!=null)context.setSelection(Math.max(0,contexts.indexOf(selected)));form.addView(context);
        LinearLayout options=new LinearLayout(this);options.setOrientation(LinearLayout.HORIZONTAL);
        priority=spinner("Priority",Arrays.asList("Highest","High","Medium","Low","Lowest","Not set"));priority.setSelection(5);
        when=spinner("When to do it",Arrays.asList("Today","Tomorrow"));
        options.addView(priority,new LinearLayout.LayoutParams(0,dp(48),1));options.addView(when,new LinearLayout.LayoutParams(0,dp(48),1));form.addView(options);
        LinearLayout actions=new LinearLayout(this);actions.setGravity(Gravity.END);
        Button cancel=new Button(this);cancel.setText("Cancel");cancel.setAllCaps(false);cancel.setTextColor(appearance.foreground);cancel.setBackgroundTintList(null);cancel.setBackground(appearance.control(this,false));cancel.setPadding(dp(14),dp(8),dp(14),dp(8));cancel.setOnClickListener(v->finish());actions.addView(cancel);
        Button save=new Button(this);save.setText("Add task");save.setAllCaps(false);save.setTextColor(appearance.accentInk);save.setBackgroundTintList(null);save.setBackground(appearance.control(this,true));save.setPadding(dp(14),dp(8),dp(14),dp(8));save.setOnClickListener(v->save());actions.addView(save);form.addView(actions);
        ScrollView scroll=new ScrollView(this);scroll.setBackground(appearance.background(this));scroll.setClipToOutline(true);scroll.addView(form);setContentView(scroll);title.requestFocus();
        title.setOnEditorActionListener((v,action,event)->{if(action==EditorInfo.IME_ACTION_DONE){save();return true;}return false;});
    }
    @Override protected void onStart(){super.onStart();getWindow().setLayout((int)(getResources().getDisplayMetrics().widthPixels*.92),WindowManager.LayoutParams.WRAP_CONTENT);}
    private Spinner spinner(String description,List<? extends CharSequence> items){Spinner s=new Spinner(this);s.setContentDescription(description);ArrayAdapter<CharSequence>a=new ArrayAdapter<CharSequence>(this,android.R.layout.simple_spinner_dropdown_item,new ArrayList<CharSequence>(items)){
        @Override public android.view.View getView(int position,android.view.View reused,android.view.ViewGroup parent){android.view.View view=super.getView(position,reused,parent);if(view instanceof TextView)((TextView)view).setTextColor(appearance.foreground);return view;}
        @Override public android.view.View getDropDownView(int position,android.view.View reused,android.view.ViewGroup parent){android.view.View view=super.getDropDownView(position,reused,parent);view.setBackgroundColor(Color.TRANSPARENT);view.setMinimumHeight(dp(48));if(view instanceof TextView)((TextView)view).setTextColor(appearance.foreground);return view;}
    };android.graphics.drawable.GradientDrawable popup=appearance.control(this,false);popup.setColor(appearance.surface);s.setPopupBackgroundDrawable(popup);s.setBackgroundTintList(null);s.setBackground(appearance.dropdown(this));s.setPadding(dp(12),0,dp(36),0);s.setAdapter(a);s.setLayoutParams(new LinearLayout.LayoutParams(-1,dp(48)));return s;}
    private void save(){
        if(title.getText().toString().trim().isEmpty()){title.setError("Enter a task title");return;}
        try{
            ReferenceStore store=new ReferenceStore(this);
            if(store.readState().equals("{}"))try(InputStream in=getAssets().open("www/initial-state.json");ByteArrayOutputStream out=new ByteArrayOutputStream()){
                byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);String seed=out.toString(StandardCharsets.UTF_8.name()).trim();if(!seed.isEmpty()&&!seed.equals("{}"))store.writeState(seed);
            }
            List<String> labels=context.getSelectedItemPosition()==0?Collections.emptyList():Collections.singletonList(contexts.get(context.getSelectedItemPosition()));
            store.addCustomTask(title.getText().toString(),labels,TaskStore.PRIORITIES.get(priority.getSelectedItemPosition()),when.getSelectedItemPosition()==1);
            finish();
        }catch(Exception error){title.setError("Couldn’t save. Please try again.");}
    }
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
}
