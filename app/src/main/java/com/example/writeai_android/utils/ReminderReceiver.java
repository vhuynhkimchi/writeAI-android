package com.example.writeai_android.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationHelper.createNotificationChannel(context);
        NotificationHelper.showReminderNotification(context);
        NotificationHelper.scheduleDailyReminder(context);
    }
}
