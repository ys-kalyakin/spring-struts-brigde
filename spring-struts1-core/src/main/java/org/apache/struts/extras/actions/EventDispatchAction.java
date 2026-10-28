package org.apache.struts.extras.actions;

import java.util.StringTokenizer;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

/**
 * <p>An <strong>Action</strong> that dispatches to one of the public methods
 * that are named in the <code>parameter</code> attribute of the
 * corresponding ActionMapping and matches a submission parameter. This is
 * useful for developers who prefer to use many submit buttons, images, or
 * submit links on a single form and whose related actions exist in a single
 * Action class.</p>
 *
 * <p>The method(s) must have the same signature (other than method name) of
 * the standard Action.execute method.</p>
 *
 * <p>To configure the use of this action in your
 * <code>struts-config.xml</code> file, create an entry like this:</p>
 *
 * <pre><code>
 *   &lt;action path="/saveSubscription"
 *           type="org.example.SubscriptionAction"
 *           name="subscriptionForm"
 *          scope="request"
 *          input="/subscription.jsp"
 *      parameter="save,back,recalc=recalculate,default=save"/&gt;
 * </code></pre>
 *
 * <p>where <code>parameter</code> contains three possible methods and one
 * default method if nothing matches (such as the user pressing the enter
 * key). The <code>key=value</code> notation aliases methods so that they
 * are exposed as different form element names. The default method is
 * selected when no request parameter matches any of the method keys.</p>
 *
 * <p>Ported to the spring-struts-bridge engine with the classic Struts 1.3
 * API.</p>
 *
 * @since Struts 1.2.9
 */
public class EventDispatchAction extends DispatchAction {

    private static final Logger log =
        LoggerFactory.getLogger(EventDispatchAction.class);

    /**
     * The method key, if present, to use if other specified method keys
     * do not match a request parameter.
     */
    private static final String DEFAULT_METHOD_KEY = "default";

    /**
     * Method which is dispatched to when there is no value for specified
     * request parameter included in the request. Subclasses should override
     * this method if they wish to provide default behavior different from
     * throwing a ServletException.
     */
    @Override
    protected ActionForward unspecified(ActionMapping mapping,
        ActionForm form, HttpServletRequest request,
        HttpServletResponse response) throws Exception {
        String message =
            messages.getMessage("event.parameter", mapping.getPath(),
                mapping.getParameter());

        log.error("{} {}", message, mapping.getParameter());

        throw new ServletException(message);
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
