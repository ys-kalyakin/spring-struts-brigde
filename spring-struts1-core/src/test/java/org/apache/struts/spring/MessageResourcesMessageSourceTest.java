package org.apache.struts.spring;

import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockServletContext;

import org.apache.struts.Globals;
import org.apache.struts.util.MessageResources;
import org.apache.struts.util.PropertyMessageResourcesFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the MessageResources → MessageSource bridge: bundle
 * values, argument interpolation and the classic missing-key format.
 */
class MessageResourcesMessageSourceTest {

    private MessageResourcesMessageSource source;

    @BeforeEach
    void setUp() {
        source = sourceFor("test-messages", false);
    }

    private MessageResourcesMessageSource sourceFor(String parameter,
        boolean returnNull) {
        MockServletContext servletContext = new MockServletContext();
        MessageResources resources =
            new PropertyMessageResourcesFactory()
                .createResources(parameter);
        resources.setReturnNull(returnNull);
        servletContext.setAttribute(Globals.MESSAGES_KEY, resources);
        MessageResourcesMessageSource source =
            new MessageResourcesMessageSource(Globals.MESSAGES_KEY, "");
        source.setServletContext(servletContext);
        return source;
    }

    @Test
    void resolvesBundleMessage() {
        assertEquals("Welcome!", source.getMessage("welcome.heading", null,
            Locale.ENGLISH));
    }

    @Test
    void interpolatesArguments() {
        assertEquals("Hello, Ivan!", source.getMessage("greeting",
            new Object[] {"Ivan"}, Locale.ENGLISH));
    }

    @Test
    void missingKeyKeepsStrutsFormat() {
        assertEquals("???ru_RU.no.such.key???", source.getMessage(
            "no.such.key", null, new Locale("ru", "RU")));
    }

    @Test
    void classicFormatWinsOverDefaultMessage() {
        assertEquals("???en.no.such.key???", source.getMessage("no.such.key",
            null, "fallback", Locale.ENGLISH));
    }

    @Test
    void defaultMessageAppliesWhenBundleReturnsNull() {
        MessageResourcesMessageSource nullReturning =
            sourceFor("test-messages", true);
        assertEquals("fallback", nullReturning.getMessage("no.such.key", null,
            "fallback", Locale.ENGLISH));
    }

    @Test
    void uninitializedBundleThrows() {
        MessageResourcesMessageSource empty =
            new MessageResourcesMessageSource(Globals.MESSAGES_KEY, "");
        empty.setServletContext(new MockServletContext());
        assertThrows(
            org.springframework.context.NoSuchMessageException.class,
            () -> empty.getMessage("welcome.heading", null, Locale.ENGLISH));
    }
}
