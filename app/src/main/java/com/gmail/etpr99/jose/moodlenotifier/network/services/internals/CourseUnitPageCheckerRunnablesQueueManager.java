package com.gmail.etpr99.jose.moodlenotifier.network.services.internals;

import com.gmail.etpr99.jose.moodlenotifier.interfaces.callbacks.JsCommandQueuePollingDoneCallback;
import com.gmail.etpr99.jose.moodlenotifier.interfaces.listeners.PageScraperRunnablesQueueIsEmptyListener;

import java.util.Queue;

public class CourseUnitPageCheckerRunnablesQueueManager implements JsCommandQueuePollingDoneCallback {
    private Queue<Runnable> pageScraperRunnablesQueue;
    private PageScraperRunnablesQueueIsEmptyListener pageScraperRunnablesQueueIsEmptyListener;

    public CourseUnitPageCheckerRunnablesQueueManager(Queue<Runnable> pageScraperRunnablesQueue) {
        this.pageScraperRunnablesQueue = pageScraperRunnablesQueue;
    }

    public Queue<Runnable> getPageScraperRunnablesQueue() {
        return pageScraperRunnablesQueue;
    }

    public PageScraperRunnablesQueueIsEmptyListener getPageScraperRunnablesQueueIsEmptyListener() {
        return pageScraperRunnablesQueueIsEmptyListener;
    }

    public void setPageScraperRunnablesQueueIsEmptyListener(PageScraperRunnablesQueueIsEmptyListener pageScraperRunnablesQueueIsEmptyListener) {
        this.pageScraperRunnablesQueueIsEmptyListener = pageScraperRunnablesQueueIsEmptyListener;
    }

    public void addRunnable(Runnable runnable) {
        pageScraperRunnablesQueue.add(runnable);
    }

    @Override
    public void onJsCommandQueuePollingDone() {
        poll();
    }

    public void poll() {
        if (pageScraperRunnablesQueue.peek() != null) {
            pageScraperRunnablesQueue.poll().run();
        } else {
            pageScraperRunnablesQueueIsEmptyListener.onPageScraperRunnablesQueueIsEmpty();
        }
    }
}
