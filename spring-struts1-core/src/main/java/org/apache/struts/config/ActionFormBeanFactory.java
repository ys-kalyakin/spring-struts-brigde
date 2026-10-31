package org.apache.struts.config;

import org.apache.commons.digester.AbstractObjectCreationFactory;
import org.apache.struts.util.RequestUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;


/**
 * An object creation factory which creates action form bean instances,
 * taking into account the default class name which may have been specified
 * on the parent element.
 */
final class ActionFormBeanFactory extends AbstractObjectCreationFactory {

    private static final Logger log =
        LoggerFactory.getLogger(ActionFormBeanFactory.class);

    private final ClassLoader cl;

    ActionFormBeanFactory(ClassLoader cl) {
        super();
        this.cl = cl;
    }

    @Override
    public Object createObject(Attributes attributes) throws Exception {
        String className = attributes.getValue("className");

        if (className == null) {
            ModuleConfig mc = (ModuleConfig) getDigester().peek();

            className = mc.getActionFormBeanClass();
        }

        try {
            return RequestUtils.applicationInstance(className, cl);
        } catch (Exception e) {
            log.error("ActionFormBeanFactory.createObject: {}", className, e);
            throw e;
        }
    }
}
