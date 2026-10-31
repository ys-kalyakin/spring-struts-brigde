package org.apache.struts.tiles.taglib;

import jakarta.servlet.jsp.JspException;

  /**
   * Add an element to the surrounding list tag.
   * Same syntax as <code>&lt;put&gt;</code>.
   */
public class AddTag extends PutTag {
    private static final long serialVersionUID = -8081198639386273712L;

  /**
   * default constructor
   */
    public AddTag() {
        super();
    }

    /**
     * Call parent tag which must implement AttributeContainer.
     * @throws JspException If we can't find an appropriate enclosing tag.
     */
    protected void callParent() throws JspException {
            // Get enclosing parent
        AddTagParent enclosingParent = findEnclosingPutListTagParent();
        enclosingParent.processNestedTag(this);
    }

    /**
     * Find parent tag which must implement AttributeContainer.
     * @throws JspException If we can't find an appropriate enclosing tag.
     */
    protected AddTagParent findEnclosingPutListTagParent() throws JspException {
        try {
            AddTagParent parent = (AddTagParent)findAncestorWithClass(this,AddTagParent.class);
            if (parent == null) {
                throw new JspException("Error - tag add : enclosing tag doesn't accept 'add' tag.");
            }
            return parent;
        } catch (ClassCastException ex) {
            throw new JspException("Error - tag add : enclosing tag doesn't accept 'add' tag." , ex);
        }
    }
}
