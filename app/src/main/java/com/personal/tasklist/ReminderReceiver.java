package com.personal.tasklist;
import android.content.*;
public final class ReminderReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context,Intent intent){if(TaskReminders.TASK.equals(intent.getAction())||TaskReminders.DAILY.equals(intent.getAction()))TaskReminders.receive(context,intent.getAction());else {TaskReminders.reschedule(context);TaskWidgetProvider.refreshAll(context);}}
}
