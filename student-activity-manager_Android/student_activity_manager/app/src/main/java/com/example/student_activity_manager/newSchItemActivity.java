package com.example.student_activity_manager;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class newSchItemActivity extends Activity {

    public static TimeItemAdapter timeItemAdapter;
    private Activity mThis = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_sch_item);

        //----------

        Spinner spinner = (Spinner) findViewById(R.id.timeModelSpinner);
        timeItemAdapter = new TimeItemAdapter(this, R.layout.timeitem_row);
        spinner.setAdapter(timeItemAdapter);

        for (TimeItem item : ScheduleActivity.timeItems)
        {
            timeItemAdapter.add(item);
        }
    }

    public void createTimeItem(View view) {
        Intent intent = new Intent(this, newTimeItemActivity.class);
        startActivity(intent);
    }

    public void tryToDelTimeItem(final TimeItem item) {
        if (!isTimeItemFree(item))
        {
            Dialog.createAndShowDialog(this, "This item still used", "Denied");
        }
        else
        {
            AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        ScheduleActivity.timeItemsTable.delete(item).get();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                timeItemAdapter.remove(item);
                                mThis.finish();
                            }
                        });
                    } catch (Exception e)
                    {
                        Dialog.createAndShowDialogFromTask(mThis, e.getMessage(), "Error");
                    }
                    return null;
                }
            };
            AsyncTaskRuner.runAsyncTask(task);
        }
    }

    private boolean isTimeItemFree(TimeItem item)
    {
        for (ScheduleItem s_item : ScheduleActivity.scheduleItems)
        {
            if (s_item.getTimeItemId().equals(item.getId()))
                return  false;
        }
        return  true;
    }
}
