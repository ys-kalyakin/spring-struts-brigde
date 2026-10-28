package org.apache.struts.config;

import org.apache.commons.digester.AbstractObjectCreationFactory;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.Rule;
import org.apache.commons.digester.RuleSetBase;
import org.apache.commons.digester.SetPropertyRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.RequestUtils;

/**
 * The set of Digester rules required to parse a Struts configuration file
 * (<code>struts-config.xml</code>).
 *
 * <p>Ported from weblegacy/struts1 1.5.x (org.apache.struts.config.ConfigRuleSet).</p>
 */
public class ConfigRuleSet extends RuleSetBase {

    /**
     * Add the set of Rule instances defined in this RuleSet to the specified
     * <code>Digester</code> instance. These rules assume that an instance of
     * <code>ModuleConfig</code> is pushed onto the evaluation stack before
     * parsing begins.
     */
    @Override
    public void addRuleInstances(Digester digester) {
        ClassLoader cl = digester.getClassLoader();

        digester.addRule("struts-config/set-property",
            new BaseConfigSetPropertyRule());

        digester.addRule("struts-config/action-mappings",
            new SetActionMappingClassRule());

        digester.addFactoryCreate("struts-config/action-mappings/action",
            new ActionMappingFactory(cl));
        digester.addSetProperties("struts-config/action-mappings/action");
        digester.addSetNext("struts-config/action-mappings/action",
            "addActionConfig", "org.apache.struts.config.ActionConfig");

        digester.addRule("struts-config/action-mappings/action/set-property",
            new BaseConfigSetPropertyRule());

        digester.addObjectCreate("struts-config/action-mappings/action/exception",
            "org.apache.struts.config.ExceptionConfig", "className");
        digester.addSetProperties(
            "struts-config/action-mappings/action/exception");
        digester.addSetNext("struts-config/action-mappings/action/exception",
            "addExceptionConfig", "org.apache.struts.config.ExceptionConfig");

        digester.addRule(
            "struts-config/action-mappings/action/exception/set-property",
            new BaseConfigSetPropertyRule());

        digester.addFactoryCreate(
            "struts-config/action-mappings/action/forward",
            new ActionForwardFactory(cl));
        digester.addSetProperties(
            "struts-config/action-mappings/action/forward");
        digester.addSetNext("struts-config/action-mappings/action/forward",
            "addForwardConfig", "org.apache.struts.config.ForwardConfig");

        digester.addRule(
            "struts-config/action-mappings/action/forward/set-property",
            new BaseConfigSetPropertyRule());

        digester.addObjectCreate("struts-config/controller",
            "org.apache.struts.config.ControllerConfig", "className");
        digester.addSetProperties("struts-config/controller");
        digester.addSetNext("struts-config/controller", "setControllerConfig",
            "org.apache.struts.config.ControllerConfig");

        digester.addRule("struts-config/controller/set-property",
            new BaseConfigSetPropertyRule());

        digester.addRule("struts-config/form-beans",
            new SetActionFormBeanClassRule());

        digester.addFactoryCreate("struts-config/form-beans/form-bean",
            new ActionFormBeanFactory(cl));
        digester.addSetProperties("struts-config/form-beans/form-bean");
        digester.addSetNext("struts-config/form-beans/form-bean",
            "addFormBeanConfig", "org.apache.struts.config.FormBeanConfig");

        digester.addObjectCreate(
            "struts-config/form-beans/form-bean/form-property",
            "org.apache.struts.config.FormPropertyConfig", "className");
        digester.addSetProperties(
            "struts-config/form-beans/form-bean/form-property");
        digester.addSetNext(
            "struts-config/form-beans/form-bean/form-property",
            "addFormPropertyConfig", "org.apache.struts.config.FormPropertyConfig");

        digester.addRule(
            "struts-config/form-beans/form-bean/form-property/set-property",
            new BaseConfigSetPropertyRule());

        digester.addRule("struts-config/form-beans/form-bean/set-property",
            new BaseConfigSetPropertyRule());

        digester.addObjectCreate("struts-config/global-exceptions/exception",
            "org.apache.struts.config.ExceptionConfig", "className");
        digester.addSetProperties("struts-config/global-exceptions/exception");
        digester.addSetNext("struts-config/global-exceptions/exception",
            "addExceptionConfig", "org.apache.struts.config.ExceptionConfig");

        digester.addRule(
            "struts-config/global-exceptions/exception/set-property",
            new BaseConfigSetPropertyRule());

        digester.addRule("struts-config/global-forwards",
            new SetActionForwardClassRule());

        digester.addFactoryCreate("struts-config/global-forwards/forward",
            new GlobalForwardFactory(cl));
        digester.addSetProperties("struts-config/global-forwards/forward");
        digester.addSetNext("struts-config/global-forwards/forward",
            "addForwardConfig", "org.apache.struts.config.ForwardConfig");

        digester.addRule(
            "struts-config/global-forwards/forward/set-property",
            new BaseConfigSetPropertyRule());

        digester.addObjectCreate("struts-config/message-resources",
            "org.apache.struts.config.MessageResourcesConfig", "className");
        digester.addSetProperties("struts-config/message-resources");
        digester.addSetNext("struts-config/message-resources",
            "addMessageResourcesConfig",
            "org.apache.struts.config.MessageResourcesConfig");

        digester.addRule("struts-config/message-resources/set-property",
            new BaseConfigSetPropertyRule());

        digester.addObjectCreate("struts-config/plug-in",
            "org.apache.struts.config.PlugInConfig");
        digester.addSetProperties("struts-config/plug-in");
        digester.addSetNext("struts-config/plug-in", "addPlugInConfig",
            "org.apache.struts.config.PlugInConfig");

        digester.addRule("struts-config/plug-in/set-property",
            new PlugInSetPropertyRule());
    }
}

/**
 * Class that records the name and value of a configuration property to be
 * used in configuring a <code>PlugIn</code> instance when instantiated.
 */
final class PlugInSetPropertyRule extends Rule {

    public PlugInSetPropertyRule() {
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

/**
 * Class that sets the name of the class to use when creating action form
 * bean instances.
 */
final class SetActionFormBeanClassRule extends Rule {

    public SetActionFormBeanClassRule() {
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

/**
 * A variant of the standard Digester <code>SetPropertyRule</code>. If the
 * element being processed has a "key" attribute, then the value will be used
 * to call <code>setProperty(key, value)</code> on the object on top of the
 * stack (assumed to be of type <code>BaseConfig</code>). Otherwise the
 * standard behaviour is invoked with "property" and "value" attributes.
 */
final class BaseConfigSetPropertyRule extends SetPropertyRule {

    public BaseConfigSetPropertyRule() {
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

/**
 * An object creation factory which creates action form bean instances,
 * taking into account the default class name which may have been specified
 * on the parent element.
 */
final class ActionFormBeanFactory extends AbstractObjectCreationFactory {

    private static final Logger log =
        LoggerFactory.getLogger(ActionFormBeanFactory.class);

    private final ClassLoader cl;

    public ActionFormBeanFactory(ClassLoader cl) {
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

/**
 * Class that sets the name of the class to use when creating action mapping
 * instances.
 */
final class SetActionMappingClassRule extends Rule {

    public SetActionMappingClassRule() {
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

/**
 * An object creation factory which creates action mapping instances.
 */
final class ActionMappingFactory extends AbstractObjectCreationFactory {

    private static final Logger log =
        LoggerFactory.getLogger(ActionMappingFactory.class);

    private final ClassLoader cl;

    public ActionMappingFactory(ClassLoader cl) {
        super();
        this.cl = cl;
    }

    @Override
    public Object createObject(Attributes attributes) throws Exception {
        String className = attributes.getValue("className");

        if (className == null) {
            ModuleConfig mc = (ModuleConfig) getDigester().peek();

            className = mc.getActionMappingClass();
        }

        try {
            return RequestUtils.applicationInstance(className, cl);
        } catch (Exception e) {
            log.error("ActionMappingFactory.createObject: {}", className, e);
            throw e;
        }
    }
}

/**
 * Class that sets the name of the class to use when creating global forward
 * instances.
 */
final class SetActionForwardClassRule extends Rule {

    public SetActionForwardClassRule() {
        super();
    }

    @Override
    public void begin(String namespace, String name, Attributes attributes)
        throws Exception {
        String className = attributes.getValue("type");

        if (className != null) {
            ModuleConfig mc = (ModuleConfig) getDigester().peek();

            mc.setActionForwardClass(className);
        }
    }
}

/**
 * An object creation factory which creates global forward instances.
 */
final class GlobalForwardFactory extends AbstractObjectCreationFactory {

    private static final Logger log =
        LoggerFactory.getLogger(GlobalForwardFactory.class);

    private final ClassLoader cl;

    public GlobalForwardFactory(ClassLoader cl) {
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

/**
 * An object creation factory which creates local forward instances for
 * actions.
 */
final class ActionForwardFactory extends AbstractObjectCreationFactory {

    private static final Logger log =
        LoggerFactory.getLogger(ActionForwardFactory.class);

    private final ClassLoader cl;

    public ActionForwardFactory(ClassLoader cl) {
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
