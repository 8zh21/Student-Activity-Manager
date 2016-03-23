package com.example.student_activity_manager;


import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceAuthenticationProvider;
import com.microsoft.windowsazure.mobileservices.authentication.MobileServiceUser;

import java.net.MalformedURLException;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class ToDoActivity extends Activity {

    private static final int GET_USER_INFO_REQUEST_CODE = 1;
    public static MobileServiceClient mClient;
    private ProgressBar mProgressBar;
    public static UserItem mUser;
    private final ToDoActivity mThis = this;

    public static final String SHAREDPREFFILE = "temp";
    public static final String USERIDPREF = "uid";
    public static final String TOKENPREF = "tkn";
    public static final String USEREDINST = "edinst";
    public static final String USERFACULTY = "faculty";

    private void authenticate() {
        // We first try to load a token cache if one exists.
        if (loadUserTokenCache(mClient))
        {
            Toast.makeText(getApplicationContext(), "You are now logged in offline", Toast.LENGTH_SHORT).show();
            checkUser();
        }
        // If we failed to load a token cache, login and create a token cache
        else
        {
            // Login using the Google provider.
            ListenableFuture<MobileServiceUser> mLogin = mClient.login(MobileServiceAuthenticationProvider.Google);

            Futures.addCallback(mLogin, new FutureCallback<MobileServiceUser>() {
                @Override
                public void onFailure(Throwable exc) {
                    Dialog.createAndShowDialog(mThis, "You must log in. Login Required", "Error");
                    ((Button) findViewById(R.id.Log_In)).setEnabled(true);
                    ((Button) findViewById(R.id.Log_In)).setVisibility(View.VISIBLE);
                }
                @Override
                public void onSuccess(MobileServiceUser user) {
                    Toast.makeText(getApplicationContext(), "You are now logged in", Toast.LENGTH_SHORT).show();
                    cacheUserToken(mClient.getCurrentUser());
                    ((Button) findViewById(R.id.Log_In)).setVisibility(View.GONE);
                    checkUser();
                }
            });
        }
    }

    private void checkUser()
    {
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    if (!loadUserInfo())
                    {
                        final List<UserItem> u = mClient.getTable("users", UserItem.class).execute().get();
                        if(u.isEmpty()) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Intent getUserInfoIntent = new Intent(mThis, FillUserInfo.class);
                                    startActivityForResult(getUserInfoIntent, GET_USER_INFO_REQUEST_CODE);
                                }
                            });
                        } else
                        {
                            mUser = u.get(0);
                            cacheUserInfo();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "User confirmed", Toast.LENGTH_SHORT).show();
                                    ((Button) findViewById(R.id.GoToSchedule)).setEnabled(true);
                                }
                            });
                        }
                    }
                    else
                    {
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

        mUser = new UserItem();
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

                            cacheUserInfo();
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

    private void cacheUserToken(MobileServiceUser user)
    {
        SharedPreferences prefs = getSharedPreferences(SHAREDPREFFILE, Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putString(USERIDPREF, user.getUserId());
        editor.putString(TOKENPREF, user.getAuthenticationToken());
        mUser.setmId(user.getUserId());
        editor.commit();
    }

    private boolean loadUserTokenCache(MobileServiceClient client)
    {
        SharedPreferences prefs = getSharedPreferences(SHAREDPREFFILE, Context.MODE_PRIVATE);
        String userId = prefs.getString(USERIDPREF, "undefined");
        if (userId == "undefined")
            return false;
        String token = prefs.getString(TOKENPREF, "undefined");
        if (token == "undefined")
            return false;

        MobileServiceUser user = new MobileServiceUser(userId);
        user.setAuthenticationToken(token);
        mUser.setmId(userId);
        client.setCurrentUser(user);

        return true;
    }

    private boolean loadUserInfo()
    {
        SharedPreferences prefs = getSharedPreferences(SHAREDPREFFILE, Context.MODE_PRIVATE);

        mUser.setmId(prefs.getString(USERIDPREF, "undefined"));
        if (mUser.getmId() == "undefined")
            return false;

        mUser.setmEdInstId(prefs.getString(USEREDINST, "undefined"));
        if (mUser.getmEdInstId() == "undefined")
            return false;

        mUser.setmFacultyId(prefs.getString(USERFACULTY, "undefined"));
        if (mUser.getmFacultyId() == "undefined")
            return false;

        return true;
    }

    private void cacheUserInfo()
    {
        SharedPreferences prefs = getSharedPreferences(SHAREDPREFFILE, Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putString(USEREDINST, mUser.getmEdInstId());
        editor.putString(USERFACULTY, mUser.getmFacultyId());
        editor.commit();
    }

    public void logIn(View view) {
        authenticate();
        ((Button) findViewById(R.id.Log_In)).setEnabled(false);
    }
}