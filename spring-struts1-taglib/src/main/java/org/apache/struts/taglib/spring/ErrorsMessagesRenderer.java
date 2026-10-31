package org.apache.struts.taglib.spring;

import org.apache.struts.taglib.TagUtils;

import java.util.Iterator;

import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

/**
 * Spring component of the tag libraries: collects {@link ActionMessages}
 * from the request attributes and renders the error list of
 * <code>html:errors</code> (header/footer around the list,
 * prefix/suffix around each message, texts resolved through the message
 * resolver). The application may override the
 * <code>errorsMessagesRenderer</code> bean to change the output of the
 * errors and MESSAGES tags.
 */
public class ErrorsMessagesRenderer {

    /**
     * The message resources of the tag libraries, used for exception texts.
     */
    protected static final org.apache.struts.util.MessageResources MESSAGES =
        org.apache.struts.util.MessageResources.getMessageResources(
            "org.apache.struts.taglib.LocalStrings");

    /**
     * Retrieves the value from request scope and, if it isn't already an
     * {@link ActionMessages}, converts the supported classes to one.
     */
    public ActionMessages getActionMessages(PageContext pageContext,
        String paramName) throws JspException {
        ActionMessages am = new ActionMessages();

        Object value = pageContext.findAttribute(paramName);

        if (value != null) {
            try {
                if (value instanceof String) {
                    am.add(ActionMessages.GLOBAL_MESSAGE,
                        new ActionMessage((String) value));
                } else if (value instanceof String[]) {
                    String[] keys = (String[]) value;

                    for (int i = 0; i < keys.length; i++) {
                        am.add(ActionMessages.GLOBAL_MESSAGE,
                            new ActionMessage(keys[i]));
                    }
                } else if (value instanceof ActionErrors) {
                    ActionMessages m = (ActionMessages) value;

                    am.add(m);
                } else if (value instanceof ActionMessages) {
                    am = (ActionMessages) value;
                } else {
                    throw new JspException(MESSAGES.getMessage(
                        "actionMessages.errors",
                        value.getClass().getName()));
                }
            } catch (JspException e) {
                throw e;
            } catch (Exception e) {
                // Unconvertible value: treat as no MESSAGES.
            }
        }

        return am;
    }

    /**
     * Render the specified error MESSAGES if there are any: each report is
     * wrapped in the prefix/suffix MESSAGES, the list in header/footer.
     *
     * @return the rendered list, or <code>null</code> when there are no
     *         errors to render
     */
    public String renderErrors(PageContext pageContext, String bundle,
        String locale, String name, String property, String header,
        String footer, String prefix, String suffix) throws JspException {
        ActionMessages errors;
        try {
            errors = getActionMessages(pageContext, name);
        } catch (JspException e) {
            TagUtils.getInstance().saveException(pageContext, e);
            throw e;
        }

        if ((errors == null) || errors.isEmpty()) {
            return null;
        }

        TagMessageResolver resolver = TagServices.service(pageContext,
            TagMessageResolver.class, TagMessageResolver::new);

        boolean headerPresent =
            resolver.present(pageContext, bundle, locale, header);

        boolean footerPresent =
            resolver.present(pageContext, bundle, locale, footer);

        boolean prefixPresent =
            resolver.present(pageContext, bundle, locale, prefix);

        boolean suffixPresent =
            resolver.present(pageContext, bundle, locale, suffix);

        // Render the error MESSAGES appropriately
        StringBuilder results = new StringBuilder();
        boolean headerDone = false;
        String message = null;
        Iterator<ActionMessage> reports =
            (property == null) ? errors.get() : errors.get(property);

        while (reports.hasNext()) {
            ActionMessage report = reports.next();

            if (!headerDone) {
                if (headerPresent) {
                    message = resolver.message(pageContext, bundle, locale,
                        header);
                    results.append(message);
                }

                headerDone = true;
            }

            if (prefixPresent) {
                message = resolver.message(pageContext, bundle, locale,
                    prefix);
                results.append(message);
            }

            if (report.isResource()) {
                message = resolver.message(pageContext, bundle, locale,
                    report.getKey(), report.getValues());
            } else {
                message = report.getKey();
            }

            if (message != null) {
                results.append(message);
            }

            if (suffixPresent) {
                message = resolver.message(pageContext, bundle, locale,
                    suffix);
                results.append(message);
            }
        }

        if (headerDone && footerPresent) {
            message = resolver.message(pageContext, bundle, locale, footer);
            results.append(message);
        }

        return results.toString();
    }
}
