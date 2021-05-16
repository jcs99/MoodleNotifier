package com.gmail.etpr99.jose.moodlenotifier.network.services.internals;

import android.app.Notification;

import com.gmail.etpr99.jose.moodlenotifier.interfaces.listeners.PageScraperRunnablesQueueIsEmptyListener;
import com.gmail.etpr99.jose.moodlenotifier.network.services.MoodleCourseUnitPageCheckerService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class PendingNotificationStorer implements PageScraperRunnablesQueueIsEmptyListener {
    static int NOTIFICATIONS_COUNT = 0;

    private MoodleCourseUnitPageCheckerService service;
    private final Queue<Map.Entry<Integer, Notification>> pendingNotifications = new LinkedBlockingQueue<>();

    public void setService(MoodleCourseUnitPageCheckerService service) {
        this.service = service;
    }

    public void addPendingNotification(Map.Entry<Integer, Notification> notification) {
        pendingNotifications.add(notification);
    }

    @Override
    public void onPageScraperRunnablesQueueIsEmpty() {
        List<Map.Entry<Integer, Notification>> pendingNotifications = new ArrayList<>();

        while (this.pendingNotifications.peek() != null) {
            pendingNotifications.add(this.pendingNotifications.poll());
        }

        service.showPendingNotificationsAndRerun(pendingNotifications);
    }

    public void unregisterServiceInstance() {
        service = null;
    }
}
