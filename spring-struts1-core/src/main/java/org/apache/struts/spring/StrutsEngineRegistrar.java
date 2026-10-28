package org.apache.struts.spring;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.web.context.ServletConfigAware;

import org.apache.struts.Globals;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.config.MessageResourcesConfig;
import org.apache.struts.config.ModuleConfig;
import org.apache.struts.config.ModuleConfigFactory;
import org.apache.struts.config.StrutsConfigParser;

/**
 * Registers the types declared in <code>struts-config.xml</code> as Spring
 * bean definitions during the bean definition phase of the context refresh:
 * action types as lazy singletons, form types as prototypes, message
 * resource bundles as {@link org.springframework.context.MessageSource}
 * beans. Classes already registered explicitly by the user (component scan,
 * Java or XML configuration) are not touched — the user definition wins, and
 * several explicit registrations of the same type fail startup. Dynamic
 * (Dyna) forms stay created through their {@code FormBeanConfig} and are not
 * registered.
 *
 * <p>The registrar runs as a {@link BeanDefinitionRegistryPostProcessor} so
 * its definitions exist before the context initializes its internal
 * <code>messageSource</code> and before any engine bean is instantiated.
 * When no front servlet <code>ServletConfig</code> is available (the engine
 * is imported into a context without the classic init-param contract), the
 * registrar does nothing.</p>
 */
public class StrutsEngineRegistrar
    implements BeanDefinitionRegistryPostProcessor, ServletConfigAware {

    /** Prefix of the auto-registered singleton bean of an action type. */
    public static final String ACTION_BEAN_PREFIX = "strutsAction:";

    /** Prefix of the auto-registered prototype bean of a form type. */
    public static final String FORM_BEAN_PREFIX = "strutsForm:";

    /** Prefix of the MessageSource bean registered for a message bundle. */
    public static final String MESSAGE_SOURCE_BEAN_PREFIX =
        "strutsMessageSource:";

    private static final Logger log =
        LoggerFactory.getLogger(StrutsEngineRegistrar.class);

    private ServletConfig servletConfig;

    @Override
    public void setServletConfig(ServletConfig servletConfig) {
        this.servletConfig = servletConfig;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(
        BeanDefinitionRegistry registry) throws BeansException {
        if (servletConfig == null) {
            log.debug("No servlet config provided; struts-config types are "
                + "not registered as Spring beans");
            return;
        }
        if (!(registry instanceof ConfigurableListableBeanFactory
                beanFactory)) {
            throw new IllegalStateException(
                "StrutsEngineRegistrar requires a listable bean factory");
        }

        Map<String, ModuleConfig> modules = parseModules();

        Set<String> processedTypes = new HashSet<>();
        for (Map.Entry<String, ModuleConfig> entry : modules.entrySet()) {
            ModuleConfig module = entry.getValue();
            for (org.apache.struts.config.ActionConfig actionConfig
                : module.findActionConfigs()) {
                String type = actionConfig.getType();
                if (type != null && !type.isEmpty()) {
                    registerAction(beanFactory, registry, type,
                        processedTypes);
                }
            }
            for (org.apache.struts.config.FormBeanConfig formConfig
                : module.findFormBeanConfigs()) {
                String type = formConfig.getType();
                if (type != null && !type.isEmpty()) {
                    registerForm(beanFactory, registry, type, processedTypes);
                }
            }
            registerMessageSources(registry, beanFactory, module);
        }
    }

    @Override
    public void postProcessBeanFactory(
        ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // Nothing to do: all definitions are registered in the registry pass.
    }

    /**
     * Parse the module configurations from the classic init-params
     * (<code>config</code>, <code>config/&lt;prefix&gt;</code>), the default
     * module first.
     */
    private Map<String, ModuleConfig> parseModules() {
        Map<String, ModuleConfig> modules = new TreeMap<>();
        String config = initParam("config", "/WEB-INF/struts-config.xml");
        boolean validating = Boolean.parseBoolean(
            initParam("validating", "true"));
        parseModule(modules, "", config, validating);
        java.util.Enumeration<String> names =
            servletConfig.getInitParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            if (name.startsWith("config/")) {
                parseModule(modules,
                    "/" + name.substring("config/".length()),
                    servletConfig.getInitParameter(name), validating);
            }
        }
        return modules;
    }

    private void parseModule(Map<String, ModuleConfig> modules, String prefix,
        String configValue, boolean validating) {
        ModuleConfig module = ModuleConfigFactory.createFactory()
            .createModuleConfig(prefix);
        for (String file : configValue.split("\\s*,\\s*")) {
            String path = file.trim();
            if (path.isEmpty()) {
                continue;
            }
            URL url;
            try {
                url = resourceUrl(path);
            } catch (MalformedURLException e) {
                throw new IllegalStateException(
                    "Malformed configuration resource path " + path, e);
            }
            if (url == null) {
                throw new IllegalStateException(
                    "Missing configuration resource " + path);
            }
            try {
                StrutsConfigParser.parse(url, module, validating);
            } catch (ServletException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
        modules.put(prefix, module);
    }

    private String initParam(String name, String defaultValue) {
        String value = servletConfig.getInitParameter(name);
        return (value != null) ? value : defaultValue;
    }

    /**
     * Resolve a configuration resource path against the servlet context;
     * overridable in tests.
     */
    protected URL resourceUrl(String path) throws MalformedURLException {
        return servletConfig.getServletContext().getResource(path);
    }

    /**
     * Register the action type as a lazy singleton, unless the user
     * registered the class explicitly; several explicit registrations of the
     * same type fail the startup.
     */
    private void registerAction(ConfigurableListableBeanFactory beanFactory,
        BeanDefinitionRegistry registry, String type,
        Set<String> processedTypes) {
        if (!processedTypes.add("action:" + type)) {
            return;
        }
        Class<?> clazz = loadClassQuietly(type);
        if (clazz == null || !Action.class.isAssignableFrom(clazz)) {
            return;
        }
        String[] userBeans =
            beanFactory.getBeanNamesForType(clazz, true, false);
        if (userBeans.length > 1) {
            throw new IllegalStateException("Multiple Spring beans of type "
                + type + " are registered ("
                + String.join(", ", userBeans)
                + "); exactly one explicit registration is allowed");
        }
        if (userBeans.length == 1) {
            log.debug("Action type {} is registered as user bean {}", type,
                userBeans[0]);
            return;
        }
        registry.registerBeanDefinition(ACTION_BEAN_PREFIX + type,
            BeanDefinitionBuilder.genericBeanDefinition(clazz)
                .setScope(BeanDefinition.SCOPE_SINGLETON)
                .setLazyInit(true)
                .getBeanDefinition());
    }

    /**
     * Register the form type as a prototype, unless the user registered the
     * class explicitly or the form is dynamic (Dyna forms stay created
     * through their form bean configuration).
     */
    private void registerForm(ConfigurableListableBeanFactory beanFactory,
        BeanDefinitionRegistry registry, String type,
        Set<String> processedTypes) {
        if (!processedTypes.add("form:" + type)) {
            return;
        }
        Class<?> clazz = loadClassQuietly(type);
        if (clazz == null || !ActionForm.class.isAssignableFrom(clazz)
            || DynaActionForm.class.isAssignableFrom(clazz)) {
            return;
        }
        String[] userBeans =
            beanFactory.getBeanNamesForType(clazz, true, false);
        if (userBeans.length > 1) {
            throw new IllegalStateException("Multiple Spring beans of type "
                + type + " are registered ("
                + String.join(", ", userBeans)
                + "); exactly one explicit registration is allowed");
        }
        if (userBeans.length == 1) {
            log.debug("Form type {} is registered as user bean {}", type,
                userBeans[0]);
            return;
        }
        AbstractBeanDefinition definition =
            BeanDefinitionBuilder.genericBeanDefinition(clazz)
                .setScope(BeanDefinition.SCOPE_PROTOTYPE)
                .getBeanDefinition();
        registry.registerBeanDefinition(FORM_BEAN_PREFIX + type, definition);
    }

    /**
     * Register a {@link MessageSource} bean for every message bundle of the
     * module; the default bundle of the default module additionally becomes
     * the context <code>messageSource</code> when the user has not defined
     * one.
     */
    private void registerMessageSources(BeanDefinitionRegistry registry,
        ConfigurableListableBeanFactory beanFactory, ModuleConfig module) {
        String prefix = module.getPrefix();
        for (MessageResourcesConfig resourcesConfig
            : module.findMessageResourcesConfigs()) {
            String key = resourcesConfig.getKey();
            String beanName = MESSAGE_SOURCE_BEAN_PREFIX + prefix + key;
            if (!registry.containsBeanDefinition(beanName)) {
                registry.registerBeanDefinition(beanName,
                    messageSourceDefinition(key, prefix));
            }
            if (prefix.isEmpty() && Globals.MESSAGES_KEY.equals(key)
                && !registry.containsBeanDefinition("messageSource")
                && !beanFactory.containsSingleton("messageSource")) {
                registry.registerBeanDefinition("messageSource",
                    messageSourceDefinition(key, prefix));
            }
        }
    }

    private AbstractBeanDefinition messageSourceDefinition(String key,
        String prefix) {
        return BeanDefinitionBuilder
            .genericBeanDefinition(MessageResourcesMessageSource.class)
            .addConstructorArgValue(key)
            .addConstructorArgValue(prefix)
            .getBeanDefinition();
    }

    /**
     * Load the type for registration; a class that cannot be loaded is
     * skipped here and keeps failing at request time with the classic
     * ClassNotFoundException.
     */
    private Class<?> loadClassQuietly(String type) {
        try {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            return (cl != null) ? cl.loadClass(type) : Class.forName(type);
        } catch (ClassNotFoundException e) {
            log.warn("Type {} cannot be loaded; it is not registered as a "
                + "Spring bean", type);
            return null;
        }
    }
}
