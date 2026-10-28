package org.apache.struts.taglib.spring;

import org.apache.struts.taglib.TagUtils;

import java.util.Locale;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;

import org.apache.struts.Globals;
import org.apache.struts.util.MessageResources;
import org.apache.struts.util.RequestUtils;

/**
 * Spring component of the tag libraries: resolves bundle messages for the
 * tags (locale from the session, the classic <code>???ru_RU.key???</code>
 * format for missing keys). The application may override the
 * <code>tagMessageResolver</code> bean to change the message resolution of
 * every tag.
 */
public class TagMessageResolver {

    /**
     * The message resources of the tag libraries, used for exception texts.
     */
    protected static final MessageResources messages =
        MessageResources.getMessageResources(
            "org.apache.struts.taglib.LocalStrings");

    /**
     * Look up and return a message string, based on the specified
     * parameters.
     */
    public String message(PageContext pageContext, String bundle,
        String locale, String key) throws JspException {
        return message(pageContext, bundle, locale, key, null);
    }

    /**
     * Look up and return a message string, based on the specified
     * parameters.
     */
    public String message(PageContext pageContext, String bundle,
        String locale, String key, Object[] args) throws JspException {
        MessageResources resources =
            retrieveMessageResources(pageContext, bundle, false);
        Locale userLocale = getUserLocale(pageContext, locale);
        String message;
        if (args == null) {
            message = resources.getMessage(userLocale, key);
        } else {
            message = resources.getMessage(userLocale, key, args);
        }
        return message;
    }

    /**
     * Return true if a message string for the specified message key is
     * present for the specified locale and bundle.
     */
    public boolean present(PageContext pageContext, String bundle,
        String locale, String key) throws JspException {
        MessageResources resources =
            retrieveMessageResources(pageContext, bundle, true);
        Locale userLocale = getUserLocale(pageContext, locale);
        return resources.isPresent(userLocale, key);
    }

    /**
     * Return the MessageResources object for the current module and the
     * given bundle.
     */
    public MessageResources retrieveMessageResources(PageContext pageContext,
        String bundle, boolean checkPageScope) throws JspException {
        MessageResources resources = null;

        if (bundle == null) {
            bundle = Globals.MESSAGES_KEY;
        }

        if (checkPageScope) {
            resources = (MessageResources) pageContext.getAttribute(bundle,
                PageContext.PAGE_SCOPE);
        }

        if (resources == null) {
            resources = (MessageResources) pageContext.getAttribute(bundle,
                PageContext.REQUEST_SCOPE);
        }

        if (resources == null) {
            TagFormLocator locator = TagServices.service(pageContext,
                TagFormLocator.class, TagFormLocator::new);
            org.apache.struts.config.ModuleConfig moduleConfig =
                locator.getModuleConfig(pageContext);
            resources = (MessageResources) pageContext.getAttribute(
                bundle + moduleConfig.getPrefix(),
                PageContext.APPLICATION_SCOPE);
        }

        if (resources == null) {
            resources = (MessageResources) pageContext.getAttribute(bundle,
                PageContext.APPLICATION_SCOPE);
        }

        if (resources == null) {
            JspException e = new JspException(
                messages.getMessage("message.bundle", bundle));
            TagUtils.getInstance().saveException(pageContext, e);
            throw e;
        }

        return resources;
    }

    /**
     * Look up and return the current user locale, based on the specified
     * parameters.
     */
    public Locale getUserLocale(PageContext pageContext, String locale) {
        return RequestUtils.getUserLocale(
            (jakarta.servlet.http.HttpServletRequest) pageContext
                .getRequest(),
            locale);
    }
}
