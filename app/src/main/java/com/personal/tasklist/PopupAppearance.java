package com.personal.tasklist;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import org.json.JSONObject;

/** The saved app colors, rendered natively for the home-screen Quick Add popup. */
final class PopupAppearance {
    final int accent,start,end,angle,foreground,muted,surface,accentInk;final boolean gradient;
    PopupAppearance(JSONObject stored){
        JSONObject theme=stored==null?new JSONObject():stored;
        accent=color(theme.optString("accent"),Color.rgb(104,183,255));start=color(theme.optString("background"),Color.rgb(16,21,31));end=color(theme.optString("gradientEnd"),Color.rgb(70,82,93));
        gradient=theme.optBoolean("gradient",false);double direction=theme.optDouble("gradientAngle",180);angle=Double.isFinite(direction)?(int)Math.max(0,Math.min(360,Math.round(direction))):180;
        foreground=foreground(start,gradient?end:start);int middle=gradient?mix(start,end,.5f):start;muted=mix(middle,foreground,.7f);surface=mix(middle,foreground,.07f);accentInk=foreground(accent,accent);
    }
    static PopupAppearance read(Context context){try{return new PopupAppearance(new JSONObject(new ReferenceStore(context).readState()).optJSONObject("uiTheme"));}catch(Exception unreadable){return new PopupAppearance(null);}}
    private static int color(String value,int fallback){String text=value.trim();if(text.matches("#[0-9a-fA-F]{3}"))text="#"+text.charAt(1)+text.charAt(1)+text.charAt(2)+text.charAt(2)+text.charAt(3)+text.charAt(3);if(!text.matches("#[0-9a-fA-F]{6}"))return fallback;return Color.parseColor(text);}
    private static int mix(int a,int b,float n){return Color.rgb(Math.round(Color.red(a)*(1-n)+Color.red(b)*n),Math.round(Color.green(a)*(1-n)+Color.green(b)*n),Math.round(Color.blue(a)*(1-n)+Color.blue(b)*n));}
    private static double contrast(int a,int b){double x=Color.luminance(a),y=Color.luminance(b);return(Math.max(x,y)+.05)/(Math.min(x,y)+.05);}
    private static int foreground(int a,int b){int dark=Color.rgb(16,21,31),light=Color.rgb(243,246,251);return Math.min(contrast(a,dark),contrast(b,dark))>Math.min(contrast(a,light),contrast(b,light))?dark:light;}
    android.graphics.drawable.GradientDrawable control(Context context,boolean primary){
        android.graphics.drawable.GradientDrawable shape=new android.graphics.drawable.GradientDrawable();float density=context.getResources().getDisplayMetrics().density;shape.setCornerRadius(12*density);
        shape.setColor(primary?accent:(gradient?Color.argb(110,Color.red(surface),Color.green(surface),Color.blue(surface)):surface));
        shape.setStroke(Math.max(1,Math.round(2*density)),primary?accent:Color.argb(85,Color.red(muted),Color.green(muted),Color.blue(muted)));return shape;
    }
    Drawable dropdown(Context context){
        float density=context.getResources().getDisplayMetrics().density;
        Drawable arrow=new Drawable(){final Paint pen=new Paint(Paint.ANTI_ALIAS_FLAG);@Override public void draw(Canvas canvas){Rect b=getBounds();float x=b.exactCenterX(),y=b.exactCenterY();pen.setColor(muted);pen.setStrokeWidth(1.5f*density);pen.setStyle(Paint.Style.STROKE);pen.setStrokeCap(Paint.Cap.ROUND);Path path=new Path();path.moveTo(x-4*density,y-2*density);path.lineTo(x,y+2*density);path.lineTo(x+4*density,y-2*density);canvas.drawPath(path,pen);}@Override public void setAlpha(int value){pen.setAlpha(value);}@Override public void setColorFilter(ColorFilter filter){pen.setColorFilter(filter);}@Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}};
        android.graphics.drawable.LayerDrawable layers=new android.graphics.drawable.LayerDrawable(new Drawable[]{control(context,false),arrow});layers.setLayerSize(1,Math.round(24*density),Math.round(24*density));layers.setLayerGravity(1,android.view.Gravity.END|android.view.Gravity.CENTER_VERTICAL);layers.setLayerInsetRight(1,Math.round(8*density));return layers;
    }
    Background background(Context context){return new Background(this,24*context.getResources().getDisplayMetrics().density);}
    static final class Background extends Drawable {
        final PopupAppearance appearance;final float radius;private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);private final RectF rect=new RectF();
        Background(PopupAppearance appearance,float radius){this.appearance=appearance;this.radius=radius;}
        @Override protected void onBoundsChange(Rect bounds){rect.set(bounds);if(appearance.gradient){double radians=Math.toRadians(appearance.angle);float dx=(float)Math.sin(radians),dy=-(float)Math.cos(radians),length=Math.abs(bounds.width()*dx)+Math.abs(bounds.height()*dy);paint.setShader(new LinearGradient(rect.centerX()-dx*length/2,rect.centerY()-dy*length/2,rect.centerX()+dx*length/2,rect.centerY()+dy*length/2,appearance.start,appearance.end,Shader.TileMode.CLAMP));}else{paint.setShader(null);paint.setColor(appearance.start);}}
        @Override public void draw(Canvas canvas){canvas.drawRoundRect(rect,radius,radius,paint);}
        @Override public void getOutline(Outline outline){outline.setRoundRect(getBounds(),radius);}
        @Override public void setAlpha(int alpha){paint.setAlpha(alpha);invalidateSelf();}
        @Override public void setColorFilter(ColorFilter filter){paint.setColorFilter(filter);invalidateSelf();}
        @Override public int getOpacity(){return PixelFormat.TRANSLUCENT;}
    }
}
