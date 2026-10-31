package org.apache.struts.config;

import org.apache.commons.digester.AbstractObjectCreationFactory;
import org.apache.struts.util.RequestUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;


/**
 * An object creation factory which creates global forward instances.
 */
final class GlobalForwardFactory extends AbstractObjectCreationFactory {

    private static final Logger log =
        LoggerFactory.getLogger(GlobalForwardFactory.class);

    private final ClassLoader cl;

    GlobalForwardFactory(ClassLoader cl) {
        super();
        this.cl = cl;
    }

    @Override
    public Object createObject(Attributes attributes) throws Exception {
        String className = attributes.getValue("className");

        if (className == null) {
            ModuleConfig mc = (ModuleConfig) getDigester().peek();

            className = mc.getActionForwardClass();
        }

        try {
            return RequestUtils.applicationInstance(className, cl);
        } catch (Exception e) {
            log.error("GlobalForwardFactory.createObject: {}", className, e);
            throw e;
        }
    }
}
