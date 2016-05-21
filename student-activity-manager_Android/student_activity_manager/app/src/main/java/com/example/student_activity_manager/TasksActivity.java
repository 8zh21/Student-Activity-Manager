/**
 * Created by Alexander Podshiblov on 08.05.2016.
 * Модуль демонстрации задач,
 * демонстрирует экран задач,
 * инициализирует таблицы синхронизации и локальное хранилище,
 * загружает с сервера/локального хранилища данные задач,
 * синхранизирует данные с сервером,
 * запускает экраны создания задач,
 * запускает создание и удаление задач,
 * строит дерево задач,
 * строит список задач для адаптера данных
 */

package com.example.student_activity_manager;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncTable;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.MobileServiceLocalStoreException;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class TasksActivity extends Activity {

    private MobileServiceClient mClient;
    private Activity mThis = this;
    public static MobileServiceSyncTable<TaskItem> tasksTable;

    public static ArrayList<TaskItemWrap> allTaskItemWraps;
    public static ArrayList<TaskItemWrap> tasksForAdapter;
    public static ArrayList<TaskItemWrap> taskTree;

    public static TaskItemAdapter taskItemAdapter;
    private AsyncTask<Void, Void, Void> refreshing;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        mClient = ToDoActivity.mClient.withFilter(new ProgressFilter(this,
                (ProgressBar) findViewById(R.id.loadingProgressBar4)));

        try {
            tasksTable = mClient.getSyncTable(getString(R.string.tasks_table_name),
                    TaskItem.class);

            initLocalStore().get();
            refreshing = refreshTasks();
            prepareListView();

        } catch (Exception e) {
            Dialog.createAndShowDialog(this, e.getMessage(), "Ошибка");
        }
    }

    private AsyncTask<Void, Void, Void> prepareListView() {

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    refreshing.get();
                } catch (Exception e) {
                    Dialog.createAndShowDialogFromTask(mThis, e.getMessage(), "Ошибка");
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ListView tasksListView = (ListView) findViewById(R.id.tasksListView);

                        tasksForAdapter = new ArrayList<TaskItemWrap>();
                        buildListForAdapter(taskTree);

                        taskItemAdapter = new TaskItemAdapter(mThis, R.layout.taskitem_row, tasksForAdapter);
                        tasksListView.setAdapter(taskItemAdapter);

                        tasksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                TaskItemWrap item = taskItemAdapter.getItem(position);
                                item.isOpen = !item.isOpen;
                                updateViewOfTasks();
                            }
                        });

                        tasksListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                            @Override
                            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                                final TaskItemWrap item = taskItemAdapter.getItem(position);
                                String[] menuItems = {"Подробнее", "Выполнено", "Добавить напоминание", "Добавить подзадачу", "Удалить"};

                                Dialog.createAndShowMenuDialog(mThis,
                                        item.item.getName(),
                                        menuItems,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (which == 0)
                                                    showTaskInfo(item);
                                                else if (which == 1)
                                                    setCompleted(item);
                                                else if (which == 2)
                                                    createNotification(item.item.getId(), item.item.getName());
                                                else if (which == 3)
                                                    addTaskToParent(item.item.getId());
                                                else if (which == 4)
                                                    tryToDelTaskItem(item);
                                            }
                                        });

                                return true;
                            }
                        });
                    }
                });
                return null;
            }
        };
        return AsyncTaskRuner.runAsyncTask(task);
    }

    private AsyncTask<Void, Void, Void> initLocalStore() throws MobileServiceLocalStoreException, ExecutionException, InterruptedException {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {

                    MobileServiceSyncContext syncContext = mClient.getSyncContext();

                    if (syncContext.isInitialized())
                        return null;

                    SQLiteLocalStore localStore = new SQLiteLocalStore(mClient.getContext(), "OfflineStore", null, 1);

                    //---------

                    Map<String, ColumnDataType> tableDefinition = new HashMap<String, ColumnDataType>();
                    tableDefinition.put("id", ColumnDataType.String);
                    tableDefinition.put("userId", ColumnDataType.String);
                    tableDefinition.put("parentId", ColumnDataType.String);
                    tableDefinition.put("name", ColumnDataType.String);
                    tableDefinition.put("comment", ColumnDataType.String);
                    tableDefinition.put("isCompleted", ColumnDataType.Boolean);

                    localStore.defineTable(getString(R.string.tasks_table_name), tableDefinition);

                    //---------

                    SimpleSyncHandler handler = new SimpleSyncHandler();

                    syncContext.initialize(localStore, handler).get();

                } catch (final Exception e) {
                    Dialog.createAndShowDialogFromTask(mThis, e.getMessage(), "Ошибка");
                }
                return null;
            }
        };

        return AsyncTaskRuner.runAsyncTask(task);
    }

    private void sync() {
        if (!isNetworkConnected()) {
            Dialog.createAndShowDialogFromTask(mThis, "Работаем оффлайн.", "Отсутствует подключение");
        }
        else {
            try {
                MobileServiceSyncContext syncContext = mClient.getSyncContext();
                syncContext.push().get();
                tasksTable.pull(null).get();

            } catch (final ExecutionException e) {
                if (e.getCause().getMessage() != null && e.getCause().getMessage().equals("{'code': 401}")) {
                    finish();
                    ToDoActivity.mThis.authenticate(true);
                }
                else if (e.getCause().getCause().getMessage() != null &&
                        e.getCause().getCause().getMessage().equals("timeout"))
                {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Таймаут соединения\nПопытка №2", Toast.LENGTH_SHORT).show();
                        }
                    });
                    sync();
                }
                else {
                    Dialog.createAndShowDialogFromTask(mThis, e.getMessage(), "Ошибка");
                }
            } catch (Exception e) {
                Dialog.createAndShowDialogFromTask(mThis, e.getMessage(), "Ошибка");
            }
        }
    }

    private AsyncTask<Void, Void, Void> refreshTasks() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    sync();
                    List<TaskItem> items = tasksTable.read(null).get();

                    buildTaskTree(items);
                } catch (Exception e) {
                    Dialog.createAndShowDialogFromTask(mThis, e.getMessage(), "Ошибка");
                }
                return null;
            }
        };
        return AsyncTaskRuner.runAsyncTask(task);
    }

    private void buildTaskTree(List<TaskItem> items)
    {
        allTaskItemWraps = new ArrayList<TaskItemWrap>();
        taskTree = new ArrayList<TaskItemWrap>();

        for (TaskItem item : items)
        {
            allTaskItemWraps.add(new TaskItemWrap(item));
        }

        for (TaskItemWrap item : allTaskItemWraps)
        {
            if(! item.item.getParentId().equals("root"))
            {
                TaskItemWrap parent = getTaskById(item.item.getParentId());
                if (parent != null)
                {
                    if (parent.childs == null)
                        parent.childs = new ArrayList<TaskItemWrap>();
                    parent.childs.add(item);
                }
            }
            else
            {
                taskTree.add(item);
            }
        }
        setLevels(taskTree, 0);

    }

    private void setLevels(List<TaskItemWrap> l, int level)
    {
        if (l == null || l.size() == 0)
            return;
        else
        {
            for (TaskItemWrap item : l)
            {
                item.level = level;
                setLevels(item.childs, level + 1);
            }
        }
    }

    public static void buildListForAdapter(List<TaskItemWrap> list)
    {
        if (list == null || list.size() == 0)
            return;

        int n = list.size();
        for (int i = 0; i < n; i++)
        {
            tasksForAdapter.add(list.get(i));
            if (list.get(i).isOpen)
                buildListForAdapter(list.get(i).childs);
        }
    }

    public static TaskItemWrap getTaskById(String id)
    {
        for(TaskItemWrap item : allTaskItemWraps)
        {
            if (item.item.getId().equals(id))
                return item;
        }
        return null;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    public void addTask(View view) {
        Intent intent = new Intent(this, newTaskActivity.class);
        intent.putExtra("parentId", "root");
        startActivity(intent);
    }

    public static void updateViewOfTasks()
    {
        tasksForAdapter.clear();
        buildListForAdapter(taskTree);
        taskItemAdapter.notifyDataSetChanged();
    }

    private void showTaskInfo(TaskItemWrap item)
    {
        String message = "Задача: " + item.item.getName() + "\n\n" +
                         "Комментарий: " + item.item.getComment() + "\n\n" +
                         "Статус: ";
        if (item.item.isCompleted())
            message += "Выполнено";
        else
            message += "Невыполнено";

        Dialog.createAndShowDialog(mThis, message, item.item.getName());
    }

    private void setCompleted(final TaskItemWrap item)
    {
        if (item.childs == null || item.childs.size() == 0 || isChildsCompleted(item))
        {
            item.item.setIsCompleted(true);
            AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        tasksTable.update(item.item).get();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateViewOfTasks();
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

    private boolean isChildsCompleted(TaskItemWrap item)
    {
        for(TaskItemWrap child : item.childs)
        {
            if (!child.item.isCompleted()) {
                Dialog.createAndShowDialog(mThis, "Имеются невыполненные подзадачи!", item.item.getName());
                return false;
            }
        }
        return true;
    }

    private void addTaskToParent(String parentId)
    {
        Intent intent = new Intent(this, newTaskActivity.class);
        intent.putExtra("parentId", parentId);
        startActivity(intent);
    }

    private void tryToDelTaskItem(final TaskItemWrap item)
    {
        Dialog.createAndShowYNDialog(this, "Вы уверенны, что хотите удалить задачу \"" + item.item.getName() + "\"",
                "Удаление",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        realDelTaskItem(item);
                    }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //nothing to do
                    }
                });
    }

    private void realDelTaskItem(final TaskItemWrap item)
    {
        if (item.childs != null && item.childs.size() != 0)
        {
            Dialog.createAndShowDialog(mThis, "В удалении отказано, имеются подзадачи!", "Оповещение");
        }
        else
        {
            AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        tasksTable.delete(item.item).get();

                        allTaskItemWraps.remove(item);
                        if (item.item.getParentId().equals("root"))
                        {
                            taskTree.remove(item);
                        }
                        else
                        {
                            getTaskById(item.item.getParentId()).childs.remove(item);
                        }
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateViewOfTasks();
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

    private void createNotification(String id, String text)
    {
        Intent intent = new Intent(this, newNotificationActivity.class);
        intent.putExtra("id", id);
        intent.putExtra("text", "Задача \"" + text + "\" ожидает выполнения");
        startActivity(intent);
    }
}
