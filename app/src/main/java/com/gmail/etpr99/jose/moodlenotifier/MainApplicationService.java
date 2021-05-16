package com.gmail.etpr99.jose.moodlenotifier;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Process;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.gmail.etpr99.jose.moodlenotifier.abstracts.ServiceBinder;
import com.gmail.etpr99.jose.moodlenotifier.application.MoodleNotifierApplication;
import com.gmail.etpr99.jose.moodlenotifier.interfaces.RecoverableExceptionHandler;
import com.gmail.etpr99.jose.moodlenotifier.interfaces.cookies.OnlineMoodleCookieProvider;
import com.gmail.etpr99.jose.moodlenotifier.interfaces.listeners.RebindServiceRequestListener;
import com.gmail.etpr99.jose.moodlenotifier.interfaces.scrapers.PersistentMoodlePageScraper;
import com.gmail.etpr99.jose.moodlenotifier.network.background.LoopUntilInternetAvailable;
import com.gmail.etpr99.jose.moodlenotifier.network.background.RetryLaterIfUnstableInternet;
import com.gmail.etpr99.jose.moodlenotifier.network.services.MoodleCourseUnitPageCheckerService;
import com.gmail.etpr99.jose.moodlenotifier.network.services.internals.CourseUnitPageCheckerRunnablesQueueManager;
import com.gmail.etpr99.jose.moodlenotifier.network.webviews.MoodleScraperWebView;

import static com.gmail.etpr99.jose.moodlenotifier.MainActivity.MAIN_CHANNEL_ID;
import static com.gmail.etpr99.jose.moodlenotifier.MainActivity.OUTGOING_NOTIFICATION_ID;

public class MainApplicationService extends Service implements RecoverableExceptionHandler, RebindServiceRequestListener {
    private boolean isServiceBound;

    private NotificationManager notificationManager;
    private ConnectivityManager connectivityManager;
    private CourseUnitPageCheckerRunnablesQueueManager courseUnitPageCheckerRunnablesQueueManager;
    private MoodleScraperWebView moodleScraperWebView;
    private PersistentMoodlePageScraper moodleScraperPageService;

    private ServiceConnection unitPageCheckerServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            isServiceBound = true;
            moodleScraperPageService = ((ServiceBinder) service).getService();
            ((OnlineMoodleCookieProvider)
                moodleScraperPageService.getMoodleCookieProvider()).setRecoverableExceptionHandler(MainApplicationService.this);
            moodleScraperPageService.run();
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            isServiceBound = false;
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        courseUnitPageCheckerRunnablesQueueManager =
			((MoodleNotifierApplication) getApplicationContext()).getAppComponent().getPageScraperRunnablesQueueManager();
        moodleScraperWebView = ((MoodleNotifierApplication) getApplicationContext()).getAppComponent().getMoodleScraperWebView();
        moodleScraperWebView.setRecoverableExceptionHandler(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(MAIN_CHANNEL_ID, "Moodle Notifier Channel", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
            startForeground(OUTGOING_NOTIFICATION_ID, createOngoingNotification());
        } else {
            notificationManager.notify(OUTGOING_NOTIFICATION_ID, createOngoingNotification());
        }

        bindCheckerService();
    }

    @Override
    public void onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        } else {
            notificationManager.cancel(OUTGOING_NOTIFICATION_ID);
        }

        unbindCheckerService();
    }

    @Override
    public void handleRecoverableException(Throwable reason) {
        unbindCheckerService();
        switch (reason.getClass().getSimpleName()) {
            case "SocketException":
                Log.w(MainActivity.MAIN_APP_TAG, "Error connecting to the remote server. Maybe there's a connection issue. " +
                    "Trying again shortly.");
                new RetryLaterIfUnstableInternet(5, this).start();
                break;
            case "SocketTimeoutException":
                Log.w(MainActivity.MAIN_APP_TAG, "Maximum amount of connection attempts exceeded, trying again in 3 minutes!");
                new RetryLaterIfUnstableInternet(3, this).start();
                break;
            case "SSLHandshakeException":
                Log.w(MainActivity.MAIN_APP_TAG, "SSL Handshake error, trying again in 3 minutes!");
                new RetryLaterIfUnstableInternet(3, this).start();
                break;
            case "UnknownHostException":
                if (connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected()) {
                    Log.w(MainActivity.MAIN_APP_TAG, "We have a Internet connection, " +
                            "however we can't establish a connection to our host for reasons we don't know why. We try again in 10 minutes.");
                    new RetryLaterIfUnstableInternet(10, this).start();
                } else {
                    Log.w(MainActivity.MAIN_APP_TAG, "No internet detected, not trying again until there isn't an available network!");
                    new LoopUntilInternetAvailable(this, this).start();
                }
                break;
            default:
                Log.e(MainActivity.MAIN_APP_TAG, "Unhandled exception caught, quitting the application!", reason);
                Process.killProcess(Process.myPid());
                break;
        }
    }

    @Override
    public void onRebindServiceRequest() throws InterruptedException {
        if (!isServiceBound) {
            bindCheckerService();
        } else {
            unbindCheckerService();
            Thread.sleep(5000);
            bindCheckerService();
        }
    }

    private Notification createOngoingNotification() {
        Intent intent = new Intent(this, MonitoringActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, MAIN_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle(getString(R.string.checker_service_outgoing_notification_title))
                .setContentText(getString(R.string.checker_service_outgoing_notification_text))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(getString(R.string.checker_service_outgoing_notification_text)))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setOngoing(true);

        return notificationBuilder.build();
    }

    private void bindCheckerService() {
        Intent serviceIntent = new Intent(this, MoodleCourseUnitPageCheckerService.class);
        bindService(serviceIntent, unitPageCheckerServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void unbindCheckerService() {
        if (isServiceBound) {
            isServiceBound = false;
            ((OnlineMoodleCookieProvider) moodleScraperPageService.getMoodleCookieProvider()).setRecoverableExceptionHandler(null);
            ((OnlineMoodleCookieProvider) moodleScraperPageService.getMoodleCookieProvider()).setCookiesAvailableCallback(null);
            courseUnitPageCheckerRunnablesQueueManager.getPageScraperRunnablesQueue().clear();
            moodleScraperPageService.getScheduledExecutorService().shutdownNow();
            moodleScraperPageService.getCourseUnitPageCheckerRunnablesQueueManager().setPageScraperRunnablesQueueIsEmptyListener(null);
            moodleScraperPageService.getPendingNotificationStorer().unregisterServiceInstance();
            moodleScraperWebView.setJsCommandQueuePollingDoneCallback(null);
            unbindService(unitPageCheckerServiceConnection);
            Log.w(MainActivity.MAIN_APP_TAG, "Service " + moodleScraperPageService.getClass().getSimpleName() + " unbound");
        }
    }
}
