package org.apache.struts.tiles.taglib;

import jakarta.servlet.jsp.JspException;

/**
 * Tag classes implementing this interface can contain nested <code>PutTag</code>s.
 * This interface defines a method called by the nested tag.
 */
public interface AddTagParent {
    /**
     * Process the nested tag.
     * @param nestedTag Nested to process.
     */
    void processNestedTag(AddTag nestedTag) throws JspException;
}
