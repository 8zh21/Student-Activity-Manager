package com.example.student_activity_manager;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class newTaskActivity extends Activity {

    private String parentId;
    private Activity mThis = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);

        parentId = getIntent().getExtras().getString("parentId");
    }

    public void saveNewTaskItem(View view) {
        String name = ((EditText) findViewById(R.id.editTaskName)).getText().toString();
        String comment = ((EditText) findViewById(R.id.editTaskComment)).getText().toString();

        if (name.isEmpty() || comment.isEmpty())
        {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
        }
        else
        {
            final TaskItem newTI = new TaskItem(parentId, name, comment, false);

            AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        final TaskItem entity = TasksActivity.tasksTable.insert(newTI).get();
                        TaskItemWrap newTIW = new TaskItemWrap(entity);
                        TasksActivity.allTaskItemWraps.add(newTIW); //добавить в общий список

                        //добавить в дерево
                        if (parentId.equals("root")) // добавить как корневую задачу
                        {
                            newTIW.level = 0;
                            TasksActivity.taskTree.add(newTIW);
                        }
                        else // добавить к родителю
                        {
                            TaskItemWrap parent = TasksActivity.getTaskById(parentId);
                            newTIW.level = parent.level + 1;
                            if (parent.childs == null)
                                parent.childs = new ArrayList<TaskItemWrap>();
                            parent.childs.add(newTIW);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TasksActivity.updateViewOfTasks();
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
