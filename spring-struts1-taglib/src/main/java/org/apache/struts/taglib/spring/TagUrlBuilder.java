package org.apache.struts.taglib.spring;

import org.apache.struts.taglib.TagUtils;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.jsp.PageContext;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.config.ForwardConfig;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.util.RequestUtils;
import org.apache.struts.util.ResponseUtils;

/**
 * Spring component of the tag libraries: builds the URLs of the
 * <code>html:form</code>, <code>html:link</code>, <code>html:img</code>,
 * <code>html:rewrite</code> and related tags — forward/action/page
 * resolution, module prefixes, servlet mappings, anchors and query
 * parameters. The application may override the <code>tagUrlBuilder</code>
 * bean to change URL construction for every tag.
 */
public class TagUrlBuilder {

    /**
     * The message resources of the tag libraries, used for exception texts.
     */
    protected static final org.apache.struts.util.MessageResources MESSAGES =
        org.apache.struts.util.MessageResources.getMessageResources(
            "org.apache.struts.taglib.LocalStrings");

    /**
     * Compute a set of query parameters that will be dynamically added to a
     * generated URL. The returned Map is keyed by parameter name, and the
     * values are either null (no value specified), a String (single value
     * specified), or a String[] array (multiple values specified).
     */
    public Map<String, Object> computeParameters(PageContext pageContext,
        String paramId, String paramName, String paramProperty,
        String paramScope, String name, String property, String scope,
        boolean transaction) throws jakarta.servlet.jsp.JspException {
        // Short circuit if no parameters are specified
        if ((paramId == null) && (name == null) && !transaction) {
            return (null);
        }

        // Locate the Map containing our multi-value parameters map
        Map<String, Object> map = null;

        try {
            if (name != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> mapObject =
                    (Map<String, Object>) lookupBean(pageContext,
                        name, property, scope);
                map = mapObject;
            }
        } catch (jakarta.servlet.jsp.JspException e) {
            TagUtils.getInstance().saveException(pageContext, e);
            throw e;
        }

        // Create a Map to contain our results from the multi-value parameters
        Map<String, Object> results = null;

        if (map != null) {
            results = new java.util.HashMap<>(map);
        } else {
            results = new java.util.HashMap<>();
        }

        // Add the single-value parameter (if any)
        if ((paramId != null) && (paramName != null)) {
            Object paramValue;

            try {
                paramValue = lookupBean(pageContext, paramName,
                    paramProperty, paramScope);
            } catch (jakarta.servlet.jsp.JspException e) {
                TagUtils.getInstance().saveException(pageContext, e);
                throw e;
            }

            if (paramValue != null) {
                String paramString;

                if (paramValue instanceof String) {
                    paramString = (String) paramValue;
                } else {
                    paramString = paramValue.toString();
                }

                Object mapValue = results.get(paramId);

                if (mapValue == null) {
                    results.put(paramId, paramString);
                } else if (mapValue instanceof String[]) {
                    String[] oldValues = (String[]) mapValue;
                    String[] newValues = Arrays.copyOf(oldValues,
                        oldValues.length + 1);
                    newValues[oldValues.length] = paramString;
                    results.put(paramId, newValues);
                } else {
                    String[] newValues = new String[2];

                    newValues[0] = mapValue.toString();
                    newValues[1] = paramString;
                    results.put(paramId, newValues);
                }
            }
        }

        // Add our transaction control token (if requested)
        if (transaction) {
            jakarta.servlet.http.HttpSession session =
                pageContext.getSession();
            String token = null;

            if (session != null) {
                token = (String) session.getAttribute(
                    Globals.TRANSACTION_TOKEN_KEY);
            }

            if (token != null) {
                results.put(org.apache.struts.taglib.html.Constants.TOKEN_KEY,
                    token);
            }
        }

        // Return the completed Map
        return (results);
    }

    private Object lookupBean(PageContext pageContext, String name,
        String property, String scope) throws jakarta.servlet.jsp.JspException {
        return TagServices.service(pageContext, TagFormLocator.class,
            TagFormLocator::new).lookup(pageContext, name, property, scope);
    }

    public String computeURL(PageContext pageContext, String forward,
        String href, String page, String action, String module,
        Map<String, ?> params, String anchor, boolean redirect)
        throws MalformedURLException {
        return this.computeURLWithCharEncoding(pageContext, forward, href,
            page, action, module, params, anchor, redirect, false);
    }

    /**
     * Compute a hyperlink URL based on the <code>forward</code>,
     * <code>href</code>, <code>action</code> or <code>page</code> parameter
     * that is not null. The returned URL will have already been passed to
     * <code>response.encodeURL()</code> for adding a session identifier.
     */
    public String computeURLWithCharEncoding(PageContext pageContext,
        String forward, String href, String page, String action,
        String module, Map<String, ?> params, String anchor, boolean redirect,
        boolean useLocalEncoding) throws MalformedURLException {
        return computeURLWithCharEncoding(pageContext, forward, href, page,
            action, module, params, anchor, redirect, true, useLocalEncoding);
    }

    public String computeURL(PageContext pageContext, String forward,
        String href, String page, String action, String module,
        Map<String, ?> params, String anchor, boolean redirect,
        boolean encodeSeparator) throws MalformedURLException {
        return computeURLWithCharEncoding(pageContext, forward, href, page,
            action, module, params, anchor, redirect, encodeSeparator, false);
    }

    /**
     * Compute a hyperlink URL based on the <code>forward</code>,
     * <code>href</code>, <code>action</code> or <code>page</code> parameter
     * that is not null. The returned URL will have already been passed to
     * <code>response.encodeURL()</code> for adding a session identifier.
     */
    public String computeURLWithCharEncoding(PageContext pageContext,
        String forward, String href, String page, String action,
        String module, Map<String, ?> params, String anchor, boolean redirect,
        boolean encodeSeparator, boolean useLocalEncoding)
        throws MalformedURLException {
        String charEncoding = "UTF-8";

        if (useLocalEncoding) {
            charEncoding = pageContext.getResponse().getCharacterEncoding();
        }

        // Validate that exactly one specifier was included
        int n = 0;

        if (forward != null) {
            n++;
        }

        if (href != null) {
            n++;
        }

        if (page != null) {
            n++;
        }

        if (action != null) {
            n++;
        }

        if (n != 1) {
            throw new MalformedURLException(
                MESSAGES.getMessage("computeURL.specifier"));
        }

        // Look up the module configuration for this request
        TagFormLocator locator = TagServices.service(pageContext,
            TagFormLocator.class, TagFormLocator::new);
        ModuleConfig moduleConfig = locator.getModuleConfig(module,
            pageContext);

        // Calculate the appropriate URL
        StringBuilder url = new StringBuilder();
        HttpServletRequest request =
            (HttpServletRequest) pageContext.getRequest();

        if (forward != null) {
            ForwardConfig forwardConfig =
                moduleConfig.findForwardConfig(forward);

            if (forwardConfig == null) {
                throw new MalformedURLException(
                    MESSAGES.getMessage("computeURL.forward", forward));
            }

            if (forwardConfig.getPath().startsWith("/")) {
                url.append(request.getContextPath());
                url.append(RequestUtils.forwardURL(request, forwardConfig,
                    moduleConfig));
            } else {
                url.append(forwardConfig.getPath());
            }
        } else if (href != null) {
            url.append(href);
        } else if (action != null) {
            ActionServlet servlet = (ActionServlet) pageContext
                .getServletContext().getAttribute(Globals.ACTION_SERVLET_KEY);
            String actionIdPath = RequestUtils.actionIdURL(action,
                moduleConfig, servlet);
            if (actionIdPath != null) {
                url.append(request.getContextPath());
                url.append(actionIdPath);
            } else {
                url.append(getActionMappingURL(action, module, pageContext,
                    false));
            }
        } else /* if (page != null) */ {
            url.append(request.getContextPath());
            url.append(this.pageURL(request, page, moduleConfig));
        }

        // Add anchor if requested (replacing any existing anchor)
        if (anchor != null) {
            String temp = url.toString();
            int hash = temp.indexOf('#');

            if (hash >= 0) {
                url.setLength(hash);
            }

            url.append('#');
            url.append(encodeURL(anchor, charEncoding));
        }

        // Add dynamic parameters if requested
        if ((params != null) && (params.size() > 0)) {
            // Save any existing anchor
            String temp = url.toString();
            int hash = temp.indexOf('#');

            if (hash >= 0) {
                anchor = temp.substring(hash + 1);
                url.setLength(hash);
                temp = url.toString();
            } else {
                anchor = null;
            }

            // Define the parameter separator
            String separator;

            if (redirect) {
                separator = "&";
            } else if (encodeSeparator) {
                separator = "&amp;";
            } else {
                separator = "&";
            }

            // Add the required request parameters
            boolean question = temp.indexOf('?') >= 0;

            for (Map.Entry<String, ?> entry : params.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (value == null) {
                    if (!question) {
                        url.append('?');
                        question = true;
                    } else {
                        url.append(separator);
                    }

                    url.append(encodeURL(key, charEncoding));
                    url.append('='); // Interpret null as "no value"
                } else if (value instanceof String) {
                    if (!question) {
                        url.append('?');
                        question = true;
                    } else {
                        url.append(separator);
                    }

                    url.append(encodeURL(key, charEncoding));
                    url.append('=');
                    url.append(encodeURL((String) value, charEncoding));
                } else if (value instanceof String[]) {
                    String[] values = (String[]) value;

                    for (int i = 0; i < values.length; i++) {
                        if (!question) {
                            url.append('?');
                            question = true;
                        } else {
                            url.append(separator);
                        }

                        url.append(encodeURL(key, charEncoding));
                        url.append('=');
                        url.append(encodeURL(values[i], charEncoding));
                    }
                } else /* Convert other objects to a string */ {
                    if (!question) {
                        url.append('?');
                        question = true;
                    } else {
                        url.append(separator);
                    }

                    url.append(encodeURL(key, charEncoding));
                    url.append('=');
                    url.append(encodeURL(value.toString(), charEncoding));
                }
            }

            // Re-add the saved anchor (if any)
            if (anchor != null) {
                url.append('#');
                url.append(encodeURL(anchor, charEncoding));
            }
        }

        // Perform URL rewriting to include our session ID (if any)
        // but only if url is not an external URL
        if ((href == null) && (pageContext.getSession() != null)) {
            HttpServletResponse response =
                (HttpServletResponse) pageContext.getResponse();

            if (redirect) {
                return (response.encodeRedirectURL(url.toString()));
            }

            return (response.encodeURL(url.toString()));
        }

        return (url.toString());
    }

    /**
     * Return the form action converted into an action mapping path: any
     * filename extension is removed and a leading slash is added when
     * missing.
     */
    public String getActionMappingName(String action) {
        String value = action;
        int question = action.indexOf("?");

        if (question >= 0) {
            value = value.substring(0, question);
        }

        int pound = value.indexOf("#");

        if (pound >= 0) {
            value = value.substring(0, pound);
        }

        int slash = value.lastIndexOf("/");
        int period = value.lastIndexOf(".");

        if ((period >= 0) && (period > slash)) {
            value = value.substring(0, period);
        }

        return value.startsWith("/") ? value : ("/" + value);
    }

    /**
     * Return the form action converted into a server-relative URL.
     */
    public String getActionMappingURL(String action,
        PageContext pageContext) {
        return getActionMappingURL(action, null, pageContext, false);
    }

    /**
     * Return the form action converted into a server-relative URL.
     */
    public String getActionMappingURL(String action, String module,
        PageContext pageContext, boolean contextRelative) {
        HttpServletRequest request =
            (HttpServletRequest) pageContext.getRequest();

        String contextPath = request.getContextPath();
        StringBuilder value = new StringBuilder();

        // Avoid setting two slashes at the beginning of an action:
        //  the length of contextPath should be more than 1
        //  in case of non-root context, otherwise length==1 (the slash)
        if (contextPath.length() > 1) {
            value.append(contextPath);
        }

        TagFormLocator locator = TagServices.service(pageContext,
            TagFormLocator.class, TagFormLocator::new);
        ModuleConfig moduleConfig = locator.getModuleConfig(module,
            pageContext);

        if ((moduleConfig != null) && (!contextRelative)) {
            value.append(moduleConfig.getPrefix());
        }

        // Use our servlet mapping, if one is specified
        String servletMapping = (String) pageContext.getAttribute(
            Globals.SERVLET_KEY, PageContext.APPLICATION_SCOPE);

        if (servletMapping != null) {
            String queryString = null;
            int question = action.indexOf("?");

            if (question >= 0) {
                queryString = action.substring(question);
            }

            String actionMapping = getActionMappingName(action);

            if (servletMapping.startsWith("*.")) {
                value.append(actionMapping);
                value.append(servletMapping.substring(1));
            } else if (servletMapping.endsWith("/*")) {
                value.append(servletMapping.substring(0,
                    servletMapping.length() - 2));
                value.append(actionMapping);
            } else if (servletMapping.equals("/")) {
                value.append(actionMapping);
            }

            if (queryString != null) {
                value.append(queryString);
            }
        // Otherwise, assume extension mapping is in use and extension is
        // already included in the action property
        } else {
            if (!action.startsWith("/")) {
                value.append("/");
            }

            value.append(action);
        }

        return value.toString();
    }

    /**
     * Return the context-relative URL that corresponds to the specified
     * <code>page</code> attribute value, calculated based on the
     * <code>pagePattern</code> property of the current module's
     * {@link ModuleConfig}.
     */
    public String pageURL(HttpServletRequest request, String page,
        ModuleConfig moduleConfig) {
        StringBuilder sb = new StringBuilder();
        String pagePattern =
            moduleConfig.getControllerConfig().getPagePattern();

        if (pagePattern == null) {
            sb.append(moduleConfig.getPrefix());
            sb.append(page);
        } else {
            boolean dollar = false;

            for (int i = 0; i < pagePattern.length(); i++) {
                char ch = pagePattern.charAt(i);

                if (dollar) {
                    switch (ch) {
                        case 'M':
                            sb.append(moduleConfig.getPrefix());

                            break;

                        case 'P':
                            sb.append(page);

                            break;

                        case '$':
                            sb.append('$');

                            break;

                        default:
                         // Silently swallow
                    }

                    dollar = false;

                    continue;
                } else if (ch == '$') {
                    dollar = true;
                } else {
                    sb.append(ch);
                }
            }
        }

        return sb.toString();
    }

    /**
     * URL-encode a string, using the response character encoding or UTF-8.
     */
    private String encodeURL(String url, String enc) {
        return ResponseUtils.encodeURL(url, enc);
    }
}
