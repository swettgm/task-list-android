package com.personal.tasklist;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.view.View;

/** Continue the editor backdrop across the inset above its WebView. */
final class StatusBarScrim extends Drawable {
    static final int COLOR=Color.argb(184,0,0,0);
    private final View owner;private final Paint paint=new Paint();
    StatusBarScrim(View owner){this.owner=owner;paint.setColor(COLOR);}
    @Override public void draw(Canvas canvas){canvas.drawRect(0,0,owner.getWidth(),owner.getPaddingTop(),paint);}
    @Override public void setAlpha(int alpha){paint.setAlpha(Math.round(184*alpha/255f));invalidateSelf();}
    @Override public void setColorFilter(ColorFilter filter){paint.setColorFilter(filter);invalidateSelf();}
    @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
}
