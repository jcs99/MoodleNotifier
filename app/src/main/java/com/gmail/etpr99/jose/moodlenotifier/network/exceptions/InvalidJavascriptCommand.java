package com.gmail.etpr99.jose.moodlenotifier.network.exceptions;

import org.jshint.LinterWarning;

import java.util.Collections;
import java.util.List;

public class InvalidJavascriptCommand extends RuntimeException {
    private List<LinterWarning> errors;

    public InvalidJavascriptCommand() {}

    public InvalidJavascriptCommand(String message) {
        super(message);
    }

    public InvalidJavascriptCommand(String message, List<LinterWarning> errors) {
        this(message);
        this.errors = Collections.unmodifiableList(errors);
    }

    public List<LinterWarning> getErrors() {
        return errors;
    }
}