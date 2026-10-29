package org.apache.struts.spring.boot;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.ServletContextInitializer;

import org.apache.catalina.servlets.DefaultServlet;

/**
 * Registers the container default servlet when the runtime does not provide
 * one: classic Struts applications serve static resources from the webapp
 * root (HTML, CSS, images, applets) and the engine forwards to them, while
 * Spring Boot does not register the container default servlet for embedded
 * containers. In a standalone container (WAR deployment) the servlet already
 * exists and nothing is registered.
 */
public class DefaultServletInitializer
    implements ServletContextInitializer {

    private static final Logger log =
        LoggerFactory.getLogger(DefaultServletInitializer.class);

    @Override
    public void onStartup(ServletContext servletContext) {
        // The registered servlet is Tomcat's: only meaningful when the
        // runtime is an embedded Tomcat (standalone containers and other
        // engines provide their own static resource handling).
        String serverInfo = servletContext.getServerInfo();
        if (serverInfo == null || !serverInfo.contains("Tomcat")) {
            log.debug("Not a Tomcat runtime ({}); the default servlet "
                + "stays with the container", serverInfo);
            return;
        }
        if (servletContext.getServletRegistration("default") != null) {
            log.debug("The container provides the default servlet");
            return;
        }
        ServletRegistration.Dynamic registration =
            servletContext.addServlet("default", DefaultServlet.class);
        if (registration == null) {
            return;
        }
        registration.setInitParameter("listings", "false");
        registration.setLoadOnStartup(1);
        registration.addMapping("/");
        log.debug("Container default servlet registered for the classic "
            + "webapp resources");
    }
}
