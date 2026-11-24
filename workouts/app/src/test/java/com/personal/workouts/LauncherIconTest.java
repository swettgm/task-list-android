package com.personal.workouts;
import android.graphics.*;
import android.graphics.drawable.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=35) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class LauncherIconTest {
    private Bitmap draw(Drawable drawable) {
        Bitmap bitmap=Bitmap.createBitmap(108,108,Bitmap.Config.ARGB_8888);
        drawable.setBounds(0,0,108,108);drawable.draw(new Canvas(bitmap));return bitmap;
    }
    @Test public void adaptiveTileShowsTheAppGradient() {
        Drawable drawable=RuntimeEnvironment.getApplication().getApplicationInfo().loadIcon(RuntimeEnvironment.getApplication().getPackageManager());
        assertTrue(drawable instanceof AdaptiveIconDrawable);
        Bitmap bitmap=draw(drawable);
        for(int y:new int[]{6,101}) {
            double fraction=(y+.5)/108;
            assertTrue(Math.abs(Color.red(bitmap.getPixel(54,y))-Math.round(32+58*fraction))<=3);
        }
    }
    @Test public void whiteArtworkHasGenerousPadding() {
        AdaptiveIconDrawable icon=(AdaptiveIconDrawable)RuntimeEnvironment.getApplication().getDrawable(R.mipmap.ic_launcher);
        Bitmap bitmap=draw(icon.getForeground());
        int left=108,right=-1;
        for(int y=0;y<108;y++)for(int x=0;x<108;x++)if(Color.alpha(bitmap.getPixel(x,y))>250) {
            assertEquals(Color.WHITE,bitmap.getPixel(x,y));left=Math.min(left,x);right=Math.max(right,x);
        }
        assertTrue(right-left>=35&&right-left<=48);
        assertTrue(Math.abs((left+right)/2.0-53.5)<=1);
    }
}
