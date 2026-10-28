package org.apache.struts.action;

import org.apache.struts.config.ActionConfig;
import org.apache.struts.config.ExceptionConfig;
import org.apache.struts.config.ForwardConfig;

/**
 * An <strong>ActionMapping</strong> represents the information that the
 * controller, <code>RequestProcessor</code>, knows about the mapping of a
 * particular request to an instance of a particular
 * <code>Action</code> class.
 *
 * <p>Ported from weblegacy/struts1 1.5.x (org.apache.struts.action.ActionMapping).</p>
 */
public class ActionMapping extends ActionConfig {

    private static final long serialVersionUID = 2801090844264312287L;

    /**
     * Find and return the <code>ForwardConfig</code> instance defining how
     * forwarding to the specified logical name should be handled. This is
     * performed by checking local and then global configurations for the
     * specified forwarding configuration. If no forwarding configuration can
     * be found, return <code>null</code>.
     */
    public ActionForward findForward(String forwardName) {
        ForwardConfig config = findForwardConfig(forwardName);

        if (config == null && getModuleConfig() != null) {
            config = getModuleConfig().findForwardConfig(forwardName);
        }

        return (ActionForward) config;
    }

    /**
     * Find and return the <code>ForwardConfig</code> instance of this
     * mapping, throwing an exception if not found locally or globally.
     *
     * @since Struts 1.4
     */
    public ActionForward findRequiredForward(String forwardName) {
        ActionForward forward = findForward(forwardName);
        if (forward == null) {
            throw new IllegalStateException(
                "Unable to find '" + forwardName
                    + "' forward of action path '" + getPath() + "'");
        }
        return forward;
    }

    /**
     * Return the logical names of all local forwards defined for this
     * mapping. If there are none, a zero-length array is returned.
     */
    public String[] findForwards() {
        ForwardConfig[] configs = findForwardConfigs();
        String[] names = new String[configs.length];
        for (int i = 0; i < configs.length; i++) {
            names[i] = configs[i].getName();
        }
        return names;
    }

    /**
     * Return the logical names of all local exception handlers defined for
     * this mapping. If there are none, a zero-length array is returned.
     */
    public String[] findExceptions() {
        ExceptionConfig[] configs = findExceptionConfigs();
        String[] names = new String[configs.length];
        for (int i = 0; i < configs.length; i++) {
            names[i] = configs[i].getType();
        }
        return names;
    }
}
