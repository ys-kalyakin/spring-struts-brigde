package org.apache.struts.extras.actions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.MessageResources;

/**
 * An abstract <strong>Action</strong> that dispatches to a public method
 * that is named by the request parameter whose name is specified by the
 * <code>parameter</code> property of the corresponding ActionMapping.
 *
 * <p>This <strong>Action</strong> is useful for developers who prefer to
 * combine many similar actions into a single class.</p>
 *
 * <p>Ported to the spring-struts-bridge engine with the classic Struts 1.3
 * API (the weblegacy 1.5 implementation delegates to commons-chain, which
 * this engine replaces with the Spring MVC pipeline).</p>
 */
public abstract class DispatchAction extends BaseAction {

    private static final Logger log =
        LoggerFactory.getLogger(DispatchAction.class);

    /**
     * The message resources for this package.
     */
    protected static MessageResources messages =
        MessageResources.getMessageResources(
            "org.apache.struts.extras.actions.LocalStrings");

    /**
     * The set of Method objects we have introspected for this Action,
     * keyed by method name.
     */
    protected HashMap<String, Method> methods = new HashMap<>();

    /**
     * The Class instance of this DispatchAction class.
     */
    protected Class<?> clazz = this.getClass();

    /**
     * The request parameter that selects the method to dispatch.
     */
    protected String parameter = null;

    /**
     * Types of the method arguments.
     */
    protected Class<?>[] types =
        {
            ActionMapping.class,
            ActionForm.class,
            HttpServletRequest.class,
            HttpServletResponse.class
        };

    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response, dispatching to the public method that is named by the
     * <code>parameter</code> attribute of the given ActionMapping.
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        if (isCancelled(request)) {
            ActionForward af = cancelled(mapping, form, request, response);

            if (af != null) {
                return af;
            }
        }

        // Get the parameter. This could be overridden in subclasses.
        String parameter = getParameter(mapping, form, request, response);

        // Get the method's name. This could be overridden in subclasses.
        String name =
            getMethodName(mapping, form, request, response, parameter);

        // Prevent recursive calls
        if ("execute".equals(name) || "perform".equals(name)) {
            String message =
                messages.getMessage("dispatch.recursive", mapping.getPath());

            log.error(message);
            throw new ServletException(message);
        }

        // Invoke the named method, and return the result
        return dispatchMethod(mapping, form, request, response, name);
    }

    /**
     * Method which is dispatched to when the request is a cancel button
     * submit. Returns null so that the default processing (dispatch to the
     * selected method) continues.
     */
    protected ActionForward cancelled(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        return null;
    }

    /**
     * Method which is dispatched to when there is no value for the
     * <code>parameter</code> in the request. The default implementation
     * throws an exception; subclasses may override.
     */
    protected ActionForward unspecified(ActionMapping mapping,
        ActionForm form, HttpServletRequest request,
        HttpServletResponse response) throws Exception {
        String message =
            messages.getMessage("dispatch.parameter", mapping.getPath(),
                mapping.getParameter());

        log.error(message);

        throw new ServletException(message);
    }

    /**
     * Dispatch to the specified method.
     */
    protected ActionForward dispatchMethod(ActionMapping mapping,
        ActionForm form, HttpServletRequest request,
        HttpServletResponse response, String name) throws Exception {
        // Make sure we have a valid method name to call.
        // This may be null if the user hacks the query string.
        if (name == null) {
            return this.unspecified(mapping, form, request, response);
        }

        // Identify the method object to be dispatched to
        Method method = null;

        try {
            method = getMethod(name);
        } catch (NoSuchMethodException e) {
            String message =
                messages.getMessage("dispatch.method", mapping.getPath(),
                    name);

            log.error("{} '{}'", message, name);

            String userMsg =
                messages.getMessage("dispatch.method.user",
                    mapping.getPath());
            NoSuchMethodException e2 = new NoSuchMethodException(userMsg);
            e2.initCause(e);
            throw e2;
        }

        return dispatchMethod(mapping, form, request, response, name, method);
    }

    /**
     * Dispatch to the specified method, identified by the resolved
     * <code>Method</code> object.
     */
    protected ActionForward dispatchMethod(ActionMapping mapping,
        ActionForm form, HttpServletRequest request,
        HttpServletResponse response, String name, Method method)
        throws Exception {
        ActionForward forward = null;

        try {
            Object[] args = { mapping, form, request, response };

            forward = (ActionForward) method.invoke(this, args);
        } catch (ClassCastException e) {
            String message = messages.getMessage("dispatch.return", mapping
                .getPath(), name);

            log.error(message);
            throw e;
        } catch (IllegalAccessException e) {
            String message = messages.getMessage("dispatch.error", mapping
                .getPath(), name);

            log.error(message);
            throw e;
        } catch (InvocationTargetException e) {
            // Rethrow the target exception if possible so that the
            // exception handling machinery can deal with it
            Throwable t = e.getTargetException();

            if (t instanceof Exception) {
                throw ((Exception) t);
            }

            log.error(messages.getMessage("dispatch.error", mapping
                .getPath(), name));
            throw new ServletException(t);
        }

        // Return the returned ActionForward instance
        return (forward);
    }

    /**
     * Returns the parameter value: the name of the request parameter that
     * selects the dispatch method. May be overridden by subclasses (for
     * example {@link MappingDispatchAction}).
     */
    protected String getParameter(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        // Identify the request parameter containing the method name
        String parameter = mapping.getParameter();

        if (parameter == null) {
            String message =
                messages.getMessage("dispatch.handler", mapping.getPath());

            log.error(message);

            throw new ServletException(message);
        }

        return parameter;
    }

    /**
     * Returns the method name, given the name of the request parameter.
     */
    protected String getMethodName(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response,
        String parameter) throws Exception {
        // Identify the method name to be dispatched to.
        // dispatchMethod() will call unspecified() if name is null
        return request.getParameter(parameter);
    }

    /**
     * Introspect the current class to identify a method of the specified
     * name that accepts the same parameter types as the
     * <code>execute</code> method does.
     */
    protected Method getMethod(String name) throws NoSuchMethodException {
        synchronized (methods) {
            Method method = methods.get(name);

            if (method == null) {
                method = clazz.getMethod(name, types);
                methods.put(name, method);
            }

            return (method);
        }
    }

    /**
     * Returns the parameter (set by the mapping).
     */
    protected String getParameter(ActionMapping mapping) {
        return mapping.getParameter();
    }

    /**
     * Returns true if the current form's cancel button was pressed. Checks
     * the request attribute {@link org.apache.struts.Globals#CANCEL_KEY}.
     */
    protected boolean isCancelled(HttpServletRequest request) {
        return (request.getAttribute(
            org.apache.struts.Globals.CANCEL_KEY) != null);
    }
}
