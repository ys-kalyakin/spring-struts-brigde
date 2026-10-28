package org.apache.struts.extras.actions;

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
 * <p>An abstract <strong>Action</strong> that dispatches to a public method
 * that is named by the <code>parameter</code> attribute of the
 * corresponding ActionMapping. This is useful if you want to use the same
 * Action class for more than one action in your struts-config.xml file.
 * Multiple mappings can point at the same class, each selecting a different
 * method.</p>
 *
 * <p>Ported to the spring-struts-bridge engine with the classic Struts 1.3
 * API (the weblegacy 1.5 implementation delegates to commons-chain, which
 * this engine replaces with the Spring MVC pipeline).</p>
 */
public class MappingDispatchAction extends DispatchAction {

    private static final Logger log =
        LoggerFactory.getLogger(MappingDispatchAction.class);

    /**
     * The message resources for this package.
     */
    protected static MessageResources messages =
        MessageResources.getMessageResources(
            "org.apache.struts.extras.actions.LocalStrings");

    /**
     * Process the specified HTTP request, and create the corresponding HTTP
     * response, dispatching to the public method that is named by the
     * <code>parameter</code> attribute of the given ActionMapping.
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        // Use the overridden getMethodName.
        return super.execute(mapping, form, request, response);
    }

    /**
     * Method which is dispatched to when the mapping does not define a
     * method to dispatch to. The default implementation throws an
     * exception.
     */
    @Override
    protected ActionForward unspecified(ActionMapping mapping,
        ActionForm form, HttpServletRequest request,
        HttpServletResponse response) throws Exception {
        String message =
            messages.getMessage("mapping.parameter", mapping.getPath());

        log.error(message);

        throw new ServletException(message);
    }

    /**
     * Returns the value of the mapping's <code>parameter</code> attribute
     * (the method name to dispatch to), without failing when it is null:
     * a null value leads to <code>unspecified</code>.
     */
    @Override
    protected String getParameter(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        return mapping.getParameter();
    }

    /**
     * Returns the unresolved mapping parameter: the method name is taken
     * from the mapping instead of the request.
     */
    @Override
    protected String getMethodName(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response,
        String parameter) throws Exception {
        // Return the unresolved mapping parameter.
        return parameter;
    }
}
