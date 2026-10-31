package org.apache.struts.tiles;

import java.io.Serializable;

  /**
   * Attribute definition used in a component definition.
   *
   */
public interface AttributeDefinition extends Serializable {

    /**
     * Return value hold by this typed attribute.
     */
    Object getValue();

    /**
     * Set role attribute.
     */
    void setRole(String role);
}
