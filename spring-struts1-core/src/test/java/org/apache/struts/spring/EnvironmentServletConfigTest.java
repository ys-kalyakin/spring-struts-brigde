package org.apache.struts.spring;

import java.net.URL;
import java.util.Collections;

import jakarta.servlet.ServletConfig;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockServletContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the {@link EnvironmentServletConfig} adapter: the classic
 * init-param contract resolved from the Spring environment with classic
 * defaults.
 */
class EnvironmentServletConfigTest {

    @Test
    void resolvesClassicDefaults() {
        ServletConfig config = new EnvironmentServletConfig(
            new MockServletContext(), new MockEnvironment());
        assertEquals("/WEB-INF/struts-config.xml",
            config.getInitParameter("config"));
        assertEquals("true", config.getInitParameter("validating"));
        assertNull(config.getInitParameter("unknown"));
        assertEquals("action", config.getServletName());
    }

    @Test
    void resolvesPropertiesOverDefaults() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("spring.struts1.config",
            "classpath:struts-config.xml");
        environment.setProperty("spring.struts1.validating", "false");
        ServletConfig config = new EnvironmentServletConfig(
            new MockServletContext(), environment, "actions");
        assertEquals("classpath:struts-config.xml",
            config.getInitParameter("config"));
        assertEquals("false", config.getInitParameter("validating"));
        assertEquals("actions", config.getServletName());
    }

    @Test
    void resolvesModuleConfigurations() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("spring.struts1.config.admin",
            "/WEB-INF/struts-admin.xml");
        ServletConfig config = new EnvironmentServletConfig(
            new MockServletContext(), environment);
        assertEquals("/WEB-INF/struts-admin.xml",
            config.getInitParameter("config/admin"));
    }

    @Test
    void enumeratesModuleInitParams() {
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("spring.struts1.config.admin",
            "/WEB-INF/struts-admin.xml");
        environment.setProperty("spring.struts1.config.upload",
            "/WEB-INF/struts-upload.xml");
        environment.setProperty("unrelated.property", "value");
        ServletConfig config = new EnvironmentServletConfig(
            new MockServletContext(), environment);
        java.util.List<String> names = Collections.list(
            config.getInitParameterNames());
        assertTrue(names.contains("config"), "config must be enumerated");
        assertTrue(names.contains("validating"),
            "validating must be enumerated");
        assertTrue(names.contains("config/admin"),
            "module configurations must be enumerated as config/<prefix>");
        assertTrue(names.contains("config/upload"));
        assertTrue(!names.contains("config.admin"),
            "raw property names must not leak through");
    }

    @Test
    void resolvesClasspathResourcesWithoutServletContext() {
        ServletConfig config = new EnvironmentServletConfig(null,
            new MockEnvironment());
        URL url = assertResolves(config, "classpath:registrar/struts-config.xml");
        assertNotNull(url, "classpath resources resolve without a servlet context");
    }

    @Test
    void nonClasspathResourcesNeedServletContext() {
        ServletConfig config = new EnvironmentServletConfig(null,
            new MockEnvironment());
        assertNull(assertResolves(config, "/WEB-INF/struts-config.xml"),
            "servlet-context resources are unresolvable without the context");
    }

    private URL assertResolves(ServletConfig config, String path) {
        try {
            return ((EnvironmentServletConfig) config).resolveResource(path);
        } catch (java.net.MalformedURLException e) {
            throw new AssertionError(e);
        }
    }
}
