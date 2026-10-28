package org.apache.struts.spring;

import jakarta.el.PropertyNotWritableException;

import org.springframework.context.ApplicationContext;

/**
 * Exposes the beans of the application context to JSP Expression Language:
 * an EL identifier that is not a scoped attribute resolves to the singleton
 * Spring bean with that name. Prototype beans are deliberately not exposed
 * — EL evaluation must not create instances. The resolver is read-only.
 */
public class SpringBeanNameResolver extends jakarta.el.BeanNameResolver {

    private final ApplicationContext applicationContext;

    public SpringBeanNameResolver(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean isNameResolved(String beanName) {
        try {
            return applicationContext.containsBeanDefinition(beanName)
                && applicationContext.isSingleton(beanName);
        } catch (RuntimeException e) {
            return false;
        }
    }

    @Override
    public Object getBean(String beanName) {
        try {
            return applicationContext.getBean(beanName);
        } catch (RuntimeException e) {
            return null;
        }
    }

    @Override
    public boolean canCreateBean(String beanName) {
        return false;
    }

    @Override
    public boolean isReadOnly(String beanName) {
        return true;
    }

    @Override
    public void setBeanValue(String beanName, Object value)
        throws PropertyNotWritableException {
        throw new PropertyNotWritableException(
            "Spring beans are not writable through EL: " + beanName);
    }
}
