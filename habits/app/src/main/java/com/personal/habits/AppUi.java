package com.personal.habits;
import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
abstract class AppUi extends Activity {
 static final int WHITE=Color.rgb(244,247,250);LinearLayout root,body,header;
 int dp(float x){return Math.round(x*getResources().getDisplayMetrics().density);}
 GradientDrawable surface(){GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{Color.rgb(32,55,71),Color.rgb(90,92,97)});g.setCornerRadius(dp(12));g.setStroke(dp(2),0x557F919D);return g;}
 void page(String title){root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(16),dp(12),dp(16),dp(12));root.setBackground(new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0xFF203747,0xFF5A5C61}));root.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(dp(16)+insets.getSystemWindowInsetLeft(),dp(12)+insets.getSystemWindowInsetTop(),dp(16)+insets.getSystemWindowInsetRight(),dp(12)+insets.getSystemWindowInsetBottom());return insets.consumeSystemWindowInsets();});setContentView(root);root.requestApplyInsets();header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);TextView heading=text(title,28);heading.setTypeface(null,Typeface.BOLD);header.addView(heading,new LinearLayout.LayoutParams(0,dp(56),1));root.addView(header);ScrollView scroll=new ScrollView(this);scroll.setFillViewport(false);body=new LinearLayout(this);body.setOrientation(1);body.setPadding(0,dp(12),0,dp(16));scroll.addView(body);root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));}
 TextView text(String s,int sp){TextView t=new TextView(this);t.setText(s);t.setTextColor(WHITE);t.setTextSize(sp);t.setGravity(Gravity.CENTER_VERTICAL);return t;}
 Button button(String s){Button b=new Button(this);b.setText(s);b.setTextSize(16);b.setTextColor(WHITE);b.setAllCaps(false);b.setMinHeight(dp(46));b.setMinimumHeight(dp(46));b.setMinWidth(0);b.setMinimumWidth(0);b.setPadding(dp(12),dp(6),dp(12),dp(6));b.setBackground(surface());LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-2,dp(48));lp.setMargins(dp(4),dp(4),0,dp(4));b.setLayoutParams(lp);return b;}
 EditText field(String value){EditText e=new EditText(this);e.setSingleLine(true);e.setTextColor(WHITE);e.setTextSize(18);e.setPadding(dp(12),dp(8),dp(12),dp(8));e.setBackground(surface());e.setText(value);e.setSelectAllOnFocus(false);e.setMinimumHeight(dp(48));return e;}
 void label(String s){TextView l=text(s,16);l.setPadding(0,dp(16),0,dp(6));body.addView(l);}
 void error(RuntimeException e){Toast.makeText(this,e.getMessage()==null?"Unable to save. Please try again.":e.getMessage(),Toast.LENGTH_LONG).show();}
}
