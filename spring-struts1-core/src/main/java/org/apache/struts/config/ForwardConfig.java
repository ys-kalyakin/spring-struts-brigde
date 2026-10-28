package org.apache.struts.config;

/**
 * A JavaBean representing the configuration information of a
 * <code>&lt;forward&gt;</code> element from a Struts configuration file.
 *
 * <p>Ported from weblegacy/struts1 1.5.x (org.apache.struts.config.ForwardConfig).</p>
 */
public class ForwardConfig extends BaseConfig {

    private static final long serialVersionUID = 7621670814492368589L;

    /** The unique identifier of this forward. */
    protected String name;

    /** The URL to which this forward will be transferred. */
    protected String path;

    /** The module-relative path (if any) of an alternate module. */
    protected String module;

    /** Should this forward trigger an HTTP redirect? */
    protected boolean redirect;

    public ForwardConfig() {
    }

    public ForwardConfig(String name, String path, boolean redirect) {
        setName(name);
        setPath(path);
        setRedirect(redirect);
    }

    public ForwardConfig(String name, String path, boolean redirect,
        String module) {
        this(name, path, redirect);
        setModule(module);
    }

    public ForwardConfig(ForwardConfig copyMe) {
        setName(copyMe.getName());
        setPath(copyMe.getPath());
        setRedirect(copyMe.getRedirect());
        setModule(copyMe.getModule());
        BaseConfig.copyProperties(copyMe, this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public boolean getRedirect() {
        return redirect;
    }

    public void setRedirect(boolean redirect) {
        this.redirect = redirect;
    }

    @Override
    public String toString() {
        return "ForwardConfig[name=" + name + ",path=" + path
            + ",redirect=" + redirect + ",module=" + module + "]";
    }
}
