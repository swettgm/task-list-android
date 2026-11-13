package com.personal.habits;
import android.content.*;
import android.graphics.Color;
public final class Appearance {
 public final int text,background,bottom,opacity,size;public final boolean gradient,haptics;
 public Appearance(Context c){android.content.SharedPreferences p=c.getSharedPreferences("appearance",0);text=color(p.getString("text","#FFFFFF"));background=color(p.getString("background","#203747"));bottom=color(p.getString("bottom","#5A5C61"));opacity=Math.max(0,Math.min(100,p.getInt("opacity",0)));size=Math.max(12,Math.min(22,p.getInt("size",16)));gradient=p.getBoolean("gradient",true);haptics=p.getBoolean("haptics",true);}
 // Keep stored sizing and in-app typography stable; widget names are 2sp smaller.
 public int widgetTextSize(){return size-2;}
 public static int color(String v){if(v==null||!v.matches("#[0-9a-fA-F]{6}"))throw new IllegalArgumentException("Use a color like #203747");return Color.parseColor(v);}
 public static void save(Context c,String text,String bg,String bottom,int opacity,int size,boolean gradient,boolean haptics){color(text);color(bg);color(bottom);if(opacity<0||opacity>100||size<12||size>22)throw new IllegalArgumentException("Invalid appearance");if(!c.getSharedPreferences("appearance",0).edit().putString("text",text).putString("background",bg).putString("bottom",bottom).putInt("opacity",opacity).putInt("size",size).putBoolean("gradient",gradient).putBoolean("haptics",haptics).commit())throw new IllegalStateException("Unable to save settings");}
}
