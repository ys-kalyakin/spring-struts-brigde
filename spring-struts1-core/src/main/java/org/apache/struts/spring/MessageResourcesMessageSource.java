package org.apache.struts.spring;

import java.util.Locale;

import jakarta.servlet.ServletContext;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.web.context.ServletContextAware;

import org.apache.struts.util.MessageResources;

/**
 * Bridges a classic Struts {@link MessageResources} bundle (stored in the
 * servlet context under its key suffixed with the module prefix) to the
 * Spring {@link MessageSource} contract. Resolution happens lazily at call
 * time, because the bundles are initialized by the engine after the context
 * has refreshed its message source. Missing keys keep the classic
 * <code>???ru_RU.key???</code> format of the bundle (returnNull=false) and
 * raise {@link NoSuchMessageException} only when the bundle is configured to
 * return null or is not initialized yet.
 */
public class MessageResourcesMessageSource
    implements MessageSource, ServletContextAware {

    private final String key;

    private final String modulePrefix;

    private ServletContext servletContext;

    public MessageResourcesMessageSource(String key, String modulePrefix) {
        this.key = key;
        this.modulePrefix = modulePrefix;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public String getMessage(String code, Object[] args, String defaultMessage,
        Locale locale) {
        try {
            return getMessageInternal(code, args, locale);
        } catch (NoSuchMessageException e) {
            return defaultMessage;
        }
    }

    @Override
    public String getMessage(String code, Object[] args, Locale locale)
        throws NoSuchMessageException {
        return getMessageInternal(code, args, locale);
    }

    @Override
    public String getMessage(MessageSourceResolvable resolvable,
        Locale locale) throws NoSuchMessageException {
        String[] codes = resolvable.getCodes();
        if (codes != null) {
            for (String code : codes) {
                try {
                    return getMessageInternal(code,
                        resolvable.getArguments(), locale);
                } catch (NoSuchMessageException e) {
                    // Try the next code.
                }
            }
        }
        String defaultMessage = resolvable.getDefaultMessage();
        if (defaultMessage != null) {
            return defaultMessage;
        }
        throw new NoSuchMessageException(
            (codes != null && codes.length > 0) ? codes[0] : "", locale);
    }

    private String getMessageInternal(String code, Object[] args,
        Locale locale) {
        MessageResources resources = messageResources();
        if (resources == null) {
            throw new NoSuchMessageException(code, locale);
        }
        String message = (args == null || args.length == 0)
            ? resources.getMessage(locale, code)
            : resources.getMessage(locale, code, args);
        if (message == null) {
            throw new NoSuchMessageException(code, locale);
        }
        return message;
    }

    private MessageResources messageResources() {
        if (servletContext == null) {
            return null;
        }
        Object resources =
            servletContext.getAttribute(key + modulePrefix);
        return (resources instanceof MessageResources messageResources)
            ? messageResources
            : null;
    }
}
