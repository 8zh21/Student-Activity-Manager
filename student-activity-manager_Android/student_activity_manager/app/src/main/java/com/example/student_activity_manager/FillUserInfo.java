package com.example.student_activity_manager;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;

import com.microsoft.windowsazure.mobileservices.MobileServiceClient;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Alexander on 15.03.2016.
 * Модуль сбора регистрационных данных,
 * демострирует эран регистрации,
 * загружает данные учебных заведений и факультетов,
 * собирает данные, сохраняет и отправляет на сервер
 *
 */

public class FillUserInfo extends Activity {

    public static final String ED_INST_ITEM_ID = "EdInstItem";
    public static final String FACULTY_ITEM_ID = "FacultyItem";
    private final FillUserInfo mThis = this;
    private AutoCompleteTextView eiTV;
    private AutoCompleteTextView faqTV;

    private EdInstItem edInstItem;
    private FacultyItem facultyItem;

    private boolean isEdInst_New = false;
    private boolean isFaculty_New = false;
    private MobileServiceClient mClient;

    private void createAndShowDialog(final String message, final String title) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message);
        builder.setTitle(title);
        builder.create().show();
    }

    private void createAndShowDialogFromTask(final String message, final String title) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                createAndShowDialog(message, title);
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fill_user_info);
        mClient = ToDoActivity.mClient.withFilter(new ProgressFilter(this,
                (ProgressBar) findViewById(R.id.loadingProgressBar2)));

        prepareEITextView();
    }

    private void prepareEITextView() {
        eiTV = (AutoCompleteTextView) findViewById(R.id.EdInstTV);
        eiTV.setThreshold(0);

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try
                {
                    final List<EdInstItem> edInstItems = mClient.getTable(getString(R.string.EdInst_table_name), EdInstItem.class).execute().get();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            eiTV.setAdapter(new ArrayAdapter(mThis, android.R.layout.simple_dropdown_item_1line, edInstItems));
                            eiTV.setEnabled(true);

                            eiTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
                                    EdInstItem selection = (EdInstItem) parent.getItemAtPosition(position);
                                    edInstItem = selection;
                                }
                            });
                        }
                    });

                } catch (Exception e) {
                    createAndShowDialogFromTask(e.getMessage(), "Ошибка");
                }
                return null;
            }
        };
        AsyncTaskRuner.runAsyncTask(task);
    }

    public void submitEdInst(View view)
    {
        eiTV.setEnabled(false);
        findViewById(R.id.submitEdInst).setEnabled(false);

        if (edInstItem == null || ! eiTV.getText().toString().equals(edInstItem.getmName()))
        {
            edInstItem = new EdInstItem(eiTV.getText().toString());
            isEdInst_New = true;
        }
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    if (isEdInst_New) {
                        //to get id from server
                        edInstItem = mClient.getTable(getString(R.string.EdInst_table_name),
                                EdInstItem.class).insert(edInstItem).get();
                    }

                } catch (final Exception e) {
                    createAndShowDialogFromTask(e.getMessage(), "Ошибка");
                }

            return null;
            }
        };
        AsyncTaskRuner.runAsyncTask(task);

        prepareFacultyTextView();
    }

    private void prepareFacultyTextView() {
        faqTV = (AutoCompleteTextView) findViewById(R.id.FacultyTV);
        faqTV.setThreshold(0);

        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try
                {
                    final List<FacultyItem> facultyItems;
                    if (!isEdInst_New)
                    {
                        facultyItems = mClient.getTable(getString(R.string.Faculties_table_name),
                                FacultyItem.class).where().field("EdInstId").eq(edInstItem.getmId()).execute().get();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                faqTV.setAdapter(new ArrayAdapter(mThis, android.R.layout.simple_dropdown_item_1line, facultyItems));
                            }
                        });
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run()
                        {
                            faqTV.setVisibility(View.VISIBLE);
                            findViewById(R.id.submitFaculty).setVisibility(View.VISIBLE);

                            faqTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                public void onItemClick(AdapterView<?> parent, View view, int position, long rowId) {
                                    FacultyItem selection = (FacultyItem) parent.getItemAtPosition(position);
                                    facultyItem = selection;
                                }
                            });
                        }
                    });

                } catch (Exception e) {
                    createAndShowDialogFromTask(e.getMessage(), "Ошибка");
                }
                return null;
            }
        };
        AsyncTaskRuner.runAsyncTask(task);
    }

    public void submitFaculty(View view)
    {
        faqTV.setEnabled(false);
        findViewById(R.id.submitFaculty).setEnabled(false);

        if (facultyItem == null || ! faqTV.getText().toString().equals(facultyItem.getmName()))
        {
            facultyItem = new FacultyItem(faqTV.getText().toString(), edInstItem.getmId());
            isFaculty_New = true;
        }
        AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>(){
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    if (isFaculty_New) {
                        facultyItem = mClient.getTable(getString(R.string.Faculties_table_name),
                                FacultyItem.class).insert(facultyItem).get();
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent answerIntent = new Intent();
                            answerIntent.putExtra(ED_INST_ITEM_ID, edInstItem.getmId());
                            answerIntent.putExtra(FACULTY_ITEM_ID, facultyItem.getmId());
                            setResult(RESULT_OK, answerIntent);
                            finish();
                        }
                    });

                } catch (final Exception e) {
                    createAndShowDialogFromTask(e.getMessage(), "Error");
                }

                return null;
            }
        };
        AsyncTaskRuner.runAsyncTask(task);
    }

    @Override
    public void onBackPressed() {
        // nothing to do
    }
}
