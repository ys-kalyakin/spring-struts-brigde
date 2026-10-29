package org.apache.struts.spring.boot;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties of the spring-struts1 starter
 * (namespace <code>spring.struts1</code>). The classic init-param contract
 * (<code>config</code>, <code>config/&lt;prefix&gt;</code>,
 * <code>validating</code>) is read directly from the
 * <code>Environment</code> by {@link org.apache.struts.spring.EnvironmentServletConfig}
 * — including module configurations (<code>spring.struts1.config.&lt;prefix&gt;</code>),
 * which cannot be bound as a typed map next to the scalar
 * <code>config</code> key. These typed properties serve the later beans
 * (servlet registration and handler-mapping synchronization).
 */
@ConfigurationProperties(prefix = "spring.struts1")
public class Struts1Properties {

    /** Master switch: when false, the starter registers nothing. */
    private boolean enabled = true;

    /**
     * Configuration resource(s) of the default module — the classic
     * <code>config</code> init-param (comma-separated list).
     */
    private String config = "/WEB-INF/struts-config.xml";

    /** Validate the configuration XML while parsing (the classic <code>validating</code> init-param). */
    private boolean validating = true;

    /** Name of the registered front servlet. */
    private String servletName = "action";

    private final Mapping mapping = new Mapping();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public boolean isValidating() {
        return validating;
    }

    public void setValidating(boolean validating) {
        this.validating = validating;
    }

    public String getServletName() {
        return servletName;
    }

    public void setServletName(String servletName) {
        this.servletName = servletName;
    }

    public Mapping getMapping() {
        return mapping;
    }

    /** Front servlet mapping settings. */
    public static class Mapping {

        /**
         * URL patterns the front servlet is mapped to (the classic
         * servlet-mapping of the action servlet).
         */
        private List<String> urlPattern = new ArrayList<>(List.of("*.do"));

        public List<String> getUrlPattern() {
            return urlPattern;
        }

        public void setUrlPattern(List<String> urlPattern) {
            this.urlPattern = urlPattern;
        }
    }
}
