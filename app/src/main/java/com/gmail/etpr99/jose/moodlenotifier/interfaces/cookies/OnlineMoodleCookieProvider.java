package com.gmail.etpr99.jose.moodlenotifier.interfaces.cookies;

import com.gmail.etpr99.jose.moodlenotifier.interfaces.RecoverableExceptionHandler;
import com.gmail.etpr99.jose.moodlenotifier.interfaces.callbacks.CookiesAvailableCallback;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import okhttp3.OkHttpClient;

/**
 * The contract which derives from the {@link MoodleCookieProvider} contract. This contract specifies how an implementation
 * should retrieve the Moodle cookies from the Moodle website (or any other website for the record, I don't know how,
 * but it could be that as well, I guess). It contains methods that specify the HTTP client (for now, it's hardcoded to be
 * the OKHttp one, I will probably figure out a way to change that later on though) to be used in the online connection
 * {@link #getOkHttpClient()}, an authentication endpoint {@link #getAuthenticationEndpoint()},the request form data object
 * to be sent while trying an authentication the Moodle website {@link #getRequestFormDataObject()},
 * and finally, a listener used to inform third-party components that the cookies have been retrieved successfully
 * and are ready to be queried {@link #getCookiesAvailableCallback()}. At last, but not least, implementations of this contract
 * are required to specify whether they want to use a {@link RecoverableExceptionHandler} instance to handle networks errors
 * (it is recommended that implementations use a non-null value, as if a network error is caught, it will immediately shutdown
 * the application, no matter how severe the error was. Tip: {@link java.net.SocketTimeoutException}
 * and {@link java.net.UnknownHostException] are not supposed to be fatal conditions that cause the application to terminate,
 * so take that as you wish).
 * The contract specifies a method for querying the current exception handler {@link #getRecoverableExceptionHandler()}
 * and another method to set an exception handler {@link #setRecoverableExceptionHandler(RecoverableExceptionHandler recoverableExceptionHandler)}.
 */
public interface OnlineMoodleCookieProvider extends MoodleCookieProvider, Runnable {
    @Nullable RecoverableExceptionHandler getRecoverableExceptionHandler();
    void setRecoverableExceptionHandler(@Nullable RecoverableExceptionHandler recoverableExceptionHandler);
    OkHttpClient getOkHttpClient();
    String getAuthenticationEndpoint();
    Map<String, String> getRequestFormDataObject();
    @NotNull CookiesAvailableCallback getCookiesAvailableCallback();
    void setCookiesAvailableCallback(CookiesAvailableCallback cookiesAvailableCallback);
}
