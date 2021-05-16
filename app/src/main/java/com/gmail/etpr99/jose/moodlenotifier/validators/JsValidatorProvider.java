package com.gmail.etpr99.jose.moodlenotifier.validators;

import com.gmail.etpr99.jose.moodlenotifier.interfaces.JsValidator;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class JsValidatorProvider {

    @Singleton
    @Provides
    public JsValidator provideJsValidator() {
        return new JsHintValidator();
    }
}
