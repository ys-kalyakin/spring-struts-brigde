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
     * The ServletContext attribute under which we store the module prefixes
     * String[].
     *
     * @since Struts 1.2
     */
    public static final String MODULE_PREFIXES_KEY =
        "org.apache.struts.globals.MODULE_PREFIXES";

    /**
     * The request attribute under which we store the original URI of the
     * request.
     *
     * @since Struts 1.3
     */
    public static final String ORIGINAL_URI_KEY =
        "org.apache.struts.globals.ORIGINAL_URI_KEY";

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

    /**
     * The context attributes key under which we store the mapping defined for
     * our controller servlet, which will be either a path-mapped pattern
     * (<code>/action/*</code>) or an extension mapped pattern
     * (<code>*.do</code>).
     */
    public static final String SERVLET_KEY =
        "org.apache.struts.action.SERVLET_MAPPING";

    /**
     * The session attributes key under which our transaction token is stored,
     * if it is used.
     */
    public static final String TRANSACTION_TOKEN_KEY =
        "org.apache.struts.action.TOKEN";

    /**
     * The page attributes key under which xhtml status is stored. This may
     * be "true" or "false". When set to true, the html tags output xhtml.
     *
     * @since Struts 1.1
     */
    public static final String XHTML_KEY = "org.apache.struts.globals.XHTML";

    /**
     * The request attributes key under which XHTML version is stored. The
     * version is stored as a {@link java.math.BigDecimal}.
     *
     * @since Struts 1.4
     */
    public static final String XHTML_VERSION_KEY = "org.apache.struts.globals.XHTML_VERSION";

    /**
     * The name of the taglib package.
     */
    public static final String TAGLIB_PACKAGE = "org.apache.struts.taglib.html";

    /**
     * The property under which a Cancel button press is reported.
     */
    public static final String CANCEL_PROPERTY = TAGLIB_PACKAGE + ".CANCEL";

    /**
     * The property under which a Cancel button press is reported, if the
     * Cancel button is rendered as an image.
     */
    public static final String CANCEL_PROPERTY_X = TAGLIB_PACKAGE + ".CANCEL.x";

    /**
     * The property under which a transaction token is reported.
     */
    public static final String TOKEN_KEY = TAGLIB_PACKAGE + ".TOKEN";
}
