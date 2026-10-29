package org.apache.struts.tiles.taglib;

import jakarta.servlet.jsp.JspException;

/**
 * Tag classes implementing this interface can contain nested PutTag.
 * This interface defines a method called by nested tags.
 */
public interface PutTagParent {
  /**
   * Process the nested tag.
   * @param nestedTag Nested tag to process.
   */
  void processNestedTag(PutTag nestedTag ) throws JspException;

}
