package com.example.student_activity_manager;

import android.app.Activity;
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
    private AsyncTask<Void, Void, Void> refreshing;


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
        Spinner spinner = (Spinner) findViewById(R.id.day_spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,
                                                                getResources().getStringArray(R.array.days));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //nothing
            }
        });
        spinner.setSelection(0);
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
                        ListView listView = (ListView) findViewById(R.id.sch_items_listView);
                        ArrayAdapter<ScheduleItem> adapter = new ArrayAdapter<ScheduleItem>(mThis, android.R.layout.simple_list_item_1,
                                scheduleItems);
                        listView.setAdapter(adapter);

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

}
