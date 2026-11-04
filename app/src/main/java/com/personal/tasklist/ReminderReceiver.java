package com.personal.tasklist;
import android.content.*;
public final class ReminderReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context,Intent intent){if(TaskReminders.COMPLETE.equals(intent.getAction())||TaskReminders.SNOOZE.equals(intent.getAction()))TaskReminders.handleAction(context,intent);else if(TaskReminders.SNOOZED.equals(intent.getAction()))TaskReminders.receiveSnoozed(context);else if(TaskReminders.TASK.equals(intent.getAction())||TaskReminders.DAILY.equals(intent.getAction()))TaskReminders.receive(context,intent.getAction());else {TaskReminders.reschedule(context);TaskWidgetProvider.refreshAll(context);}}
}
