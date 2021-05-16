package com.gmail.etpr99.jose.moodlenotifier;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;

import android.content.Intent;
import android.os.Bundle;

import com.gmail.etpr99.jose.moodlenotifier.application.MoodleNotifierApplication;
import com.gmail.etpr99.jose.moodlenotifier.network.webviews.MoodleScraperWebView;

import javax.inject.Inject;

public class CheckPageActivity extends AppCompatActivity implements MoodleScraperWebView.OnPageFinishedListener {
    @Inject MoodleScraperWebView moodleScraperWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.check_page_activity_layout);
        ((MoodleNotifierApplication) getApplicationContext()).getAppComponent().inject(this);
        moodleScraperWebView.setOnPageFinishedListener(this);
        moodleScraperWebView.loadUrl(String.format(getString(R.string.moodle_esgts_website_course_page_link),
            getIntent().getIntExtra("course_page_id", 0)));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        moodleScraperWebView.setOnPageFinishedListener(null);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (getIntent().getIntExtra("course_page_id", 0) != 0) {
            moodleScraperWebView.loadUrl(String.format(getString(R.string.moodle_esgts_website_course_page_link),
                getIntent().getIntExtra("course_page_id", 0)));
        }
    }

    @Override
    public void onPageFinished() {
        if (((ConstraintLayout) findViewById(R.id.check_page_activity_layout)).indexOfChild(moodleScraperWebView) == -1) {
            addWebViewInstanceToThisActivity();
        }
    }

    private void addWebViewInstanceToThisActivity() {
        ConstraintLayout layout = findViewById(R.id.check_page_activity_layout);

        layout.addView(moodleScraperWebView, 0);

        ConstraintSet constraintSet = new ConstraintSet();

        constraintSet.connect(moodleScraperWebView.getId(), ConstraintSet.BOTTOM, layout.getId(), ConstraintSet.BOTTOM);
        constraintSet.connect(moodleScraperWebView.getId(), ConstraintSet.END, layout.getId(), ConstraintSet.END);
        constraintSet.connect(moodleScraperWebView.getId(), ConstraintSet.START, layout.getId(), ConstraintSet.START);
        constraintSet.connect(moodleScraperWebView.getId(), ConstraintSet.TOP, layout.getId(), ConstraintSet.TOP);

        constraintSet.applyTo(layout);
    }
}
