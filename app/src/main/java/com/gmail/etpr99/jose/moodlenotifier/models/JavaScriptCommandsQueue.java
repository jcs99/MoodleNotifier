package com.gmail.etpr99.jose.moodlenotifier.models;

import com.gmail.etpr99.jose.moodlenotifier.abstracts.ChainValueCallback;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class JavaScriptCommandsQueue {
    private Queue<String> linksToExecute;
    private Queue<String> jsCommandsToExecute;
    private Queue<ChainValueCallback<String>> jsValueCallbacks;

    public JavaScriptCommandsQueue(Queue<String> linksToExecute, Queue<String> jsCommandsToExecute,
                   Queue<ChainValueCallback<String>> jsValueCallbacks) {
        this.linksToExecute = linksToExecute;
        this.jsCommandsToExecute = jsCommandsToExecute;
        this.jsValueCallbacks = jsValueCallbacks;
    }

    public Queue<String> getLinksToExecute() {
        return linksToExecute;
    }

    public void setLinksToExecute(Queue<String> linksToExecute) {
        this.linksToExecute = linksToExecute;
    }

    public Queue<String> getJsCommandsToExecute() {
        return jsCommandsToExecute;
    }

    public void setJsCommandsToExecute(Queue<String> jsCommandsToExecute) {
        this.jsCommandsToExecute = jsCommandsToExecute;
    }

    public Queue<ChainValueCallback<String>> getJsValueCallbacks() {
        return jsValueCallbacks;
    }

    public void setJsValueCallbacks(Queue<ChainValueCallback<String>> jsValueCallbacks) {
        this.jsValueCallbacks = jsValueCallbacks;
    }

    public static final class JavaScriptCommandsQueueBuilder {
        private Queue<String> linksToExecute = new LinkedBlockingQueue<>();
        private Queue<String> jsCommandsToExecute = new LinkedBlockingQueue<>();
        private Queue<ChainValueCallback<String>> jsValueCallbacks = new LinkedBlockingQueue<>();

        public JavaScriptCommandsQueueBuilder addLinkToExecute(String linkToExecute) {
            this.linksToExecute.add(linkToExecute);
            return this;
        }

        public JavaScriptCommandsQueueBuilder addJsCommandToExecute(String jsCommandToExecute) {
            this.jsCommandsToExecute.add(jsCommandToExecute);
            return this;
        }

        public JavaScriptCommandsQueueBuilder addJsValueCallback(ChainValueCallback<String> jsValueCallback) {
            this.jsValueCallbacks.add(jsValueCallback);
            return this;
        }

        public JavaScriptCommandsQueue build() {
            return new JavaScriptCommandsQueue(linksToExecute, jsCommandsToExecute, jsValueCallbacks);
        }
    }
}
