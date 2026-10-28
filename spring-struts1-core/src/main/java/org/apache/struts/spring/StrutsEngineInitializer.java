package org.apache.struts.spring;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.context.ServletConfigAware;

import org.apache.struts.action.ActionServlet;

/**
 * Initializes the {@link ActionServlet} facade with the init-params of the
 * front <code>DispatcherServlet</code> (declared in web.xml), so the classic
 * <code>config</code> contract works unchanged.
 */
public class StrutsEngineInitializer
    implements ServletConfigAware, InitializingBean {

    private final ActionServlet actionServlet;

    private ServletConfig servletConfig;

    public StrutsEngineInitializer(ActionServlet actionServlet) {
        this.actionServlet = actionServlet;
    }

    @Override
    public void setServletConfig(ServletConfig servletConfig) {
        this.servletConfig = servletConfig;
    }

    @Override
    public void afterPropertiesSet() throws ServletException {
        actionServlet.init(servletConfig);
    }
}
