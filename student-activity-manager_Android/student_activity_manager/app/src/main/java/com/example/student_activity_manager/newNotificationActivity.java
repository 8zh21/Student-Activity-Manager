package com.example.student_activity_manager;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import java.util.Calendar;

public class newNotificationActivity extends Activity {

    private TimePicker timePicker;
    private DatePicker datePicker;
    private String taskId;
    private String text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_notification);

        taskId = getIntent().getExtras().getString("id");
        text = getIntent().getExtras().getString("text");

        timePicker = (TimePicker) findViewById(R.id.notTimePicker);
        timePicker.setIs24HourView(true);
        datePicker = (DatePicker) findViewById(R.id.notDatePicker);

    }

    public void saveNotification(View view) {

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        Intent intent = new Intent(this, NotificationCreator.class);
        intent.setAction(taskId);
        intent.putExtra("text", text);
        intent.putExtra("id", taskId);

        PendingIntent pIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                     timePicker.getCurrentHour(), timePicker.getCurrentMinute());
        long time = calendar.getTimeInMillis();

        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pIntent);

        finish();
    }
}
