package com.example.student_activity_manager;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceAuthenticationProvider;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;
import com.microsoft.windowsazure.mobileservices.table.query.Query;
import com.microsoft.windowsazure.mobileservices.table.query.QueryOperations;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncContext;
import com.microsoft.windowsazure.mobileservices.table.sync.MobileServiceSyncTable;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.ColumnDataType;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.MobileServiceLocalStoreException;
import com.microsoft.windowsazure.mobileservices.table.sync.localstore.SQLiteLocalStore;
import com.microsoft.windowsazure.mobileservices.table.sync.synchandler.SimpleSyncHandler;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.microsoft.windowsazure.mobileservices.table.query.QueryOperations.val;




public class ToDoActivity extends Activity {

    private static final int GET_USER_INFO_REQUEST_CODE = 1;
    public static MobileServiceClient mClient;
    private ProgressBar mProgressBar;
    public static UserItem mUser;
    private final ToDoActivity mThis = this;

    private void authenticate() {

        // Login using the Google provider.
        ListenableFuture<MobileServiceUser> mLogin = mClient.login(MobileServiceAuthenticationProvider.Google);
        Futures.addCallback(mLogin, new FutureCallback<MobileServiceUser>() {
            @Override
            public void onFailure(Throwable exc) {
                Dialog.createAndShowDialog(mThis, ((Exception) exc).getMessage(), "Error");
            }
            @Override
            public void onSuccess(MobileServiceUser user) {
                Toast.makeText(getApplicationContext(), "You are now logged in", Toast.LENGTH_SHORT).show();
                checkUser();
            }
        });
    }



    private void checkUser()
    {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    final List<UserItem> u = mClient.getTable("users", UserItem.class).execute().get();

                    //Offline Sync TO DO

                    if (u.isEmpty())
                    {
                        mUser = new UserItem();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent getUserInfoIntent = new Intent(mThis, FillUserInfo.class);
                                startActivityForResult(getUserInfoIntent, GET_USER_INFO_REQUEST_CODE);
                            }
                        });
                    }
                    else
                    {
                        mUser = u.get(0);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "User confirmed", Toast.LENGTH_SHORT).show();
                                ((Button) findViewById(R.id.GoToSchedule)).setEnabled(true);
                            }
                        });
                    }
                } catch (final Exception e){
                    Dialog.createAndShowDialogFromTask(mThis, e.getMessage(), "Error");
                }
                return null;
            }
        };

        AsyncTaskRuner.runAsyncTask(task);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do);

        mProgressBar = (ProgressBar) findViewById(R.id.loadingProgressBar);

        // Initialize the progress bar
        mProgressBar.setVisibility(ProgressBar.GONE);
        ((Button) findViewById(R.id.GoToSchedule)).setEnabled(false);

        try {
            // Create the Mobile Service Client instance, using the provided
            // Mobile Service URL and key
            mClient = new MobileServiceClient(
                    "https://student-activity-manager.azurewebsites.net",
                    this).withFilter(new ProgressFilter(this, mProgressBar));

            authenticate();

        } catch (MalformedURLException e) {
            Dialog.createAndShowDialog(this, "There was an error creating the Mobile Service. Verify the URL", "Error");
        } catch (Exception e){
            Dialog.createAndShowDialog(this, e.getMessage(), "Error");
        }
    }

    public void goToSchedule(View view)
    {
        Intent intent = new Intent(this, ScheduleActivity.class);
        startActivity(intent);
    }

    @Override
    protected  void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GET_USER_INFO_REQUEST_CODE && resultCode == RESULT_OK)
        {
            mUser.setmEdInstId(data.getStringExtra(FillUserInfo.ED_INST_ITEM_ID));
            mUser.setmFacultyId(data.getStringExtra(FillUserInfo.FACULTY_ITEM_ID));

            registerNewUser(mUser);
        }
    }

    private void registerNewUser(UserItem u)
    {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try
                {
                    mClient.getTable(getString(R.string.Users_table_name), UserItem.class)
                           .insert(mUser);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "You are registered", Toast.LENGTH_SHORT).show();
                            ((Button) findViewById(R.id.GoToSchedule)).setEnabled(true);
                        }
                    });

                } catch (final Exception e){
                    Dialog.createAndShowDialogFromTask(mThis, e.getMessage(), "Error");
                }
                return null;
            }
        };
        AsyncTaskRuner.runAsyncTask(task);
    }


}