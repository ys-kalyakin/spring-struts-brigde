package org.apache.struts.spring;

import java.util.concurrent.ConcurrentHashMap;

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
 *
 * <p>Per-request cost: the type → class and type → bean-name lookups are
 * cached per resolver instance (bean-definition scanning does not repeat on
 * every request). Only successful lookups are cached — a multi-bean
 * configuration error is re-evaluated on every resolution — and the cached
 * data is the lookup result, never the instance itself: actions keep their
 * singleton semantics and request-scoped forms still receive a fresh
 * instance per resolution. Bean definitions registered in the context after
 * the first resolution are therefore not seen; the engine registers its own
 * beans during startup, and explicit user registration happens before
 * request processing.</p>
 */
public class StrutsInstanceResolver implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    /** Loaded configuration classes, keyed by declared type name. */
    private final ConcurrentHashMap<String, Class<?>> classes =
        new ConcurrentHashMap<>();

    /** Successful bean-name lookups, keyed by declared type name. */
    private final ConcurrentHashMap<String, String[]> beanNames =
        new ConcurrentHashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
        throws BeansException {
        this.applicationContext = applicationContext;
        this.classes.clear();
        this.beanNames.clear();
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
        String[] names = beanNamesForType(type, clazz);
        if (names.length == 1) {
            return applicationContext.getBean(names[0], Action.class);
        }
        return (Action) applicationContext.getAutowireCapableBeanFactory()
            .createBean(clazz, AutowireCapableBeanFactory.AUTOWIRE_NO,
                false);
    }

    /**
     * Resolve a fresh form instance for the given form bean configuration.
     */
    public ActionForm resolveForm(FormBeanConfig config) throws Exception {
        String type = config.getType();
        Class<?> clazz = loadClass(type);
        if (DynaActionForm.class.isAssignableFrom(clazz)) {
            return config.createActionForm();
        }
        String[] names = beanNamesForType(type, clazz);
        if (names.length == 1) {
            return applicationContext.getBean(names[0], ActionForm.class);
        }
        return (ActionForm) applicationContext.getAutowireCapableBeanFactory()
            .createBean(clazz, AutowireCapableBeanFactory.AUTOWIRE_NO,
                false);
    }

    /**
     * Return the bean names of the given type through the cache: the
     * (expensive, whole-context) bean-definition scan runs once per type;
     * the multi-bean configuration error is re-evaluated on every call and
     * never cached.
     */
    private String[] beanNamesForType(String type, Class<?> clazz) {
        String[] cached = beanNames.get(type);
        if (cached != null) {
            return cached;
        }
        String[] found = applicationContext.getBeanNamesForType(clazz);
        if (found.length > 1) {
            throw new IllegalStateException("Multiple Spring beans of type "
                + type + " are registered ("
                + String.join(", ", found)
                + "); exactly one bean (or none) is required");
        }
        beanNames.put(type, found);
        return found;
    }

    private Class<?> loadClass(String type) throws ClassNotFoundException {
        Class<?> cached = classes.get(type);
        if (cached != null) {
            return cached;
        }
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Class<?> clazz = (cl != null) ? cl.loadClass(type)
            : Class.forName(type);
        classes.put(type, clazz);
        return clazz;
    }
}
