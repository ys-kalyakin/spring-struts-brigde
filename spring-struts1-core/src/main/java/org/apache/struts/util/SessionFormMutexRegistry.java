package org.apache.struts.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;

/**
 * Per-session mutexes serializing the mutation window of session-scoped
 * forms (CVE-2016-1181): acquiring or creating the session instance,
 * running <code>reset()</code> and populating it — including the multipart
 * path — happen under the session's mutex, so concurrent requests of one
 * session cannot interleave these steps on the shared instance. Validation
 * and action execution run outside the window (classic shared-state
 * semantics).
 *
 * <p>The mutexes live in a static registry keyed by session id; this class
 * doubles as the {@link HttpSessionListener} that evicts the entry of a
 * destroyed session. The engine registers it as a servlet listener in both
 * runtimes (Spring Boot registration bean and the engine initializer's
 * programmatic registration for web.xml deployments). When a container
 * rejects the programmatic registration, entries accumulate one per session
 * id and a warning is logged — a degradation the initializer tolerates like
 * the programmatic filter registration.</p>
 */
public final class SessionFormMutexRegistry implements HttpSessionListener {

    private static final Map<String, Object> MUTEXES =
        new ConcurrentHashMap<>();

    /**
     * The mutex serializing the form lifecycle window of the given session,
     * created on first use.
     */
    public static Object mutexFor(HttpSession session) {
        return MUTEXES.computeIfAbsent(session.getId(), id -> new Object());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        MUTEXES.remove(event.getSession().getId());
    }
}
