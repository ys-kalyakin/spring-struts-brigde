package org.apache.struts.tiles;

/**
 * Common implementation of attribute definition.
 */
public class UntypedAttribute implements AttributeDefinition {
    private static final long serialVersionUID = -6268589644481252985L;

    /**
     * Role associated to this attribute.
     */
    protected String role = null;

    protected Object value = null;

    /**
     * Constructor.
     * @param value Object to store.
     */
    public UntypedAttribute(Object value) {
        this.value = value;
    }

    /**
     * Constructor.
     * @param value Object to store.
     * @param role Asociated role.
     */
    public UntypedAttribute(Object value, String role) {
        this.value = value;
        this.role = role;
    }

    /**
     * Get role.
     */
    public String getRole() {
        return role;
    }

    /**
     * Set role.
     * @param role Associated role.
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Get value.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Set value.
     * @param value New value.
     */
    public void setValue(Object value) {
        this.value = value;
    }

    /**
     * Get String representation of this object.
     */
    public String toString() {
        return value.toString();
    }
}
