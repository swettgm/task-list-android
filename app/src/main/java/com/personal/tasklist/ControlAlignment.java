package com.personal.tasklist;

import android.app.AlertDialog;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/** Align text without moving or resizing its control. */
final class ControlAlignment {
    static void text(TextView view){view.setIncludeFontPadding(false);view.setGravity(Gravity.START|Gravity.CENTER_VERTICAL);view.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);}
    static void dialog(AlertDialog dialog){for(int which:new int[]{AlertDialog.BUTTON_POSITIVE,AlertDialog.BUTTON_NEGATIVE,AlertDialog.BUTTON_NEUTRAL}){Button button=dialog.getButton(which);if(button!=null)text(button);}}
}
