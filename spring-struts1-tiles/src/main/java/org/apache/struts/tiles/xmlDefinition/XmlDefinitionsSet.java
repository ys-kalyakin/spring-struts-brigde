package org.apache.struts.tiles.xmlDefinition;

import java.util.HashMap;
import java.util.Map;

import org.apache.struts.tiles.NoSuchDefinitionException;

/**
 * A set of definitions read from XML definitions file.
*/
public class XmlDefinitionsSet
{
    /** Defined definitions. */
  protected Map<String, XmlDefinition> definitions;

     /**
      * Constructor.
      */
  public XmlDefinitionsSet()
   {
   definitions = new HashMap<>();
   }

  /**
   * Put definition in set.
   * @param definition Definition to add.
   */
  public void putDefinition(XmlDefinition definition)
  {
  definitions.put( definition.getName(), definition );
  }

  /**
   * Get requested definition.
   * @param name Definition name.
   */
  public XmlDefinition getDefinition(String name)
  {
  return definitions.get( name );
  }

  /**
   * Get definitions map.
   */
  public Map<String, XmlDefinition> getDefinitions()
  {
  return definitions;
  }

  /**
   * Resolve extended instances.
   */
  public void resolveInheritances() throws NoSuchDefinitionException
    {
      // Walk through all definitions and resolve individual inheritance
    for( XmlDefinition definition : definitions.values() )
      {
      definition.resolveInheritance( this );
      }  // end loop
    }

  /**
   * Add definitions from specified child definitions set.
   * For each definition in child, look if it already exists in this set.
   * If not, add it, if yes, overload parent's definition with child definition.
   * @param child Definition used to overload this object.
   */
  public void extend( XmlDefinitionsSet child )
    {
    if(child==null)
      return;
    for( XmlDefinition childInstance : child.getDefinitions().values() )
      {
      XmlDefinition parentInstance = getDefinition(childInstance.getName() );
      if( parentInstance != null )
        {
        parentInstance.overload( childInstance );
        }
       else
        putDefinition( childInstance );
      } // end loop
    }
    /**
     * Get String representation.
     */
  public String toString()
    {
    return "definitions=" + definitions.toString() ;
    }

}
