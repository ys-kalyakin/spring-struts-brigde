package org.apache.struts.fixtures;

import org.apache.struts.action.ActionForm;

/**
 * Fixture form with a classic JavaBeans indexed write method only: no
 * array or list field backs the property, so population must resolve the
 * <code>setZipPostal(int, String)</code> method (the pattern used by the
 * weblegacy example forms, e.g. <code>CityStateZip</code>).
 */
public class IndexedMethodsForm extends ActionForm {

    private final String[] zipPostal = new String[2];

    public String getZipPostal(int index) {
        return zipPostal[index];
    }

    public void setZipPostal(int index, String value) {
        zipPostal[index] = value;
    }
}
