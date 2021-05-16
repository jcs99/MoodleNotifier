package com.gmail.etpr99.jose.moodlenotifier.network.webviews;

import android.content.Context;
import android.view.View;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class MoodleScraperWebViewProvider {

    @Singleton
    @Provides
    public MoodleScraperWebView provideMoodleScraperWebViewInstance(Context context) {
        MoodleScraperWebView moodleScraperWebView = new MoodleScraperWebView(context);
        moodleScraperWebView.setId(View.generateViewId());
        return moodleScraperWebView;
    }
}
