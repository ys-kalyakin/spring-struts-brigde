package org.apache.struts.config;

import org.apache.commons.digester.AbstractObjectCreationFactory;
import org.apache.struts.util.RequestUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;


/**
 * An object creation factory which creates local forward instances for
 * actions.
 */
final class ActionForwardFactory extends AbstractObjectCreationFactory {

    private static final Logger log =
        LoggerFactory.getLogger(ActionForwardFactory.class);

    private final ClassLoader cl;

    ActionForwardFactory(ClassLoader cl) {
        super();
        this.cl = cl;
    }

    @Override
    public Object createObject(Attributes attributes) throws Exception {
        String className = attributes.getValue("className");

        if (className == null) {
            ModuleConfig mc = (ModuleConfig) getDigester().peek(1);

            className = mc.getActionForwardClass();
        }

        try {
            return RequestUtils.applicationInstance(className, cl);
        } catch (Exception e) {
            log.error("ActionForwardFactory.createObject: {}", className, e);
            throw e;
        }
    }
}
