package org.apache.struts.tiles;

import java.io.Serializable;

  /**
   * Attribute definition used in a component definition.
   *
   */
public interface AttributeDefinition extends Serializable
{

    /**
     * Return value hold by this typed attribute.
     */
  public Object getValue();

    /**
     * Set role attribute.
     */
  public void setRole(String role);
}
