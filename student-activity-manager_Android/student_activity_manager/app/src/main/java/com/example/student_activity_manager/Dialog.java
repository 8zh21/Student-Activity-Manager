package com.example.student_activity_manager;

import android.app.Activity;
import android.app.AlertDialog;

/**
 * Created by Alexander on 20.03.2016.
 */
public class Dialog {
    public static void createAndShowDialog(final Activity activity, final String message, final String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }

    public static void createAndShowDialogFromTask(final Activity activity, final String message, final String title) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createAndShowDialog(activity, message, title);
            }
        });
    }
}
