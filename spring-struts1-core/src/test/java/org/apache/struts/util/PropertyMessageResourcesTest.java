package org.apache.struts.util;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for MessageResources backed by Spring MessageSource (task 3.1):
 * locale from the caller, fallback to the base bundle, and the Struts
 * missing-key format.
 */
class PropertyMessageResourcesTest {

    private MessageResources resources(boolean returnNull) {
        MessageResourcesFactory factory =
            new PropertyMessageResourcesFactory();
        MessageResources resources =
            factory.createResources("test-messages");
        resources.setReturnNull(returnNull);
        return resources;
    }

    @Test
    void messageByLocaleWithBaseBundleFallback() {
        MessageResources resources = resources(false);

        assertEquals("Добро пожаловать!",
            resources.getMessage(new Locale("ru", "RU"), "welcome.heading"));
        assertEquals("Пустое приложение Struts",
            resources.getMessage(new Locale("ru", "RU"), "welcome.title"));
        // Locale-specific bundle missing the key falls back to the base bundle
        assertEquals("Hello, {0}!",
            resources.getMessage(new Locale("ru", "RU"), "greeting"));
        // The no-locale variant routes through the configured default locale
        resources.defaultLocale = Locale.ENGLISH;
        assertEquals("Welcome!", resources.getMessage("welcome.heading"));
    }

    @Test
    void missingKeyInStrutsFormat() {
        MessageResources resources = resources(false);

        assertEquals("???ru_RU.no.such.key???",
            resources.getMessage(new Locale("ru", "RU"), "no.such.key"));
        assertTrue(resources.getMessage("no.such.key")
            .startsWith("???") && resources.getMessage("no.such.key")
                .endsWith(".no.such.key???"));
    }

    @Test
    void returnNullSuppressedFormat() {
        MessageResources resources = resources(true);
        assertNull(resources.getMessage(new Locale("ru", "RU"),
            "no.such.key"));
    }

    @Test
    void parametricReplacement() {
        MessageResources resources = resources(false);

        assertEquals("Hello, Ivan!",
            resources.getMessage(new Locale("en"), "greeting", "Ivan"));
        assertEquals("???en.greeting.missing???",
            resources.getMessage(Locale.ENGLISH, "greeting.missing", "x"));
    }

    @Test
    void isPresentChecksKey() {
        MessageResources resources = resources(false);
        assertTrue(resources.isPresent(new Locale("ru", "RU"),
            "welcome.heading"));
        assertFalse(resources.isPresent(new Locale("ru", "RU"),
            "no.such.key"));
    }

    @Test
    void factoryCreatesPropertyMessageResources() {
        MessageResourcesFactory factory = MessageResourcesFactory
            .createFactory();
        assertEquals(PropertyMessageResourcesFactory.class,
            factory.getClass());
        MessageResources resources = factory.createResources("test-messages");
        assertEquals(PropertyMessageResources.class, resources.getClass());
        assertEquals("test-messages", resources.getConfig());
    }
}
