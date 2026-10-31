package org.apache.struts.tiles.beans;

import java.io.Serializable;

/**
 * Interface for MenuItems.
 * @see SimpleMenuItem
 */
public interface MenuItem extends Serializable {

    /**
     * Set value property.
     */
    void setValue(String value);

    /**
     * Get value property.
     */
    String getValue();

    /**
     * Set link property.
     */
    void setLink(String link);

    /**
     * Get link property.
     */
    String getLink();

    /**
     * Set icon property.
     */
    void setIcon(String link);

    /**
     * Get icon property.
     */
    String getIcon();

    /**
     * Set tooltip property.
     */
    void setTooltip(String link);

    /**
     * Get tooltip property.
     */
    String getTooltip();
}
