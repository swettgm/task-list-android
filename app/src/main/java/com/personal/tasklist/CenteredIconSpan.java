package com.personal.tasklist;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.style.ImageSpan;

/** Align a label image with the font's vertical center, including enlarged text. */
final class CenteredIconSpan extends ImageSpan {
    CenteredIconSpan(Drawable drawable){super(drawable,ImageSpan.ALIGN_BASELINE);}
    @Override public int getSize(Paint paint,CharSequence text,int start,int end,Paint.FontMetricsInt metrics){
        if(metrics!=null){Paint.FontMetricsInt font=paint.getFontMetricsInt();int center=(font.ascent+font.descent)/2,height=getDrawable().getBounds().height();metrics.ascent=Math.min(font.ascent,center-height/2);metrics.descent=Math.max(font.descent,center+(height+1)/2);metrics.top=Math.min(font.top,metrics.ascent);metrics.bottom=Math.max(font.bottom,metrics.descent);}
        return getDrawable().getBounds().width();
    }
    @Override public void draw(Canvas canvas,CharSequence text,int start,int end,float x,int top,int y,int bottom,Paint paint){
        Paint.FontMetrics font=paint.getFontMetrics();float center=y+(font.ascent+font.descent)/2;int saved=canvas.save();canvas.translate(x,center-getDrawable().getBounds().height()/2f);getDrawable().draw(canvas);canvas.restoreToCount(saved);
    }
}
