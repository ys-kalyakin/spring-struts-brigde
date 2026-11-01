package org.apache.struts.util;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import jakarta.servlet.http.HttpSessionEvent;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

/**
 * Unit tests of the per-session form mutex registry: one stable mutex per
 * session id, distinct mutexes per session, and eviction when the session
 * is destroyed (the engine registers the registry itself as the listener).
 */
class SessionFormMutexRegistryTest {

    private final SessionFormMutexRegistry registry =
        new SessionFormMutexRegistry();

    @Test
    void sameSessionGetsTheSameMutex() {
        MockHttpSession session = new MockHttpSession();
        assertSame(SessionFormMutexRegistry.mutexFor(session),
            SessionFormMutexRegistry.mutexFor(session));
        assertSame(SessionFormMutexRegistry.mutexFor(session),
            registry.mutexFor(session),
            "the static accessor and the listener instance share the map");
    }

    @Test
    void differentSessionsGetDifferentMutexes() {
        assertNotSame(SessionFormMutexRegistry.mutexFor(new MockHttpSession()),
            SessionFormMutexRegistry.mutexFor(new MockHttpSession()));
    }

    @Test
    void sessionDestroyedEvictsTheMutex() {
        MockHttpSession session = new MockHttpSession();
        Object before = SessionFormMutexRegistry.mutexFor(session);

        registry.sessionDestroyed(new HttpSessionEvent(session));

        assertNotSame(before, SessionFormMutexRegistry.mutexFor(session),
            "a destroyed session's mutex is evicted, a new one is created "
                + "on the next use");
    }
}
