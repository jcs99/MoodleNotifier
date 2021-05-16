package com.gmail.etpr99.jose.moodlenotifier.interfaces.scrapers;

import com.gmail.etpr99.jose.moodlenotifier.models.JavaScriptCommandsQueue;

import org.jetbrains.annotations.NotNull;

public interface MoodlePageScraper extends MoodlePageAccessor {
    @NotNull JavaScriptCommandsQueue getJsCommandsQueue();
    void createJsCommandsQueue();
}
