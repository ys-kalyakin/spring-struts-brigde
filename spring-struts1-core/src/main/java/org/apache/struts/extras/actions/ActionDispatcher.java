package org.apache.struts.extras.actions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.MessageResources;

/**
 * <p>Action <i>helper</i> class that dispatches to a public method in an
 * Action. This class is provided as an alternative mechanism to using
 * DispatchAction and its various flavours and means <i>dispatch</i>
 * behaviour can be easily implemented into any <code>Action</code> without
 * having to inherit from a particular super <code>Action</code>.</p>
 *
 * <p>To implement <i>dispatch</i> behaviour in an <code>Action</code> class,
 * create your custom Action as follows, along with the methods you require
 * (and optionally "cancelled" and "unspecified" methods):</p>
 *
 * <pre>
 *   public class MyCustomAction extends Action {
 *
 *       protected ActionDispatcher dispatcher
 *                = new ActionDispatcher(this, ActionDispatcher.MAPPING_FLAVOR);
 *
 *       public ActionForward execute(ActionMapping mapping,
 *                                    ActionForm form,
 *                                    HttpServletRequest request,
 *                                    HttpServletResponse response)
 *                           throws Exception {
 *           return dispatcher.execute(mapping, form, request, response);
 *       }
 *   }
 * </pre>
 *
 * <p>It provides three flavours of determing the name of the method:</p>
 *
 * <ul>
 *
 * <li><strong>{@link #DEFAULT_FLAVOR}</strong> - uses the parameter
 * specified in the struts-config.xml to get the method name from the Request
 * (equivalent to <code>DispatchAction</code> <b>except</b> uses "method" as
 * a default if the <code>parameter</code> is not specified in the
 * struts-config.xml).</li>
 *
 * <li><strong>{@link #DISPATCH_FLAVOR}</strong> - uses the parameter
 * specified in the struts-config.xml to get the method name from the Request
 * (equivalent to <code>DispatchAction</code>).</li>
 *
 * <li><strong>{@link #MAPPING_FLAVOR}</strong> - uses the parameter
 * specified in the struts-config.xml as the method name (equivalent to
 * <code>MappingDispatchAction</code>).</li>
 *
 * </ul>
 *
 * <p>Ported to the spring-struts-bridge engine with the classic Struts 1.3
 * API.</p>
 *
 * @since Struts 1.2.7
 */
public class ActionDispatcher {

    /**
     * Indicates "default" dispatch flavor.
     */
    public static final int DEFAULT_FLAVOR = 0;

    /**
     * Indicates "mapping" dispatch flavor.
     */
    public static final int MAPPING_FLAVOR = 1;

    /**
     * Indicates flavor compatible with DispatchAction.
     */
    public static final int DISPATCH_FLAVOR = 2;

    private static final Logger log =
        LoggerFactory.getLogger(ActionDispatcher.class);

    /**
     * The message resources for this package.
     */
    protected static MessageResources messages =
        MessageResources.getMessageResources(
            "org.apache.struts.extras.actions.LocalStrings");

    /**
     * The associated Action to dispatch to.
     */
    protected Action actionInstance;

    /**
     * Indicates dispatch <i>flavor</i>.
     */
    protected int flavor;

    /**
     * The Class instance of this <code>DispatchAction</code> class.
     */
    protected Class<? extends Action> clazz;

    /**
     * The set of Method objects we have introspected for this class, keyed
     * by method name. This collection is populated as different methods are
     * called, so that introspection needs to occur only once per method
     * name. A concurrent map: dispatch lookups do not serialize concurrent
     * requests sharing the dispatcher.
     */
    protected Map<String, Method> methods = new ConcurrentHashMap<>();

    /**
     * The set of argument type classes for the reflected method call. These
     * are the same for all calls, so calculate them only once.
     */
    protected Class<?>[] types =
        {
        ActionMapping.class, ActionForm.class, HttpServletRequest.class,
        HttpServletResponse.class
        };

    /**
     * Construct an instance of this class from the supplied parameters.
     *
     * @param actionInstance The action instance to be invoked.
     */
    public ActionDispatcher(Action actionInstance) {
        this(actionInstance, DEFAULT_FLAVOR);
    }

    /**
     * Construct an instance of this class from the supplied parameters.
     *
     * @param actionInstance The action instance to be invoked.
     * @param flavor         The flavor of dispatch to use.
     */
    public ActionDispatcher(Action actionInstance, int flavor) {
        this.actionInstance = actionInstance;
        this.flavor = flavor;

        clazz = actionInstance.getClass();
    }

    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response (or forward to another web component that will create it).
     * Return an <code>ActionForward</code> instance describing where and how
     * control should be forwarded, or <code>null</code> if the response has
     * already been completed.
     */
    public ActionForward execute(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        // Process "cancelled"
        if (isCancelled(request)) {
            ActionForward af = cancelled(mapping, form, request, response);

            if (af != null) {
                return af;
            }
        }

        // Identify the request parameter containing the method name
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
     * <p>Dispatches to the target class' <code>unspecified</code> method, if
     * present, otherwise throws a ServletException. Classes utilizing
     * <code>ActionDispatcher</code> should provide an
     * <code>unspecified</code> method if they wish to provide behavior
     * different than throwing a ServletException.</p>
     */
    protected ActionForward unspecified(ActionMapping mapping,
        ActionForm form, HttpServletRequest request,
        HttpServletResponse response) throws Exception {
        // Identify if there is an "unspecified" method to be dispatched to
        String name = "unspecified";
        Method method = null;

        try {
            method = getMethod(name);
        } catch (NoSuchMethodException e) {
            String message =
                messages.getMessage("dispatch.parameter", mapping.getPath(),
                    mapping.getParameter());

            log.error(message);

            throw new ServletException(message, e);
        }

        return dispatchMethod(mapping, form, request, response, name, method);
    }

    /**
     * <p>Dispatches to the target class' cancelled method, if present,
     * otherwise returns null. Classes utilizing <code>ActionDispatcher</code>
     * should provide a <code>cancelled</code> method if they wish to provide
     * behavior different than returning null.</p>
     */
    protected ActionForward cancelled(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        // Identify if there is an "cancelled" method to be dispatched to
        String name = "cancelled";
        Method method = null;

        try {
            method = getMethod(name);
        } catch (NoSuchMethodException e) {
            return null;
        }

        return dispatchMethod(mapping, form, request, response, name, method);
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
            log.atError()
                .setMessage(() -> messages.getMessage("dispatch.method",
                    mapping.getPath(), name))
                .setCause(e).log();

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

            forward = (ActionForward) method.invoke(actionInstance, args);
        } catch (ClassCastException e) {
            log.atError()
                .setMessage(() -> messages.getMessage("dispatch.return",
                    mapping.getPath(), name))
                .setCause(e).log();
            throw e;
        } catch (IllegalAccessException e) {
            log.atError()
                .setMessage(() -> messages.getMessage("dispatch.error",
                    mapping.getPath(), name))
                .setCause(e).log();
            throw e;
        } catch (InvocationTargetException e) {
            // Rethrow the target exception if possible so that the
            // exception handling machinery can deal with it
            Throwable t = e.getTargetException();

            if (t instanceof Exception) {
                throw ((Exception) t);
            }

            log.atError()
                .setMessage(() -> messages.getMessage("dispatch.error",
                    mapping.getPath(), name))
                .setCause(e).log();
            throw new ServletException(t);
        }

        // Return the returned ActionForward instance
        return (forward);
    }

    /**
     * Introspect the current class to identify a method of the specified
     * name that accepts the same parameter types as the
     * <code>execute</code> method does.
     *
     * @param name Name of the method to be introspected
     * @return The method with the specified name.
     * @throws NoSuchMethodException if no such method can be found
     */
    protected Method getMethod(String name) throws NoSuchMethodException {
        Method method = methods.get(name);

        if (method == null) {
            method = clazz.getMethod(name, types);
            Method previous = methods.putIfAbsent(name, method);
            if (previous != null) {
                method = previous;
            }
        }

        return (method);
    }

    /**
     * <p>Returns the parameter value as influenced by the selected
     * {@link #flavor} specified for this <code>ActionDispatcher</code>.</p>
     */
    protected String getParameter(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        String parameter = mapping.getParameter();

        if ("".equals(parameter)) {
            parameter = null;
        }

        if ((parameter == null) && (flavor == DEFAULT_FLAVOR)) {
            // use "method" for DEFAULT_FLAVOR if no parameter was provided
            return "method";
        }

        if ((parameter == null)
            && ((flavor == MAPPING_FLAVOR) || (flavor == DISPATCH_FLAVOR))) {
            String message =
                messages.getMessage("dispatch.handler", mapping.getPath());

            log.error(message);

            throw new ServletException(message);
        }

        return parameter;
    }

    /**
     * Returns the method name, given a parameter's value.
     */
    protected String getMethodName(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response,
        String parameter) throws Exception {
        // "Mapping" flavor, defaults to "method"
        if (flavor == MAPPING_FLAVOR) {
            return parameter;
        }

        // default behaviour
        return request.getParameter(parameter);
    }

    /**
     * <p>Returns <code>true</code> if the current form's cancel button was
     * pressed: the request attribute named by
     * {@link org.apache.struts.Globals#CANCEL_KEY} has been set, which
     * normally occurs if the cancel button generated by
     * <strong>CancelTag</strong> was pressed by the user in the current
     * request.</p>
     */
    protected boolean isCancelled(HttpServletRequest request) {
        return (request.getAttribute(
            org.apache.struts.Globals.CANCEL_KEY) != null);
    }
}
