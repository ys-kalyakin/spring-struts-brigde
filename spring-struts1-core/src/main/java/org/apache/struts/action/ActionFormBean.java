package org.apache.struts.action;

import org.apache.struts.config.FormBeanConfig;

/**
 * Subclass of <code>FormBeanConfig</code> whose type is a Java class
 * implementing <code>ActionForm</code> — the default form bean class used
 * when parsing configuration files.
 *
 * <p>Ported from weblegacy/struts1 1.5.x (org.apache.struts.action.ActionFormBean).</p>
 */
public class ActionFormBean extends FormBeanConfig {

    private static final long serialVersionUID = 1L;

    public ActionFormBean() {
    }

    public ActionFormBean(String name, String type) {
        setName(name);
        setType(type);
    }
}
