package com.personal.tasklist;

import static org.junit.Assert.*;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=34)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class WidgetEmptyMessageTest {
    @Test public void visibleTextAndFlowerAreCenteredAtNarrowAndWideWidgetSizes() {
        Context context=RuntimeEnvironment.getApplication();
        for(String text:new String[]{"You’re all done! Get some rest. 🪻","You’re done until 10:30 AM! Get some rest. 🪻"})
            for(int width:new int[]{140,250,360,420}){
                Bitmap image=WidgetEmptyMessage.render(context,text,Color.WHITE,width);
                int left=image.getWidth(),right=-1;
                for(int y=0;y<image.getHeight();y++)for(int x=0;x<image.getWidth();x++)
                    if(Color.alpha(image.getPixel(x,y))>8){left=Math.min(left,x);right=Math.max(right,x);}
                assertTrue("Real glyph pixels must be rendered",right>=left);
                assertEquals("Visible glyphs, including the flower, center within the image",(image.getWidth()-1)/2f,(left+right)/2f,1f);
                assertTrue(image.getWidth()<=Math.round((width-16)*context.getResources().getDisplayMetrics().density)+2);
                image.recycle();
            }
    }
}
