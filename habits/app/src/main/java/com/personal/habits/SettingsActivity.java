package com.personal.habits;
import android.appwidget.*;
import android.content.*;
import android.os.Bundle;
import android.widget.*;
public final class SettingsActivity extends AppUi {
 private int widgetId=-1;
 @Override public void onCreate(Bundle state){super.onCreate(state);widgetId=getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,-1);setResult(RESULT_CANCELED);page("Widget settings");Button back=button("Back");back.setOnClickListener(v->finish());header.addView(back);Appearance a=new Appearance(this);
 label("Text color");EditText text=field(String.format("#%06X",a.text&0xFFFFFF));body.addView(text);label("Background");EditText bg=field(String.format("#%06X",a.background&0xFFFFFF));body.addView(bg);label("Gradient bottom");EditText bottom=field(String.format("#%06X",a.bottom&0xFFFFFF));body.addView(bottom);
 TextView opacity=text("Background opacity · "+a.opacity+"%",16);opacity.setPadding(0,dp(20),0,dp(8));body.addView(opacity);SeekBar alpha=new SeekBar(this);alpha.setMax(100);alpha.setProgress(a.opacity);alpha.setContentDescription("Widget background opacity");body.addView(alpha);alpha.setOnSeekBarChangeListener(listener(value->opacity.setText("Background opacity · "+value+"%")));
 TextView size=text("Habit text size · "+a.widgetTextSize(),16);size.setPadding(0,dp(16),0,dp(8));body.addView(size);SeekBar scale=new SeekBar(this);scale.setMax(10);scale.setProgress(a.size-12);scale.setContentDescription("Habit text size");body.addView(scale);scale.setOnSeekBarChangeListener(listener(value->size.setText("Habit text size · "+(value+10))));
 CheckBox gradient=new CheckBox(this);gradient.setText("Gradient background");gradient.setTextColor(WHITE);gradient.setTextSize(17);gradient.setChecked(a.gradient);body.addView(gradient);CheckBox vibration=new CheckBox(this);vibration.setText("Tap vibration");vibration.setTextColor(WHITE);vibration.setTextSize(17);vibration.setChecked(a.haptics);body.addView(vibration);
 TextView hint=text("Settings apply to all Habits widgets.",14);hint.setAlpha(.7f);hint.setPadding(0,dp(12),0,dp(12));body.addView(hint);Button save=button("Save");save.setOnClickListener(v->{try{Appearance.save(this,text.getText().toString().trim(),bg.getText().toString().trim(),bottom.getText().toString().trim(),alpha.getProgress(),scale.getProgress()+12,gradient.isChecked(),vibration.isChecked());HabitWidget.refresh(this);if(widgetId!=AppWidgetManager.INVALID_APPWIDGET_ID){HabitWidget.update(this,AppWidgetManager.getInstance(this),widgetId);setResult(RESULT_OK,new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,widgetId));}finish();}catch(RuntimeException e){error(e);}});body.addView(save);
 label("Changelog");TextView log=text("1.0.4 · Habit names start hidden and hide again five seconds after revealing. The widget’s headings and habit rows are closer together.",14);log.setAlpha(.7f);body.addView(log);
 }
 interface Changed{void value(int v);}
 private SeekBar.OnSeekBarChangeListener listener(Changed c){return new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int p,boolean user){c.value(p);}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}};}
}
