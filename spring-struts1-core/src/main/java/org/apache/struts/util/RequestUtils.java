package org.apache.struts.util;

import java.lang.reflect.InvocationTargetException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.config.ActionConfig;
import org.apache.struts.config.FormBeanConfig;
import org.apache.struts.config.ForwardConfig;
import org.apache.struts.config.ModuleConfig;

/**
 * General purpose utility methods related to processing a servlet request.
 *
 * <p>Ported from weblegacy/struts1 1.5.x (org.apache.struts.util.RequestUtils).</p>
 *
 * <p><strong>ConvertUtils isolation:</strong> this class is the single access
 * point of the engine for commons-beanutils population and the static
 * <code>ConvertUtils</code> registry. No other engine class is allowed to
 * touch beanutils converters directly.</p>
 */
public class RequestUtils {

    /**
     * Create and return a fully-qualified class instance of the given name,
     * using the given class loader.
     */
    public static Object applicationInstance(String className, ClassLoader cl)
        throws ClassNotFoundException, InstantiationException,
        IllegalAccessException, InvocationTargetException,
        NoSuchMethodException {
        Class<?> clazz = (cl != null) ? cl.loadClass(className)
            : Class.forName(className);
        return clazz.getDeclaredConstructor().newInstance();
    }

    /**
     * <p>Populate the properties of the specified JavaBean from the specified
     * request. Conversion is performed by commons-beanutils with its default
     * converter registry (compatible semantics with Struts 1).</p>
     */
    public static void populate(Object bean, HttpServletRequest request)
        throws ServletException {
        populate(bean, null, null, request);
    }

    /**
     * <p>Populate the properties of the specified JavaBean from the specified
     * request, optionally restricting the processed parameter names to those
     * with the given prefix and/or suffix (which are stripped from the
     * property names).</p>
     */
    public static void populate(Object bean, String prefix, String suffix,
        HttpServletRequest request) throws ServletException {
        java.util.Map<String, String[]> filtered =
            new java.util.LinkedHashMap<>();
        for (java.util.Map.Entry<String, String[]> entry : request
            .getParameterMap().entrySet()) {
            String name = entry.getKey();
            if (prefix != null) {
                if (!name.startsWith(prefix)) {
                    continue;
                }
                name = name.substring(prefix.length());
            }
            if (suffix != null) {
                if (!name.endsWith(suffix)) {
                    continue;
                }
                name = name.substring(0,
                    name.length() - suffix.length());
            }
            filtered.put(name, entry.getValue());
        }
        try {
            BeanUtils.populate(bean, filtered);
        } catch (Exception e) {
            throw new ServletException("BeanUtils.populate", e);
        }
    }

    /**
     * Create and return an <code>ActionForm</code> instance appropriate to
     * the information in the specified <code>FormBeanConfig</code>.
     */
    public static ActionForm createActionForm(FormBeanConfig config,
        ActionServlet servlet) throws ServletException {
        try {
            return config.createActionForm();
        } catch (IllegalStateException e) {
            throw new ServletException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Create and return an <code>ActionForm</code> instance for the form
     * bean configured on the given mapping, storing it under the mapping's
     * scope and attribute name (used by the <code>html:form</code> tag to
     * render form beans that do not exist yet).
     */
    public static ActionForm createActionForm(HttpServletRequest request,
        org.apache.struts.action.ActionMapping mapping,
        ModuleConfig moduleConfig, ActionServlet servlet) {
        if (mapping == null) {
            return null;
        }
        String attribute = mapping.getAttribute();
        if (attribute == null) {
            return null;
        }
        String name = mapping.getName();
        FormBeanConfig config = moduleConfig.findFormBeanConfig(name);
        if (config == null) {
            return null;
        }
        ActionForm instance;
        try {
            instance = config.createActionForm();
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException(e.getMessage(), e.getCause());
        }
        if (instance == null) {
            return null;
        }
        if ("request".equals(mapping.getScope())) {
            request.setAttribute(attribute, instance);
        } else {
            jakarta.servlet.http.HttpSession session = request.getSession();
            session.setAttribute(attribute, instance);
        }
        return instance;
    }

    /**
     * Look up and return current user locale, based on the specified
     * parameters: the session attribute named by <code>locale</code> (or the
     * standard key when null), falling back to the request locale.
     */
    public static java.util.Locale getUserLocale(HttpServletRequest request,
        String locale) {
        java.util.Locale userLocale = null;
        jakarta.servlet.http.HttpSession session = request.getSession(false);

        if (locale == null) {
            locale = org.apache.struts.Globals.LOCALE_KEY;
        }

        if (session != null) {
            userLocale = (java.util.Locale) session.getAttribute(locale);
        }

        if (userLocale == null) {
            userLocale = request.getLocale();
        }

        return userLocale;
    }

    /**
     * Compute a context-relative path for the specified forward, applying
     * the module prefix (or the module named by the forward) and the
     * configured forward pattern.
     */
    public static String forwardURL(HttpServletRequest request,
        ForwardConfig forward) {
        return forwardURL(request, forward, null);
    }

    /**
     * Compute a context-relative path for the specified forward, in the
     * given module.
     */
    public static String forwardURL(HttpServletRequest request,
        ForwardConfig forward, ModuleConfig moduleConfig) {
        if (moduleConfig == null) {
            moduleConfig = ModuleUtils.getInstance().getModuleConfig(request);
        }

        String path = forward.getPath();
        String prefix = moduleConfig.getPrefix();

        if (forward.getModule() != null) {
            prefix = forward.getModule();
            if ("/".equals(prefix)) {
                prefix = "";
            }
        }

        StringBuilder sb = new StringBuilder();
        String forwardPattern =
            moduleConfig.getControllerConfig().getForwardPattern();

        if (forwardPattern == null) {
            sb.append(prefix);
            if (!path.startsWith("/")) {
                sb.append("/");
            }
            sb.append(path);
        } else {
            boolean dollar = false;

            for (int i = 0; i < forwardPattern.length(); i++) {
                char ch = forwardPattern.charAt(i);

                if (dollar) {
                    switch (ch) {
                    case 'M':
                        sb.append(prefix);
                        break;
                    case 'P':
                        if (!path.startsWith("/")) {
                            sb.append("/");
                        }
                        sb.append(path);
                        break;
                    case '$':
                        sb.append('$');
                        break;
                    default:
                        break;
                    }
                    dollar = false;
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
     * Return the URL representing the current request.
     */
    public static java.net.URL requestURL(HttpServletRequest request)
        throws java.net.MalformedURLException {
        StringBuilder url = requestToServerUriStringBuilder(request);
        return new java.net.URL(url.toString());
    }

    /**
     * Return the string representing the scheme, server, and port number of
     * the current request, including the request URI.
     */
    public static StringBuilder requestToServerUriStringBuilder(
        HttpServletRequest request) {
        return createServerUriStringBuilder(request.getScheme(),
            request.getServerName(), request.getServerPort(),
            request.getRequestURI());
    }

    /**
     * Return a StringBuilder representing the scheme, server, and port
     * number of the current request.
     */
    public static StringBuilder requestToServerStringBuilder(
        HttpServletRequest request) {
        return createServerStringBuilder(request.getScheme(),
            request.getServerName(), request.getServerPort());
    }

    /**
     * Return a StringBuilder representing the scheme, server, and port
     * number.
     */
    public static StringBuilder createServerStringBuilder(String scheme,
        String server, int port) {
        StringBuilder url = new StringBuilder();

        if (port < 0) {
            port = 80;
        }

        url.append(scheme);
        url.append("://");
        url.append(server);
        if ((scheme.equals("http") && (port != 80))
            || (scheme.equals("https") && (port != 443))) {
            url.append(':');
            url.append(port);
        }

        return url;
    }

    /**
     * Return a StringBuilder representing the scheme, server, and port
     * number of the server, appended with the given URI.
     */
    public static StringBuilder createServerUriStringBuilder(String scheme,
        String server, int port, String uri) {
        StringBuilder serverUri = createServerStringBuilder(scheme, server,
            port);
        serverUri.append(uri);
        return serverUri;
    }

    /**
     * Create and return a fully-qualified class of the given name.
     */
    public static Class<?> applicationClass(String className)
        throws ClassNotFoundException {
        return Class.forName(className);
    }

    /**
     * Returns the true path of the destination action if the specified
     * forward is an action-aliased URL. This method version forms the URL
     * based on the module of the forward.
     */
    public static String actionIdURL(ForwardConfig forward,
        HttpServletRequest request, ActionServlet servlet) {
        ModuleConfig moduleConfig = null;
        if (forward.getModule() != null) {
            String prefix = forward.getModule();
            moduleConfig = ModuleUtils.getInstance().getModuleConfig(prefix,
                servlet.getServletContext());
        } else {
            moduleConfig = ModuleUtils.getInstance().getModuleConfig(request);
        }
        return actionIdURL(forward.getPath(), moduleConfig, servlet);
    }

    /**
     * Returns the true path of the destination action if the specified path
     * is an action-aliased URL (the alias of an action's
     * <code>actionId</code>), or <code>null</code> otherwise.
     */
    public static String actionIdURL(String originalPath,
        ModuleConfig moduleConfig, ActionServlet servlet) {
        if (originalPath.startsWith("http") || originalPath.startsWith("/")) {
            return null;
        }

        // Split the forward path into the resource and query string;
        // it is possible a forward (or redirect) has added parameters.
        String actionId = null;
        String qs = null;
        int qpos = originalPath.indexOf("?");
        if (qpos == -1) {
            actionId = originalPath;
        } else {
            actionId = originalPath.substring(0, qpos);
            qs = originalPath.substring(qpos);
        }

        // Find the action of the given actionId
        ActionConfig actionConfig = moduleConfig.findActionConfigId(actionId);
        if (actionConfig == null) {
            return null;
        }

        String path = actionConfig.getPath();
        String mapping = getServletMapping(servlet);
        StringBuilder actionIdPath = new StringBuilder();

        // Form the path based on the servlet mapping pattern
        if (mapping.startsWith("*")) {
            actionIdPath.append(path);
            actionIdPath.append(mapping.substring(1));
        } else if (mapping.startsWith("/")) { // implied ends with a *
            mapping = mapping.substring(0, mapping.length() - 1);
            if (mapping.endsWith("/") && path.startsWith("/")) {
                actionIdPath.append(mapping);
                actionIdPath.append(path.substring(1));
            } else {
                actionIdPath.append(mapping);
                actionIdPath.append(path);
            }
        } else {
            actionIdPath.append(path);
        }

        // Lastly add any query parameters (the ? is part of the query string)
        if (qs != null) {
            actionIdPath.append(qs);
        }

        return actionIdPath.toString();
    }

    /**
     * Returns the servlet mapping the engine was configured with (the value
     * exposed under {@link org.apache.struts.Globals#SERVLET_KEY}).
     */
    public static String getServletMapping(ActionServlet servlet) {
        return (String) servlet.getServletContext()
            .getAttribute(org.apache.struts.Globals.SERVLET_KEY);
    }
}
