package com.gmail.etpr99.jose.moodlenotifier.abstracts;

import android.os.Binder;

import com.gmail.etpr99.jose.moodlenotifier.interfaces.scrapers.PersistentMoodlePageScraper;

public abstract class ServiceBinder extends Binder {
    public abstract PersistentMoodlePageScraper getService();
}
