package com.gmail.etpr99.jose.moodlenotifier.interfaces;

import com.gmail.etpr99.jose.moodlenotifier.network.exceptions.InvalidJavascriptCommand;

public interface JsValidator {
    boolean validate(String command) throws InvalidJavascriptCommand;
}
