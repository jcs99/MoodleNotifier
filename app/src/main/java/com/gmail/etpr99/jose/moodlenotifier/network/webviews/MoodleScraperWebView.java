package com.gmail.etpr99.jose.moodlenotifier.network.webviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.Process;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.annotation.CallSuper;

import com.gmail.etpr99.jose.moodlenotifier.application.MoodleNotifierApplication;
import com.gmail.etpr99.jose.moodlenotifier.interfaces.RecoverableExceptionHandler;
import com.gmail.etpr99.jose.moodlenotifier.interfaces.callbacks.JsCommandQueuePollingDoneCallback;
import com.gmail.etpr99.jose.moodlenotifier.models.JavaScriptCommandsQueue;
import com.gmail.etpr99.jose.moodlenotifier.abstracts.ChainValueCallback;
import com.gmail.etpr99.jose.moodlenotifier.interfaces.JsValidator;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;

import javax.inject.Inject;

import static com.gmail.etpr99.jose.moodlenotifier.MainActivity.MAIN_APP_TAG;

public class MoodleScraperWebView extends WebView {
    @Inject JsValidator jsValidator;

    private RecoverableExceptionHandler recoverableExceptionHandler;
    private JsCommandQueuePollingDoneCallback jsCommandQueuePollingDoneCallback;
    private JavaScriptCommandsQueue currentJsCommandsQueue;
    private MoodleScraperWebViewClient moodleScraperWebViewClient;

    @SuppressLint("SetJavaScriptEnabled")
    MoodleScraperWebView(Context context) {
        super(context);
        ((MoodleNotifierApplication) context.getApplicationContext()).getAppComponent().inject(this);
        getSettings().setJavaScriptEnabled(true);
        MoodleScraperWebViewClient moodleScraperWebViewClient = new MoodleScraperWebViewClient();
        super.setWebViewClient(moodleScraperWebViewClient);
        this.moodleScraperWebViewClient = moodleScraperWebViewClient;
    }

    public RecoverableExceptionHandler getRecoverableExceptionHandler() {
        return recoverableExceptionHandler;
    }

    public void setRecoverableExceptionHandler(RecoverableExceptionHandler recoverableExceptionHandler) {
        this.recoverableExceptionHandler = recoverableExceptionHandler;
    }

    public JsCommandQueuePollingDoneCallback getJsCommandQueuePollingDoneCallback() {
        return jsCommandQueuePollingDoneCallback;
    }

    public void setJsCommandQueuePollingDoneCallback(JsCommandQueuePollingDoneCallback jsCommandQueuePollingDoneCallback) {
        this.jsCommandQueuePollingDoneCallback = jsCommandQueuePollingDoneCallback;
    }

    public void setOnPageFinishedListener(OnPageFinishedListener onPageFinishedListener) {
        moodleScraperWebViewClient.onPageFinishedListener = onPageFinishedListener;
    }

    @Override
    public void setWebViewClient(WebViewClient webViewClient) {
        throw new UnsupportedOperationException("The WebViewClient instance of this WebView is final!");
    }

    public void onJsEvaluationDone(Object arg, JavaScriptCommandsQueue jsCommandsQueue) {
        if (jsCommandsQueue.getLinksToExecute().peek() != null) {
            if (arg instanceof Map) {
                @SuppressWarnings("unchecked") Map<String, Object> argsMap = (Map) arg;
                Uri uri = Uri.parse(jsCommandsQueue.getLinksToExecute().poll());
                Uri.Builder uriBuilder = uri.buildUpon();

                for (Map.Entry<String, Object> entry : argsMap.entrySet()) {
                    uriBuilder.appendQueryParameter(entry.getKey(), entry.getValue().toString());
                }

                loadUrl(uriBuilder.build().toString());
            } else {
                loadUrl(jsCommandsQueue.getLinksToExecute().poll());
            }
        } else {
            currentJsCommandsQueue = null;
            if (jsCommandQueuePollingDoneCallback != null) {
                jsCommandQueuePollingDoneCallback.onJsCommandQueuePollingDone();
            }
        }
    }

    public void startPolling(JavaScriptCommandsQueue jsCommandsQueue) {
        if (jsCommandsQueue != null) {
            currentJsCommandsQueue = jsCommandsQueue;
            loadUrl(jsCommandsQueue.getLinksToExecute().poll());
        } else {
            throw new NullPointerException("jsCommandsQueue == null");
        }
    }

    private void pollJsCommand(JavaScriptCommandsQueue jsCommandsQueue) {
        if (jsCommandsQueue != null) {
            if (jsValidator != null) {
                jsValidator.validate(jsCommandsQueue.getJsCommandsToExecute().peek());
            }

            ChainValueCallback<String> headJsCallback = jsCommandsQueue.getJsValueCallbacks().poll();
            headJsCallback.receiveCurrentJsCommandsQueue(jsCommandsQueue);
            evaluateJavascript(jsCommandsQueue.getJsCommandsToExecute().poll(), headJsCallback);
        }
    }

    private static class MoodleScraperWebViewClient extends WebViewClient {
        private OnPageFinishedListener onPageFinishedListener;

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return true;
        }

        @Override
        @CallSuper
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            Exception exception;

            switch (errorCode) {
                case WebViewClient.ERROR_CONNECT:
                    exception = new SocketException("Error connecting");
                    break;
                case WebViewClient.ERROR_TIMEOUT:
                    exception = new SocketTimeoutException("Connect timeout");
                    break;
                case WebViewClient.ERROR_HOST_LOOKUP:
                    exception = new UnknownHostException("Unknown host name");
                    break;
                default:
                    exception = new RuntimeException("Error " + errorCode + " received in WebView");
            }

            MoodleScraperWebView moodleScraperWebView = (MoodleScraperWebView) view;

            if (moodleScraperWebView.getRecoverableExceptionHandler() != null) {
                moodleScraperWebView.getRecoverableExceptionHandler().handleRecoverableException(exception);
            } else {
                Log.e(MAIN_APP_TAG, "An error occurred in the WebView and no exception handler is set, quitting!",
                    exception);
                Process.killProcess(Process.myPid());
            }
        }

        @Override
        @CallSuper
        public void onPageFinished(WebView view, String url) {
            if (onPageFinishedListener != null) {
                onPageFinishedListener.onPageFinished();
            }

            MoodleScraperWebView moodleScraperWebView = (MoodleScraperWebView) view;
            moodleScraperWebView.pollJsCommand(moodleScraperWebView.currentJsCommandsQueue);
        }
    }

    public interface OnPageFinishedListener {
        void onPageFinished();
    }
}
