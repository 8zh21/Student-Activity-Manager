package com.example.student_activity_manager;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * Created by Alexander on 20.03.2016.
 * Модуль создания нового элемента расписания,
 * демонстрирует экран создания нового элемента расписания,
 * заускает создание нового элемента времени,
 * удаляет элемент времени,
 * сохраняет новый элемент расписания
 */

public class newSchItemActivity extends Activity {

    public static TimeItemAdapter timeItemAdapter;
    protected Activity mThis = this;
    protected EditText editTitle;
    protected EditText editClassroom;
    protected Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_sch_item);

        //----------

        spinner = (Spinner) findViewById(R.id.timeModelSpinner);
        timeItemAdapter = new TimeItemAdapter(this, R.layout.timeitem_row, ScheduleActivity.timeItems);
        spinner.setAdapter(timeItemAdapter);
        timeItemAdapter.notifyDataSetChanged();

        editTitle = (EditText) findViewById(R.id.editTitle);
        editClassroom = (EditText) findViewById(R.id.editClassroom);
    }

    public void createTimeItem(View view) {
        Intent intent = new Intent(this, newTimeItemActivity.class);
        startActivity(intent);
    }

    public void tryToDelTimeItem(final TimeItem item) {

        Dialog.createAndShowYNDialog(this, "Вы уверенны, что хотите удалить времеменную модель \"" + item.getName() + "\"?",
                "Удаление",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        realDelTimeItem(item);
                    }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //nothing to do
                    }
                });
    }

    private  void realDelTimeItem(final TimeItem item)
    {
        if (!isTimeItemFree(item))
        {
            Dialog.createAndShowDialog(this, "Эта модель используется", "Запрещенно");
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
                                //ScheduleActivity.timeItems.remove(item);
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

    private boolean isTimeItemFree(TimeItem item)
    {
        for (ScheduleItem s_item : ScheduleActivity.scheduleItems)
        {
            if (s_item.getTimeItemId().equals(item.getId()))
                return  false;
        }
        return  true;
    }

    public void saveNewSchItem(View view) {

        String title = editTitle.getText().toString();
        String classroom = editClassroom.getText().toString();

        if (title.isEmpty())
        {
            Toast.makeText(this, "Введите заголовок", Toast.LENGTH_SHORT).show();
        }
        else if (classroom.isEmpty())
        {
            Toast.makeText(this, "Введите аудиторию", Toast.LENGTH_SHORT).show();
        }
        else if (spinner.getSelectedItem() == null)
        {
            Toast.makeText(this, "Выберете временную модель", Toast.LENGTH_SHORT).show();
        }
        else
        {
            final ScheduleItem newSI = new ScheduleItem(title, classroom,
                    ((TimeItem) spinner.getSelectedItem()).getId(),
                    ScheduleActivity.daySpinner.getSelectedItemPosition());

            newSI.setUserId(ToDoActivity.mUser.getmId());

            AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        final ScheduleItem entity = ScheduleActivity.scheduleItemsTable.insert(newSI).get();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                ScheduleActivity.scheduleItemAdapter.add(entity);
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
