package org.apache.struts.spring;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.config.ActionConfig;
import org.apache.struts.config.FormBeanConfig;

/**
 * Resolves <code>Action</code> and <code>ActionForm</code> instances:
 * classes registered as Spring beans are taken from the context (respecting
 * their scope — singleton for actions, prototype for forms); classes without
 * a Spring registration are created directly by the type name from the
 * configuration (classic fallback mode). Directly created actions are cached
 * per class like classic Struts does.
 */
public class StrutsInstanceResolver implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    /** Cache of directly created actions, keyed by class name. */
    private final Map<String, Action> actionCache = new ConcurrentHashMap<>();

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
        if (beanNames.length > 0) {
            return applicationContext.getBean(beanNames[0], Action.class);
        }
        // Classic fallback: create directly and reuse between requests.
        return actionCache.computeIfAbsent(type, key -> {
            try {
                return (Action) instantiate(clazz);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(
                    "Cannot create action of type " + type, e);
            }
        });
    }

    /**
     * Resolve a fresh form instance for the given form bean configuration:
     * a registered prototype bean yields a new instance per call, the
     * classic fallback creates directly.
     */
    public ActionForm resolveForm(FormBeanConfig config) throws Exception {
        String type = config.getType();
        Class<?> clazz = loadClass(type);

        String[] beanNames = applicationContext.getBeanNamesForType(clazz);
        if (beanNames.length > 0) {
            return applicationContext.getBean(beanNames[0], ActionForm.class);
        }
        return (ActionForm) instantiate(clazz);
    }

    private Class<?> loadClass(String type) throws ClassNotFoundException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        return (cl != null) ? cl.loadClass(type) : Class.forName(type);
    }

    private Object instantiate(Class<?> clazz)
        throws ReflectiveOperationException {
        Object instance = clazz.getDeclaredConstructor().newInstance();
        if (instance instanceof Action || instance instanceof ActionForm) {
            return instance;
        }
        throw new IllegalStateException("Type " + clazz.getName()
            + " is neither an Action nor an ActionForm");
    }
}
