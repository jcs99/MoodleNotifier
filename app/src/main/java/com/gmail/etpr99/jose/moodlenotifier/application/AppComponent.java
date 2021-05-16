package com.gmail.etpr99.jose.moodlenotifier.application;

import android.app.Application;
import android.content.Context;

import com.gmail.etpr99.jose.moodlenotifier.CheckPageActivity;
import com.gmail.etpr99.jose.moodlenotifier.MainActivity;
import com.gmail.etpr99.jose.moodlenotifier.ManageCourseActivity;
import com.gmail.etpr99.jose.moodlenotifier.MonitoringActivity;
import com.gmail.etpr99.jose.moodlenotifier.MoodleCourseUnitSelectorActivity;
import com.gmail.etpr99.jose.moodlenotifier.interfaces.cookies.OnlineMoodleCookieProvider;
import com.gmail.etpr99.jose.moodlenotifier.network.ProvidesMoodleCookieProvider;
import com.gmail.etpr99.jose.moodlenotifier.network.services.MoodleCourseUnitPageCheckerService;
import com.gmail.etpr99.jose.moodlenotifier.network.services.internals.CourseUnitPageCheckerRunnable;
import com.gmail.etpr99.jose.moodlenotifier.network.services.internals.CourseUnitPageCheckerRunnablesQueueManager;
import com.gmail.etpr99.jose.moodlenotifier.network.services.internals.CourseUnitPageCheckerRunnablesQueueManagerProvider;
import com.gmail.etpr99.jose.moodlenotifier.network.services.internals.PendingNotificationStorer;
import com.gmail.etpr99.jose.moodlenotifier.network.services.internals.PendingNotificationStorerProvider;
import com.gmail.etpr99.jose.moodlenotifier.network.webviews.MoodleScraperWebView;
import com.gmail.etpr99.jose.moodlenotifier.network.webviews.MoodleScraperWebViewProvider;
import com.gmail.etpr99.jose.moodlenotifier.persistence.AppDatabaseProvider;
import com.gmail.etpr99.jose.moodlenotifier.validators.JsValidatorProvider;

import javax.inject.Singleton;

import dagger.Binds;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;

@Singleton
@Component(modules = {
    AppComponent.ContextModule.class,
    AppDatabaseProvider.class,
    JsValidatorProvider.class,
    MoodleScraperWebViewProvider.class,
    CourseUnitPageCheckerRunnablesQueueManagerProvider.class,
    ProvidesMoodleCookieProvider.class,
    PendingNotificationStorerProvider.class
})
public interface AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application(Application application);

        AppComponent build();
    }

    MoodleScraperWebView getMoodleScraperWebView();
    CourseUnitPageCheckerRunnablesQueueManager getPageScraperRunnablesQueueManager();
    OnlineMoodleCookieProvider getOnlineMoodleCookieProvider();
    PendingNotificationStorer getPendingNotificationStorer();

    void inject(MainActivity mainActivity);
    void inject(MoodleCourseUnitSelectorActivity moodleCourseUnitSelectorActivity);
    void inject(MonitoringActivity monitoringActivity);
    void inject(ManageCourseActivity manageCourseActivity);
    void inject(CheckPageActivity checkPageActivity);
    void inject(MoodleScraperWebView moodleScraperWebView);
    void inject(MoodleCourseUnitPageCheckerService moodleCourseUnitPageCheckerService);
    void inject(CourseUnitPageCheckerRunnable courseUnitPageCheckerRunnable);

    @Module
    abstract class ContextModule {

        @Singleton
        @Binds
        public abstract Context context(Application app);
    }
}
