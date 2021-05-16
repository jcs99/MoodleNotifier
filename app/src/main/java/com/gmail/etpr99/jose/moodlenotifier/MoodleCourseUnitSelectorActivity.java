package com.gmail.etpr99.jose.moodlenotifier;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gmail.etpr99.jose.moodlenotifier.application.AppComponent;
import com.gmail.etpr99.jose.moodlenotifier.application.MoodleNotifierApplication;
import com.gmail.etpr99.jose.moodlenotifier.interfaces.cookies.MoodleCookieProvider;
import com.gmail.etpr99.jose.moodlenotifier.interfaces.cookies.OnlineMoodleCookieProvider;
import com.gmail.etpr99.jose.moodlenotifier.interfaces.callbacks.CookiesAvailableCallback;
import com.gmail.etpr99.jose.moodlenotifier.interfaces.scrapers.MoodlePageScraper;
import com.gmail.etpr99.jose.moodlenotifier.models.JavaScriptCommandsQueue;
import com.gmail.etpr99.jose.moodlenotifier.network.webviews.MoodleScraperWebView;
import com.gmail.etpr99.jose.moodlenotifier.abstracts.ChainValueCallback;
import com.gmail.etpr99.jose.moodlenotifier.persistence.AppDatabase;
import com.gmail.etpr99.jose.moodlenotifier.persistence.daos.CourseDao;
import com.gmail.etpr99.jose.moodlenotifier.persistence.entities.Course;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

public class MoodleCourseUnitSelectorActivity extends AppCompatActivity implements MoodlePageScraper {
    @Inject AppDatabase appDatabase;
    @Inject OnlineMoodleCookieProvider onlineMoodleCookieProvider;

    private JavaScriptCommandsQueue jsCommandsQueue;
    private MoodleScraperWebView moodleScraperWebView;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.moodle_course_unit_selector_activity_layout);
        createJsCommandsQueue();
        AppComponent appComponent = ((MoodleNotifierApplication) getApplicationContext()).getAppComponent();
        appComponent.inject(this);
        moodleScraperWebView = appComponent.getMoodleScraperWebView();
        onlineMoodleCookieProvider.setCookiesAvailableCallback(new CookiesAvailableCallbackImpl(this));
        onlineMoodleCookieProvider.run();
    }

    @NotNull
    @Override
    public MoodleCookieProvider getMoodleCookieProvider() {
        return onlineMoodleCookieProvider;
    }

    @Override
    @NotNull
    public JavaScriptCommandsQueue getJsCommandsQueue() {
        return jsCommandsQueue;
    }

    /**
     * TODO: Create a more rigid structure that supports a load URL - execute a JavaScript command lifecycle.
     * As it stands, the current structure could have an element removed from one of the Lists and the calls would proceed
     * as if nothing happened. That would cause a internal crash in the WebView.
     * FIXME asap
     */
    @Override
    public void createJsCommandsQueue() {
        JavaScriptCommandsQueue.JavaScriptCommandsQueueBuilder jsCommandsQueueBuilder = new JavaScriptCommandsQueue.JavaScriptCommandsQueueBuilder();
        jsCommandsQueueBuilder.addJsValueCallback(new StudentIdProcessor(this, true))
            .addJsValueCallback(new CourseUnitListProcessor(this, true))
            .addLinkToExecute(getString(R.string.moodle_esgts_website_link))
            .addLinkToExecute(getString(R.string.moodle_esgts_website_user_profile_link))
            .addJsCommandToExecute("document.querySelectorAll('#action-menu-0-menu a')[1].href.split('id=')[1]")
            .addJsCommandToExecute("var node;\n" +
                "\n" +
                "for (const dt of document.querySelectorAll('dt')) {\n" +
                "  if (dt.textContent.includes('Perfis da disciplina')) {\n" +
                "    node = dt.parentNode;\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "JSON.stringify(Array.prototype.slice.call(node.lastChild.getElementsByTagName(\"li\")).map(function (li)\n" +
                "{\n" +
                "  var courseKeyStringPos = li.childNodes[0].href.indexOf(\"course\");\n" +
                "  var equalCharStringPos = li.childNodes[0].href.substring(courseKeyStringPos).indexOf(\"=\") + 1;\n" +
                "  var ampersandStringPos = li.childNodes[0].href.substring(courseKeyStringPos).indexOf(\"&\");\n" +
                "\n" +
                "  var obj = {\n" +
                "     \"courseId\": li.childNodes[0].href.substring(courseKeyStringPos + equalCharStringPos, courseKeyStringPos + ampersandStringPos),\n" +
                "     \"courseName\": li.innerText\n" +
                "  }\n" +
                "\n" +
                "  return obj;\n" +
                "}));"
            );

        jsCommandsQueue = jsCommandsQueueBuilder.build();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.moodle_course_unit_selector_activity_menu, menu);
        this.menu = menu;
        return true;
    }

    public void onCourseUnitsChosen(LinkedHashMap<Integer, String> chosenCourses) {
        CourseDao courseDao = appDatabase.courseDao();
        List<Course> coursesToInsert = new ArrayList<>();

        for (Map.Entry<Integer, String> chosenCourse : chosenCourses.entrySet()) {
            if (courseDao.contains(chosenCourse.getKey()) == 0) {
                coursesToInsert.add(new Course(chosenCourse.getKey(), chosenCourse.getValue(), true));
            }
        }

        courseDao.insertAll(coursesToInsert.toArray(new Course[0]));
        Toast.makeText(this, getString(R.string.courses_chosen_message), Toast.LENGTH_LONG).show();

        if (getIntent().getSerializableExtra("calling_activity") == MainActivity.class) {
            startService(new Intent(this, MainApplicationService.class));
        } else if (getIntent().getSerializableExtra("calling_activity") == MonitoringActivity.class) {
            Intent intent = new Intent(this, MonitoringActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.putExtra("added_courses", chosenCourses);
            intent.putExtra("calling_activity", MoodleCourseUnitSelectorActivity.class);
            startActivity(intent);
        }

        finish();
    }

    private void setCookiesAndStartPolling() {
        CookieManager.getInstance().setCookie(getString(R.string.moodle_esgts_website_link),
            String.format(getString(R.string.moodle_esgts_website_session_cookie_name), onlineMoodleCookieProvider.getMoodleSessionCookie()));

        moodleScraperWebView.startPolling(jsCommandsQueue);
    }

    private void initRecyclerView(List<Map.Entry<Integer, String>> courseList) {
        RecyclerView recyclerView = findViewById(R.id.course_unit_selector_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new CourseUnitListAdapter(this, menu, courseList));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    private static class CookiesAvailableCallbackImpl implements CookiesAvailableCallback {
        private final WeakReference<MoodleCourseUnitSelectorActivity> moodleCourseUnitSelectorActivityWeakReference;

        CookiesAvailableCallbackImpl(MoodleCourseUnitSelectorActivity moodleCourseUnitSelectorActivityWeakReference) {
            this.moodleCourseUnitSelectorActivityWeakReference = new WeakReference<>(moodleCourseUnitSelectorActivityWeakReference);
        }

        @Override
        public void onCookiesAvailable() {
            moodleCourseUnitSelectorActivityWeakReference.get().setCookiesAndStartPolling();
        }
    }

    private static class StudentIdProcessor extends ChainValueCallback<String> {
        StudentIdProcessor(Context context, boolean isContextWeakReference) {
            super(context, isContextWeakReference);
        }

        @Override
        public void doOnReceiveValue(String resultToProcess) {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("id", Integer.valueOf(resultToProcess.replace("\"", "")));
            params.put("showallcourses", 1);

            setArg(params);
        }
    }

    private static class CourseUnitListProcessor extends ChainValueCallback<String> {
        CourseUnitListProcessor(Context context, boolean isContextWeakReference) {
            super(context, isContextWeakReference);
        }

        @Override
        public void doOnReceiveValue(String resultToProcess) {
            List<Map.Entry<Integer, String>> courseList = new ArrayList<>();
            final MoodleCourseUnitSelectorActivity activity = (MoodleCourseUnitSelectorActivity) getContext();

            resultToProcess = resultToProcess.replace("\\\"","'");

            try {
                JSONArray jsonArray = new JSONArray(resultToProcess.substring(1, resultToProcess.length() - 1));
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    if (activity.appDatabase.courseDao().contains(jsonObject.getInt("courseId")) == 0) {
                        courseList.add(new AbstractMap.SimpleEntry<>(
                            jsonObject.getInt("courseId"), jsonObject.getString("courseName")));
                    }
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

            ((MoodleCourseUnitSelectorActivity) getContext()).findViewById(R.id.loading_panel).setVisibility(View.GONE);
            ((MoodleCourseUnitSelectorActivity) getContext()).initRecyclerView(courseList);
        }
    }

    private static class CourseUnitListAdapter extends RecyclerView.Adapter<CourseUnitListAdapter.CourseUnitViewHolder>
    implements CompoundButton.OnCheckedChangeListener, MenuItem.OnMenuItemClickListener {
        private MoodleCourseUnitSelectorActivity moodleCourseUnitSelectorActivity;
        private Menu menu;
        private List<Map.Entry<Integer, String>> courseList;

        private List<CheckBox> selectedCheckboxes = new ArrayList<>();

        CourseUnitListAdapter(MoodleCourseUnitSelectorActivity moodleCourseUnitSelectorActivity,
                              Menu menu, List<Map.Entry<Integer, String>> courseList) {
            this.moodleCourseUnitSelectorActivity = moodleCourseUnitSelectorActivity;
            this.menu = menu;
            this.courseList = courseList;
            menu.getItem(0).setOnMenuItemClickListener(this);
        }

        private static class CourseUnitViewHolder extends RecyclerView.ViewHolder {
            CheckBox checkBox;
            TextView textView;

            CourseUnitViewHolder(@NonNull View view) {
                super(view);
                checkBox = view.findViewById(R.id.course_unit_selector_check_box);
                textView = view.findViewById(R.id.course_unit_selector_text_view);
            }
        }

        @NonNull
        @Override
        public CourseUnitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new CourseUnitViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.moodle_course_unit_selector_recyler_view_layout, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull CourseUnitViewHolder holder, int position) {
            holder.textView.setText(courseList.get(position).getValue());
            holder.checkBox.setTag(courseList.get(position));
            holder.checkBox.setOnCheckedChangeListener(this);
        }

        @Override
        public int getItemCount() {
            return courseList.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        @SuppressWarnings("RedundantCast")
        public void onCheckedChanged(CompoundButton checkBox, boolean isChecked) {
            if (isChecked) {
                menu.getItem(0).setEnabled(true);
                menu.getItem(0).getIcon().setAlpha(255);
                selectedCheckboxes.add((CheckBox) checkBox);
            } else {
                selectedCheckboxes.remove((CheckBox) checkBox);
            }
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            LinkedHashMap<Integer, String> selectedCourses = new LinkedHashMap<>();

            for (CheckBox checkBox : selectedCheckboxes) {
                @SuppressWarnings("unchecked") Map.Entry<Integer, String> checkBoxTags = (Map.Entry<Integer, String>) checkBox.getTag();
                selectedCourses.put(checkBoxTags.getKey(), checkBoxTags.getValue());
            }

            menu.getItem(0).setEnabled(false);
            moodleCourseUnitSelectorActivity.onCourseUnitsChosen(selectedCourses);
            menu = null;
            moodleCourseUnitSelectorActivity = null;
            return true;
        }
    }
}