package com.personal.crm;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import java.io.*;
import static org.junit.Assert.*;
/** Check the actual manifest icon, not just scale values in an SVG or vector. */
@RunWith(RobolectricTestRunner.class) @Config(sdk=35) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class LauncherIconTest {
 private Drawable icon(){Context c=RuntimeEnvironment.getApplication();return c.getApplicationInfo().loadIcon(c.getPackageManager());}
 private Bitmap draw(Drawable d){Bitmap b=Bitmap.createBitmap(108,108,Bitmap.Config.ARGB_8888);d.setBounds(0,0,108,108);d.draw(new Canvas(b));return b;}
 private Rect visible(Bitmap b){Rect r=new Rect(108,108,-1,-1);for(int y=0;y<108;y++)for(int x=0;x<108;x++)if(Color.alpha(b.getPixel(x,y))>8){r.left=Math.min(r.left,x);r.right=Math.max(r.right,x);r.top=Math.min(r.top,y);r.bottom=Math.max(r.bottom,y);}return new Rect(r.left,r.top,r.right+1,r.bottom+1);}
 @Test public void launcherLoadsNewTransparentRasterAsset(){Drawable d=icon();assertTrue("The manifest must load the new raster launcher resource",d instanceof BitmapDrawable);assertTrue(((BitmapDrawable)d).getBitmap().hasAlpha());}
 @Test public void renderedLauncherIsSmallerThanPreviousVectorAndHasNoBackplate()throws Exception{Context context=RuntimeEnvironment.getApplication();Bitmap bitmap=draw(icon());Rect actual=visible(bitmap),previous=visible(draw(context.getDrawable(R.drawable.ic_app)));System.out.println("Manifest launcher bounds: "+actual+"; previous vector: "+previous);File out=new File("../build/browser-qa/crm-launcher-render.png");out.getParentFile().mkdirs();try(OutputStream stream=new FileOutputStream(out)){assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,stream));}assertTrue("Rendered person must be smaller than the previous icon: "+actual+" vs "+previous,actual.width()<previous.width()&&actual.height()<previous.height());assertTrue("Icon padding must survive Android resource loading: "+actual,actual.width()>=42&&actual.width()<=56&&actual.height()>=44&&actual.height()<=58);assertTrue(Math.abs(actual.exactCenterX()-54)<=2&&Math.abs(actual.exactCenterY()-54)<=2);for(int[] point:new int[][]{{0,0},{107,0},{0,107},{107,107},{54,4},{4,54},{103,54},{54,103}})assertEquals("No opaque icon background",0,Color.alpha(bitmap.getPixel(point[0],point[1])));}
}
