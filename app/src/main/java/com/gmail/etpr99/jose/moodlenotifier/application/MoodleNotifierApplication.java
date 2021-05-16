package com.gmail.etpr99.jose.moodlenotifier.application;

import androidx.multidex.MultiDexApplication;

public class MoodleNotifierApplication extends MultiDexApplication {
    private AppComponent appComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        appComponent = DaggerAppComponent.builder().application(this).build();
    }

    public AppComponent getAppComponent() {
        return appComponent;
    }
}
