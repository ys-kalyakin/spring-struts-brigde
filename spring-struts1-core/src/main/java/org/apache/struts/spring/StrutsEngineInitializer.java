package org.apache.struts.spring;

import java.util.EnumSet;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ServletConfigAware;
import org.springframework.web.filter.RequestContextFilter;

import jakarta.servlet.jsp.JspApplicationContext;
import jakarta.servlet.jsp.JspFactory;

import org.apache.struts.action.ActionServlet;

/**
 * Initializes the {@link ActionServlet} facade with the init-params of the
 * front <code>DispatcherServlet</code> (declared in web.xml), so the classic
 * <code>config</code> contract works unchanged, and wires the Spring JSP
 * stack into the web application: the {@link RequestContextFilter} for
 * request-context exposure and a {@link BeanNameResolver} that lets EL
 * expressions resolve Spring beans by name.
 */
public class StrutsEngineInitializer
    implements ServletConfigAware, ApplicationContextAware, InitializingBean {

    /** Servlet context attribute guarding the one-time EL registration. */
    public static final String BEAN_NAME_RESOLVER_KEY =
        "org.apache.struts.spring.beanNameResolver";

    private static final Logger log =
        LoggerFactory.getLogger(StrutsEngineInitializer.class);

    private final ActionServlet actionServlet;

    private ServletConfig servletConfig;

    private ApplicationContext applicationContext;

    public StrutsEngineInitializer(ActionServlet actionServlet) {
        this.actionServlet = actionServlet;
    }

    @Override
    public void setServletConfig(ServletConfig servletConfig) {
        this.servletConfig = servletConfig;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws ServletException {
        actionServlet.init(servletConfig);
        registerRequestContextFilter();
        registerBeanNameResolver();
    }

    /**
     * Register the Spring {@link RequestContextFilter} programmatically so
     * request-bound Spring facilities are available for direct JSP requests
     * as well. Containers that forbid registration at this point are
     * tolerated: the engine renders JSP through forwards from the
     * DispatcherServlet, where the context is already exposed.
     */
    private void registerRequestContextFilter() {
        try {
            ServletContext servletContext =
                servletConfig.getServletContext();
            if (servletContext
                .getFilterRegistration("springRequestContext") == null) {
                FilterRegistration.Dynamic filter = servletContext
                    .addFilter("springRequestContext", RequestContextFilter.class);
                if (filter != null) {
                    filter.addMappingForUrlPatterns(
                        EnumSet.of(DispatcherType.REQUEST), true, "/*");
                    log.debug("RequestContextFilter registered");
                }
            }
        } catch (RuntimeException e) {
            log.warn("RequestContextFilter was not registered: {}",
                e.getMessage());
        }
    }

    /**
     * Register the Spring bean-name resolver for EL once per servlet
     * context, before the first JSP is compiled.
     */
    private void registerBeanNameResolver() {
        try {
            ServletContext servletContext =
                servletConfig.getServletContext();
            if (servletContext.getAttribute(BEAN_NAME_RESOLVER_KEY) != null) {
                return;
            }
            JspFactory jspFactory = JspFactory.getDefaultFactory();
            if (jspFactory == null) {
                log.debug("No JspFactory yet; EL bean resolution stays "
                    + "unavailable");
                return;
            }
            JspApplicationContext jspApplicationContext =
                jspFactory.getJspApplicationContext(servletContext);
            jspApplicationContext.addELResolver(
                new jakarta.el.BeanNameELResolver(
                    new SpringBeanNameResolver(applicationContext)));
            servletContext.setAttribute(BEAN_NAME_RESOLVER_KEY,
                Boolean.TRUE);
            log.debug("Spring bean-name EL resolver registered");
        } catch (RuntimeException e) {
            log.warn("Spring BeanNameResolver was not registered: {}",
                e.getMessage());
        }
    }
}
