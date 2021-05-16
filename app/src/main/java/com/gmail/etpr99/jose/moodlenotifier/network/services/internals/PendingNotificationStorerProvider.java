package com.gmail.etpr99.jose.moodlenotifier.network.services.internals;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class PendingNotificationStorerProvider {

    @Provides
    @Singleton
    public PendingNotificationStorer providePendingNotificationStorer() {
        return new PendingNotificationStorer();
    }
}
