package org.apache.struts.extras.actions;

import java.lang.reflect.Method;
import java.util.StringTokenizer;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * <p>An <code>ActionDispatcher</code> variant that resolves the method by
 * event rules: each comma separated entry of the mapping's
 * <code>parameter</code> attribute names a request parameter (optionally
 * aliased as <code>key=value</code>) and the method it triggers, with the
 * special key <code>default</code> naming the fallback method.</p>
 *
 * <p>Ported to the spring-struts-bridge engine with the classic Struts 1.3
 * API.</p>
 */
public class EventActionDispatcher extends ActionDispatcher {

    private static final Logger log =
        LoggerFactory.getLogger(EventActionDispatcher.class);

    /**
     * The method key, if present, to use if other specified method keys
     * do not match a request parameter.
     */
    private static final String DEFAULT_METHOD_KEY = "default";

    /**
     * Construct an event dispatcher for the given action (mapping flavor:
     * the event rules come from the mapping's <code>parameter</code>).
     *
     * @param action The action instance to be invoked.
     */
    public EventActionDispatcher(Action action) {
        // N.B. MAPPING_FLAVOR causes the getParameter() method
        //      in ActionDispatcher to throw an exception if the
        //      parameter is missing
        super(action, ActionDispatcher.MAPPING_FLAVOR);
    }

    /**
     * Dispatches to the target class' <code>unspecified</code> method, if
     * present, otherwise throws a ServletException.
     */
    @Override
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
                messages.getMessage("event.parameter", mapping.getPath());

            log.error("{} {}", message, mapping.getParameter());

            throw new ServletException(message, e);
        }

        return dispatchMethod(mapping, form, request, response, name, method);
    }

    /**
     * Returns the method name, given a parameter's value: the first method
     * key present as a request parameter (standalone or as an image button
     * <code>.x</code> submission) wins; otherwise the default method is
     * used.
     */
    @Override
    protected String getMethodName(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response,
        String parameter) throws Exception {

        StringTokenizer st = new StringTokenizer(parameter, ",");
        String defaultMethodName = null;

        while (st.hasMoreTokens()) {
            String methodKey = st.nextToken().trim();
            String methodName = methodKey;

            // The key can either be a direct method name or an alias
            // to a method as indicated by a "key=value" signature
            int equals = methodKey.indexOf('=');
            if (equals > -1) {
                methodName = methodKey.substring(equals + 1).trim();
                methodKey = methodKey.substring(0, equals).trim();
            }

            // Set the default if it passes by
            if (methodKey.equals(DEFAULT_METHOD_KEY)) {
                defaultMethodName = methodName;
            }

            // If the method key exists as a standalone parameter or with
            // the image suffixes (.x/.y), the method name has been found.
            if ((request.getParameter(methodKey) != null)
                  || (request.getParameter(methodKey + ".x") != null)) {
                return methodName;
            }
        }

        return defaultMethodName;
    }
}
