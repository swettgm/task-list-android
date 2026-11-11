package com.personal.tasklist;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.view.View;

/** Continue the modal backdrop across every system-bar and cutout inset. */
final class StatusBarScrim extends Drawable {
    static final int COLOR=Color.argb(184,0,0,0);
    private final View owner;private final Paint paint=new Paint();
    StatusBarScrim(View owner){this.owner=owner;paint.setColor(COLOR);}
    @Override public void draw(Canvas canvas){canvas.drawRect(0,0,owner.getWidth(),owner.getPaddingTop(),paint);canvas.drawRect(0,owner.getHeight()-owner.getPaddingBottom(),owner.getWidth(),owner.getHeight(),paint);canvas.drawRect(0,owner.getPaddingTop(),owner.getPaddingLeft(),owner.getHeight()-owner.getPaddingBottom(),paint);canvas.drawRect(owner.getWidth()-owner.getPaddingRight(),owner.getPaddingTop(),owner.getWidth(),owner.getHeight()-owner.getPaddingBottom(),paint);}
    @Override public void setAlpha(int alpha){paint.setAlpha(Math.round(184*alpha/255f));invalidateSelf();}
    @Override public void setColorFilter(ColorFilter filter){paint.setColorFilter(filter);invalidateSelf();}
    @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
