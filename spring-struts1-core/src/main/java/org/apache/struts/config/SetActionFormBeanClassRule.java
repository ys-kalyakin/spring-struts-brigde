package org.apache.struts.config;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;


/**
 * Class that sets the name of the class to use when creating action form
 * bean instances.
 */
final class SetActionFormBeanClassRule extends Rule {

    SetActionFormBeanClassRule() {
        super();
    }

    @Override
    public void begin(String namespace, String name, Attributes attributes)
        throws Exception {
        String className = attributes.getValue("type");

        if (className != null) {
            ModuleConfig mc = (ModuleConfig) getDigester().peek();

            mc.setActionFormBeanClass(className);
        }
    }
}
