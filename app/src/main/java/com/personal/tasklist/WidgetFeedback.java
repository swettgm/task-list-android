package com.personal.tasklist;

import android.content.Context;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.provider.Settings;

/** A light widget tap, respecting the phone's touch-feedback setting. */
final class WidgetFeedback {
    static void tick(Context context) {
        try {
            if(Settings.System.getInt(context.getContentResolver(),Settings.System.HAPTIC_FEEDBACK_ENABLED,1)==0)return;
            Vibrator vibrator=(Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
            if(vibrator==null||!vibrator.hasVibrator())return;
            VibrationEffect effect=Build.VERSION.SDK_INT>=29?VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK):VibrationEffect.createOneShot(10,40);
            if(Build.VERSION.SDK_INT>=33)Api33.vibrate(vibrator,effect);
            else vibrator.vibrate(effect,new AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build());
        } catch(RuntimeException unavailable) { /* Feedback must never block a task action. */ }
    }
    @android.annotation.TargetApi(33)
    private static final class Api33 {
        static void vibrate(Vibrator vibrator,VibrationEffect effect){vibrator.vibrate(effect,new android.os.VibrationAttributes.Builder().setUsage(android.os.VibrationAttributes.USAGE_TOUCH).build());}
    }
}
