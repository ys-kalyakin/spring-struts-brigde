package org.apache.struts.config;

import java.io.Serializable;

/**
 * The collection of static configuration information that describes a Struts
 *-based module. Multiple modules are supported by providing multiple
 * configuration files.
 *
 * <p>Ported from weblegacy/struts1 1.5.x (org.apache.struts.config.ModuleConfig);
 * multi-module support is deferred to a later phase.</p>
 */
public interface ModuleConfig extends Serializable {

    String getPrefix();

    void setPrefix(String prefix);

    ControllerConfig getControllerConfig();

    void setControllerConfig(ControllerConfig cc);

    String getActionFormBeanClass();

    void setActionFormBeanClass(String actionFormBeanClass);

    String getActionMappingClass();

    void setActionMappingClass(String actionMappingClass);

    void addActionConfig(ActionConfig config);

    void addExceptionConfig(ExceptionConfig config);

    void addFormBeanConfig(FormBeanConfig config);

    String getActionForwardClass();

    void setActionForwardClass(String actionForwardClass);

    void addForwardConfig(ForwardConfig config);

    void addMessageResourcesConfig(MessageResourcesConfig config);

    void addPlugInConfig(PlugInConfig plugInConfig);

    ActionConfig findActionConfig(String path);

    ActionConfig[] findActionConfigs();

    ExceptionConfig findExceptionConfig(String type);

    ExceptionConfig findException(Class<?> type);

    ExceptionConfig[] findExceptionConfigs();

    FormBeanConfig findFormBeanConfig(String name);

    FormBeanConfig[] findFormBeanConfigs();

    ForwardConfig findForwardConfig(String name);

    ForwardConfig[] findForwardConfigs();

    MessageResourcesConfig findMessageResourcesConfig(String key);

    MessageResourcesConfig[] findMessageResourcesConfigs();

    PlugInConfig[] findPlugInConfigs();

    void freeze();

    boolean getConfigured();
}
