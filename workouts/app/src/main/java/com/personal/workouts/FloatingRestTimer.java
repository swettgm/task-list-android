package com.personal.workouts;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.graphics.*;
import android.os.Build;
import android.view.*;
import java.util.Locale;
import org.json.JSONObject;

/** A small, touchable timer window. Touches outside its bounds go to the app below. */
final class FloatingRestTimer extends View {
    private final WindowManager windows;
    private final WindowManager.LayoutParams layout;
    private final WorkoutStore store;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Typeface timerFont=Build.VERSION.SDK_INT>=28
        ?Typeface.create(Typeface.create("sans-serif",Typeface.NORMAL),750,false)
        :Typeface.create("sans-serif",Typeface.BOLD);
    private static final int OVERDUE=0xffff6b7c;
    private final int margin, slop;
    private float positionX=1f,positionY=0f;
    private float startX,startY;
    private int startLeft,startTop;
    private boolean dragging,attached,overdue;
    private int roomX,roomY,foreground,background;
    private float progress;
    private String label="";

    FloatingRestTimer(Service context) {
        super(windowContext(context));
        windows=getContext().getSystemService(WindowManager.class);
        store=new WorkoutStore(context);
        margin=dp(8);
        slop=ViewConfiguration.get(context).getScaledTouchSlop();
        int size=dp(76);
        layout=new WindowManager.LayoutParams(size,size,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT);
        layout.gravity=Gravity.TOP|Gravity.LEFT;
        layout.setTitle("Exercise rest timer");
        setClickable(true);
        setOutlineProvider(new ViewOutlineProvider(){
            @Override public void getOutline(View view,Outline outline){
                outline.setOval(0,0,view.getWidth(),view.getHeight());
            }
        });
        setElevation(dp(6));
        setOnClickListener(v->{
            Intent intent=new Intent(context,MainActivity.class).setAction(RestTimerService.OPEN_CONTROLS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            try {context.startActivity(intent);}catch(RuntimeException ignored){ /* Android may restrict launches. */ }
        });
    }

    private static Context windowContext(Context context) {
        if(Build.VERSION.SDK_INT>=30){
            Display display=context.getSystemService(DisplayManager.class).getDisplay(Display.DEFAULT_DISPLAY);
            if(display!=null)return context.createDisplayContext(display)
                .createWindowContext(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,null);
        }
        return context;
    }

    boolean show() {
        JSONObject settings=store.read().optJSONObject("settings");
        JSONObject position=settings==null?null:settings.optJSONObject("timerPosition");
        if(position==null&&settings!=null)position=settings.optJSONObject("overlayTimerPosition");
        updateRoom();
        if(position!=null){
            positionX=clampFraction(position.optDouble("x",1),1);
            positionY=clampFraction(position.optDouble("y",0),0);
        }else if(settings!=null){
            positionX=clampFraction((roomX-dp(8))/(double)Math.max(1,roomX),1);
            positionY=clampFraction((roomY-dp(settings.optBoolean("spotify",true)?106:16))/(double)Math.max(1,roomY),0);
        }
        layout.x=margin+Math.round(positionX*roomX);
        layout.y=margin+Math.round(positionY*roomY);
        try {windows.addView(this,layout);attached=true;return true;}
        catch(RuntimeException unavailable){return false;}
    }

    boolean isShowing(){return attached;}

    void remove() {
        if(attached){
            attached=false;
            try {windows.removeViewImmediate(this);}catch(IllegalArgumentException ignored){ }
        }
    }

    void update(JSONObject state,long remaining) {
        JSONObject settings=state.optJSONObject("settings");
        foreground=Color.parseColor(settings.optString("text","#f4f7fa"));
        background=Color.parseColor(settings.optString("top","#203747"));
        long duration=Math.max(1,state.optLong("restDurationMs",Math.max(remaining,90000)));
        progress=Math.max(0,Math.min(1f,(float)remaining/duration));
        overdue=remaining<=0;
        String nextLabel=formatTime(remaining);
        if(!label.equals(nextLabel)){
            label=nextLabel;
            setContentDescription("Rest · "+label+". Open Exercise");
        }
        if(!dragging){
            int oldX=roomX,oldY=roomY;
            updateRoom();
            if(oldX!=roomX||oldY!=roomY)
                move(margin+Math.round(positionX*roomX),margin+Math.round(positionY*roomY));
        }
        invalidate();
    }

    static String formatTime(long milliseconds) {
        long seconds=milliseconds>0?(milliseconds+999)/1000:Math.abs(milliseconds)/1000;
        return (milliseconds<=0?"-":"")+String.format(Locale.getDefault(),"%d:%02d",seconds/60,seconds%60);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float center=getWidth()/2f,radius=getWidth()*(36f/84f);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(background);
        canvas.drawCircle(center,center,center,paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(getWidth()*(4f/84f));
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setColor((foreground&0xffffff)|0x45000000);
        canvas.drawCircle(center,center,radius,paint);
        paint.setColor(foreground);
        if(progress>0)canvas.drawArc(center-radius,center-radius,center+radius,center+radius,-90,360*progress,false,paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setColor(overdue?OVERDUE:foreground);
        paint.setTypeface(timerFont);
        paint.setTextSize(dp(16));
        canvas.drawText(label,center,center-(paint.ascent()+paint.descent())/2,paint);
    }

    @Override public boolean onTouchEvent(android.view.MotionEvent event) {
        switch(event.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                startX=event.getRawX();startY=event.getRawY();startLeft=layout.x;startTop=layout.y;
                dragging=false;
                return true;
            case MotionEvent.ACTION_MOVE:
                float dx=event.getRawX()-startX,dy=event.getRawY()-startY;
                if(!dragging&&Math.hypot(dx,dy)>slop)dragging=true;
                if(dragging)move(startLeft+Math.round(dx),startTop+Math.round(dy));
                return true;
            case MotionEvent.ACTION_UP:
                if(dragging)savePosition();else performClick();
                dragging=false;
                return true;
            case MotionEvent.ACTION_CANCEL:
                if(dragging)move(startLeft,startTop);
                dragging=false;
                return true;
            default: return true;
        }
    }

    @Override public boolean performClick(){return super.performClick();}

    private void savePosition() {
        positionX=(layout.x-margin)/(float)Math.max(1,roomX);
        positionY=(layout.y-margin)/(float)Math.max(1,roomY);
        synchronized(WorkoutStore.LOCK){
            JSONObject state=store.read(),settings=state.optJSONObject("settings");
            if(settings==null)return;
            try {
                settings.put("timerPosition",new JSONObject().put("x",positionX).put("y",positionY));
                store.write(state.toString());
            }catch(org.json.JSONException ignored){ }
        }
    }

    private void move(int x,int y) {
        layout.x=Math.max(margin,Math.min(margin+roomX,x));
        layout.y=Math.max(margin,Math.min(margin+roomY,y));
        if(attached){
            try {windows.updateViewLayout(this,layout);}catch(RuntimeException unavailable){remove();}
        }
    }

    @SuppressWarnings("deprecation")
    private void updateRoom() {
        Point available=new Point();
        if(Build.VERSION.SDK_INT>=30){
            WindowMetrics metrics=windows.getCurrentWindowMetrics();
            Insets insets=metrics.getWindowInsets().getInsetsIgnoringVisibility(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());
            available.set(metrics.getBounds().width()-insets.left-insets.right,metrics.getBounds().height()-insets.top-insets.bottom);
        }else windows.getDefaultDisplay().getSize(available);
        roomX=Math.max(0,available.x-layout.width-margin*2);
        roomY=Math.max(0,available.y-layout.height-margin*2);
    }

    private int dp(int value){return Math.round(value*getResources().getDisplayMetrics().density);}
    private static float clampFraction(double value,float fallback){
        return Double.isFinite(value)?(float)Math.max(0,Math.min(1,value)):fallback;
    }
}
