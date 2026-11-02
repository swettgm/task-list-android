package com.personal.tasklist;

import android.content.Context;
import android.graphics.*;

/** Bare accent plus, aligned to the widget's right edge with a full tap area. */
final class PlusIcon {
    static Bitmap render(Context context) {
        PopupAppearance theme=PopupAppearance.read(context);int size=Math.round(48*context.getResources().getDisplayMetrics().density);
        Bitmap bitmap=Bitmap.createBitmap(size,size,Bitmap.Config.ARGB_8888);bitmap.setDensity(context.getResources().getDisplayMetrics().densityDpi);
        Canvas canvas=new Canvas(bitmap);Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);paint.setColor(theme.accent);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(size/12f);paint.setStrokeCap(Paint.Cap.ROUND);
        float cx=size*34/48f,cy=size/2f,radius=size*10/48f;
        canvas.drawLine(cx-radius,cy,cx+radius,cy,paint);canvas.drawLine(cx,cy-radius,cx,cy+radius,paint);return bitmap;
    }
}
