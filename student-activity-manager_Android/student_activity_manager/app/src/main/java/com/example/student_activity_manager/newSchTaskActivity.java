package com.example.student_activity_manager;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class newSchTaskActivity extends Activity {

    private String scheduleItemId;
    private Activity mThis = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_sch_task);

        scheduleItemId = getIntent().getExtras().getString("scheduleItemId");
    }

    public void addNewTask(View view) {
        String text = ((EditText) findViewById(R.id.schTaskText)).getText().toString();

        if (text.isEmpty())
        {
            Toast.makeText(this, "Введите текст задания", Toast.LENGTH_SHORT).show();
        }
        else
        {
            final ScheduleTaskItem sTI = new ScheduleTaskItem(text, scheduleItemId);

            AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        final ScheduleTaskItem entity = ScheduleActivity.scheduleTaskTable.insert(sTI).get();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ScheduleTasksActivity.taskAdapter.add(entity);
                                ScheduleActivity.scheduleTaskItems.add(entity);
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
