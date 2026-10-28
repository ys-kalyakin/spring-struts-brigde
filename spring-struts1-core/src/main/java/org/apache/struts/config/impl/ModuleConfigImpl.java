package org.apache.struts.config.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.struts.config.ActionConfig;
import org.apache.struts.config.ControllerConfig;
import org.apache.struts.config.ExceptionConfig;
import org.apache.struts.config.FormBeanConfig;
import org.apache.struts.config.ForwardConfig;
import org.apache.struts.config.MessageResourcesConfig;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.config.PlugInConfig;

/**
 * The default implementation of the {@link ModuleConfig} interface.
 *
 * <p>Ported from weblegacy/struts1 1.5.x
 * (org.apache.struts.config.impl.ModuleConfigImpl).</p>
 */
public class ModuleConfigImpl implements ModuleConfig {

    private static final long serialVersionUID = 1L;

    private static final Logger log =
        LoggerFactory.getLogger(ModuleConfigImpl.class);

    /** The controller configuration object for this module. */
    protected ControllerConfig controllerConfig = new ControllerConfig();

    /** Has this module been completely configured yet? */
    protected boolean configured = false;

    /** The set of action configurations for this module, keyed by path. */
    protected Map<String, ActionConfig> actionConfigs = new LinkedHashMap<>();

    /** The list of action configurations for this module, in registration order. */
    protected List<ActionConfig> actionConfigList = new ArrayList<>();

    /** Action configurations keyed by their <code>actionId</code>. */
    protected Map<String, ActionConfig> actionConfigIds = new HashMap<>();

    /** The set of exception configurations for this module, keyed by class name. */
    protected Map<String, ExceptionConfig> exceptions = new HashMap<>();

    /** The set of form bean configurations for this module, keyed by name. */
    protected Map<String, FormBeanConfig> formBeans = new HashMap<>();

    /** The set of global forward configurations for this module, keyed by name. */
    protected Map<String, ForwardConfig> forwards = new HashMap<>();

    /** The set of message resources configurations for this module, keyed by key. */
    protected Map<String, MessageResourcesConfig> messageResources =
        new HashMap<>();

    /** The set of plugin configurations for this module, in registration order. */
    protected List<PlugInConfig> plugIns = new ArrayList<>();

    /** The module prefix. */
    protected String prefix = "";

    /** The default class name to be used when creating action form bean instances. */
    protected String actionFormBeanClass =
        "org.apache.struts.action.ActionFormBean";

    /** The default class name to be used when creating action mapping instances. */
    protected String actionMappingClass =
        "org.apache.struts.action.ActionMapping";

    /** The default class name to be used when creating action forward instances. */
    protected String actionForwardClass =
        "org.apache.struts.action.ActionForward";

    public ModuleConfigImpl() {
    }

    public ModuleConfigImpl(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public boolean getConfigured() {
        return configured;
    }

    @Override
    public ControllerConfig getControllerConfig() {
        return controllerConfig;
    }

    @Override
    public void setControllerConfig(ControllerConfig cc) {
        throwIfConfigured();
        this.controllerConfig = cc;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String getActionFormBeanClass() {
        return actionFormBeanClass;
    }

    @Override
    public void setActionFormBeanClass(String actionFormBeanClass) {
        this.actionFormBeanClass = actionFormBeanClass;
    }

    @Override
    public String getActionMappingClass() {
        return actionMappingClass;
    }

    @Override
    public void setActionMappingClass(String actionMappingClass) {
        this.actionMappingClass = actionMappingClass;
    }

    @Override
    public void addActionConfig(ActionConfig config) {
        throwIfConfigured();
        config.setModuleConfig(this);

        String path = config.getPath();
        if (actionConfigs.containsKey(path)) {
            log.warn("Overriding ActionConfig of path {}", path);
        }

        String actionId = config.getActionId();
        if ((actionId != null) && !actionId.isEmpty()) {
            if (actionConfigIds.containsKey(actionId)) {
                log.warn("Overriding actionId[{}] for path[{}] with path[{}]",
                    actionId, actionConfigIds.get(actionId).getPath(), path);
            }
            actionConfigIds.put(actionId, config);
        }

        actionConfigs.put(path, config);
        actionConfigList.add(config);
    }

    @Override
    public void addExceptionConfig(ExceptionConfig config) {
        throwIfConfigured();

        String key = config.getType();
        if (exceptions.containsKey(key)) {
            log.warn("Overriding ExceptionConfig of type {}", key);
        }

        exceptions.put(key, config);
    }

    @Override
    public void addFormBeanConfig(FormBeanConfig config) {
        throwIfConfigured();

        String key = config.getName();
        if (formBeans.containsKey(key)) {
            log.warn("Overriding ActionForm of name {}", key);
        }

        formBeans.put(key, config);
    }

    @Override
    public String getActionForwardClass() {
        return actionForwardClass;
    }

    @Override
    public void setActionForwardClass(String actionForwardClass) {
        this.actionForwardClass = actionForwardClass;
    }

    @Override
    public void addForwardConfig(ForwardConfig config) {
        throwIfConfigured();

        String key = config.getName();
        if (forwards.containsKey(key)) {
            log.warn("Overriding global forward of name {}", key);
        }

        forwards.put(key, config);
    }

    @Override
    public void addMessageResourcesConfig(MessageResourcesConfig config) {
        throwIfConfigured();

        String key = config.getKey();
        if (messageResources.containsKey(key)) {
            log.warn("Overriding MessageResources of key {}", key);
        }

        messageResources.put(key, config);
    }

    @Override
    public void addPlugInConfig(PlugInConfig plugInConfig) {
        throwIfConfigured();
        plugIns.add(plugInConfig);
    }

    @Override
    public ActionConfig findActionConfig(String path) {
        return actionConfigs.get(path);
    }

    @Override
    public ActionConfig findActionConfigId(String actionId) {
        if (actionId != null) {
            return actionConfigIds.get(actionId);
        }
        return null;
    }

    @Override
    public ActionConfig[] findActionConfigs() {
        return actionConfigList.toArray(new ActionConfig[0]);
    }

    @Override
    public ExceptionConfig findExceptionConfig(String type) {
        return exceptions.get(type);
    }

    @Override
    public ExceptionConfig findException(Class<?> type) {
        while (type != null) {
            ExceptionConfig config = findExceptionConfig(type.getName());
            if (config != null) {
                return config;
            }
            type = type.getSuperclass();
        }
        return null;
    }

    @Override
    public ExceptionConfig[] findExceptionConfigs() {
        return exceptions.values().toArray(new ExceptionConfig[0]);
    }

    @Override
    public FormBeanConfig findFormBeanConfig(String name) {
        return formBeans.get(name);
    }

    @Override
    public FormBeanConfig[] findFormBeanConfigs() {
        return formBeans.values().toArray(new FormBeanConfig[0]);
    }

    @Override
    public ForwardConfig findForwardConfig(String name) {
        return forwards.get(name);
    }

    @Override
    public ForwardConfig[] findForwardConfigs() {
        return forwards.values().toArray(new ForwardConfig[0]);
    }

    @Override
    public MessageResourcesConfig findMessageResourcesConfig(String key) {
        return messageResources.get(key);
    }

    @Override
    public MessageResourcesConfig[] findMessageResourcesConfigs() {
        return messageResources.values().toArray(new MessageResourcesConfig[0]);
    }

    @Override
    public PlugInConfig[] findPlugInConfigs() {
        return plugIns.toArray(new PlugInConfig[0]);
    }

    @Override
    public void freeze() {
        configured = true;
    }

    protected void throwIfConfigured() {
        if (configured) {
            throw new IllegalStateException("Configuration is frozen");
        }
    }
}
