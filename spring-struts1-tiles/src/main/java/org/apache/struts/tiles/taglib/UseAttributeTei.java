package org.apache.struts.tiles.taglib;


import jakarta.servlet.jsp.tagext.TagData;
import jakarta.servlet.jsp.tagext.TagExtraInfo;
import jakarta.servlet.jsp.tagext.VariableInfo;


/**
 * Implementation of <code>TagExtraInfo</code> for the <b>UseAttribute</b>
 * tag, identifying the scripting object(s) to be made visible.
 *
 */

public final class UseAttributeTei extends TagExtraInfo {


    /**
     * Return information about the scripting variables to be created.
     */
    public VariableInfo[] getVariableInfo(TagData data) {

        String classname = data.getAttributeString("classname");
        if (classname == null) {
            classname = "java.lang.Object";
        }
        String id = data.getAttributeString("id");
        if (id == null) {
            id = data.getAttributeString("name");
        }

        return new VariableInfo[] {
            new VariableInfo(id,
                       classname,
                       true,
                       VariableInfo.AT_END)
        };

    }


}
