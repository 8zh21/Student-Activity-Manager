package com.example.student_activity_manager;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncTable;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.MobileServiceLocalStoreException;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ScheduleActivity extends Activity {

    public static MobileServiceSyncTable<ScheduleItem> scheduleItemsTable;
    public static MobileServiceSyncTable<TimeItem> timeItemsTable;
    private MobileServiceClient mClient;
    private Activity mThis = this;
    public static List<ScheduleItem> scheduleItems;
    public static List<TimeItem> timeItems;
    public static Spinner daySpinner;
    public  static ScheduleItemAdapter scheduleItemAdapter;
    private AsyncTask<Void, Void, Void> refreshing;
    public static ScheduleItem schItemOnEdition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        mClient = ToDoActivity.mClient.withFilter(new ProgressFilter(this,
                  (ProgressBar) findViewById(R.id.loadingProgressBar3)));

        try {
            scheduleItemsTable = mClient.getSyncTable(getString(R.string.scheduleItems_table_name),
                                                      ScheduleItem.class);
            timeItemsTable = mClient.getSyncTable(getString(R.string.timeItems_table_name),
                                                  TimeItem.class);
            initLocalStore().get();
            refreshing = refreshSchedule();

            prepareDaySelector();
            prepareListView();

        } catch (Exception e) {
            Dialog.createAndShowDialog(this, e.getMessage(), "Error");
        }
    }

    private void prepareDaySelector()
    {
        daySpinner = (Spinner) findViewById(R.id.day_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                                                                getResources().getStringArray(R.array.days));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(adapter);
        daySpinner.setSelection(0);
    }

    private void prepareListView()
    {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    refreshing.get();
                } catch (Exception e) {
                    Dialog.createAndShowDialogFromTask(mThis, e.getMessage(), "Error");
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ListView scheduleItemView = (ListView) findViewById(R.id.sch_items_listView);
                        scheduleItemAdapter = new ScheduleItemAdapter(mThis, R.layout.scheduleitem_row, scheduleItems);
                        scheduleItemView.setAdapter(scheduleItemAdapter);
                        scheduleItemAdapter.filter(daySpinner.getSelectedItemPosition());

                        daySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                scheduleItemAdapter.filter(daySpinner.getSelectedItemPosition());
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                //nothing to do
                            }
                        });

                        scheduleItemView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                ScheduleItem item = scheduleItemAdapter.getItem(position);
                                TimeItem timeItem = getTimeItemFromId(item.getTimeItemId());
                                String time = timeItem.getStartTime() + "-" + timeItem.getFinishTime();
                                String message = item.getTitle() +
                                        "\n\n" + "At classroom: " + item.getClassroom();

                                Dialog.createAndShowDialog(mThis, message, time);
                            }
                        });

                        scheduleItemView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                            @Override
                            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                                String[] menuItems = {"Edit", "Delete"};
                                final ScheduleItem scheduleItem =  scheduleItemAdapter.getItem(position);
                                Dialog.createAndShowSchItemMenuDialog(mThis,
                                        scheduleItem.getTitle(),
                                        menuItems,
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                if (which == 0)
                                                    editScheduleItem(scheduleItem);
                                                else if (which == 1)
                                                    tryToDelScheduleItem(scheduleItem);
                                            }
                                        });

                                return true;
                            }
                        });

                        findViewById(R.id.add_sch_item_button).setVisibility(View.VISIBLE);
                    }
                });
                return  null;
            }
        };
        AsyncTaskRuner.runAsyncTask(task);

    }

    private AsyncTask<Void, Void, Void> sync() {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    MobileServiceSyncContext syncContext = mClient.getSyncContext();
                    syncContext.push().get();
                    scheduleItemsTable.pull(null).get();
                    timeItemsTable.pull(null).get();

                } catch (final Exception e) {
                    Dialog.createAndShowDialogFromTask(mThis, e.getMessage(), "Error");
                }
                return null;
            }
        };
        return  AsyncTaskRuner.runAsyncTask(task);
    }

    private AsyncTask<Void, Void, Void> refreshSchedule()
    {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    refreshFromMobileServiceTableSyncTable();

                } catch (Exception e) {
                    Dialog.createAndShowDialogFromTask(mThis, e.getMessage(), "Error");
                }
                return null;
            }
        };
        return AsyncTaskRuner.runAsyncTask(task);
    }

    private void refreshFromMobileServiceTableSyncTable() throws ExecutionException, InterruptedException
    {
        sync().get();
        scheduleItems = scheduleItemsTable.read(null).get();
        timeItems = timeItemsTable.read(null).get();
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
                    tableDefinition.put("title", ColumnDataType.String);
                    tableDefinition.put("classroom", ColumnDataType.String);
                    tableDefinition.put("timeItemId", ColumnDataType.String);
                    tableDefinition.put("day", ColumnDataType.Integer);

                    localStore.defineTable(getString(R.string.scheduleItems_table_name), tableDefinition);

                    //---------

                    tableDefinition = new HashMap<String, ColumnDataType>();
                    tableDefinition.put("id", ColumnDataType.String);
                    tableDefinition.put("userId", ColumnDataType.String);
                    tableDefinition.put("name", ColumnDataType.String);
                    tableDefinition.put("sh", ColumnDataType.Integer);
                    tableDefinition.put("sm", ColumnDataType.Integer);
                    tableDefinition.put("fh", ColumnDataType.Integer);
                    tableDefinition.put("fm", ColumnDataType.Integer);

                    localStore.defineTable(getString(R.string.timeItems_table_name), tableDefinition);

                    //---------

                    SimpleSyncHandler handler = new SimpleSyncHandler();

                    syncContext.initialize(localStore, handler).get();

                } catch (final Exception e) {
                    Dialog.createAndShowDialogFromTask(mThis, e.getMessage(), "Error");
                }

                return null;
            }
        };

        return AsyncTaskRuner.runAsyncTask(task);
    }

    public void createScheduleItem(View view) {
        Intent intent = new Intent(this, newSchItemActivity.class);
        startActivity(intent);
    }

    public static TimeItem getTimeItemFromId(String timeId)
    {
        for (TimeItem tItem : timeItems)
        {
            if (timeId.equals(tItem.getId()))
            {
                return tItem;
            }
        }
        return  null;
    }

    private void editScheduleItem(ScheduleItem item)
    {
        schItemOnEdition = item;
        Intent intent = new Intent(this, editSchItemActivity.class);
        startActivity(intent);
    }

    private void tryToDelScheduleItem(final ScheduleItem item)
    {
        Dialog.createAndShowYNDialog(this, "Are you sure you want to delete the item \"" + item.getTitle() + "\"",
                "Deleting",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        realDelScheduleItem(item);
                    }
                },
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //nothing to do
                    }
                });
    }

    private void realDelScheduleItem(final ScheduleItem item)
    {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    ScheduleActivity.scheduleItemsTable.delete(item).get();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            scheduleItemAdapter.remove(item);
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
