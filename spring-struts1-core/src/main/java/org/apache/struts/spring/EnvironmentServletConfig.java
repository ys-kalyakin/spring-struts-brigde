package org.apache.struts.spring;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;

import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

/**
 * <code>ServletConfig</code> adapter backed by the Spring
 * <code>Environment</code>: the classic init-param contract
 * (<code>config</code>, <code>config/&lt;prefix&gt;</code>,
 * <code>validating</code>) is resolved lazily from the properties
 * <code>spring.struts1.config</code>,
 * <code>spring.struts1.config.&lt;prefix&gt;</code> and
 * <code>spring.struts1.validating</code>, with the classic defaults.
 *
 * <p>Used in Spring Boot mode where no front servlet with classic init-params
 * exists: the engine reads the same parameters from the environment.
 * Configuration resources are resolved from the classpath for
 * <code>classpath:</code> paths and from the servlet context otherwise; when
 * no servlet context is available yet (Boot applications before the web
 * server starts), non-classpath resources cannot be resolved.</p>
 */
public class EnvironmentServletConfig implements ServletConfig {

    /** Default value of the classic <code>config</code> init-param. */
    public static final String DEFAULT_CONFIG = "/WEB-INF/struts-config.xml";

    /** Property holding the default-module configuration resource(s). */
    public static final String CONFIG_PROPERTY = "spring.struts1.config";

    /** Prefix of the module configuration properties (<code>config/&lt;prefix&gt;</code>). */
    public static final String MODULE_PROPERTY_PREFIX = "spring.struts1.config.";

    /** Property holding the XML-validating flag. */
    public static final String VALIDATING_PROPERTY = "spring.struts1.validating";

    private final ServletContext servletContext;

    private final Environment environment;

    private final String servletName;

    public EnvironmentServletConfig(ServletContext servletContext,
        Environment environment) {
        this(servletContext, environment, "action");
    }

    public EnvironmentServletConfig(ServletContext servletContext,
        Environment environment, String servletName) {
        this.servletContext = servletContext;
        this.environment = environment;
        this.servletName = servletName;
    }

    @Override
    public String getServletName() {
        return servletName;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public String getInitParameter(String name) {
        if ("config".equals(name)) {
            return environment.getProperty(CONFIG_PROPERTY, DEFAULT_CONFIG);
        }
        if (name != null && name.startsWith("config/")) {
            return environment.getProperty(
                MODULE_PROPERTY_PREFIX + name.substring("config/".length()));
        }
        if ("validating".equals(name)) {
            return environment.getProperty(VALIDATING_PROPERTY, "true");
        }
        return null;
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        Set<String> names = new TreeSet<>();
        names.add("config");
        names.add("validating");
        if (environment instanceof AbstractEnvironment abstractEnvironment) {
            for (PropertySource<?> source
                : abstractEnvironment.getPropertySources()) {
                if (source instanceof EnumerablePropertySource<?> enumerable) {
                    for (String key : enumerable.getPropertyNames()) {
                        if (key.startsWith(MODULE_PROPERTY_PREFIX)) {
                            names.add("config/" + key.substring(
                                MODULE_PROPERTY_PREFIX.length()));
                        }
                    }
                }
            }
        }
        return Collections.enumeration(names);
    }

    /**
     * Resolve a configuration resource: <code>classpath:</code> paths through
     * the class loader, other paths through the servlet context (which may be
     * unavailable in Boot applications before the web server starts).
     */
    public URL resolveResource(String path) throws MalformedURLException {
        if (path != null && path.startsWith("classpath:")) {
            String resource = path.substring("classpath:".length());
            if (resource.startsWith("/")) {
                resource = resource.substring(1);
            }
            ClassLoader loader =
                Thread.currentThread().getContextClassLoader();
            if (loader == null) {
                loader = EnvironmentServletConfig.class.getClassLoader();
            }
            return loader.getResource(resource);
        }
        return (servletContext != null) ? servletContext.getResource(path)
            : null;
    }
}
