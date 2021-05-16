package com.gmail.etpr99.jose.moodlenotifier.network.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.webkit.CookieManager;

import com.gmail.etpr99.jose.moodlenotifier.MonitoringActivity;
import com.gmail.etpr99.jose.moodlenotifier.application.AppComponent;
import com.gmail.etpr99.jose.moodlenotifier.application.MoodleNotifierApplication;
import com.gmail.etpr99.jose.moodlenotifier.R;
import com.gmail.etpr99.jose.moodlenotifier.models.JavaScriptCommandsQueue.JavaScriptCommandsQueueBuilder;
import com.gmail.etpr99.jose.moodlenotifier.abstracts.ServiceBinder;
import com.gmail.etpr99.jose.moodlenotifier.interfaces.cookies.OnlineMoodleCookieProvider;
import com.gmail.etpr99.jose.moodlenotifier.interfaces.cookies.MoodleCookieProvider;
import com.gmail.etpr99.jose.moodlenotifier.interfaces.scrapers.PersistentMoodlePageScraper;
import com.gmail.etpr99.jose.moodlenotifier.network.services.internals.CourseUnitPageCheckerRunnable;
import com.gmail.etpr99.jose.moodlenotifier.network.services.internals.CourseUnitPageCheckerRunnablesQueueManager;
import com.gmail.etpr99.jose.moodlenotifier.network.services.internals.PendingNotificationStorer;
import com.gmail.etpr99.jose.moodlenotifier.persistence.AppDatabase;
import com.gmail.etpr99.jose.moodlenotifier.persistence.entities.Course;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import static com.gmail.etpr99.jose.moodlenotifier.MainActivity.MAIN_APP_TAG;

public class MoodleCourseUnitPageCheckerService extends Service implements PersistentMoodlePageScraper {
    @Inject AppDatabase appDatabase;
    @Inject OnlineMoodleCookieProvider onlineMoodleCookieProvider;

    private PendingNotificationStorer pendingNotificationStorer;
    private CourseUnitPageCheckerRunnablesQueueManager courseUnitPageCheckerRunnablesQueueManager;
    private NotificationManager notificationManager;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    @NotNull
    @Override
    public MoodleCookieProvider getMoodleCookieProvider() {
        return onlineMoodleCookieProvider;
    }

    @NotNull
    @Override
    public CourseUnitPageCheckerRunnablesQueueManager getCourseUnitPageCheckerRunnablesQueueManager() {
        return courseUnitPageCheckerRunnablesQueueManager;
    }

    @NotNull
    @Override
    public PendingNotificationStorer getPendingNotificationStorer() {
        return pendingNotificationStorer;
    }

    @Override
    public ScheduledExecutorService getScheduledExecutorService() {
        return scheduledExecutorService;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return getBinder();
    }

    @Override
    public void onCreate() {
        Log.i(MAIN_APP_TAG,"Starting the service " + this.getClass().getSimpleName());
        AppComponent appComponent = ((MoodleNotifierApplication) getApplicationContext()).getAppComponent();
        appComponent.inject(this);
        onlineMoodleCookieProvider.setCookiesAvailableCallback(this::runPageScrapers);
        pendingNotificationStorer = appComponent.getPendingNotificationStorer();
        pendingNotificationStorer.setService(this);
        courseUnitPageCheckerRunnablesQueueManager = appComponent.getPageScraperRunnablesQueueManager();
        courseUnitPageCheckerRunnablesQueueManager.setPageScraperRunnablesQueueIsEmptyListener(pendingNotificationStorer);
        appComponent.getMoodleScraperWebView().setJsCommandQueuePollingDoneCallback(courseUnitPageCheckerRunnablesQueueManager);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public void onDestroy() {
        Log.w(MAIN_APP_TAG, "Unregistering the service " + this.getClass().getSimpleName());
    }

    @Override
    public ServiceBinder getBinder() {
        return new ServiceBinder() {
            @Override
            public PersistentMoodlePageScraper getService() {
                return MoodleCourseUnitPageCheckerService.this;
            }
        };
    }

    @Override
    public void run() {
        for (Course course : appDatabase.courseDao().getAllChecked()) {
            CourseUnitPageCheckerRunnable courseUnitPageCheckerRunnable = new CourseUnitPageCheckerRunnable(getApplicationContext());
            JavaScriptCommandsQueueBuilder jsCommandsQueueBuilder = new JavaScriptCommandsQueueBuilder();
            jsCommandsQueueBuilder
                .addLinkToExecute(String.format(getString(R.string.moodle_esgts_website_course_page_link), course.getId()))
                .addJsCommandToExecute("document.getElementById('region-main').innerHTML")
                .addJsValueCallback(courseUnitPageCheckerRunnable
                        .new CourseUnitPageCheckerProcessor(this, true));
            courseUnitPageCheckerRunnable.setCourseId(course.getId());
            courseUnitPageCheckerRunnable.setJsCommandsQueue(jsCommandsQueueBuilder.build());
            courseUnitPageCheckerRunnablesQueueManager.addRunnable(courseUnitPageCheckerRunnable);
        }

        scheduledExecutorService.submit(onlineMoodleCookieProvider);
    }

    public void showPendingNotificationsAndRerun(List<Map.Entry<Integer, Notification>> pendingNotifications) {
        for (Map.Entry<Integer, Notification> pendingNotification : pendingNotifications) {
            notificationManager.notify(pendingNotification.getKey(), pendingNotification.getValue());
        }

        scheduledExecutorService.schedule(this, 3L, TimeUnit.MINUTES);
        MonitoringActivity.countDownTimer.start();
    }

    private void runPageScrapers() {
        CookieManager.getInstance().setCookie(getString(R.string.moodle_esgts_website_link),
                String.format(getString(R.string.moodle_esgts_website_session_cookie_name), onlineMoodleCookieProvider.getMoodleSessionCookie()));

        courseUnitPageCheckerRunnablesQueueManager.poll();
    }
}
