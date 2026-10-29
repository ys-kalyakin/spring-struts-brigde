package org.apache.struts.tiles.taglib;

import jakarta.servlet.jsp.JspException;

/**
 * Tag classes implementing this interface can contains nested PutTag.
 * This interface defines a method called by nested tags.
 */
public interface PutListTagParent {
  /**
   * Add an attribute to container.
   * @param nestedTag Nested PutTag defining the attribute.
   */
  void processNestedTag(PutListTag nestedTag) throws JspException;

}
