package com.personal.tasklist;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.TypedValue;

/** Centers the visible glyphs, including emoji font side bearings, rather than just their advance. */
final class WidgetEmptyMessage {
    static Bitmap render(Context context, String text, int color, int availableWidthDp) {
        android.util.DisplayMetrics metrics=context.getResources().getDisplayMetrics();
        int width=Math.max(80,Math.min(2048,Math.round((Math.max(96,availableWidthDp)-16)*metrics.density)));
        TextPaint paint=new TextPaint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(color);
        paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,13,metrics));
        int pad=Math.max(4,Math.round(paint.getTextSize()/2));
        StaticLayout layout=StaticLayout.Builder.obtain(text,0,text.length(),paint,width)
                .setAlignment(Layout.Alignment.ALIGN_CENTER).setIncludePad(true).build();
        Bitmap source=Bitmap.createBitmap(width+2*pad,Math.max(1,layout.getHeight()),Bitmap.Config.ARGB_8888);
        source.setDensity(metrics.densityDpi);
        Canvas canvas=new Canvas(source);canvas.translate(pad,0);layout.draw(canvas);
        int sourceWidth=source.getWidth(),height=source.getHeight();
        int[] pixels=new int[sourceWidth*height];source.getPixels(pixels,0,sourceWidth,0,0,sourceWidth,height);
        Bitmap centered=Bitmap.createBitmap(sourceWidth,height,Bitmap.Config.ARGB_8888);
        centered.setDensity(metrics.densityDpi);Canvas output=new Canvas(centered);
        int top=height,bottom=-1,widest=0;
        for(int line=0;line<layout.getLineCount();line++){
            int start=Math.max(0,layout.getLineTop(line)),end=Math.min(height,layout.getLineBottom(line));
            int left=sourceWidth,right=-1;
            for(int y=start;y<end;y++)for(int x=0;x<sourceWidth;x++)if((pixels[y*sourceWidth+x]>>>24)>8){
                left=Math.min(left,x);right=Math.max(right,x);top=Math.min(top,y);bottom=Math.max(bottom,y);
            }
            if(right<left)continue;
            int visible=right-left+1;widest=Math.max(widest,visible);
            android.graphics.Rect from=new android.graphics.Rect(left,start,right+1,end);
            int x=(sourceWidth-visible)/2;
            output.drawBitmap(source,from,new android.graphics.Rect(x,start,x+visible,end),null);
        }
        // A transparent fallback also keeps host-side tests and unusual missing-font devices safe.
        if(bottom<top){centered.recycle();return source;}
        int cropWidth=Math.min(sourceWidth,widest+2),cropLeft=(sourceWidth-cropWidth)/2;
        Bitmap result=Bitmap.createBitmap(centered,cropLeft,top,cropWidth,bottom-top+1);
        result.setDensity(metrics.densityDpi);source.recycle();if(result!=centered)centered.recycle();return result;
    }
}
