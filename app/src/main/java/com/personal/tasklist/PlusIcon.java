package com.personal.tasklist;

import android.content.Context;
import android.graphics.*;

/** Rounded add glyph, painted in the saved app background over its accent. */
final class PlusIcon {
    static Bitmap render(Context context) {
        PopupAppearance theme=PopupAppearance.read(context);int size=Math.round(48*context.getResources().getDisplayMetrics().density);
        Bitmap bitmap=Bitmap.createBitmap(size,size,Bitmap.Config.ARGB_8888);bitmap.setDensity(context.getResources().getDisplayMetrics().densityDpi);
        Canvas canvas=new Canvas(bitmap);Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);paint.setColor(theme.accent);canvas.drawRoundRect(0,0,size,size,size*.25f,size*.25f,paint);
        paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(size*.075f);paint.setStrokeCap(Paint.Cap.ROUND);paint.setColor(theme.start);
        float center=size/2f,radius=size*.25f;
        if(theme.gradient){double angle=Math.toRadians(theme.angle);float dx=(float)Math.sin(angle),dy=-(float)Math.cos(angle),length=radius*2*(Math.abs(dx)+Math.abs(dy));paint.setShader(new LinearGradient(center-dx*length/2,center-dy*length/2,center+dx*length/2,center+dy*length/2,theme.start,theme.end,Shader.TileMode.CLAMP));}
        canvas.drawLine(center-radius,center,center+radius,center,paint);canvas.drawLine(center,center-radius,center,center+radius,paint);return bitmap;
    }
}
