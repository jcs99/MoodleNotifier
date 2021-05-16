package com.gmail.etpr99.jose.moodlenotifier.abstracts;

import android.content.Context;
import android.webkit.ValueCallback;

import com.gmail.etpr99.jose.moodlenotifier.application.MoodleNotifierApplication;
import com.gmail.etpr99.jose.moodlenotifier.models.JavaScriptCommandsQueue;
import com.gmail.etpr99.jose.moodlenotifier.network.webviews.MoodleScraperWebView;

import java.lang.ref.WeakReference;

public abstract class ChainValueCallback<T> implements ValueCallback<T> {
    private WeakReference<Context> weakContextReference;
    private Context strongContextReference;
    private JavaScriptCommandsQueue currentJsCommandsQueue;
    private Object argToPass;
    private MoodleScraperWebView moodleScraperWebView;

    public ChainValueCallback(Context context, boolean isContextWeakReference) {
        moodleScraperWebView =
            ((MoodleNotifierApplication) context.getApplicationContext()).getAppComponent().getMoodleScraperWebView();
        if (isContextWeakReference) {
            weakContextReference = new WeakReference<>(context);
        } else {
            strongContextReference = context;
        }
    }

    public void receiveCurrentJsCommandsQueue(JavaScriptCommandsQueue currentJsCommandsQueue) {
        this.currentJsCommandsQueue = currentJsCommandsQueue;
    }

    @Override
    public void onReceiveValue(T value) {
        doOnReceiveValue(value);

        if (getContext() != null) {
            moodleScraperWebView.onJsEvaluationDone(argToPass, currentJsCommandsQueue);
        }
    }

    public abstract void doOnReceiveValue(T value);

    protected Context getContext() {
        return weakContextReference != null ? weakContextReference.get() : strongContextReference;
    }

    protected void setArg(Object arg) {
        this.argToPass = arg;
    }
}
