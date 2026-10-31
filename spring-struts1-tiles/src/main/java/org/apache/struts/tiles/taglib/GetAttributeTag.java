package org.apache.struts.tiles.taglib;

import org.apache.struts.taglib.spring.SpringTagSupport;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.PageContext;

import org.apache.struts.tiles.ComponentContext;

  /**
   * Retrieve the value of the specified component/template attribute property,
   * and render it to the current JspWriter as a String.
   * The usual toString() conversion is applied on the found value.
   */
public class GetAttributeTag extends SpringTagSupport implements ComponentConstants {
    private static final long serialVersionUID = 6301259591967456357L;

    private String attribute = null;
    /** Role attribute */
    private String role = null;
    /**
     * Do we ignore error if attribute is not found.
     * Default value is <code>false</code>, which will throw an exception.
     */
    private boolean isErrorIgnored = false;

  /**
   * Default constructor.
   */
    public GetAttributeTag() {
        super();
    }

    /**
     * Release all allocated resources.
     */
    public void release() {

        super.release();
        attribute = null;
        role = null;
        isErrorIgnored = false;
    }

    /**
     * Set attribute.
     * @param attribute Attribute.
     */
    public void setAttribute(String attribute) {
        this.attribute = attribute;
    }

    /**
     * Get attribute.
     * @return Attribute.
     */
    public String getAttribute() {
        return attribute;
    }

    /**
     * Set Name.
     * Same as setAttribute().
     * @param value Attribute.
     */
    public void setName(String value) {
        this.attribute = value;
    }

    /**
     * Get Name.
     * Set as getAttribute().
     * @return Attribute.
     */
    public String getName() {
        return attribute;
    }

    /**
     * Set ignoring flag when attribute is not found.
     * @param ignore default: <code>false</code>: Exception is thrown when attribute is not found, set to <code>
     * true</code> to ignore missing attributes silently
     */
    public void setIgnore(boolean ignore) {
        this.isErrorIgnored = ignore;
    }

    /**
     * Get ignore flag.
     * @return <code>false</code>: Exception is thrown when attribute is not found, set to <code>
     * true</code> to ignore missing attributes silently
     */
    public boolean getIgnore() {
        return isErrorIgnored;
    }

    /**
     * Set role.
     * @param role The role the user must be in to store content.
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Get role.
     * @return Role.
     */
    public String getRole() {
        return role;
    }

    /**
     * Close tag.
     * @throws JspException On error processing tag.
     */
    public int doEndTag() throws JspException {

      // Check role
        if (role != null && !((HttpServletRequest)pageContext.getRequest()).isUserInRole(role)) {
            return EVAL_PAGE;
        } // end if

      // Get context
        ComponentContext compContext = (ComponentContext)pageContext.getAttribute(
            ComponentConstants.COMPONENT_CONTEXT, PageContext.REQUEST_SCOPE);

        if (compContext == null) {
            throw new JspException ("Error - tag.getAsString : component context is not defined. Check tag syntax");
        }

        Object value = compContext.getAttribute(attribute);
        if (value == null) {
            if (!isErrorIgnored) {
                throw new JspException ("Error - tag.getAsString : attribute '" + attribute
                    + "' not found in context. Check tag syntax");
            } else {
                return EVAL_PAGE;
            }
        } // end if


        try {
            pageContext.getOut().print(value);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new JspException ("Error - tag.getProperty : IOException ", ex);
        }

        return EVAL_PAGE;
    }
}
