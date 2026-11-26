package com.personal.crm;
import android.content.*;
public final class ReminderReceiver extends BroadcastReceiver {
 @Override public void onReceive(Context context,Intent intent){if(Reminders.ACTION.equals(intent.getAction()))Reminders.deliver(context);}
}
