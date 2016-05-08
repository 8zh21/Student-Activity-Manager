package com.example.student_activity_manager;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Pair;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Alexander on 14.04.2016.
 */
public class Notifier {
    public static void createOfUpdateNotifications(Context context, List<ScheduleItem> schItems,
                                                   List<TimeItem> timeItems, List<ScheduleTaskItem> schTasks)
    {
        AlarmManager manager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);


        NotificationManager notificationManager = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);

        for (int i = 0; i < 7; i++)
        {
            notificationManager.cancel(i);
        }

        Map<Integer, Pair<Long, String>> notList = getListOfNot(schItems, schTasks);
        for(Map.Entry<Integer, Pair<Long, String>> entry : notList.entrySet())
        {
            int notId = entry.getKey();
            long time = entry.getValue().first;
            String message = entry.getValue().second;

            Intent notificationIntent = new Intent(context, ToDoActivity.class);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,
                    notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT);

            Notification.Builder builder = new Notification.Builder(context);
            builder.setSmallIcon(R.drawable.ic_launcher) //иконка уведомления
                    .setAutoCancel(true) //уведомление закроется по клику на него
                    .setTicker("У вас невыполненные задачи на завтра") //текст, который отобразится вверху статус-бара при создании уведомления
                    .setContentText(message) // Основной текст уведомления
                    .setContentIntent(PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT))
                    .setWhen(time)//отображаемое время уведомления
                    .setContentTitle("Student Activity Manager") //заголовок уведомления
                    .setDefaults(Notification.DEFAULT_ALL); // звук, вибро и диодный индикатор выставляются по умолчанию

            Notification notification = new Notification.BigTextStyle(builder).bigText(message).build();
            notificationManager.notify(notId, notification);


        }

    }

    private static Map<Integer, Pair<Long, String>> getListOfNot(List<ScheduleItem> schItems, List<ScheduleTaskItem> schTasks)
    {
        Map<Integer, Pair<Long, String>> result = new HashMap<Integer, Pair<Long, String>>();
        Calendar calendar = Calendar.getInstance();
        for (ScheduleTaskItem taskItem : schTasks)
        {
            if (!taskItem.getIsCompleted())
            {
                long time = getTimeForTask(taskItem, schItems);
                calendar.setTimeInMillis(time);
                int day = calendar.get(Calendar.DAY_OF_WEEK);
                String nameOfLesson = getScheduleItemById(taskItem.getSchItemId(), schItems).getTitle();

                Pair<Long, String> lastValue = result.get(day);
                if (lastValue == null)
                    result.put(day, new Pair<Long, String>(time, nameOfLesson + " : " + taskItem.getText() + "."));
                else
                    result.put(day, new Pair<Long, String>(lastValue.first, lastValue.second + "\n"
                                                                            + nameOfLesson + " : "
                                                                            + taskItem.getText() + "."));
            }
        }

        return result;
    }

    private static long getTimeForTask(ScheduleTaskItem task, List<ScheduleItem> schItems)
    {
        Calendar calendar = Calendar.getInstance();
        int currDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        ScheduleItem si = getScheduleItemById(task.getSchItemId(), schItems);
        int dayOfTask = (si.getDay() + 2) % 7; //for calendar formal, which start on SUNDAY

        calendar.set(Calendar.DAY_OF_WEEK, dayOfTask - 1); // a one day earlier
        if (dayOfTask <= currDayOfWeek)
            calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH) + 7);

        calendar.set(Calendar.HOUR_OF_DAY, 18);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        return calendar.getTimeInMillis();

    }

    private static ScheduleItem getScheduleItemById(String id, List<ScheduleItem> schItems)
    {
        for (ScheduleItem item : schItems)
        {
            if (id.equals(item.getId()))
            {
                return item;
            }
        }
        return  null;
    }
}
