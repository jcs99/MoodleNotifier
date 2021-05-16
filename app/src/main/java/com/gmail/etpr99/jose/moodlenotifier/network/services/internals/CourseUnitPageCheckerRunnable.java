package com.gmail.etpr99.jose.moodlenotifier.network.services.internals;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.gmail.etpr99.jose.moodlenotifier.CheckPageActivity;
import com.gmail.etpr99.jose.moodlenotifier.application.AppComponent;
import com.gmail.etpr99.jose.moodlenotifier.application.MoodleNotifierApplication;
import com.gmail.etpr99.jose.moodlenotifier.R;
import com.gmail.etpr99.jose.moodlenotifier.models.JavaScriptCommandsQueue;
import com.gmail.etpr99.jose.moodlenotifier.network.webviews.MoodleScraperWebView;
import com.gmail.etpr99.jose.moodlenotifier.persistence.entities.CoursePageHtml;
import com.gmail.etpr99.jose.moodlenotifier.abstracts.ChainValueCallback;
import com.gmail.etpr99.jose.moodlenotifier.persistence.AppDatabase;
import com.gmail.etpr99.jose.moodlenotifier.persistence.daos.CourseDao;
import com.gmail.etpr99.jose.moodlenotifier.persistence.daos.CoursePageHtmlDao;

import java.util.AbstractMap;

import javax.inject.Inject;

import static com.gmail.etpr99.jose.moodlenotifier.MainActivity.MAIN_APP_TAG;
import static com.gmail.etpr99.jose.moodlenotifier.MainActivity.MAIN_CHANNEL_ID;
import static com.gmail.etpr99.jose.moodlenotifier.network.services.internals.PendingNotificationStorer.NOTIFICATIONS_COUNT;

public class CourseUnitPageCheckerRunnable implements Runnable {
    @Inject AppDatabase appDatabase;

    private int courseId;
    private JavaScriptCommandsQueue jsCommandsQueue;
    private MoodleScraperWebView moodleScraperWebView;
    private PendingNotificationStorer pendingNotificationStorer;

    public CourseUnitPageCheckerRunnable(Context context) {
        AppComponent appComponent = ((MoodleNotifierApplication) context).getAppComponent();
        appComponent.inject(this);
        moodleScraperWebView = appComponent.getMoodleScraperWebView();
        pendingNotificationStorer = appComponent.getPendingNotificationStorer();
    }

    public int getCourseId() {
        return courseId;
    }

    public void setCourseId(int courseId) {
        this.courseId = courseId;
    }

    public JavaScriptCommandsQueue getJsCommandsQueue() {
        return jsCommandsQueue;
    }

    public void setJsCommandsQueue(JavaScriptCommandsQueue jsCommandsQueue) {
        this.jsCommandsQueue = jsCommandsQueue;
    }

    @Override
    public void run() {
        moodleScraperWebView.startPolling(jsCommandsQueue);
    }

    public class CourseUnitPageCheckerProcessor extends ChainValueCallback<String> {
        public CourseUnitPageCheckerProcessor(Context context, boolean isContextWeakReference) {
            super(context, isContextWeakReference);
        }

        @Override
        public void doOnReceiveValue(String processedHtml) {
            CourseDao courseDao = appDatabase.courseDao();
            CoursePageHtmlDao coursePageHtmlDao = appDatabase.coursePageHtmlDao();

            if (coursePageHtmlDao.contains(courseId) == 0) {
                coursePageHtmlDao.insert(new CoursePageHtml(courseId, processedHtml));
                Log.i(MAIN_APP_TAG, "Persisting a copy of the Moodle course page ID: " + courseId + " HTML to the database!");
            } else {
                if (coursePageHtmlDao.findHtmlByCourseId(courseId).equals(processedHtml)) {
                    Log.i(MAIN_APP_TAG, "Checked page ID " + courseId + ", nothing has changed!");
                } else {
                    Log.e(MAIN_APP_TAG, "MOODLE PAGE ID: " + courseId + " HAS CHANGED, CHECK ASAP!!!!!!!!!!!!!!!!!!!!");
                    buildCourseUnitPageChangedNotification(courseDao.findById(courseId).getName());
                }
            }
        }

        private void buildCourseUnitPageChangedNotification(String pageName) {
            Intent intent = new Intent(getContext(), CheckPageActivity.class);
            intent.putExtra("course_page_id", courseId);
            PendingIntent pendingIntent = PendingIntent.getActivity(getContext(),(int) System.currentTimeMillis() & 0xfffffff, intent, 0);

            NotificationCompat.Builder moodlePageChangedNotification = new NotificationCompat.Builder(getContext(), MAIN_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle(getContext().getString(R.string.checker_service_page_has_changed_notification_title))
                .setContentText(String.format(getContext().getString(R.string.checker_service_page_has_changed_notification_text),
                    pageName))
                .setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(String.format(getContext().getString(R.string.checker_service_page_has_changed_notification_text),
                        pageName)))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

            pendingNotificationStorer.addPendingNotification(new AbstractMap.SimpleEntry<>(NOTIFICATIONS_COUNT, moodlePageChangedNotification.build()));
            NOTIFICATIONS_COUNT++;
        }
    }
}
