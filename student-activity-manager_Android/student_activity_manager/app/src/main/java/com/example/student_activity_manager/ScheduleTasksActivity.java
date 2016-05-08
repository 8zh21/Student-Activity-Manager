package com.example.student_activity_manager;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;

import java.util.ArrayList;
import java.util.List;

public class ScheduleTasksActivity extends Activity {

    private Activity mThis = this;
    private String scheduleItemId = null;
    public  static SchTaskItemAdapter1 taskAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_tasks);

        int mode = getIntent().getExtras().getInt("position");

        ListView tasksListView = (ListView) findViewById(R.id.schTasksListView);

        final CheckBox cb = ((CheckBox) findViewById(R.id.hideCompletedCheckBox));

        if (mode == -1)
        {
            List<ScheduleTaskItem> taskItemsForAdapter = new ArrayList<ScheduleTaskItem>(ScheduleActivity.scheduleTaskItems);
            taskAdapter = new SchTaskItemAdapter2(this, R.layout.schedule_task_item_row_2,
                                                  taskItemsForAdapter);
            findViewById(R.id.createSchTaskButton).setVisibility(View.GONE);
            cb.setChecked(true);
        }
        else
        {
            scheduleItemId = ScheduleActivity.scheduleItems.get(mode).getId();
            taskAdapter = new SchTaskItemAdapter1(this, R.layout.schedule_task_item_row_1,
                                                  getTasks(scheduleItemId));
        }

        tasksListView.setAdapter(taskAdapter);
        taskAdapter.notifyDataSetChanged();

        tasksListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                String[] menuItems = {"Выполнено", "Удалить"};
                final ScheduleTaskItem scheduleTaskItem = taskAdapter.getItem(position);
                Dialog.createAndShowMenuDialog(mThis,
                        "Задание",
                        menuItems,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == 0)
                                    setCompleted(scheduleTaskItem);
                                else if (which == 1)
                                    tryToDeleteSchTaskItem(scheduleTaskItem);
                            }
                        });
                return true;
            }
        });

        cb.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        taskAdapter.filter();
                    }
                }
        );
        taskAdapter.filter();
    }



    private List<ScheduleTaskItem> getTasks(String schItemId)
    {
        ArrayList<ScheduleTaskItem> list = new ArrayList<ScheduleTaskItem>();

        for (ScheduleTaskItem item : ScheduleActivity.scheduleTaskItems)
        {
            if (item.getSchItemId().equals(schItemId))
                list.add(item);
        }
        return list;
    }

    private void setCompleted(final ScheduleTaskItem item) {
        item.setIsCompleted(true);
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    ScheduleActivity.scheduleTaskTable.update(item).get();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ((SchTaskItemAdapter1)taskAdapter).filter();
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

    private void tryToDeleteSchTaskItem(final ScheduleTaskItem item) {
        Dialog.createAndShowYNDialog(this, "Вы уверенны, что хотите удалить эту задачу?",
                "Удаление",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        realDeleteSchTaskItem(item);
                    }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //nothing to do
                    }
                });
    }

    private void realDeleteSchTaskItem(final ScheduleTaskItem item)
    {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    ScheduleActivity.scheduleTaskTable.delete(item).get();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            taskAdapter.remove(item);
                            ScheduleActivity.scheduleTaskItems.remove(item);
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

    public void createSchTask(View view)
    {
        Intent intent = new Intent(this, newSchTaskActivity.class);
        intent.putExtra("scheduleItemId", scheduleItemId);
        startActivity(intent);
    }

    public ScheduleItem getScheduleItemById(String id)
    {
        for (ScheduleItem item : ScheduleActivity.scheduleItemAdapter.getAllScheduleItems())
        {
            if (id.equals(item.getId()))
            {
                return item;
            }
        }
        return  null;
    }
}
