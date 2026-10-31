package org.apache.struts.util;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.ActionServletWrapper;
import org.apache.struts.config.ActionConfig;
import org.apache.struts.config.FormBeanConfig;
import org.apache.struts.config.ForwardConfig;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.upload.FormFile;
import org.apache.struts.upload.MultipartRequestHandler;
import org.apache.struts.upload.MultipartRequestWrapper;
import org.apache.struts.upload.SpringMultipartRequestHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * General purpose utility methods related to processing a servlet request.
 *
 * <p>Ported from weblegacy/struts1 1.5.x (org.apache.struts.util.RequestUtils).</p>
 *
 * <p><strong>Population:</strong> form population is performed by
 * {@link FormPopulator} on top of the engine's own
 * {@link StrutsConversionService}; the populate path does not depend on
 * the static commons-beanutils <code>ConvertUtils</code> registry.</p>
 */
public class RequestUtils {

    /**
     * The <code>Log</code> instance for this class.
     */
    private static final Logger LOG =
        LoggerFactory.getLogger(RequestUtils.class);

    /**
     * Pattern matching 'class' access (CVE-2014-0114).
     */
    protected static final Pattern CLASS_ACCESS_PATTERN = Pattern
        .compile("(.*\\.|^|.*|\\[('|\"))class(\\.|('|\")]|\\[).*",
            Pattern.CASE_INSENSITIVE);

    /**
     * Create and return a fully-qualified class instance of the given name,
     * using the current thread's context class loader.
     */
    public static Object applicationInstance(String className)
        throws ClassNotFoundException, InstantiationException,
        IllegalAccessException, InvocationTargetException,
        NoSuchMethodException {
        return applicationInstance(className,
            Thread.currentThread().getContextClassLoader());
    }

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
     * request. Conversion is performed by the engine's own conversion service
     * ({@link StrutsConversionService}) with the semantics of the
     * commons-beanutils default registry (compatible with Struts 1).</p>
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
     *
     * <p>Multipart requests (<code>multipart/form-data</code>) are parsed
     * through a <code>MultipartRequestHandler</code> before population: text
     * elements populate like regular parameters, file elements fill
     * properties of the matching types. When a limit is exceeded, population
     * is skipped and the handler is attached to the form so the application
     * can inspect the request attributes.</p>
     *
     * <p>Ported from weblegacy/struts1 1.5.x.</p>
     */
    public static void populate(Object bean, String prefix, String suffix,
        HttpServletRequest request) throws ServletException {
        // Build a list of relevant request parameters from this request
        HashMap<String, Object> properties = new HashMap<>();

        // Iterator of parameter names
        Enumeration<String> names = null;

        // Map for multipart parameters
        Map<String, Object> multipartParameters = null;

        String contentType = request.getContentType();
        String method = request.getMethod();
        boolean isMultipart = false;

        if (bean instanceof ActionForm) {
            ((ActionForm) bean).setMultipartRequestHandler(null);
        }

        MultipartRequestHandler multipartHandler = null;
        if ((contentType != null)
            && (contentType.startsWith("multipart/form-data"))
            && (method.equalsIgnoreCase("POST"))) {
            // Get the ActionServletWrapper from the form bean
            ActionServletWrapper servlet;

            if (bean instanceof ActionForm) {
                servlet = ((ActionForm) bean).getServletWrapper();
            } else {
                throw new ServletException("bean that's supposed to be "
                    + "populated from a multipart request is not of type "
                    + "\"org.apache.struts.action.ActionForm\", but type "
                    + "\"" + bean.getClass().getName() + "\"");
            }

            // Obtain a MultipartRequestHandler: when the request has already
            // been resolved by a Spring MultipartResolver, adapt its parts
            // instead of parsing the body a second time
            if (request instanceof MultipartHttpServletRequest) {
                multipartHandler = new SpringMultipartRequestHandler();
            } else {
                multipartHandler = getMultipartHandler(request);
            }

            if (multipartHandler != null) {
                isMultipart = true;

                // Set servlet and mapping info
                servlet.setServletFor(multipartHandler);
                multipartHandler.setMapping((ActionMapping) request
                    .getAttribute(Globals.MAPPING_KEY));

                // Initialize multipart request class handler
                multipartHandler.handleRequest(request);

                // stop here if the maximum length has been exceeded
                Boolean maxLengthExceeded =
                    (Boolean) request.getAttribute(MultipartRequestHandler.ATTRIBUTE_MAX_LENGTH_EXCEEDED);

                if ((maxLengthExceeded != null)
                    && (maxLengthExceeded.booleanValue())) {
                    ((ActionForm) bean).setMultipartRequestHandler(multipartHandler);
                    return;
                }

                // retrieve form values and put into properties
                multipartParameters =
                    getAllParametersForMultipartRequest(request,
                        multipartHandler);
                names = Collections.enumeration(multipartParameters.keySet());
            }
        }

        if (!isMultipart) {
            names = request.getParameterNames();
        }

        while (names.hasMoreElements()) {
            String name = names.nextElement();
            String stripped = name;

            if (prefix != null) {
                if (!stripped.startsWith(prefix)) {
                    continue;
                }

                stripped = stripped.substring(prefix.length());
            }

            if (suffix != null) {
                if (!stripped.endsWith(suffix)) {
                    continue;
                }

                stripped =
                    stripped.substring(0, stripped.length() - suffix.length());
            }

            Object parameterValue = null;

            if (isMultipart) {
                parameterValue = multipartParameters.get(name);
                parameterValue = rationalizeMultipleFileProperty(bean, name,
                    parameterValue);
            } else {
                parameterValue = request.getParameterValues(name);
            }

            // CVE-2014-0114 security problem patch
            if (CLASS_ACCESS_PATTERN.matcher(stripped).matches()) {
                LOG.trace("ignore parameter: paramName={}", stripped);
                continue;
            }

            // Populate parameters, except "standard" struts attributes
            // such as 'org.apache.struts.action.CANCEL'
            if (!(stripped.startsWith("org.apache.struts."))) {
                properties.put(stripped, parameterValue);
            }
        }

        // Set the corresponding properties of our bean
        try {
            FormPopulator.populate(bean, properties);
        } catch (Exception e) {
            throw new ServletException(
                FormPopulator.POPULATE_ERROR_MESSAGE, e);
        } finally {
            if (multipartHandler != null) {
                // Set the multipart request handler for our ActionForm.
                ((ActionForm) bean).setMultipartRequestHandler(multipartHandler);
            }
        }
    }

    /**
     * If the specified property has multiple FormFile objects, but the form
     * bean type can only process a single FormFile object, then this method
     * attempts to take this situation into account.
     *
     * <p>Ported from weblegacy/struts1 1.5.x.</p>
     */
    private static Object rationalizeMultipleFileProperty(Object bean,
            String name, Object parameterValue) throws ServletException {

        if (!(parameterValue instanceof FormFile[])) {
            return parameterValue;
        }

        final FormFile[] formFileValue = (FormFile[]) parameterValue;
        final Class<?> propertyType =
            new BeanWrapperImpl(bean).getPropertyType(name);

        if (propertyType == null) {
            return parameterValue;
        }

        if (List.class.isAssignableFrom(propertyType)) {
            return Arrays.asList(formFileValue);
        }

        if (FormFile.class.isAssignableFrom(propertyType)) {
            switch (formFileValue.length) {
                case 0:
                    LOG.error("FormFile-parameter \"{}\" for FormBean "
                            + "\"{}\" has unexpected length of null!",
                            name, bean.getClass().getName());
                    return null;

                case 1:
                    return formFileValue[0];

                default:
                    LOG.error("FormFile-parameter \"{}\" for FormBean "
                            + "\"{}\" should have an Array or a List as "
                            + "method parameter!", name,
                            bean.getClass().getName());
                    return formFileValue[0];
            }
        }

        // no changes
        return parameterValue;
    }

    /**
     * Try to locate a multipart request handler for this request: the
     * mapping-specific handler stored under {@link Globals#MULTIPART_KEY}
     * first, then the module's controller configuration.
     *
     * <p>Ported from weblegacy/struts1 1.5.x.</p>
     */
    private static MultipartRequestHandler getMultipartHandler(
        HttpServletRequest request) throws ServletException {
        MultipartRequestHandler multipartHandler = null;
        String multipartClass =
            (String) request.getAttribute(Globals.MULTIPART_KEY);

        request.removeAttribute(Globals.MULTIPART_KEY);

        // Try to initialize the mapping specific request handler
        if (multipartClass != null) {
            try {
                multipartHandler =
                    (MultipartRequestHandler) applicationInstance(multipartClass);
            } catch (ClassNotFoundException cnfe) {
                LOG.error("MultipartRequestHandler class \"{}\" "
                    + "in mapping class not found, "
                    + "defaulting to global multipart class", multipartClass);
            } catch (InstantiationException ie) {
                LOG.error("InstantiationException when instantiating "
                    + "MultipartRequestHandler \"{}\", "
                    + "defaulting to global multipart class, exception: {}",
                    multipartClass, ie.getMessage());
            } catch (IllegalAccessException iae) {
                LOG.error("IllegalAccessException when instantiating "
                    + "MultipartRequestHandler \"{}\", "
                    + "defaulting to global multipart class, exception: {}",
                    multipartClass, iae.getMessage());
            } catch (ReflectiveOperationException re) {
                LOG.error("Cannot instantiate MultipartRequestHandler "
                    + "\"{}\", defaulting to global multipart class, "
                    + "exception: {}", multipartClass, re.getMessage());
            }

            if (multipartHandler != null) {
                return multipartHandler;
            }
        }

        ModuleConfig moduleConfig =
            ModuleUtils.getInstance().getModuleConfig(request);

        multipartClass = moduleConfig.getControllerConfig().getMultipartClass();

        // Try to initialize the global request handler
        if (multipartClass != null) {
            try {
                multipartHandler =
                    (MultipartRequestHandler) applicationInstance(multipartClass);
            } catch (ReflectiveOperationException re) {
                throw new ServletException("Cannot instantiate multipart "
                    + "class \"" + multipartClass + "\"", re);
            }
        }

        return multipartHandler;
    }

    /**
     * Create a <code>Map</code> containing all of the parameters supplied for
     * a multipart request, keyed by parameter name: text and file elements
     * from the multipart body plus query string parameters.
     *
     * <p>Ported from weblegacy/struts1 1.5.x.</p>
     */
    private static Map<String, Object> getAllParametersForMultipartRequest(
        HttpServletRequest request, MultipartRequestHandler multipartHandler) {

        final Map<String, Object> parameters =
                new HashMap<>(multipartHandler.getAllElements());

        if (request instanceof MultipartRequestWrapper) {
            request =
                (HttpServletRequest) ((MultipartRequestWrapper) request)
                .getRequest();

            parameters.putAll(request.getParameterMap());
        } else {
            LOG.debug("Gathering multipart parameters for unwrapped request");
        }

        return parameters;
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
