package org.apache.struts;

import java.io.Serializable;

/**
 * Global manifest constants for the entire Struts Framework.
 *
 * <p>Ported from weblegacy/struts1 1.5.x (org.apache.struts.Globals).</p>
 */
public class Globals implements Serializable {
    private static final long serialVersionUID = 5585920111597892090L;

    /**
     * The context attributes key under which our <code>ActionServlet</code>
     * instance will be stored.
     *
     * @since Struts 1.1
     */
    public static final String ACTION_SERVLET_KEY =
        "org.apache.struts.action.ACTION_SERVLET";

    /**
     * The request attributes key under which a boolean <code>true</code>
     * value should be stored if this request was cancelled.
     *
     * @since Struts 1.1
     */
    public static final String CANCEL_KEY = "org.apache.struts.action.CANCEL";

    /**
     * The base of the context attributes key under which our
     * <code>ModuleConfig</code> data structure will be stored. This will be
     * suffixed with the actual module prefix (including the leading "/"
     * character) to form the actual attributes key.
     *
     * @since Struts 1.1
     */
    public static final String MODULE_KEY = "org.apache.struts.action.MODULE";

    /**
     * The request attributes key under which your action should store an
     * <code>org.apache.struts.action.ActionErrors</code> object.
     */
    public static final String ERROR_KEY = "org.apache.struts.action.ERROR";

    /**
     * The request attributes key under which a <code>Throwable</code> that
     * caused an error is stored.
     */
    public static final String EXCEPTION_KEY =
        "org.apache.struts.action.EXCEPTION";

    /**
     * The session attributes key under which the user's selected
     * <code>java.util.Locale</code> is stored, if any.
     */
    public static final String LOCALE_KEY = "org.apache.struts.action.LOCALE";

    /**
     * The request attributes key under which our
     * <code>org.apache.struts.action.ActionMapping</code> instance is passed.
     */
    public static final String MAPPING_KEY =
        "org.apache.struts.action.mapping.instance";

    /**
     * The request attributes key under which your action should store an
     * <code>org.apache.struts.action.ActionMessages</code> object.
     *
     * @since Struts 1.1
     */
    public static final String MESSAGE_KEY =
        "org.apache.struts.action.ACTION_MESSAGE";

    /**
     * The base of the context attributes key under which our module
     * <code>MessageResources</code> will be stored. This will be suffixed
     * with the actual module prefix (including the leading "/" character) to
     * form the actual attributes key.
     */
    public static final String MESSAGES_KEY =
        "org.apache.struts.action.MESSAGE";

    /**
     * The base of the context attributes key under which an array of
     * <code>PlugIn</code> instances will be stored. This will be suffixed
     * with the actual module prefix (including the leading "/" character) to
     * form the actual attributes key.
     *
     * @since Struts 1.1
     */
    public static final String PLUG_INS_KEY =
        "org.apache.struts.action.PLUG_INS";
}
