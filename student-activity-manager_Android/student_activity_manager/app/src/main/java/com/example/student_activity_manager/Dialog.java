/**
 * Created by Alexander Podshiblov on 20.03.2016.
 * Ксласс со статическими методами показа различных диалоговых окон
 */

package com.example.student_activity_manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class Dialog {
    // Создает и демонстрирует диалоговое мнформационное окно
    public static void createAndShowDialog(final Activity activity, final String message, final String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }

    // Создает и демонстрирует диалоговое информационное окно из фонового процесса
    public static void createAndShowDialogFromTask(final Activity activity, final String message, final String title) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createAndShowDialog(activity, message, title);
            }
        });
    }

    // Создает и демонстрирует диалоговое окно с выбором да/нет
    public static void createAndShowYNDialog(final Activity activity, final String message, final String title,
                                             DialogInterface.OnClickListener yesListener,
                                             DialogInterface.OnClickListener noListener)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.setPositiveButton("Да", yesListener);
        builder.setNegativeButton("Нет", noListener);
        builder.setCancelable(false);
        builder.show();
    }

    // Создает и демострирует диалоговое окно с меню
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
