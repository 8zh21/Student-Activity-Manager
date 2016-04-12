package com.example.student_activity_manager;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

public class newTimeItemActivity extends Activity {

    EditText nameTextEdit;
    TimePicker startTP;
    TimePicker finishTP;
    Activity mThis = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_time_item);

        nameTextEdit = (EditText)findViewById(R.id.timeItemName);
        startTP = (TimePicker) findViewById(R.id.startTimePicker);
        startTP.setIs24HourView(true);
        finishTP = (TimePicker) findViewById(R.id.finishTimePicker);
        finishTP.setIs24HourView(true);

    }

    public void saveNewTimeItem(View view)
    {
        int sh = startTP.getCurrentHour();
        int sm = startTP.getCurrentMinute();
        int fh = finishTP.getCurrentHour();
        int fm = finishTP.getCurrentMinute();

        if (nameTextEdit.getText().toString().isEmpty())
        {
            Toast.makeText(this, "Enter the name", Toast.LENGTH_SHORT).show();
        }
        else if (sh > fh || (sh == fh && sm >= fm))
        {
            Toast.makeText(this, "Start must be earlier than the finish", Toast.LENGTH_SHORT).show();
        }
        else{
            final TimeItem newTM = new TimeItem(nameTextEdit.getText().toString(),
                                                sh, sm, fh, fm);

            newTM.setUserId(ToDoActivity.mUser.getmId());

            AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        final TimeItem entity = ScheduleActivity.timeItemsTable.insert(newTM).get();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                newSchItemActivity.timeItemAdapter.add(entity);
                                mThis.finish();
                            }
                        });
                    } catch (Exception e)
                    {
                        Dialog.createAndShowDialogFromTask(mThis, e.getMessage(), "Ошибка");
                    }
                    return null;
                }
            };
            AsyncTaskRuner.runAsyncTask(task);
        }
    }
}
