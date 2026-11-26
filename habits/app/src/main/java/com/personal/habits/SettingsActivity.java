package com.personal.habits;
import android.app.AlertDialog;
import android.appwidget.*;
import android.graphics.Typeface;
import android.content.*;
import android.os.Bundle;
import android.widget.*;
public final class SettingsActivity extends AppUi {
 private int widgetId=-1;
 private Appearance.TitleFont titleFont;
 @Override public void onCreate(Bundle state){super.onCreate(state);widgetId=getIntent().getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,-1);setResult(RESULT_CANCELED);page("Widget settings");Button back=button("Back");back.setOnClickListener(v->finish());header.addView(back);Appearance a=new Appearance(this);titleFont=a.titleFont;
 label("Text color");EditText text=field(String.format("#%06X",a.text&0xFFFFFF));body.addView(text);label("Background");EditText bg=field(String.format("#%06X",a.background&0xFFFFFF));body.addView(bg);label("Gradient bottom");EditText bottom=field(String.format("#%06X",a.bottom&0xFFFFFF));body.addView(bottom);
 TextView opacity=text("Background opacity · "+a.opacity+"%",16);opacity.setPadding(0,dp(20),0,dp(8));body.addView(opacity);SeekBar alpha=new SeekBar(this);alpha.setMax(100);alpha.setProgress(a.opacity);alpha.setContentDescription("Widget background opacity");body.addView(alpha);alpha.setOnSeekBarChangeListener(listener(value->opacity.setText("Background opacity · "+value+"%")));
 TextView size=text("Habit text size · "+a.widgetTextSize(),16);size.setPadding(0,dp(16),0,dp(8));body.addView(size);SeekBar scale=new SeekBar(this);scale.setMax(10);scale.setProgress(a.size-12);scale.setContentDescription("Habit text size");body.addView(scale);scale.setOnSeekBarChangeListener(listener(value->size.setText("Habit text size · "+(value+10))));
 label("Habit title font");
 Button font=button(titleFont.label);
 showFontChoice(font);
 font.setOnClickListener(v->{
  Appearance.TitleFont[] choices=Appearance.TitleFont.values();
  CharSequence[] labels=new CharSequence[choices.length];
  for(int i=0;i<choices.length;i++)labels[i]=choices[i].style(choices[i].label);
  new AlertDialog.Builder(this).setTitle("Habit title font")
   .setSingleChoiceItems(labels,titleFont.ordinal(),(dialog,index)->{
    titleFont=choices[index];
    showFontChoice(font);
    dialog.dismiss();
   }).setNegativeButton("Cancel",null).show();
 });
 body.addView(font);
 CheckBox gradient=new CheckBox(this);gradient.setText("Gradient background");gradient.setTextColor(WHITE);gradient.setTextSize(17);gradient.setChecked(a.gradient);body.addView(gradient);CheckBox vibration=new CheckBox(this);vibration.setText("Tap vibration");vibration.setTextColor(WHITE);vibration.setTextSize(17);vibration.setChecked(a.haptics);body.addView(vibration);
 TextView hint=text("The title font applies in the app and on all Habits widgets.",14);hint.setAlpha(.7f);hint.setPadding(0,dp(12),0,dp(12));body.addView(hint);Button save=button("Save");save.setOnClickListener(v->{try{Appearance.save(this,text.getText().toString().trim(),bg.getText().toString().trim(),bottom.getText().toString().trim(),alpha.getProgress(),scale.getProgress()+12,gradient.isChecked(),vibration.isChecked(),titleFont);HabitWidget.refresh(this);if(widgetId!=AppWidgetManager.INVALID_APPWIDGET_ID){HabitWidget.update(this,AppWidgetManager.getInstance(this),widgetId);setResult(RESULT_OK,new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,widgetId));}finish();}catch(RuntimeException e){error(e);}});body.addView(save);
 label("Changelog");TextView log=text("1.0.8 · Choose a font for habit titles in the app and widget.\n\n1.0.7 · Widget double taps cycle empty → X → check → empty. Saved history and future-day locks are unchanged.\n\n1.0.6 · Double tap a widget circle to change its mark. Single taps leave it unchanged. Future days remain read-only.\n\n1.0.5 · Habit names are always visible. Restored the earlier widget spacing; hiding and auto-hide have been removed. Future days are read-only.",14);log.setAlpha(.7f);body.addView(log);
 }
 private void showFontChoice(Button font){
  font.setText(titleFont.label);
  font.setContentDescription("Habit title font: "+titleFont.label);
  font.setTypeface(Typeface.create(titleFont.family,Typeface.NORMAL));
 }
 interface Changed{void value(int v);}
 private SeekBar.OnSeekBarChangeListener listener(Changed c){return new SeekBar.OnSeekBarChangeListener(){public void onProgressChanged(SeekBar s,int p,boolean user){c.value(p);}public void onStartTrackingTouch(SeekBar s){}public void onStopTrackingTouch(SeekBar s){}};}
}
