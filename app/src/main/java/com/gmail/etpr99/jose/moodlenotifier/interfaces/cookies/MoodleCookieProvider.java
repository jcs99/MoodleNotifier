package com.gmail.etpr99.jose.moodlenotifier.interfaces.cookies;

/**
 * A simple contract that defines two methods used to supply the necessary cookies to access Moodle pages that
 * require authentication: {@link #getMoodleSessionCookie()}
 * The names of these methods reflect the real name of the cookies used in the Moodle website.
 * This base implementation is supposed to be agnostic to where the cookies come from
 * (they could come from an online resource, a JSON object, a local database, or the user typing them at hand (...))
 *
 * @author José Simões
 */
public interface MoodleCookieProvider {
    String getMoodleSessionCookie();
}
