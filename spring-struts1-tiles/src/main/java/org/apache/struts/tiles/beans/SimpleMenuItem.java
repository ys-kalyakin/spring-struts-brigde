package org.apache.struts.tiles.beans;

import java.io.Serializable;

/**
 * A MenuItem implementation.
 * Used to read menu items in definitions.
 */
public class SimpleMenuItem implements MenuItem, Serializable {
    private static final long serialVersionUID = -5488084076348864047L;

    private String value = null;

    private String link = null;

    private String icon = null;

    private String tooltip = null;

    /**
     * Constructor.
     */
    public SimpleMenuItem() {
        super();
    }

    /**
     * Set value property.
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Get value property.
     */
    public String getValue() {
        return value;
    }

    /**
     * Set link property.
     */
    public void setLink(String link) {
        this.link = link;
    }

    /**
     * Get link property.
     */
    public String getLink() {
        return link;
    }

    /**
     * Set icon property.
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     * Get icon property.
     */
    public String getIcon() {
        return icon;
    }

    /**
     * Set tooltip property.
     */
    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    /**
     * Get tooltip property.
     */
    public String getTooltip() {
        return tooltip;
    }

    /**
     * Return String representation.
     */
    public String toString() {
        StringBuilder buff = new StringBuilder("SimpleMenuItem[");

        if (getValue() != null) {
            buff.append("value=").append(getValue()).append(", ");
        }

        if (getLink() != null) {
            buff.append("link=").append(getLink()).append(", ");
        }

        if (getTooltip() != null) {
            buff.append("tooltip=").append(getTooltip()).append(", ");
        }

        if (getIcon() != null) {
            buff.append("icon=").append(getIcon()).append(", ");
        }

        buff.append("]");
        return buff.toString();
    }

}
