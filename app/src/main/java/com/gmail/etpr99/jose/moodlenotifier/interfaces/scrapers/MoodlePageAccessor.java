package com.gmail.etpr99.jose.moodlenotifier.interfaces.scrapers;

import com.gmail.etpr99.jose.moodlenotifier.interfaces.cookies.MoodleCookieProvider;

import org.jetbrains.annotations.NotNull;

public interface MoodlePageAccessor {
    @NotNull MoodleCookieProvider getMoodleCookieProvider();
}
