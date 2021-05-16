package com.gmail.etpr99.jose.moodlenotifier.network.services.internals;

import java.util.concurrent.LinkedBlockingQueue;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class CourseUnitPageCheckerRunnablesQueueManagerProvider {

    @Singleton
    @Provides
    public CourseUnitPageCheckerRunnablesQueueManager provideCourseUnitPageCheckerRunnablesQueueManager() {
        return new CourseUnitPageCheckerRunnablesQueueManager(new LinkedBlockingQueue<>());
    }
}
