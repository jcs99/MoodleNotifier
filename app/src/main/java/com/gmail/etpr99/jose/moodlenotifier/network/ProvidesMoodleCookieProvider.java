package com.gmail.etpr99.jose.moodlenotifier.network;

import com.gmail.etpr99.jose.moodlenotifier.interfaces.cookies.MoodleCookieProvider;
import com.gmail.etpr99.jose.moodlenotifier.interfaces.cookies.OnlineMoodleCookieProvider;

import dagger.Module;
import dagger.Provides;

@Module
public class ProvidesMoodleCookieProvider {

    @Provides
    public MoodleCookieProvider provideMoodleCookieProvider() {
        return provideOnlineMoodleCookieProvider();
    }

    @Provides
    public OnlineMoodleCookieProvider provideOnlineMoodleCookieProvider() {
        return new DefaultOnlineMoodleCookieProvider();
    }
}
