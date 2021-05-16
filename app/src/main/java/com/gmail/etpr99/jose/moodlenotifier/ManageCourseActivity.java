package com.gmail.etpr99.jose.moodlenotifier;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.gmail.etpr99.jose.moodlenotifier.application.MoodleNotifierApplication;
import com.gmail.etpr99.jose.moodlenotifier.persistence.AppDatabase;
import com.gmail.etpr99.jose.moodlenotifier.persistence.entities.Course;

import javax.inject.Inject;

public class ManageCourseActivity extends AppCompatActivity {
    @Inject AppDatabase appDatabase;

    private int referencingChildViewId;
    private boolean hasCourseChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manage_course_activity_layout);
        ((MoodleNotifierApplication) getApplicationContext()).getAppComponent().inject(this);

        referencingChildViewId = getIntent().getIntExtra("child_view_id", -1);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, new SettingsFragment(this, appDatabase,
                        (Course) getIntent().getSerializableExtra("course")))
                .commit();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else {
            return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MonitoringActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("calling_activity", ManageCourseActivity.class);

        if (hasCourseChanged) {
            intent.putExtra("changed_course", true);
            intent.putExtra("referencing_child_view_id", referencingChildViewId);
        }

        finishActivity(intent);
    }

    private void finishActivity(Intent intent) {
        startActivity(intent);
        finish();
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private Context context;
        private AppDatabase appDatabase;
        private Course course;
        private SwitchPreferenceCompat manageCoursePreference;
        private Preference removeCoursePreference;

        SettingsFragment(Context context, AppDatabase appDatabase, Course course) {
            this.context = context;
            this.appDatabase = appDatabase;
            this.course = course;
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.manage_course_activity_root_preferences, rootKey);
            PreferenceCategory mainPrefCategory = findPreference("main_pref_category");
            mainPrefCategory.setTitle("Gerir curso " + course.getName());
            manageCoursePreference = getPreferenceScreen().findPreference("manage_course");
            manageCoursePreference.setChecked(course.isShouldBeChecked());
            manageCoursePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                appDatabase.courseDao().setShouldPageBeChecked(course.getId(), (Boolean) newValue);
                ((ManageCourseActivity) context).hasCourseChanged = true;
                return true;
            });
            removeCoursePreference = getPreferenceScreen().findPreference("remove_course");
            removeCoursePreference.setOnPreferenceClickListener((preference) -> {
                new AlertDialog.Builder(context, R.style.AlertDialogTheme)
                    .setTitle("Eliminar curso")
                    .setMessage("Tem a certeza que deseja eliminar este curso?")
                    .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                        appDatabase.courseDao().delete(course);
                        Intent intent = new Intent(context, MonitoringActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.putExtra("calling_activity", ManageCourseActivity.class);
                        intent.putExtra("deleted_course",
                            ((ManageCourseActivity) context).getIntent().getSerializableExtra("course"));
                        ((ManageCourseActivity) context).finishActivity(intent);
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
                return true;
            });
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            context = null;
            manageCoursePreference.setOnPreferenceChangeListener(null);
            removeCoursePreference.setOnPreferenceClickListener(null);
        }
    }
}