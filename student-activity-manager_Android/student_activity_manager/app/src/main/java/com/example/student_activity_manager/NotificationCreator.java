/**
 * Created by Alexander Podshiblov on 08.05.2016.
 * Модуль демонстрации уведомлений,
 * получает сигнал от системы,
 * создает и демонстрирует уведомление в прогрессбаре
 */

package com.example.student_activity_manager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotificationCreator extends BroadcastReceiver {
    public NotificationCreator() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(context, ToDoActivity.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

        Notification n  = new Notification.Builder(context)
                .setContentTitle("Оповещение о задании")
                .setTicker("Оповещение о задании")
                .setContentText(intent.getStringExtra("text"))
                .setContentIntent(PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT))
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setDefaults(Notification.DEFAULT_ALL)
                .build();

        String id = intent.getStringExtra("id");
        notificationManager.notify(id.hashCode(), n);
    }
}
