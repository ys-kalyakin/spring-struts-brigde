package org.apache.struts.action;

import java.io.IOException;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.struts.Globals;
import org.apache.struts.config.ExceptionConfig;
import org.apache.struts.util.ModuleException;

/**
 * An <strong>ExceptionHandler</strong> is configured in the Struts
 * configuration file to handle a specific type of exception thrown by an
 * <code>Action.execute</code> method. The default implementation forwards to
 * the configured path and stores the exception and its message in the
 * request.
 *
 * <p>Ported from weblegacy/struts1 1.5.x (org.apache.struts.action.ExceptionHandler).</p>
 */
public class ExceptionHandler {

    private static final Logger log =
        LoggerFactory.getLogger(ExceptionHandler.class);

    /**
     * The name of a configuration property which can be set to specify an
     * alternative path which should be used when the response has already
     * been committed.
     */
    public static final String INCLUDE_PATH = "INCLUDE_PATH";

    /**
     * The name of a configuration property which indicates that Struts
     * should do nothing if the response has already been committed.
     */
    public static final String SILENT_IF_COMMITTED = "SILENT_IF_COMMITTED";

    /**
     * Handle the exception. Return the <code>ActionForward</code> instance
     * (if any) the handler wants the caller to forward to.
     */
    public ActionForward execute(Exception ex, ExceptionConfig ae,
        ActionMapping mapping, ActionForm formInstance,
        HttpServletRequest request, HttpServletResponse response)
        throws ServletException {
        log.debug("ExceptionHandler executing for exception", ex);

        ActionForward forward;
        ActionMessage error;
        String property;

        // Build the forward from the exception mapping if it exists
        // or from the form input
        if (ae.getPath() != null) {
            forward = new ActionForward(ae.getPath());
        } else {
            forward = (ActionForward) mapping.getInputForward();
        }

        // Figure out the error
        if (ex instanceof ModuleException) {
            error = ((ModuleException) ex).getActionMessage();
            property = ((ModuleException) ex).getProperty();
        } else if (ae.getKey() != null) {
            error = new ActionMessage(ae.getKey(), ex.getMessage());
            property = error.getKey();
        } else {
            error = null;
            property = null;
        }

        logException(ex);

        // Store the exception and the error message in the request
        request.setAttribute(Globals.EXCEPTION_KEY, ex);
        storeException(request, property, error, forward, ae.getScope());

        if (!response.isCommitted()) {
            return forward;
        }

        log.debug("Response is already committed, attempting include");
        if (!silent(ae)) {
            handleCommittedResponse(ex, ae, mapping, formInstance, request,
                response, forward);
        }
        return null;
    }

    /**
     * Log the exception with a standard message.
     */
    protected void logException(Exception ex) {
        log.error("Exception caught by Struts: {}", ex.getMessage(), ex);
    }

    /**
     * Store the given exception message in the given request (or session)
     * scope as an <code>ActionErrors</code> under the standard key.
     */
    protected void storeException(HttpServletRequest request,
        String property, ActionMessage error, ActionForward forward,
        String scope) {
        if (error == null) {
            return;
        }
        ActionErrors errors = new ActionErrors();
        errors.add((property != null) ? property
            : ActionMessages.GLOBAL_MESSAGE, error);
        if ("session".equals(scope)) {
            HttpSession session = request.getSession();
            session.setAttribute(Globals.ERROR_KEY, errors);
        } else {
            request.setAttribute(Globals.ERROR_KEY, errors);
        }
    }

    /**
     * Attempt to give useful information when the response has already been
     * committed: include the configured or forwarded path, or write the
     * exception to the response directly.
     */
    protected void handleCommittedResponse(Exception ex,
        ExceptionConfig config, ActionMapping mapping, ActionForm formInstance,
        HttpServletRequest request, HttpServletResponse response,
        ActionForward actionForward) {
        String includePath = determineIncludePath(config, actionForward);

        if (includePath != null && includePath.startsWith("/")) {
            RequestDispatcher dispatcher =
                request.getRequestDispatcher(includePath);
            if (dispatcher != null) {
                try {
                    dispatcher.include(request, response);
                    return;
                } catch (IOException | ServletException e) {
                    log.error("Include of path {} failed", includePath, e);
                }
            }
        }

        try {
            response.getWriter().println("Unexpected error: " + ex);
        } catch (IOException e) {
            log.error("Error reporting exception", e);
        }
    }

    /**
     * Return a path to include when the response was committed: the
     * INCLUDE_PATH property if set, otherwise the forward's path.
     */
    protected String determineIncludePath(ExceptionConfig config,
        ActionForward actionForward) {
        String includePath = config.getProperty(INCLUDE_PATH);
        if (includePath == null && actionForward != null) {
            includePath = actionForward.getPath();
        }
        return includePath;
    }

    private boolean silent(ExceptionConfig config) {
        return Boolean.parseBoolean(config.getProperty(SILENT_IF_COMMITTED));
    }
}
