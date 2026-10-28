package org.apache.struts.util;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.ResourceBundleMessageSource;

/**
 * Concrete implementation of {@link MessageResources} that delegates message
 * resolution to a Spring {@link MessageSource} backed by the configured
 * bundle base name. The Struts 1 semantics are preserved: the locale chain is
 * resolved by the MessageSource (locale specific bundle, then the base
 * bundle) and a missing key yields <code>???<locale>.<key>???</code> unless
 * <code>returnNull</code> is set.
 *
 * <p>Ported from weblegacy/struts1 1.5.x
 * (org.apache.struts.util.PropertyMessageResources), backed by Spring
 * MessageSource per the spring-struts-bridge architecture decision.</p>
 */
public class PropertyMessageResources extends MessageResources {

    /**
     * The Spring message source backing this instance.
     */
    protected transient MessageSource messageSource;

    public PropertyMessageResources(MessageResourcesFactory factory,
        String config) {
        this(factory, config, false);
    }

    public PropertyMessageResources(MessageResourcesFactory factory,
        String config, boolean returnNull) {
        super(factory, config, returnNull);
        this.messageSource = createDefaultMessageSource();
    }

    /**
     * Create the default backing MessageSource. ResourceBundle's default
     * control falls back to the JVM default locale before the base bundle;
     * Struts semantics only walk the requested locale's chain
     * (e.g. ru_RU → ru → base), so a no-fallback control is used.
     */
    protected MessageSource createDefaultMessageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource() {
            @Override
            protected ResourceBundle doGetBundle(String basename,
                Locale locale) throws MissingResourceException {
                ClassLoader cl = Thread.currentThread()
                    .getContextClassLoader();
                return ResourceBundle.getBundle(basename, locale,
                    (cl != null) ? cl
                        : PropertyMessageResources.class.getClassLoader(),
                    ResourceBundle.Control.getNoFallbackControl(
                        ResourceBundle.Control.FORMAT_PROPERTIES));
            }
        };
        source.setBasename(config);
        source.setDefaultEncoding("UTF-8");
        return source;
    }

    /**
     * Replace the backing message source (used by the engine when the
     * application provides its own <code>MessageSource</code>).
     */
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public String getMessage(Locale locale, String key) {
        if (locale == null) {
            locale = defaultLocale;
        }

        try {
            return messageSource.getMessage(key, null, locale);
        } catch (NoSuchMessageException e) {
            return returnNull ? null
                : ("???" + messageKey(locale, key) + "???");
        }
    }
}
