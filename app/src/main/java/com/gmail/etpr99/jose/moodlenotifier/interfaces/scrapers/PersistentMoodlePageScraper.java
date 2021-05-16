package com.gmail.etpr99.jose.moodlenotifier.interfaces.scrapers;

import com.gmail.etpr99.jose.moodlenotifier.abstracts.ServiceBinder;
import com.gmail.etpr99.jose.moodlenotifier.interfaces.listeners.PageScraperRunnablesQueueIsEmptyListener;
import com.gmail.etpr99.jose.moodlenotifier.network.services.internals.CourseUnitPageCheckerRunnablesQueueManager;
import com.gmail.etpr99.jose.moodlenotifier.network.services.internals.PendingNotificationStorer;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ScheduledExecutorService;

public interface PersistentMoodlePageScraper extends MoodlePageAccessor, Runnable {
    ServiceBinder getBinder();
    @NotNull CourseUnitPageCheckerRunnablesQueueManager getCourseUnitPageCheckerRunnablesQueueManager();
    @NotNull PendingNotificationStorer getPendingNotificationStorer();
    ScheduledExecutorService getScheduledExecutorService();
}
