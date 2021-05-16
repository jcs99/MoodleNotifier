package com.gmail.etpr99.jose.moodlenotifier.network;

import android.os.Handler;
import android.os.Process;
import android.util.Log;

import androidx.annotation.NonNull;

import com.gmail.etpr99.jose.moodlenotifier.interfaces.RecoverableExceptionHandler;
import com.gmail.etpr99.jose.moodlenotifier.interfaces.cookies.OnlineMoodleCookieProvider;
import com.gmail.etpr99.jose.moodlenotifier.interfaces.callbacks.CookiesAvailableCallback;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLHandshakeException;

import static com.gmail.etpr99.jose.moodlenotifier.MainActivity.MAIN_APP_TAG;

public class DefaultOnlineMoodleCookieProvider implements OnlineMoodleCookieProvider {
    private static int SAFE_RETRY_COUNT = 0;
    private static final String MOODLE_URL = "moodle.esgt.ipsantarem.pt";
    private static final String USERNAME = "170100228@esg.ipsantarem.pt";
    private static final String PASSWORD = "nopasswordforyou:)";

    private final Handler handler = new Handler();

    private final CookieJarImpl cookieJar;
    private final OkHttpClient okHttpClient;

    private String loginToken;
    private String moodleSessionCookie;
    private CookiesAvailableCallback cookiesAvailableCallback;
    private RecoverableExceptionHandler recoverableExceptionHandler;

    public DefaultOnlineMoodleCookieProvider() {
        cookieJar = new CookieJarImpl();
        okHttpClient = new OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();
    }

    @Override
    public String getMoodleSessionCookie() {
        return moodleSessionCookie;
    }

    @Override
    public RecoverableExceptionHandler getRecoverableExceptionHandler() {
        return recoverableExceptionHandler;
    }

    @Override
    public void setRecoverableExceptionHandler(RecoverableExceptionHandler recoverableExceptionHandler) {
        this.recoverableExceptionHandler = recoverableExceptionHandler;
    }

    @Override
    public OkHttpClient getOkHttpClient() {
        return okHttpClient;
    }

    @Override
    public String getAuthenticationEndpoint() {
        return "https://" + MOODLE_URL + "/login/index.php";
    }

    @Override
    public Map<String, String> getRequestFormDataObject() {
        return new HashMap<String, String>() {{
            put("anchor", "");
            put("logintoken", loginToken);
            put("username", USERNAME);
            put("password", PASSWORD);
        }};
    }

    @Override
    @NotNull
    public CookiesAvailableCallback getCookiesAvailableCallback() {
        return cookiesAvailableCallback;
    }

    @Override
    public void setCookiesAvailableCallback(CookiesAvailableCallback cookiesAvailableCallback) {
        this.cookiesAvailableCallback = cookiesAvailableCallback;
    }

    @Override
    public void run() {
        Request loginTokenRequest = new Request.Builder()
                .url(getAuthenticationEndpoint())
                .get()
                .build();

        getOkHttpClient().newCall(loginTokenRequest).enqueue(new LoginTokenResponseCallback(this));
    }

    private void handleNetworkFailure(IOException ioe, Call call, Callback callback) {
        if ((ioe instanceof SocketTimeoutException) || (ioe instanceof SSLHandshakeException)) {
            Log.e(MAIN_APP_TAG, "Unstable internet detected!", ioe);
            if (SAFE_RETRY_COUNT != 3) {
                Log.w(MAIN_APP_TAG, "Trying a connection again!");
                SAFE_RETRY_COUNT++;
                call.clone().enqueue(callback);
                return;
            } else {
                SAFE_RETRY_COUNT = 0;
            }
        } else if (ioe instanceof UnknownHostException) {
            Log.e(MAIN_APP_TAG, "Unknown host exception caught, possibly no Internet access?", ioe);
        }

        handleExceptionOnRecoverableExceptionHandler(ioe);
    }

    private void handleExceptionOnRecoverableExceptionHandler(IOException ioe) {
        if (getRecoverableExceptionHandler() != null) {
            try {
               handler.post(() -> getRecoverableExceptionHandler().handleRecoverableException(ioe));
            } catch (Throwable th) {
                Log.e(MAIN_APP_TAG, "Error while posting recoverable exception event to listener, staging the exit of the application!", th);
                Process.killProcess(Process.myPid());
            }
        } else {
            Log.w(MAIN_APP_TAG, "Recoverable exception handler is not set, quitting!", ioe);
            Process.killProcess(Process.myPid());
        }
    }

    private static class CookieJarImpl implements CookieJar {
        final Map<String, List<Cookie>> cookieStore = new HashMap<>();

        @Override
        public void saveFromResponse(@NonNull HttpUrl url, @NonNull List<Cookie> cookies) {
            cookieStore.put(url.host(), cookies);
        }

        @NotNull
        @Override
        public List<Cookie> loadForRequest(@NonNull HttpUrl url) {
            List<Cookie> cookies = cookieStore.get(url.host());
            return cookies != null ? cookies : new ArrayList<>();
        }
    }

    private static class LoginTokenResponseCallback implements Callback {
        private final WeakReference<DefaultOnlineMoodleCookieProvider> defaultOnlineMoodleCookieProvider;

        LoginTokenResponseCallback(DefaultOnlineMoodleCookieProvider defaultOnlineMoodleCookieProvider) {
            this.defaultOnlineMoodleCookieProvider = new WeakReference<>(defaultOnlineMoodleCookieProvider);
        }

        private DefaultOnlineMoodleCookieProvider getDefaultOnlineMoodleCookieProvider() {
            return defaultOnlineMoodleCookieProvider.get();
        }

        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            getDefaultOnlineMoodleCookieProvider().handleNetworkFailure(e, call, this);
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
            @SuppressWarnings("ConstantConditions") Document doc = Jsoup.parse(response.body().string());
            Element element = doc.select("input[name=logintoken]").get(0);
            getDefaultOnlineMoodleCookieProvider().loginToken = element.attr("value");

            FormBody.Builder formBuilder = new FormBody.Builder();

            for (Map.Entry<String, String> formEntry: getDefaultOnlineMoodleCookieProvider().getRequestFormDataObject().entrySet()) {
                formBuilder.add(formEntry.getKey(), formEntry.getValue());
            }

            Request request = new Request.Builder()
                    .url(getDefaultOnlineMoodleCookieProvider().getAuthenticationEndpoint())
                    .post(formBuilder.build())
                    .build();

            getDefaultOnlineMoodleCookieProvider().getOkHttpClient()
                    .newCall(request)
                    .enqueue(new LoginHttpResponseCallback(getDefaultOnlineMoodleCookieProvider()));
        }
    }

    private static class LoginHttpResponseCallback implements Callback {
        private final WeakReference<DefaultOnlineMoodleCookieProvider> defaultOnlineMoodleCookieProvider;

        LoginHttpResponseCallback(DefaultOnlineMoodleCookieProvider defaultOnlineMoodleCookieProvider) {
            this.defaultOnlineMoodleCookieProvider = new WeakReference<>(defaultOnlineMoodleCookieProvider);
        }

        private DefaultOnlineMoodleCookieProvider getDefaultOnlineMoodleCookieProvider() {
            return defaultOnlineMoodleCookieProvider.get();
        }

        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            getDefaultOnlineMoodleCookieProvider().handleNetworkFailure(e, call, this);
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) {
            if (SAFE_RETRY_COUNT != 0) SAFE_RETRY_COUNT = 0;
            // means that we successfully logged in
            if (response.request().url().toString().equals("https://" + MOODLE_URL + "/")) {
                CookieJarImpl cookieJar = getDefaultOnlineMoodleCookieProvider().cookieJar;
                getDefaultOnlineMoodleCookieProvider().moodleSessionCookie = cookieJar.cookieStore.get(MOODLE_URL).get(0).value();
            }

            getDefaultOnlineMoodleCookieProvider().handler
                .post(getDefaultOnlineMoodleCookieProvider()
                .getCookiesAvailableCallback()::onCookiesAvailable);
        }
    }
}
