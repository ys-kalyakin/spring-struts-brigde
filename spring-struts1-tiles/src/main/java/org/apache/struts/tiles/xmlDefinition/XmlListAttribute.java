package org.apache.struts.tiles.xmlDefinition;

import java.util.ArrayList;
import java.util.List;


/**
 * An attribute as a <code>List</code>.
 * This attribute associates a name with a list. The list can be found by the
 * property name.
 * Elements in list are retrieved using List methods.
 * This class is used to read configuration files.
 */
public class XmlListAttribute extends XmlAttribute {
    /** List.
     * We declare a List to avoid cast.
     * Parent "value" property points to the same list.
     */
    private List<Object> list;

    /**
     * Constructor.
     */
    public XmlListAttribute() {
        list = new ArrayList<>();
        setValue(list);
    }

    /**
     * Constructor.
     * @param name Name.
     * @param value List.
     */
    public XmlListAttribute(String name, List<Object> value) {
        super(name, value);
        list = value;
    }

    /**
     * Add an element in list.
     * We use a property to avoid rewriting a new class.
     * @param element XmlAttribute to add.
     */
    public void add(XmlAttribute element) {
        list.add(element.getValue());
    }

    /**
     * Add an element in list.
     * @param value Object to add.
     */
    public void add(Object value) {
    //list.add( value );
      // To correct a bug in digester, we need to check the object type
      // Digester doesn't call correct method according to object type ;-(
        if (value instanceof XmlAttribute) {
            add((XmlAttribute)value);
            return;
        } else {
            list.add(value);
        }
    }

    /**
     * Add an element in list.
     * @param value Object to add.
     */
    public void addObject(Object value) {
        list.add(value);
    }



}
