/**
 * Created by Alexander Podshiblov on 15.03.2016.
 * Главный модуль приложения,
 * демонстрирует главный экран, запускает экраны регистрации, расписания и долговременных задач,
 * инициализирует объект мобильного клиента,
 * запускает авторизацию,
 * кэширует и загружает токен авторизации и регистрационные данные,
 */

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
    public static ToDoActivity mThis;

    public static final String SHAREDPREFFILE = "temp";
    public static final String USERIDPREF = "uid";
    public static final String TOKENPREF = "tkn";
    public static final String USERFACULTY = "faculty";

    public void authenticate(boolean isForcibly) {

        // We first try to load a token cache if one exists.
        if (!isForcibly && loadUserTokenCache(mClient))
        {
            Toast.makeText(getApplicationContext(), "Токен авторизации загружен", Toast.LENGTH_SHORT).show();
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
                    Dialog.createAndShowDialog(mThis, "Авторизация обязательна", "Ошибка");
                    ((Button) findViewById(R.id.Log_In)).setEnabled(true);
                    ((Button) findViewById(R.id.Log_In)).setVisibility(View.VISIBLE);
                }
                @Override
                public void onSuccess(MobileServiceUser user) {
                    Toast.makeText(getApplicationContext(), "Вы авторизованы", Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(getApplicationContext(), "Пользователь подтвержден", Toast.LENGTH_SHORT).show();
                                    ((Button) findViewById(R.id.GoToSchedule)).setEnabled(true);
                                    ((Button) findViewById(R.id.GoToTasks)).setEnabled(true);
                                }
                            });
                        }
                    }
                    else
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Пользователь подтвержден", Toast.LENGTH_SHORT).show();
                                ((Button) findViewById(R.id.GoToSchedule)).setEnabled(true);
                                ((Button) findViewById(R.id.GoToTasks)).setEnabled(true);
                            }
                        });
                    }
                } catch (final Exception e){
                    if (e.getCause().getMessage() != null && e.getCause().getMessage().equals("{'code': 401}")) {
                        authenticate(true);
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
                        checkUser();
                    }
                    else {
                        Dialog.createAndShowDialogFromTask(mThis, e.getMessage(), "Ошибка");
                    }
                }
                return null;
            }
        };

        AsyncTaskRuner.runAsyncTask(task);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mThis = this;

        setContentView(R.layout.activity_to_do);

        mUser = new UserItem();
        mProgressBar = (ProgressBar) findViewById(R.id.loadingProgressBar);

        mProgressBar.setVisibility(ProgressBar.GONE);
        ((Button) findViewById(R.id.GoToSchedule)).setEnabled(false);
        ((Button) findViewById(R.id.GoToTasks)).setEnabled(false);

        try {
            mClient = new MobileServiceClient(
                    "https://student-activity-manager.azurewebsites.net",
                    this).withFilter(new ProgressFilter(this, mProgressBar));

            authenticate(false);

        } catch (MalformedURLException e) {
            Dialog.createAndShowDialog(this, "There was an error creating the Mobile Service. Verify the URL", "Error");
        } catch (Exception e){
            Dialog.createAndShowDialog(this, e.getMessage(), "Ошибка");
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
                            Toast.makeText(getApplicationContext(), "Вы зарегестрированы", Toast.LENGTH_SHORT).show();

                            cacheUserInfo();
                            ((Button) findViewById(R.id.GoToSchedule)).setEnabled(true);
                            ((Button) findViewById(R.id.GoToTasks)).setEnabled(true);
                        }
                    });

                } catch (final Exception e){
                    Dialog.createAndShowDialogFromTask(mThis, e.getMessage(), "Ошибка");
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

        mUser.setmFacultyId(prefs.getString(USERFACULTY, "undefined"));
        if (mUser.getmFacultyId() == "undefined")
            return false;

        return true;
    }

    private void cacheUserInfo()
    {
        SharedPreferences prefs = getSharedPreferences(SHAREDPREFFILE, Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        //editor.putString(USEREDINST, mUser.getmEdInstId());
        editor.putString(USERFACULTY, mUser.getmFacultyId());
        editor.commit();
    }

    public void logIn(View view) {
        authenticate(true);
    }

    public void goToTasks(View view) {
        Intent intent = new Intent(this, TasksActivity.class);
        startActivity(intent);
    }
}