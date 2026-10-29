package org.apache.struts.spring.boot;

import jakarta.servlet.ServletContext;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.web.context.ServletConfigAware;
import org.springframework.web.context.WebApplicationContext;

import org.apache.struts.spring.EnvironmentServletConfig;
import org.apache.struts.spring.StrutsHandlerMapping;

/**
 * Applies the Boot-mode configuration to the engine beans of the root
 * context: injects the {@link EnvironmentServletConfig} adapter into the
 * remaining {@link ServletConfigAware} beans (the registrar builds its own
 * adapter earlier, during the bean-definition phase) and synchronizes the
 * handler mapping with <code>spring.struts1.mapping.url-pattern</code>.
 *
 * <p>The handler mapping is also promoted to the highest precedence: in the
 * Boot root context the MVC resource handler (<code>/**</code>) would
 * otherwise intercept action requests before the Struts mapping is
 * consulted.</p>
 *
 * <p>Registered as a static bean so it exists before regular singletons are
 * created; the servlet context is resolved lazily at injection time, when
 * the Boot web server has already created it.</p>
 */
public class StrutsBootConfigurer implements BeanPostProcessor,
    ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
        throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean,
        String beanName) throws BeansException {
        if (bean instanceof ServletConfigAware aware) {
            EnvironmentServletConfig config = buildServletConfig();
            if (config.getServletContext() != null) {
                aware.setServletConfig(config);
            }
        }
        if (bean instanceof StrutsHandlerMapping mapping) {
            mapping.setOrder(Ordered.HIGHEST_PRECEDENCE);
            mapping.setServletMapping(servletMapping());
        }
        return bean;
    }

    private EnvironmentServletConfig buildServletConfig() {
        ServletContext servletContext = servletContext();
        Environment environment = applicationContext.getEnvironment();
        return new EnvironmentServletConfig(servletContext, environment);
    }

    private ServletContext servletContext() {
        return (applicationContext instanceof WebApplicationContext webContext)
            ? webContext.getServletContext()
            : null;
    }

    private String servletMapping() {
        String patterns = applicationContext.getEnvironment().getProperty(
            "spring.struts1.mapping.url-pattern", "*.do");
        int comma = patterns.indexOf(',');
        return (comma >= 0) ? patterns.substring(0, comma).trim()
            : patterns.trim();
    }
}
