package com.personal.crm;
import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.*;
import android.util.Xml;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import org.xmlpull.v1.XmlPullParser;
import java.io.*;
import static org.junit.Assert.*;
/** Verify the same launcher format and visible alpha boundary as the companion apps. */
@RunWith(RobolectricTestRunner.class) @Config(sdk=35) @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class LauncherIconTest {
 private Context context(){return RuntimeEnvironment.getApplication();}
 private Drawable icon(){return context().getApplicationInfo().loadIcon(context().getPackageManager());}
 private Bitmap draw(Drawable d){Bitmap b=Bitmap.createBitmap(108,108,Bitmap.Config.ARGB_8888);d.setBounds(0,0,108,108);d.draw(new Canvas(b));return b;}
 private File companion(String app){for(File dir=new File(System.getProperty("user.dir"));dir!=null;dir=dir.getParentFile()){File file=new File(dir,app+"/app/src/main/res/drawable/ic_app.xml");if(file.exists())return file;}throw new AssertionError("Companion icon source not found");}
 @Test public void manifestUsesSameVectorLauncherMethod()throws Exception{assertTrue(icon() instanceof VectorDrawable);assertEquals(R.drawable.ic_app,context().getApplicationInfo().icon);android.content.pm.ApplicationInfo info=context().getPackageManager().getApplicationInfo(context().getPackageName(),android.content.pm.PackageManager.GET_META_DATA);assertTrue(info.metaData==null||!info.metaData.containsKey("com.samsung.android.icon_container.has_icon_container"));}
 @Test public void renderedAlphaBoundaryExactlyMatchesRouteNotesAndMeditation()throws Exception{Bitmap actual=draw(icon());for(String app:new String[]{"route-manager","meditation"}){try(InputStream stream=new FileInputStream(companion(app))){XmlPullParser parser=Xml.newPullParser();parser.setInput(stream,"UTF-8");Bitmap reference=draw(Drawable.createFromXml(context().getResources(),parser));for(int y=0;y<108;y++)for(int x=0;x<108;x++)assertEquals("Launcher boundary differs from "+app+" at "+x+","+y,Color.alpha(reference.getPixel(x,y)),Color.alpha(actual.getPixel(x,y)));}}System.out.println("CRM alpha silhouette matches Route Notes and Meditation pixel-for-pixel at 108x108");}
 @Test public void roundedWhiteTileKeepsBoldPersonCentered()throws Exception{Bitmap b=draw(icon());assertEquals(0,Color.alpha(b.getPixel(0,0)));assertEquals(0,Color.alpha(b.getPixel(107,107)));assertEquals(Color.WHITE,b.getPixel(54,9));int left=108,top=108,right=-1,bottom=-1;for(int y=0;y<108;y++)for(int x=0;x<108;x++){int color=b.getPixel(x,y);if(Color.alpha(color)>250&&Color.red(color)<100&&Color.green(color)<110&&Color.blue(color)<130){left=Math.min(left,x);right=Math.max(right,x);top=Math.min(top,y);bottom=Math.max(bottom,y);}}int width=right-left+1,height=bottom-top+1;System.out.println("Bold single-person foreground: "+width+"x"+height);assertTrue(width>=52&&width<=66&&height>=54&&height<=68);assertTrue(Math.abs((left+right)/2.0-53.5)<=2&&Math.abs((top+bottom)/2.0-53.5)<=2);File out=new File("../build/browser-qa/crm-launcher-render.png");out.getParentFile().mkdirs();try(OutputStream stream=new FileOutputStream(out)){assertTrue(b.compress(Bitmap.CompressFormat.PNG,100,stream));}}
}
