package org.apache.struts.tiles;

/**
 * Component attribute.
 * Such attribute value represent an instance name.
 */
public class DefinitionNameAttribute extends UntypedAttribute {
    private static final long serialVersionUID = -7680396673346627828L;

    /**
     * Constructor.
     */
    public DefinitionNameAttribute(String value) {
        super(value);
    }

    /**
     * Constructor.
     */
    public DefinitionNameAttribute(String value, String role) {
        super(value, role);
    }

}
