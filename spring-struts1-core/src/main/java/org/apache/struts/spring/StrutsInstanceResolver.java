package org.apache.struts.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.config.ActionConfig;
import org.apache.struts.config.FormBeanConfig;

/**
 * Resolves <code>Action</code> and <code>ActionForm</code> instances through
 * Spring only: the instance is taken from the context by type — the
 * user-registered bean when the class is registered explicitly, otherwise
 * the definition the {@link StrutsEngineRegistrar} registered from the
 * configuration (singleton for actions, prototype for forms). Several beans
 * of one type are a startup configuration error. Dynamic (Dyna) forms stay
 * created through their {@link FormBeanConfig} (which wires the dynamic
 * class and applies the declared initial values). As a defensive fallback —
 * when the registrar has not run (no classic init-param contract) — the
 * instance is created by the autowire-capable bean factory, so every form
 * and action is a Spring-processed bean.
 */
public class StrutsInstanceResolver implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
        throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Resolve the action instance for the given mapping configuration.
     */
    public Action resolveAction(ActionConfig config) throws Exception {
        String type = config.getType();
        if (type == null || type.isEmpty()) {
            throw new IllegalStateException("Action of path "
                + config.getPath() + " declares no type");
        }
        Class<?> clazz = loadClass(type);
        String[] beanNames = applicationContext.getBeanNamesForType(clazz);
        if (beanNames.length > 1) {
            throw new IllegalStateException("Multiple Spring beans of type "
                + type + " are registered ("
                + String.join(", ", beanNames)
                + "); exactly one bean (or none) is required");
        }
        if (beanNames.length == 1) {
            return applicationContext.getBean(beanNames[0], Action.class);
        }
        return (Action) applicationContext.getAutowireCapableBeanFactory()
            .createBean(clazz, AutowireCapableBeanFactory.AUTOWIRE_NO,
                false);
    }

    /**
     * Resolve a fresh form instance for the given form bean configuration.
     */
    public ActionForm resolveForm(FormBeanConfig config) throws Exception {
        Class<?> clazz = loadClass(config.getType());
        if (DynaActionForm.class.isAssignableFrom(clazz)) {
            return config.createActionForm();
        }
        String[] beanNames = applicationContext.getBeanNamesForType(clazz);
        if (beanNames.length > 1) {
            throw new IllegalStateException("Multiple Spring beans of type "
                + config.getType() + " are registered ("
                + String.join(", ", beanNames)
                + "); exactly one bean (or none) is required");
        }
        if (beanNames.length == 1) {
            return applicationContext.getBean(beanNames[0], ActionForm.class);
        }
        return (ActionForm) applicationContext.getAutowireCapableBeanFactory()
            .createBean(clazz, AutowireCapableBeanFactory.AUTOWIRE_NO,
                false);
    }

    private Class<?> loadClass(String type) throws ClassNotFoundException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return (cl != null) ? cl.loadClass(type) : Class.forName(type);
    }
}
