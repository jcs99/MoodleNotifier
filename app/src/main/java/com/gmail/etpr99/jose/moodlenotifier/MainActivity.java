package com.gmail.etpr99.jose.moodlenotifier;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.content.ContextCompat;

import com.gmail.etpr99.jose.moodlenotifier.application.MoodleNotifierApplication;
import com.gmail.etpr99.jose.moodlenotifier.persistence.AppDatabase;

import javax.inject.Inject;

public class MainActivity extends Activity {
    public static final String MAIN_APP_TAG = "MoodleNotifier";
    public static final String MAIN_CHANNEL_ID = "MoodleNotifierChannel";
    public static final int OUTGOING_NOTIFICATION_ID = 1;

    @Inject AppDatabase appDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MoodleNotifierApplication) getApplicationContext()).getAppComponent().inject(this);
        checkIfDatabaseIsEmpty();
    }

    private void checkIfDatabaseIsEmpty() {
        if (appDatabase.courseDao().getAll().size() == 0) {
            new AlertDialog.Builder(this)
                .setTitle(getString(R.string.no_courses_found_dialog_title))
                .setMessage(getString(R.string.no_courses_found_dialog_text))
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    Intent intent = new Intent(this, MoodleCourseUnitSelectorActivity.class);
                    intent.putExtra("calling_activity", MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(android.R.string.no, ((dialog, which) -> finish()))
                .setOnDismissListener(dialog -> finish())
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
        } else {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(this, new Intent(this, MainApplicationService.class));
            } else {
                startService(new Intent(this, MainApplicationService.class));
            }

            finish();
        }
    }
}
