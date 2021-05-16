package com.gmail.etpr99.jose.moodlenotifier.validators;

import com.gmail.etpr99.jose.moodlenotifier.network.exceptions.InvalidJavascriptCommand;
import com.gmail.etpr99.jose.moodlenotifier.interfaces.JsValidator;

import org.jshint.JSHint;
import org.jshint.LinterWarning;

public class JsHintValidator implements JsValidator {

    @Override
    public boolean validate(String command) throws InvalidJavascriptCommand {
        if (command == null) return false;

        JSHint jsHint = new JSHint();
        if (jsHint.lint(command)) {
            return true;
        }

        for (LinterWarning error : jsHint.getErrors()) {
            if (error.getCode().startsWith("E")) {
                throw new InvalidJavascriptCommand("Invalid Javascript command detected! List of errors: "
                    + error.getRaw()
                    + " Javascript code: " + command, jsHint.getErrors());
            }
        }

        return true;
    }
}
