/**
 * Created by Alexander Podshiblov on 20.03.2016.
 * Класс со статическим методом запуска асинхронной задачи
 */

package com.example.student_activity_manager;

import android.os.AsyncTask;
import android.os.Build;

public class AsyncTaskRuner {
    public static AsyncTask<Void, Void, Void> runAsyncTask(AsyncTask<Void, Void, Void> task) {
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            return task.execute();
        }
        */
        return task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[])null);
    }
}
