package org.apache.struts.util;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * General purpose abstract class that describes the API for retrieving
 * Locale-sensitive messages from underlying resource locations.
 *
 * <p>Ported from weblegacy/struts1 1.5.x (org.apache.struts.util.MessageResources).</p>
 */
public abstract class MessageResources {

    private static final Logger log =
        LoggerFactory.getLogger(MessageResources.class);

    /**
     * The configuration parameter used to initialize this MessageResources.
     */
    protected String config;

    /**
     * The default Locale for our environment.
     */
    protected Locale defaultLocale = Locale.getDefault();

    /**
     * The MessageResourcesFactory that created this instance.
     */
    protected MessageResourcesFactory factory;

    /**
     * The set of previously created MessageFormat objects, keyed by the
     * locale and message key.
     */
    protected HashMap<String, MessageFormat> formats = new HashMap<>();

    /**
     * Indicates whether a <code>null</code> String is returned instead of an
     * error message string when an unknown Locale or key is requested.
     */
    protected boolean returnNull = false;

    /**
     * Indicates whether 'escape' processing is performed on the message
     * string before a <code>MessageFormat</code> is applied.
     */
    private boolean escape = true;

    public MessageResources(MessageResourcesFactory factory, String config) {
        this(factory, config, false);
    }

    public MessageResources(MessageResourcesFactory factory, String config,
        boolean returnNull) {
        this.factory = factory;
        this.config = config;
        this.returnNull = returnNull;
    }

    public String getConfig() {
        return config;
    }

    public MessageResourcesFactory getFactory() {
        return factory;
    }

    public boolean getReturnNull() {
        return returnNull;
    }

    public void setReturnNull(boolean returnNull) {
        this.returnNull = returnNull;
    }

    public boolean isEscape() {
        return escape;
    }

    public void setEscape(boolean escape) {
        this.escape = escape;
    }

    /**
     * Returns a text message for the specified key, for the default Locale.
     */
    public String getMessage(String key) {
        return getMessage(defaultLocale, key);
    }

    /**
     * Returns a text message for the specified key, for the default Locale,
     * with parametric replacement.
     */
    public String getMessage(String key, Object[] args) {
        return getMessage(defaultLocale, key, args);
    }

    public String getMessage(String key, Object arg0) {
        return getMessage(defaultLocale, key, new Object[] { arg0 });
    }

    public String getMessage(String key, Object arg0, Object arg1) {
        return getMessage(defaultLocale, key, new Object[] { arg0, arg1 });
    }

    public String getMessage(String key, Object arg0, Object arg1,
        Object arg2) {
        return getMessage(defaultLocale, key,
            new Object[] { arg0, arg1, arg2 });
    }

    public String getMessage(String key, Object arg0, Object arg1,
        Object arg2, Object arg3) {
        return getMessage(defaultLocale, key,
            new Object[] { arg0, arg1, arg2, arg3 });
    }

    /**
     * Returns a text message for the specified key, for the specified Locale.
     * A null String result will be returned if no matching message is found,
     * or if <code>returnNull</code> is set; otherwise an error message string
     * in the form <code>???<locale>.<key>???</code> is returned.
     */
    public abstract String getMessage(Locale locale, String key);

    /**
     * Returns a text message after parametric replacement of the specified
     * parameter placeholders. A null String result will be returned if no
     * matching message is found, or if <code>returnNull</code> is set.
     */
    public String getMessage(Locale locale, String key, Object[] args) {
        if (locale == null) {
            locale = defaultLocale;
        }

        MessageFormat format = null;
        String formatKey = messageKey(locale, key);

        synchronized (formats) {
            format = formats.get(formatKey);

            if (format == null) {
                String formatString = getMessage(locale, key);

                if (formatString == null) {
                    return returnNull ? null
                        : ("???" + formatKey + "???");
                }

                format = new MessageFormat(escape(formatString));
                format.setLocale(locale);
                formats.put(formatKey, format);
            }
        }

        return format.format(args);
    }

    public String getMessage(Locale locale, String key, Object arg0) {
        return getMessage(locale, key, new Object[] { arg0 });
    }

    public String getMessage(Locale locale, String key, Object arg0,
        Object arg1) {
        return getMessage(locale, key, new Object[] { arg0, arg1 });
    }

    public String getMessage(Locale locale, String key, Object arg0,
        Object arg1, Object arg2) {
        return getMessage(locale, key, new Object[] { arg0, arg1, arg2 });
    }

    public String getMessage(Locale locale, String key, Object arg0,
        Object arg1, Object arg2, Object arg3) {
        return getMessage(locale, key,
            new Object[] { arg0, arg1, arg2, arg3 });
    }

    /**
     * Return <code>true</code> if a message string for the specified message
     * key exists for the default Locale.
     */
    public boolean isPresent(String key) {
        return isPresent(defaultLocale, key);
    }

    /**
     * Return <code>true</code> if a message string for the specified message
     * key exists for the specified Locale.
     */
    public boolean isPresent(Locale locale, String key) {
        String message = getMessage(locale, key);
        return (message != null) && !message.startsWith("???");
    }

    /**
     * Escape any single quote characters that are included in the specified
     * message string.
     */
    protected String escape(String string) {
        if (!isEscape() || (string == null)
            || (string.indexOf('\'') < 0)) {
            return string;
        }

        int n = string.length();
        StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; i++) {
            char ch = string.charAt(i);

            if (ch == '\'') {
                sb.append('\'');
            }

            sb.append(ch);
        }

        return sb.toString();
    }

    /**
     * Compute and return a key to be used in caching information by a Locale.
     * The locale key for the default Locale in our environment is a zero
     * length String.
     */
    protected String localeKey(Locale locale) {
        return (locale == null) ? "" : locale.toString();
    }

    /**
     * Compute and return a key to be used in caching information by Locale
     * and message key.
     */
    protected String messageKey(Locale locale, String key) {
        return (localeKey(locale) + "." + key);
    }

    /**
     * Compute and return a key to be used in caching information by locale
     * key and message key.
     */
    protected String messageKey(String localeKey, String key) {
        return (localeKey + "." + key);
    }

    /**
     * Create and return an instance of <code>MessageResources</code> of the
     * appropriate class for the specified configuration parameter.
     */
    public synchronized static MessageResources getMessageResources(
        String config) {
        return MessageResourcesFactory.createFactory()
            .createResources(config);
    }

    public void log(String message) {
        log.info(message);
    }

    public void log(String message, Throwable throwable) {
        log.error(message, throwable);
    }
}
