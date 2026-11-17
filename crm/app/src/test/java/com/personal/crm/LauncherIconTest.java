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
/** Check what Android loads from the manifest, including adaptive layer expansion. */
@RunWith(RobolectricTestRunner.class) @Config(sdk=35) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class LauncherIconTest {
 private Drawable icon(){Context context=RuntimeEnvironment.getApplication();return context.getApplicationInfo().loadIcon(context.getPackageManager());}
 @Test public void launcherUsesAdaptiveBoundaryAndTransparentBackground(){Drawable icon=icon();assertTrue("The manifest must expose an adaptive icon instead of a legacy vector",icon instanceof AdaptiveIconDrawable);AdaptiveIconDrawable adaptive=(AdaptiveIconDrawable)icon;assertTrue(adaptive.getForeground() instanceof InsetDrawable);assertEquals(0,((ColorDrawable)adaptive.getBackground()).getColor()>>>24);}
 @Test public void renderedPersonStaysSmallCenteredAndTransparent()throws Exception{Drawable icon=icon();Bitmap bitmap=Bitmap.createBitmap(108,108,Bitmap.Config.ARGB_8888);icon.setBounds(0,0,108,108);icon.draw(new Canvas(bitmap));int left=108,top=108,right=-1,bottom=-1;for(int y=0;y<108;y++)for(int x=0;x<108;x++)if(Color.alpha(bitmap.getPixel(x,y))>8){left=Math.min(left,x);right=Math.max(right,x);top=Math.min(top,y);bottom=Math.max(bottom,y);}int width=right-left+1,height=bottom-top+1;System.out.println("Rendered launcher person: "+width+"x"+height+" at "+left+","+top);assertTrue("The adaptive layer must not enlarge the person: "+width+"x"+height,width>=48&&width<=64&&height>=50&&height<=66);assertTrue(Math.abs((left+right)/2.0-53.5)<=2&&Math.abs((top+bottom)/2.0-53.5)<=2);for(int y:new int[]{0,107})for(int x:new int[]{0,107})assertEquals(0,Color.alpha(bitmap.getPixel(x,y)));File out=new File("../build/browser-qa/crm-launcher-render.png");out.getParentFile().mkdirs();try(OutputStream stream=new FileOutputStream(out)){assertTrue(bitmap.compress(Bitmap.CompressFormat.PNG,100,stream));}}
}
