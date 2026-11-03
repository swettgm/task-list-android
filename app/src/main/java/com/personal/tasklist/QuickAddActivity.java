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
    private ScrollView panel;
    private android.animation.ValueAnimator savedBorder;
    private Spinner priority, when;
    private Button labelsButton,timeButton,dateButton;
    private final java.util.LinkedHashSet<String> selectedLabels=new java.util.LinkedHashSet<>();
    private java.time.LocalDate selectedDate;private java.time.LocalTime selectedTime;private boolean weekend,settingDate;private int selectedWhen;
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
        title = new EditText(this);title.setContentDescription("Task title");title.setHint("What needs doing?");title.setTextColor(appearance.foreground);title.setHintTextColor(appearance.muted);title.setBackgroundTintList(null);title.setBackground(appearance.control(this,false));title.setPadding(dp(12),dp(8),dp(12),dp(8));title.setTextSize(16);title.setSingleLine(true);title.setImeOptions(EditorInfo.IME_ACTION_DONE);
        form.addView(title,fieldParams());
        contexts.addAll(new TaskStore(this).labels());
        String selected=getIntent().getStringExtra("widget_label");if(selected!=null&&!selected.isEmpty()){if(!contexts.contains(selected))contexts.add(selected);selectedLabels.add(selected);}
        labelsButton=controlButton("Labels",()->showLabels());labelsButton.setContentDescription("Context labels");updateLabelText();form.addView(labelsButton,fieldParams());
        selectedDate=java.time.LocalDate.now(java.time.ZoneId.of("America/New_York"));
        LinearLayout options=new LinearLayout(this);options.setOrientation(LinearLayout.HORIZONTAL);
        priority=spinner("Priority",Arrays.asList("High","Medium","Low","Not set"));priority.setSelection(3);
        when=spinner("When to do it",Arrays.asList("Today","Tomorrow","Weekend","Choose date…"));
        LinearLayout.LayoutParams priorityParams=new LinearLayout.LayoutParams(0,dp(40),1);priorityParams.setMarginEnd(dp(8));
        options.addView(priority,priorityParams);options.addView(when,new LinearLayout.LayoutParams(0,dp(40),1));form.addView(options,fieldParams());
        LinearLayout timing=new LinearLayout(this);timing.setOrientation(LinearLayout.HORIZONTAL);
        timeButton=controlButton("Any time",()->showTime());timeButton.setContentDescription("Task time");dateButton=controlButton("Date",()->showDate());dateButton.setContentDescription("Task date");
        LinearLayout.LayoutParams timeParams=new LinearLayout.LayoutParams(0,dp(40),1);timeParams.setMarginEnd(dp(8));timing.addView(timeButton,timeParams);timing.addView(dateButton,new LinearLayout.LayoutParams(0,dp(40),1));form.addView(timing,fieldParams());
        when.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){@Override public void onNothingSelected(AdapterView<?> parent){}@Override public void onItemSelected(AdapterView<?> parent,android.view.View view,int position,long id){if(settingDate)return;if(position==3){if(selectedWhen!=3)showDate();return;}selectedWhen=position;weekend=position==2;java.time.LocalDate today=java.time.LocalDate.now(java.time.ZoneId.of("America/New_York"));selectedDate=position==1?today.plusDays(1):position==2?WeekendSchedule.saturday(today):today;updateDateText();}});
        LinearLayout actions=new LinearLayout(this);actions.setGravity(Gravity.END);
        Button cancel=new Button(this);cancel.setText("Cancel");cancel.setAllCaps(false);cancel.setTextColor(appearance.foreground);cancel.setBackgroundTintList(null);cancel.setBackground(appearance.control(this,false));cancel.setPadding(dp(14),dp(8),dp(14),dp(8));cancel.setOnClickListener(v->finish());LinearLayout.LayoutParams cancelParams=new LinearLayout.LayoutParams(-2,dp(42));cancelParams.setMarginEnd(dp(8));actions.addView(cancel,cancelParams);
        Button save=new Button(this);save.setText("Add task");save.setAllCaps(false);save.setTextColor(appearance.accentInk);save.setBackgroundTintList(null);save.setBackground(appearance.control(this,true));save.setPadding(dp(14),dp(8),dp(14),dp(8));save.setOnClickListener(v->save());actions.addView(save,new LinearLayout.LayoutParams(-2,dp(42)));LinearLayout.LayoutParams actionParams=new LinearLayout.LayoutParams(-1,dp(42));actionParams.topMargin=dp(8);form.addView(actions,actionParams);
        ScrollView scroll=new ScrollView(this);panel=scroll;scroll.setVerticalScrollBarEnabled(false);scroll.setHorizontalScrollBarEnabled(false);scroll.setBackground(appearance.background(this));scroll.setClipToOutline(true);scroll.addView(form);setContentView(scroll);title.requestFocus();
        title.setOnEditorActionListener((v,action,event)->{if(action==EditorInfo.IME_ACTION_DONE){save();return true;}return false;});
    }
    @Override protected void onStart(){super.onStart();getWindow().setLayout((int)(getResources().getDisplayMetrics().widthPixels*.92),WindowManager.LayoutParams.WRAP_CONTENT);}
    private Spinner spinner(String description,List<? extends CharSequence> items){Spinner s=new Spinner(this);s.setContentDescription(description);ArrayAdapter<CharSequence>a=new ArrayAdapter<CharSequence>(this,android.R.layout.simple_spinner_dropdown_item,new ArrayList<CharSequence>(items)){
        @Override public android.view.View getView(int position,android.view.View reused,android.view.ViewGroup parent){android.view.View view=super.getView(position,reused,parent);if(view instanceof TextView){((TextView)view).setTextColor(appearance.foreground);((TextView)view).setTextSize(16);view.setPadding(0,0,0,0);}return view;}
        @Override public android.view.View getDropDownView(int position,android.view.View reused,android.view.ViewGroup parent){android.view.View view=super.getDropDownView(position,reused,parent);view.setBackgroundColor(Color.TRANSPARENT);view.setMinimumHeight(dp(48));if(view instanceof TextView)((TextView)view).setTextColor(appearance.foreground);return view;}
    };android.graphics.drawable.GradientDrawable popup=appearance.control(this,false);popup.setColor(appearance.surface);s.setPopupBackgroundDrawable(popup);s.setBackgroundTintList(null);s.setBackground(appearance.dropdown(this));s.setPadding(dp(12),0,dp(36),0);s.setAdapter(a);s.setLayoutParams(new LinearLayout.LayoutParams(-1,dp(48)));return s;}
    private void save(){
        if(title.getText().toString().trim().isEmpty()){title.setError("Enter a task title");return;}
        try{
            ReferenceStore store=new ReferenceStore(this);
            if(store.readState().equals("{}"))try(InputStream in=getAssets().open("www/initial-state.json");ByteArrayOutputStream out=new ByteArrayOutputStream()){
                byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);String seed=out.toString(StandardCharsets.UTF_8.name()).trim();if(!seed.isEmpty()&&!seed.equals("{}"))store.writeState(seed);
            }
            store.addCustomTask(title.getText().toString(),new ArrayList<>(selectedLabels),TaskStore.PRIORITIES.get(priority.getSelectedItemPosition()),selectedDate.toString(),selectedTime==null?null:selectedTime.toString(),weekend);
            resetFields();title.requestFocus();flashSavedBorder();android.view.inputmethod.InputMethodManager keyboard=(android.view.inputmethod.InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);if(keyboard!=null)keyboard.showSoftInput(title,android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
        }catch(Exception error){title.setError("Couldn’t save. Please try again.");}
    }
    private Button controlButton(String text,Runnable click){Button b=new Button(this);b.setText(text);b.setAllCaps(false);b.setSingleLine(true);b.setEllipsize(android.text.TextUtils.TruncateAt.END);b.setTextColor(appearance.foreground);b.setTextSize(16);b.setBackgroundTintList(null);b.setBackground(appearance.control(this,false));b.setPadding(dp(12),0,dp(12),0);b.setOnClickListener(v->click.run());return b;}
    private void updateLabelText(){android.text.SpannableStringBuilder text=new android.text.SpannableStringBuilder();for(String label:selectedLabels){if(text.length()>0)text.append("  ");text.append(TaskStore.contextDisplay(this,label));}labelsButton.setText(text.length()==0?"Labels":text);}
    private void showLabels(){CharSequence[] names=new CharSequence[contexts.size()];boolean[] chosen=new boolean[contexts.size()];for(int i=0;i<contexts.size();i++){names[i]=new android.text.SpannableStringBuilder().append(TaskStore.contextDisplay(this,contexts.get(i))).append(" ").append(contexts.get(i));chosen[i]=selectedLabels.contains(contexts.get(i));}android.app.AlertDialog dialog=new android.app.AlertDialog.Builder(this).setTitle("Labels").setMultiChoiceItems(names,chosen,(d,index,checked)->{if(checked)selectedLabels.add(contexts.get(index));else selectedLabels.remove(contexts.get(index));updateLabelText();}).setPositiveButton("Done",null).setNeutralButton("Clear",(d,index)->{selectedLabels.clear();updateLabelText();}).create();dialog.show();dialog.getWindow().setBackgroundDrawable(appearance.background(this));dialog.getListView().setVerticalScrollBarEnabled(false);}
    private void updateDateText(){dateButton.setText(selectedDate.format(java.time.format.DateTimeFormatter.ofPattern("MMM d",java.util.Locale.getDefault())));}
    private void showDate(){java.time.LocalDate initial=selectedDate;android.app.DatePickerDialog dialog=new android.app.DatePickerDialog(this,(v,y,m,d)->{selectedDate=java.time.LocalDate.of(y,m+1,d);weekend=false;selectedWhen=3;settingDate=true;when.setSelection(3);settingDate=false;updateDateText();},initial.getYear(),initial.getMonthValue()-1,initial.getDayOfMonth());dialog.setOnCancelListener(d->{settingDate=true;when.setSelection(selectedWhen);settingDate=false;});dialog.show();}
    private void showTime(){java.time.LocalTime initial=selectedTime==null?java.time.LocalTime.now():selectedTime;android.app.TimePickerDialog dialog=new android.app.TimePickerDialog(this,(v,h,m)->{selectedTime=java.time.LocalTime.of(h,m);timeButton.setText(selectedTime.format(java.time.format.DateTimeFormatter.ofPattern("h:mm a",java.util.Locale.getDefault())));},initial.getHour(),initial.getMinute(),android.text.format.DateFormat.is24HourFormat(this));dialog.setButton(android.content.DialogInterface.BUTTON_NEUTRAL,"Any time",(d,which)->{selectedTime=null;timeButton.setText("Any time");});dialog.show();}
    private void resetFields(){title.getText().clear();title.setError(null);selectedLabels.clear();updateLabelText();priority.setSelection(3);selectedTime=null;timeButton.setText("Any time");selectedDate=java.time.LocalDate.now(java.time.ZoneId.of("America/New_York"));weekend=false;selectedWhen=0;settingDate=true;when.setSelection(0);settingDate=false;dateButton.setText("Date");}
    private void flashSavedBorder(){
        if(savedBorder!=null)savedBorder.cancel();if(!android.animation.ValueAnimator.areAnimatorsEnabled())return;
        android.graphics.drawable.GradientDrawable border=new android.graphics.drawable.GradientDrawable();border.setColor(Color.TRANSPARENT);border.setCornerRadius(dp(24));panel.setForeground(border);
        android.animation.ValueAnimator animation=android.animation.ValueAnimator.ofFloat(0,1,3,1,3,0);savedBorder=animation;animation.setDuration(800);animation.setInterpolator(new android.view.animation.LinearInterpolator());animation.addUpdateListener(value->border.setStroke(Math.round(dp(1)*(Float)value.getAnimatedValue()),Color.WHITE));animation.addListener(new android.animation.AnimatorListenerAdapter(){@Override public void onAnimationEnd(android.animation.Animator value){if(savedBorder==value){savedBorder=null;panel.setForeground(null);}}});animation.start();
    }
    @Override protected void onDestroy(){if(savedBorder!=null)savedBorder.cancel();super.onDestroy();}
    private LinearLayout.LayoutParams fieldParams(){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(40));p.topMargin=dp(8);return p;}
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
}
