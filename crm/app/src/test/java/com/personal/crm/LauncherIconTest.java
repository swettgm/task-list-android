package com.personal.crm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=35)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class LauncherIconTest {
    private Context context() { return RuntimeEnvironment.getApplication(); }
    private Bitmap draw(Drawable drawable) {
        Bitmap bitmap = Bitmap.createBitmap(108, 108, Bitmap.Config.ARGB_8888);
        drawable.setBounds(0, 0, 108, 108);
        drawable.draw(new Canvas(bitmap));
        return bitmap;
    }
    @Test public void launcherUsesAdaptiveLayersWithoutAnExtraTile() {
        Drawable drawable = context().getApplicationInfo().loadIcon(context().getPackageManager());
        assertTrue(drawable instanceof AdaptiveIconDrawable);
        assertEquals(R.mipmap.ic_launcher, context().getApplicationInfo().icon);
        AdaptiveIconDrawable icon = (AdaptiveIconDrawable) drawable;
        Bitmap foreground = draw(icon.getForeground());
        assertEquals(0, Color.alpha(foreground.getPixel(0, 0)));
        assertEquals(0, Color.alpha(foreground.getPixel(54, 9)));
        Bitmap background = draw(icon.getBackground());
        assertEquals(255, Color.alpha(background.getPixel(0, 0)));
        assertTrue(Color.red(background.getPixel(54, 9)) < Color.red(background.getPixel(54, 98)));
    }
    @Test public void renderedIconShowsTheAppGradientAcrossTheVisibleTile() {
        Bitmap bitmap=draw(context().getApplicationInfo().loadIcon(context().getPackageManager()));
        for (int y : new int[]{6,101}) {
            double fraction=(y+0.5)/108.0;
            int actual=bitmap.getPixel(54,y);
            int expectedRed=(int)Math.round(32+58*fraction);
            int expectedGreen=(int)Math.round(55+37*fraction);
            int expectedBlue=(int)Math.round(71+26*fraction);
            assertTrue("Red gradient endpoint at "+y+" was "+Color.red(actual),
                    Math.abs(Color.red(actual)-expectedRed)<=3);
            assertTrue(Math.abs(Color.green(actual)-expectedGreen)<=3);
            assertTrue(Math.abs(Color.blue(actual)-expectedBlue)<=3);
        }
    }
    @Test public void whiteArtworkFitsTheAdaptiveSafeArea() {
        AdaptiveIconDrawable icon = (AdaptiveIconDrawable) context().getApplicationInfo().loadIcon(context().getPackageManager());
        Bitmap bitmap = draw(icon.getForeground());
        int left=108, top=108, right=-1, bottom=-1;
        for (int y=0; y<108; y++) for (int x=0; x<108; x++) {
            int color=bitmap.getPixel(x,y);
            if (Color.alpha(color)>250) {
                assertTrue(Color.red(color)>240 && Color.green(color)>240 && Color.blue(color)>240);
                left=Math.min(left,x); right=Math.max(right,x);
                top=Math.min(top,y); bottom=Math.max(bottom,y);
            }
        }
        assertTrue(right-left>=36 && right-left<=45);
        assertTrue(bottom-top>=38 && bottom-top<=46);
        assertTrue(Math.abs((left+right)/2.0-53.5)<=2);
        assertTrue(Math.abs((top+bottom)/2.0-53.5)<=2);
    }
}
