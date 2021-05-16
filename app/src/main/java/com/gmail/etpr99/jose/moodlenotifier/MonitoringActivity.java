package com.gmail.etpr99.jose.moodlenotifier;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.gmail.etpr99.jose.moodlenotifier.application.MoodleNotifierApplication;
import com.gmail.etpr99.jose.moodlenotifier.persistence.AppDatabase;
import com.gmail.etpr99.jose.moodlenotifier.persistence.entities.Course;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class MonitoringActivity extends AppCompatActivity {
    @Inject AppDatabase appDatabase;

    private ListView listView;

    // the activity field is unregistered on the onDestroy() method, so we can safely ignore this warning
    @SuppressLint("StaticFieldLeak")
    public static MonitoringCountDownTimer countDownTimer
            = new MonitoringCountDownTimer(TimeUnit.MINUTES.toMillis(2L), 1000);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.monitoring_activity_layout);
        ((MoodleNotifierApplication) getApplicationContext()).getAppComponent().inject(this);

        listView = findViewById(R.id.courses_being_monitored_list_view);
        listView.setAdapter(new MonitoringActivityListViewAdapter(this, appDatabase));

        countDownTimer.setParentActivity(this);
    }

    @Override
    protected void onResume() {
        if (getIntent() != null) {
            Intent intent = getIntent();
            if (intent.getSerializableExtra("calling_activity") == ManageCourseActivity.class) {
                if (intent.getBooleanExtra("changed_course", false)) {
                    int referencingChildViewId = getIntent().getIntExtra("referencing_child_view_id", 0);
                    LinearLayout childLinearLayout = (LinearLayout) listView.getChildAt(referencingChildViewId);
                    ImageView childImageView = childLinearLayout.findViewById(R.id.monitoring_activity_list_view_image_view);
                    int courseId = ((MonitoringActivityListViewAdapter.ViewHolder) childLinearLayout.getTag()).courseId;

                    if (appDatabase.courseDao().shouldPageBeChecked(courseId)) {
                        childImageView.setImageResource(R.drawable.ic_check_24px);
                    } else {
                        childImageView.setImageResource(R.drawable.ic_close_24px);
                    }
                } else if (intent.getSerializableExtra("deleted_course") != null) {
                    Course removedCourse = (Course) getIntent().getSerializableExtra("deleted_course");
                    MonitoringActivityListViewAdapter adapter = (MonitoringActivityListViewAdapter) listView.getAdapter();
                    adapter.courses.remove(removedCourse);
                    adapter.notifyDataSetChanged();
                }
            } else if (intent.getSerializableExtra("calling_activity") == MoodleCourseUnitSelectorActivity.class) {
                if (intent.getSerializableExtra("added_courses") != null) {
                    @SuppressWarnings("unchecked")
                    HashMap<Integer, String> courses =
                        (HashMap<Integer, String>) intent.getSerializableExtra("added_courses");
                    MonitoringActivityListViewAdapter adapter = (MonitoringActivityListViewAdapter) listView.getAdapter();

                    for (Map.Entry<Integer, String> course : courses.entrySet()) {
                        adapter.courses.add(new Course(course.getKey(), course.getValue(), true));
                    }

                    Collections.sort(adapter.courses, (o1, o2) -> o1.getName().compareTo(o2.getName()));
                    adapter.notifyDataSetChanged();
                }
            }
        }

        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        countDownTimer.unregisterActivity();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = new MenuInflater(this);
        menuInflater.inflate(R.menu.monitoring_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent preferencesIntent = new Intent(this, MoodleNotifierPreferencesActivity.class);
                startActivity(preferencesIntent);
                break;
            case R.id.add_course:
                Intent courseUnitSelectorIntent = new Intent(this, MoodleCourseUnitSelectorActivity.class);
                courseUnitSelectorIntent.putExtra("calling_activity", MonitoringActivity.class);
                startActivity(courseUnitSelectorIntent);
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    private static class MonitoringActivityListViewAdapter extends BaseAdapter {
        private Context context;
        private AppDatabase appDatabase;
        private List<Course> courses;

        MonitoringActivityListViewAdapter(Context context, AppDatabase appDatabase) {
            this.context = context;
            this.appDatabase = appDatabase;
            this.courses = appDatabase.courseDao().getAll();
        }

        @Override
        public int getCount() {
            return courses.size();
        }

        @Override
        public Course getItem(int position) {
            return courses.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.monitoring_activity_list_view_layout, parent,
                    false);
                viewHolder = new ViewHolder(getItem(position).getId(), convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            Course currentCourse = getItem(position);
            viewHolder.itemName.setText(currentCourse.getName());

            if (currentCourse.isShouldBeChecked()) {
                viewHolder.itemImage.setImageResource(R.drawable.ic_check_24px);
            } else {
                viewHolder.itemImage.setImageResource(R.drawable.ic_close_24px);
            }

            convertView.setOnClickListener((view) -> {
                Intent intent = new Intent(context, ManageCourseActivity.class);
                intent.putExtra("course", appDatabase.courseDao().findById(getItem(position).getId()));
                intent.putExtra("child_view_id", position);
                context.startActivity(intent);
            });

            return convertView;
        }

        private class ViewHolder {
            int courseId;
            TextView itemName;
            ImageView itemImage;

            ViewHolder(int courseId, View view) {
                this.courseId = courseId;
                itemName = view.findViewById(R.id.monitoring_activity_list_view_text_view);
                itemImage = view.findViewById(R.id.monitoring_activity_list_view_image_view);
            }
        }
    }

    public static class MonitoringCountDownTimer extends CountDownTimer {
        private Activity parentActivity;

        MonitoringCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        void setParentActivity(Activity parentActivity) {
            this.parentActivity = parentActivity;
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (parentActivity != null) {
               TextView textView = parentActivity.findViewById(R.id.remaining_time_text_view);
               if (TimeUnit.MILLISECONDS.toHours(millisUntilFinished) >= 1) {
                   textView.setText(String.format(Locale.getDefault(), "%d horas, %d minutos e %d segundos",
                   TimeUnit.MILLISECONDS.toHours(millisUntilFinished),
                   TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                   TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))
                   ));
               } else if (TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) >= 1) {
                   textView.setText(String.format(Locale.getDefault(), "%d minutos e %d segundos",
                   TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
                   TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))
                   ));
               } else {
                   textView.setText(String.format(Locale.getDefault(), "%d segundos",
                    TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
               }
            }
        }

        @Override
        public void onFinish() {
            if (parentActivity != null) {
                TextView textView = parentActivity.findViewById(R.id.remaining_time_text_view);
                textView.setText("A correr a verificação!");
            }
        }

        public void unregisterActivity() {
            parentActivity = null;
        }
    }
}
