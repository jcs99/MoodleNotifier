package com.gmail.etpr99.jose.moodlenotifier.network.background;

import android.util.Log;

import com.gmail.etpr99.jose.moodlenotifier.interfaces.listeners.RebindServiceRequestListener;

import java.util.concurrent.TimeUnit;

import static com.gmail.etpr99.jose.moodlenotifier.MainActivity.MAIN_APP_TAG;

public final class RetryLaterIfUnstableInternet extends Thread {
    private int timeUntilRetryAgain;
    private RebindServiceRequestListener rebindServiceRequestListener;

    public RetryLaterIfUnstableInternet(int timeUntilRetryAgain, RebindServiceRequestListener rebindServiceRequestListener) {
        this.timeUntilRetryAgain = timeUntilRetryAgain;
        this.rebindServiceRequestListener = rebindServiceRequestListener;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(TimeUnit.MINUTES.toMillis(timeUntilRetryAgain));
            Log.i(MAIN_APP_TAG, "Booting up the service again!");
            rebindServiceRequestListener.onRebindServiceRequest();
        } catch (InterruptedException ie) {
            throw new RuntimeException(ie);
        }
    }
}
