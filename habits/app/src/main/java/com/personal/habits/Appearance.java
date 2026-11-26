package com.personal.habits;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
public final class Appearance {
 enum TitleFont {
  DEFAULT("System default","sans-serif-medium",R.layout.widget_habit_row),
  LIGHT("Light","sans-serif-light",R.layout.widget_habit_row_light),
  CONDENSED("Condensed","sans-serif-condensed",R.layout.widget_habit_row_condensed),
  SERIF("Serif","serif",R.layout.widget_habit_row_serif),
  MONOSPACE("Monospace","monospace",R.layout.widget_habit_row_monospace);

  final String label,family;
  final int widgetLayout;
  TitleFont(String label,String family,int widgetLayout){this.label=label;this.family=family;this.widgetLayout=widgetLayout;}
  Typeface typeface(){return Typeface.create(family,Typeface.NORMAL);}
  static TitleFont read(SharedPreferences preferences){
   try{return valueOf(preferences.getString("titleFont",DEFAULT.name()));}
   catch(IllegalArgumentException|ClassCastException invalid){return DEFAULT;}
  }

 }
 final TitleFont titleFont;
 public final int text,background,bottom,opacity,size;public final boolean gradient,haptics;
 public Appearance(Context c){android.content.SharedPreferences p=c.getSharedPreferences("appearance",0);text=color(p.getString("text","#FFFFFF"));background=color(p.getString("background","#203747"));bottom=color(p.getString("bottom","#5A5C61"));opacity=Math.max(0,Math.min(100,p.getInt("opacity",0)));size=Math.max(12,Math.min(22,p.getInt("size",16)));gradient=p.getBoolean("gradient",true);haptics=p.getBoolean("haptics",true);titleFont=TitleFont.read(p);}
 // Keep stored sizing and in-app typography stable; widget names are 2sp smaller.
 public int widgetTextSize(){return size-2;}
 public static int color(String v){if(v==null||!v.matches("#[0-9a-fA-F]{6}"))throw new IllegalArgumentException("Use a color like #203747");return Color.parseColor(v);}
 public static void save(Context c,String text,String bg,String bottom,int opacity,int size,boolean gradient,boolean haptics){
  save(c,text,bg,bottom,opacity,size,gradient,haptics,new Appearance(c).titleFont);
 }
 static void save(Context c,String text,String bg,String bottom,int opacity,int size,boolean gradient,boolean haptics,TitleFont font){color(text);color(bg);color(bottom);if(opacity<0||opacity>100||size<12||size>22)throw new IllegalArgumentException("Invalid appearance");if(!c.getSharedPreferences("appearance",0).edit().putString("text",text).putString("background",bg).putString("bottom",bottom).putInt("opacity",opacity).putInt("size",size).putBoolean("gradient",gradient).putBoolean("haptics",haptics).putString("titleFont",font.name()).commit())throw new IllegalStateException("Unable to save settings");}
}
