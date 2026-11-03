package com.personal.tasklist;

import static org.junit.Assert.*;
import android.content.Context;
import android.os.Vibrator;
import android.provider.Settings;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

@RunWith(org.robolectric.RobolectricTestRunner.class)
@Config(sdk=28)
public class WidgetFeedbackTest {
    private Context context;private Vibrator vibrator;
    @Before public void setup(){context=RuntimeEnvironment.getApplication();vibrator=(Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);Shadows.shadowOf(vibrator).setHasVibrator(true);vibrator.cancel();Settings.System.putInt(context.getContentResolver(),Settings.System.HAPTIC_FEEDBACK_ENABLED,1);}
    @Test public void validWidgetTouchProducesFeedback(){WidgetFeedback.tick(context);assertTrue(Shadows.shadowOf(vibrator).isVibrating());}
    @Test public void disabledTouchFeedbackAndMissingHardwareStaySilent(){Settings.System.putInt(context.getContentResolver(),Settings.System.HAPTIC_FEEDBACK_ENABLED,0);WidgetFeedback.tick(context);assertFalse(Shadows.shadowOf(vibrator).isVibrating());Settings.System.putInt(context.getContentResolver(),Settings.System.HAPTIC_FEEDBACK_ENABLED,1);Shadows.shadowOf(vibrator).setHasVibrator(false);WidgetFeedback.tick(context);assertFalse(Shadows.shadowOf(vibrator).isVibrating());}
    @Test @Config(sdk=33) public void modernWidgetTouchUsesTheSupportedEffect(){WidgetFeedback.tick(context);assertTrue(Shadows.shadowOf(vibrator).isVibrating());}
}
