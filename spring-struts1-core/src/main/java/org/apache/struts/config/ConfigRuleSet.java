package org.apache.struts.config;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSetBase;


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

