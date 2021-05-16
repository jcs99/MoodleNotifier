package com.gmail.etpr99.jose.moodlenotifier.network.background;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.gmail.etpr99.jose.moodlenotifier.interfaces.listeners.RebindServiceRequestListener;

import java.util.Objects;

import static android.content.Context.CONNECTIVITY_SERVICE;
import static com.gmail.etpr99.jose.moodlenotifier.MainActivity.MAIN_APP_TAG;

public final class LoopUntilInternetAvailable extends Thread {
    private ConnectivityManager connectivityManager;
    private RebindServiceRequestListener rebindServiceRequestListener;

    public LoopUntilInternetAvailable(Context context, RebindServiceRequestListener rebindServiceRequestListener) {
        connectivityManager = (ConnectivityManager) Objects.requireNonNull(context.getSystemService(CONNECTIVITY_SERVICE));
        this.rebindServiceRequestListener = rebindServiceRequestListener;
    }

    @Override
    public void run() {
        while (true) {
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isConnected()) {
                Log.i(MAIN_APP_TAG, "Internet is available again, booting up the service!");

                try {
                    rebindServiceRequestListener.onRebindServiceRequest();
                } catch (InterruptedException ie) {
                    throw new RuntimeException(ie);
                }

                break;
            }
            try {
                Thread.sleep(7000);
            } catch (InterruptedException ignored) {}
        }
    }
}
