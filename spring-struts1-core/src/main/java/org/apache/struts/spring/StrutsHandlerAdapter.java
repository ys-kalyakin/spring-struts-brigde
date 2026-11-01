package org.apache.struts.spring;

import java.util.List;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.context.ServletContextAware;

import org.apache.struts.Globals;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;
import org.apache.struts.action.ExceptionHandler;
import org.apache.struts.config.ExceptionConfig;
import org.apache.struts.config.FormBeanConfig;
import org.apache.struts.config.ForwardConfig;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.util.ModuleUtils;
import org.apache.struts.util.RequestUtils;
import org.apache.struts.util.SessionFormMutexRegistry;
import org.apache.struts.upload.MultipartRequestWrapper;

import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * Spring MVC handler adapter that runs the classic Struts 1 request
 * pipeline for an {@link ActionMapping} handler: locale/content/no-cache
 * processing, form creation and population (populate), validation
 * (validate), action execution (execute) and forward processing — mirroring
 * the semantics of the classic <code>RequestProcessor</code>, including
 * declarative exception handling.
 */
public class StrutsHandlerAdapter implements HandlerAdapter,
    ServletContextAware {

    private final ActionServlet actionServlet;

    private final StrutsInstanceResolver instanceResolver;

    private ServletContext servletContext;

    /**
     * The definition-forward resolvers resolved from the application
     * context once on first use; an empty list is cached too (bean
     * definitions registered later are not picked up).
     */
    private volatile List<DefinitionForwardResolver> definitionResolvers;

    public StrutsHandlerAdapter(ActionServlet actionServlet,
        StrutsInstanceResolver instanceResolver) {
        this.actionServlet = actionServlet;
        this.instanceResolver = instanceResolver;
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public boolean supports(Object handler) {
        return handler instanceof ActionMapping;
    }

    @Override
    public ModelAndView handle(HttpServletRequest request,
        HttpServletResponse response, Object handler) throws Exception {
        ActionMapping mapping = (ActionMapping) handler;
        // The handler mapping has already selected the module by the request
        // path prefix; fall back to the mapping's own module configuration.
        ModuleConfig moduleConfig =
            ModuleUtils.getInstance().getModuleConfig(request);
        if (moduleConfig == null) {
            moduleConfig = mapping.getModuleConfig();
        }
        if (moduleConfig == null) {
            moduleConfig = actionServlet.getModuleConfig();
        }

        request = processMultipart(request);
        request.setAttribute(Globals.MAPPING_KEY, mapping);
        request.setAttribute(Globals.MODULE_KEY, moduleConfig);
        // Classic postback support: html:form without an action submits to
        // the URI of the current action.
        if (request.getAttribute(Globals.ORIGINAL_URI_KEY) == null) {
            request.setAttribute(Globals.ORIGINAL_URI_KEY,
                request.getServletPath());
        }

        processContent(response, moduleConfig);
        processNoCache(response, moduleConfig);
        processLocale(request, moduleConfig);

        ActionForm form = null;
        try {
            form = processActionForm(request, mapping, moduleConfig);
            processPopulate(request, mapping, form);

            if (processValidate(request, response, mapping, form)) {
                return null;
            }

            if (mapping.getForward() != null) {
                processForwardConfig(request, response,
                    new ActionForward(mapping.getForward()));
                return null;
            }
            if (mapping.getInclude() != null) {
                doInclude(mapping.getInclude(), request, response);
                return null;
            }

            Action action = instanceResolver.resolveAction(mapping);
            action.setServlet(actionServlet);
            ActionForward forward = action.execute(mapping, form, request,
                response);
            processForwardConfig(request, response, forward);
        } catch (Exception e) {
            processException(request, response, e, mapping, form);
        } finally {
            // Multipart cleanup: temp files of the parsed request are
            // destroyed once the response has been fully produced (the
            // rollback() of the classic pipeline on validation failures is
            // kept too; both are idempotent).
            cleanupMultipart(form);
        }
        return null;
    }

    /**
     * Wrap a multipart POST request into a <code>MultipartRequestWrapper</code>
     * so that text elements parsed from the body become visible as request
     * parameters; non-multipart requests are returned unchanged.
     *
     * <p>Ported from the classic <code>RequestProcessor.processMultipart</code>.</p>
     */
    protected HttpServletRequest processMultipart(HttpServletRequest request) {
        if (request instanceof MultipartHttpServletRequest) {
            // already resolved by a user-configured Spring MultipartResolver
            return request;
        }
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return request;
        }

        String contentType = request.getContentType();

        if ((contentType != null)
            && contentType.startsWith("multipart/form-data")) {
            return new MultipartRequestWrapper(request);
        }
        return request;
    }

    /**
     * Destroy the temporary resources of the form's multipart handler, if
     * any (classic <code>MultipartRequestHandler.finish()</code>).
     */
    protected void cleanupMultipart(ActionForm form) {
        if (form != null && form.getMultipartRequestHandler() != null) {
            form.getMultipartRequestHandler().finish();
        }
    }

    /**
     * Set the content type configured for this application on the response.
     */
    protected void processContent(HttpServletResponse response,
        ModuleConfig moduleConfig) {
        String contentType = moduleConfig.getControllerConfig()
            .getContentType();
        if (contentType != null) {
            response.setContentType(contentType);
        }
    }

    /**
     * Set the no-cache HTTP headers if configured for this application.
     */
    protected void processNoCache(HttpServletResponse response,
        ModuleConfig moduleConfig) {
        if (moduleConfig.getControllerConfig().getNocache()) {
            response.setHeader("Pragma", "No-cache");
            response.setHeader("Cache-Control", "no-cache,no-store,max-age=0");
            response.setDateHeader("Expires", 1);
        }
    }

    /**
     * Select and store the Locale for this request into the user's session
     * if not already present, when the application is configured to do so
     * (the classic <code>locale</code> controller setting, on by default).
     */
    protected void processLocale(HttpServletRequest request,
        ModuleConfig moduleConfig) {
        if (!moduleConfig.getControllerConfig().getLocale()) {
            return;
        }
        HttpSession session = request.getSession();
        if (session.getAttribute(Globals.LOCALE_KEY) == null
            && request.getLocale() != null) {
            session.setAttribute(Globals.LOCALE_KEY, request.getLocale());
        }
    }

    /**
     * Create (or reuse, for session scope) the form bean associated with
     * this mapping and store it under the configured scope.
     */
    protected ActionForm processActionForm(HttpServletRequest request,
        ActionMapping mapping, ModuleConfig moduleConfig) throws Exception {
        if (mapping.getName() == null) {
            return null;
        }
        FormBeanConfig config = moduleConfig
            .findFormBeanConfig(mapping.getName());
        if (config == null) {
            throw new ServletException("No form bean found under name "
                + mapping.getName() + " for path " + mapping.getPath());
        }
        String attribute = (mapping.getAttribute() != null)
            ? mapping.getAttribute()
            : mapping.getName();

        if ("request".equals(mapping.getScope())) {
            ActionForm instance = instanceResolver.resolveForm(config);
            request.setAttribute(attribute, instance);
            return instance;
        }
        // CVE-2016-1181: acquiring or creating the session instance is part
        // of the serialized form lifecycle window
        HttpSession session = request.getSession();
        synchronized (SessionFormMutexRegistry.mutexFor(session)) {
            ActionForm instance = (ActionForm) session.getAttribute(attribute);
            if (instance == null) {
                instance = instanceResolver.resolveForm(config);
                session.setAttribute(attribute, instance);
            }
            return instance;
        }
    }

    /**
     * Populate the properties of the specified form bean from the request
     * parameters, after resetting them. Multipart requests are parsed by the
     * configured <code>MultipartRequestHandler</code> before population.
     */
    protected void processPopulate(HttpServletRequest request,
        ActionMapping mapping, ActionForm form) throws Exception {
        if (form == null) {
            return;
        }

        form.setServlet(actionServlet);

        if (mapping.getMultipartClass() != null) {
            request.setAttribute(Globals.MULTIPART_KEY,
                mapping.getMultipartClass());
        }

        if ("request".equals(mapping.getScope())) {
            form.reset(mapping, request);
            RequestUtils.populate(form, mapping.getPrefix(),
                mapping.getSuffix(), request);
        } else {
            // CVE-2016-1181: reset and populate (including the multipart
            // parse and handler attachment) run under the session mutex, so
            // a concurrent request of the same session cannot interleave
            // them on the shared instance
            synchronized (SessionFormMutexRegistry.mutexFor(
                request.getSession())) {
                form.reset(mapping, request);
                RequestUtils.populate(form, mapping.getPrefix(),
                    mapping.getSuffix(), request);
            }
        }

        // Set the cancellation request attribute if appropriate
        if ((request.getParameter(Globals.CANCEL_PROPERTY) != null)
            || (request.getParameter(Globals.CANCEL_PROPERTY_X) != null)) {
            request.setAttribute(Globals.CANCEL_KEY, Boolean.TRUE);
        }
    }

    /**
     * Call the <code>validate()</code> method of the form bean when the
     * mapping asks for validation. On errors, the errors are stored in the
     * request and control is forwarded to the input page; the caller must
     * stop processing then.
     *
     * @return <code>true</code> if the response has been handled (forward to
     *         the input page happened or an error was reported)
     */
    protected boolean processValidate(HttpServletRequest request,
        HttpServletResponse response, ActionMapping mapping, ActionForm form)
        throws Exception {
        if (!mapping.getValidate() || form == null) {
            return false;
        }
        ActionErrors errors = form.validate(mapping, request);
        if (errors == null || errors.isEmpty()) {
            return false;
        }

        // Special handling for multipart request
        if (form.getMultipartRequestHandler() != null) {
            form.getMultipartRequestHandler().rollback();
        }

        request.setAttribute(Globals.ERROR_KEY, errors);

        if (mapping.getInput() == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Validation error, but no input attribute of path "
                    + mapping.getPath());
            return true;
        }
        processForwardConfig(request, response, mapping.getInputForward());
        return true;
    }

    /**
     * Forward or redirect to the resource described by the forward
     * configuration, if any. Module-relative paths are expanded with the
     * module prefix (classic <code>processForwardConfig</code> semantics).
     * Before ordinary dispatch, registered
     * {@link DefinitionForwardResolver} beans get the chance to resolve the
     * path as a named view definition (the Spring-native tiles hook).
     */
    protected void processForwardConfig(HttpServletRequest request,
        HttpServletResponse response, ForwardConfig forward) throws Exception {
        if (forward == null) {
            return;
        }

        if (resolveDefinitionForward(forward.getPath(), request, response)) {
            return;
        }

        ModuleConfig moduleConfig =
            ModuleUtils.getInstance().getModuleConfig(request);
        String forwardPath = forward.getPath();
        String uri;

        // Paths not starting with / are passed through without any
        // processing (they are already context-relative or absolute URLs)
        if (forwardPath.startsWith("/")) {
            uri = RequestUtils.forwardURL(request, forward, moduleConfig);
        } else {
            uri = forwardPath;
        }

        if (forward.getRedirect()) {
            // Only prepend the context path for context-relative URIs
            if (uri.startsWith("/")) {
                uri = request.getContextPath() + uri;
            }
            response.sendRedirect(response.encodeRedirectURL(uri));
            return;
        }
        doForward(uri, request, response);
    }

    /**
     * Offer the raw forward path to the registered
     * {@link DefinitionForwardResolver} beans (if any). The resolver list
     * is resolved from the application context once (an empty list is
     * cached as well) instead of scanning bean definitions on every
     * forward.
     *
     * @return <code>true</code> if a resolver produced the response
     */
    protected boolean resolveDefinitionForward(String path,
        HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        List<DefinitionForwardResolver> resolvers = this.definitionResolvers;
        if (resolvers == null) {
            ApplicationContext context = actionServlet
                .getApplicationContext();
            if (context == null) {
                return false;
            }
            resolvers = List.copyOf(context
                .getBeansOfType(DefinitionForwardResolver.class).values());
            this.definitionResolvers = resolvers;
        }
        for (DefinitionForwardResolver resolver : resolvers) {
            if (resolver.resolveForward(path, request, response)) {
                return true;
            }
        }
        return false;
    }

    protected void doForward(String path, HttpServletRequest request,
        HttpServletResponse response) throws Exception {
        RequestDispatcher dispatcher = servletContext
            .getRequestDispatcher(path);
        if (dispatcher == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                "Cannot get dispatcher for path " + path);
            return;
        }
        dispatcher.forward(request, response);
    }

    protected void doInclude(String path, HttpServletRequest request,
        HttpServletResponse response) throws Exception {
        RequestDispatcher dispatcher = servletContext
            .getRequestDispatcher(path);
        if (dispatcher == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND,
                "Cannot get dispatcher for path " + path);
            return;
        }
        dispatcher.include(request, response);
    }

    /**
     * Handle the given exception: look up a matching
     * <code>ExceptionConfig</code> on the mapping (local first) or the
     * module (global) and delegate to its handler; rethrow into the
     * container when no configuration matches.
     */
    protected void processException(HttpServletRequest request,
        HttpServletResponse response, Exception exception,
        ActionMapping mapping, ActionForm form) throws Exception {
        ExceptionConfig config = (mapping != null)
            ? mapping.findException(exception.getClass())
            : null;
        if (config == null) {
            if (exception instanceof ServletException) {
                throw (ServletException) exception;
            }
            throw new ServletException(exception);
        }
        ExceptionHandler handler = (ExceptionHandler) RequestUtils
            .applicationInstance(config.getHandler(),
                Thread.currentThread().getContextClassLoader());
        ActionForward forward = handler.execute(exception, config, mapping,
            form, request, response);
        if (forward != null) {
            processForwardConfig(request, response, forward);
        }
    }
}
