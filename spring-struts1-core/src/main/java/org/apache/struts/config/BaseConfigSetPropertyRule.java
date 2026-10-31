package org.apache.struts.config;

import org.apache.commons.digester.SetPropertyRule;
import org.xml.sax.Attributes;


/**
 * A variant of the standard Digester <code>SetPropertyRule</code>. If the
 * element being processed has a "key" attribute, then the value will be used
 * to call <code>setProperty(key, value)</code> on the object on top of the
 * stack (assumed to be of type <code>BaseConfig</code>). Otherwise the
 * standard behaviour is invoked with "property" and "value" attributes.
 */
final class BaseConfigSetPropertyRule extends SetPropertyRule {

    BaseConfigSetPropertyRule() {
        super("property", "value");
    }

    @Override
    public void begin(String namespace, String name, Attributes attributes)
        throws Exception {
        if (attributes.getIndex("key") == -1) {
            super.begin(namespace, name, attributes);
            return;
        }

        if (attributes.getIndex("property") != -1) {
            throw new IllegalArgumentException(
                "<set-property> accepts only one of 'key' or 'property' attributes.");
        }

        Object topOfStack = getDigester().peek();

        if (topOfStack instanceof BaseConfig) {
            BaseConfig config = (BaseConfig) topOfStack;

            config.setProperty(attributes.getValue("key"),
                attributes.getValue("value"));
        } else {
            throw new IllegalArgumentException(
                "'key' attribute of <set-property> only applicable to subclasses of BaseConfig; "
                    + "object on top of stack is " + topOfStack
                    + " [key: " + attributes.getValue("key") + ", value: "
                    + attributes.getValue("value") + "]");
        }
    }
}
