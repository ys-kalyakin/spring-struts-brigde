package org.apache.struts.action;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.struts.upload.MultipartRequestHandler;

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
     * The servlet instance to which we are attached, if any.
     */
    protected transient ActionServlet servlet = null;

    /**
     * The MultipartRequestHandler for this form, can be <code>null</code>.
     */
    protected transient MultipartRequestHandler multipartRequestHandler;

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

    /**
     * Return the servlet instance to which we are attached.
     */
    protected ActionServlet getServlet() {
        return this.servlet;
    }

    /**
     * Return the controller servlet instance to which we are attached, as an
     * <code>ActionServletWrapper</code>.
     */
    public ActionServletWrapper getServletWrapper() {
        return new ActionServletWrapper(getServlet());
    }

    /**
     * Set the servlet instance to which we are attached.
     */
    public void setServlet(ActionServlet servlet) {
        this.servlet = servlet;
    }

    /**
     * Return the <code>MultipartRequestHandler</code> for this form, or
     * <code>null</code> if this form's enctype is not "multipart/form-data".
     */
    public MultipartRequestHandler getMultipartRequestHandler() {
        return multipartRequestHandler;
    }

    /**
     * Set the Handler provided for use in dealing with file uploads.
     */
    public void setMultipartRequestHandler(
        MultipartRequestHandler multipartRequestHandler) {
        this.multipartRequestHandler = multipartRequestHandler;
    }
}
