package org.apache.struts.tiles;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;

import org.apache.struts.action.ActionServlet;
import org.apache.struts.config.PlugInConfig;
import org.apache.struts.config.impl.ModuleConfigImpl;

import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;

/**
 * Unit tests of TilesPlugin initialization: definitions-config loading, the
 * classic servlet-context factory key, the Spring definition-forward hook
 * registration and the single-factory-per-module guarantee.
 */
class TilesPluginTest {

    private static final String DEFINITIONS_FACTORY =
        "org.apache.struts.tiles.DEFINITIONS_FACTORY";

    private AnnotationConfigWebApplicationContext applicationContext;

    @AfterAll
    static void resetTilesUtil() {
        org.apache.struts.tiles.testsupport.TilesStaticReset.resetTilesUtil();
    }

    private ActionServlet servletWith(MockServletContext sc) throws Exception {
        applicationContext = new AnnotationConfigWebApplicationContext();
        applicationContext.refresh();
        ActionServlet servlet = new ActionServlet();
        MockServletConfig config = new MockServletConfig(sc, "action");
        config.addInitParameter("config", "/WEB-INF/struts-config.xml");
        servlet.setApplicationContext(applicationContext);
        servlet.init(config);
        return servlet;
    }

    private MockServletContext contextServingConfigFixture() {
        return new MockServletContext() {
            @Override
            public java.net.URL getResource(String path)
                throws java.net.MalformedURLException {
                return "/WEB-INF/struts-config.xml".equals(path)
                    ? TilesPluginTest.class.getResource(
                        "/org/apache/struts/config/struts-config-a.xml")
                    : super.getResource(path);
            }
        };
    }

    private TilesPlugin initPlugin(ActionServlet servlet, String prefix,
        String definitionsConfig) throws Exception {
        PlugInConfig pluginConfig = new PlugInConfig();
        pluginConfig.setClassName(TilesPlugin.class.getName());
        pluginConfig.addProperty("definitions-config", definitionsConfig);
        pluginConfig.addProperty("moduleAware", "false");

        TilesPlugin plugin = new TilesPlugin();
        plugin.setCurrentPlugInConfigObject(pluginConfig);
        plugin.init(servlet, new ModuleConfigImpl(prefix));
        return plugin;
    }

    @Test
    void initializesFactoryAndSpringHook() throws Exception {
        MockServletContext sc = contextServingConfigFixture();
        ActionServlet servlet = servletWith(sc);
        initPlugin(servlet, "",
            "/org/apache/struts/tiles/fixtures/tiles-defs.xml");

        Object factory = sc.getAttribute(DEFINITIONS_FACTORY);
        assertNotNull(factory, "factory stored under the classic key");
        assertTrue(factory instanceof DefinitionsFactory);

        // the engine hook is a bean in the application context
        assertNotNull(applicationContext.getBeanFactory()
            .getBean("tilesDefinitionForwardResolver"));
    }

    @Test
    void secondFactoryForSameModuleFails() throws Exception {
        MockServletContext sc = contextServingConfigFixture();
        ActionServlet servlet = servletWith(sc);
        initPlugin(servlet, "",
            "/org/apache/struts/tiles/fixtures/tiles-defs.xml");

        assertThrows(UnavailableException.class, () -> initPlugin(servlet,
            "", "/org/apache/struts/tiles/fixtures/tiles-defs.xml"));
    }

    @Test
    void missingDefinitionsFileFailsStartup() throws Exception {
        MockServletContext sc = contextServingConfigFixture();
        ActionServlet servlet = servletWith(sc);
        // explicit definitions-config that can't be opened -> FactoryNotFound
        // (a DefinitionsFactoryException), wrapped by the plugin
        ServletException e = assertThrows(ServletException.class,
            () -> initPlugin(servlet, "", "/no/such/tiles-defs.xml"));
        assertTrue(e.getCause() instanceof FactoryNotFoundException);
    }

    @Test
    void destroyReleasesFactory() throws Exception {
        MockServletContext sc = contextServingConfigFixture();
        ActionServlet servlet = servletWith(sc);
        TilesPlugin plugin = initPlugin(servlet, "",
            "/org/apache/struts/tiles/fixtures/tiles-defs.xml");
        assertNotNull(sc.getAttribute(DEFINITIONS_FACTORY));
        plugin.destroy();
    }

    @Test
    void moduleAwarePrefixesFactoryKey() throws Exception {
        MockServletContext sc = contextServingConfigFixture();
        ActionServlet servlet = servletWith(sc);

        PlugInConfig pluginConfig = new PlugInConfig();
        pluginConfig.setClassName(TilesPlugin.class.getName());
        pluginConfig.addProperty("definitions-config",
            "/org/apache/struts/tiles/fixtures/tiles-defs.xml");
        pluginConfig.addProperty("moduleAware", "true");
        TilesPlugin plugin = new TilesPlugin();
        plugin.setCurrentPlugInConfigObject(pluginConfig);
        plugin.init(servlet, new ModuleConfigImpl("/tiles"));

        assertNotNull(sc.getAttribute(DEFINITIONS_FACTORY + "/tiles"),
            "module-aware factory stored with the module prefix");
    }
}
