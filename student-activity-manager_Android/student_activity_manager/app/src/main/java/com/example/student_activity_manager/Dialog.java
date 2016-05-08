package com.example.student_activity_manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

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

    public static void createAndShowYNDialog(final Activity activity, final String message, final String title,
                                             DialogInterface.OnClickListener yesListener,
                                             DialogInterface.OnClickListener noListener)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.setPositiveButton("Yes", yesListener);
        builder.setNegativeButton("No", noListener);
        builder.setCancelable(false);
        builder.show();
    }

    public static void createAndShowMenuDialog(final Activity activity, final String title,
                                                      String[] menuItems, DialogInterface.OnClickListener onClickListener)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setTitle(title);
        builder.setItems(menuItems, onClickListener);
        builder.setCancelable(true);
        builder.show();
    }
}
