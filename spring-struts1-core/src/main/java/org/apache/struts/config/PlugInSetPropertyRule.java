package org.apache.struts.config;

import org.apache.commons.digester.Rule;
import org.xml.sax.Attributes;


/**
 * Class that records the name and value of a configuration property to be
 * used in configuring a <code>PlugIn</code> instance when instantiated.
 */
final class PlugInSetPropertyRule extends Rule {

    PlugInSetPropertyRule() {
        super();
    }

    @Override
    public void begin(String namespace, String names, Attributes attributes)
        throws Exception {
        PlugInConfig plugInConfig = (PlugInConfig) getDigester().peek();

        plugInConfig.addProperty(attributes.getValue("property"),
            attributes.getValue("value"));
    }
}
