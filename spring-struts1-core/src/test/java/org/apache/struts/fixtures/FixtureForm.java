package org.apache.struts.fixtures;

import org.apache.struts.action.ActionForm;

/**
 * Plain ActionForm fixture registered in fixture struts-config.xml files.
 */
public class FixtureForm extends ActionForm {

    private static final long serialVersionUID = 1L;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
