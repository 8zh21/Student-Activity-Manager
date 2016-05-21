/**
 * Created by Alexander Podshiblov on 15.03.2016.
 * Демонстрирует прогрессбар в процессе выполнения операции
 */

package com.example.student_activity_manager;

import android.app.Activity;
import android.widget.ProgressBar;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.NextServiceFilterCallback;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilter;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterRequest;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;

public class ProgressFilter implements ServiceFilter {

    private ProgressBar pB;
    private Activity activity;

    public ProgressFilter(Activity activity, ProgressBar pB) {
        this.activity = activity;
        this.pB = pB;
    }

    @Override
    public ListenableFuture<ServiceFilterResponse> handleRequest(ServiceFilterRequest request, NextServiceFilterCallback nextServiceFilterCallback) {

        final SettableFuture<ServiceFilterResponse> resultFuture = SettableFuture.create();


        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (pB != null) pB.setVisibility(ProgressBar.VISIBLE);
            }
        });

        ListenableFuture<ServiceFilterResponse> future = nextServiceFilterCallback.onNext(request);

        Futures.addCallback(future, new FutureCallback<ServiceFilterResponse>() {
            @Override
            public void onFailure(Throwable e) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (pB != null) pB.setVisibility(ProgressBar.GONE);
                    }
                });

                resultFuture.setException(e);
            }

            @Override
            public void onSuccess(ServiceFilterResponse response) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (pB != null) pB.setVisibility(ProgressBar.GONE);
                    }
                });

                resultFuture.set(response);
            }
        });

        return resultFuture;
    }
}

