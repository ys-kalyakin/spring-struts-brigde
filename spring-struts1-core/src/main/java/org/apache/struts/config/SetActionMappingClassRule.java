package org.apache.struts.config;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;


/**
 * Class that sets the name of the class to use when creating action mapping
 * instances.
 */
final class SetActionMappingClassRule extends Rule {

    SetActionMappingClassRule() {
        super();
    }

    @Override
    public void begin(String namespace, String name, Attributes attributes)
        throws Exception {
        String className = attributes.getValue("type");

        if (className != null) {
            ModuleConfig mc = (ModuleConfig) getDigester().peek();

            mc.setActionMappingClass(className);
        }
    }
}
