package org.apache.struts.action;

import jakarta.servlet.http.HttpServletRequest;

/**
 * An <strong>ActionForm</strong> is a JavaBean optionally associated with
 * one or more <code>ActionMappings</code>. Such a bean will have had its
 * properties initialized from the corresponding request parameters before
 * the <code>execute</code> method of the <code>Action</code> is called.
 *
 * <p>Ported from weblegacy/struts1 1.5.x (org.apache.struts.action.ActionForm).</p>
 */
public abstract class ActionForm {

    /**
     * Can be used to reset bean properties to their default state, before the
     * properties are repopulated by corresponding request parameters.
     */
    public void reset(ActionMapping mapping, HttpServletRequest request) {
    }

    /**
     * Validate the properties that have been set for this HTTP request, and
     * return an <code>ActionErrors</code> object that encapsulates any
     * validation errors that have been found. If no errors are found, return
     * <code>null</code> or an <code>ActionErrors</code> object with no
     * recorded error messages.
     */
    public ActionErrors validate(ActionMapping mapping,
        HttpServletRequest request) {
        return null;
    }
}
