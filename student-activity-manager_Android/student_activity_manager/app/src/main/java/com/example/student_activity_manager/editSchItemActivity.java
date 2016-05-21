/**
 * Created by Alexander Podshiblov on 23.03.2016.
 * Модуль редактирования элемента расписания, демонстрирует экран редактирования и
 * сохраняет изменения
 */

package com.example.student_activity_manager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

public class editSchItemActivity extends newSchItemActivity {

    private ScheduleItem item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((Button)findViewById(R.id.submitNewSchItemButton)).setText("Save");
        item = ScheduleActivity.schItemOnEdition;
        mThis = this;
        editTitle.setText(item.getTitle());
        editClassroom.setText(item.getClassroom());
        spinner.setSelection(findTimeItemPositionById(item.getTimeItemId()));
    }

    @Override
    public void saveNewSchItem(View view) {
        String title = editTitle.getText().toString();
        String classroom = editClassroom.getText().toString();

        if (title.isEmpty())
        {
            Toast.makeText(this, "Enter the title", Toast.LENGTH_SHORT).show();
        }
        else if (classroom.isEmpty())
        {
            Toast.makeText(this, "Enter the classroom", Toast.LENGTH_SHORT).show();
        }
        else if (spinner.getSelectedItem() == null)
        {
            Toast.makeText(this, "Choose the time item", Toast.LENGTH_SHORT).show();
        }
        else
        {
            item.setTitle(title);
            item.setClassroom(classroom);
            item.setTimeItemId(((TimeItem) spinner.getSelectedItem()).getId());

            AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        ScheduleActivity.scheduleItemsTable.update(item).get();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ScheduleActivity.scheduleItemAdapter.notifyDataSetChanged();
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

    private int findTimeItemPositionById(String timeId)
    {
        return timeItemAdapter.getPosition(ScheduleActivity.getTimeItemFromId(timeId));
    }
}
