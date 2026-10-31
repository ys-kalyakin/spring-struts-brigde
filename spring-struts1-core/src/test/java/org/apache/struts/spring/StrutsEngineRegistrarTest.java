package org.apache.struts.spring;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

import jakarta.servlet.ServletConfig;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.mock.web.MockServletConfig;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

import org.apache.struts.fixtures.FixtureAction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the engine registrar: types declared in struts-config are
 * registered as Spring bean definitions (actions singleton lazy, forms
 * prototype, bundles as MessageSource), user-registered classes are
 * respected and duplicates fail the startup.
 */
class StrutsEngineRegistrarTest {

    private static final String FIXTURE_CONFIG =
        "src/test/resources/registrar/struts-config.xml";

    private DefaultListableBeanFactory registryWithRegistrar(
        GenericWebApplicationContext context,
        MockServletContext servletContext) {
        context.setServletContext(servletContext);
        DefaultListableBeanFactory registry =
            (DefaultListableBeanFactory) context.getBeanFactory();
        StrutsEngineRegistrar registrar = new StrutsEngineRegistrar() {
            @Override
            protected URL resourceUrl(String path)
                throws MalformedURLException {
                return Path.of(FIXTURE_CONFIG).toUri().toURL();
            }
        };
        ServletConfig servletConfig = new MockServletConfig(servletContext);
        ((MockServletConfig) servletConfig).addInitParameter("config",
            FIXTURE_CONFIG);
        registrar.setServletConfig(servletConfig);
        registrar.postProcessBeanDefinitionRegistry(registry);
        return registry;
    }

    @Test
    void registersDefinitionsFromConfiguration() {
        GenericWebApplicationContext context =
            new GenericWebApplicationContext();
        DefaultListableBeanFactory registry =
            registryWithRegistrar(context, new MockServletContext());

        String actionName = StrutsEngineRegistrar.ACTION_BEAN_PREFIX
            + FixtureAction.class.getName();
        assertTrue(registry.containsBeanDefinition(actionName),
            "Action type must be registered");
        assertEquals("singleton",
            registry.getBeanDefinition(actionName).getScope());
        assertTrue(registry.getBeanDefinition(actionName).isLazyInit(),
            "Action singleton must be lazy like classic Struts");

        String formName = StrutsEngineRegistrar.FORM_BEAN_PREFIX
            + "org.apache.struts.fixtures.FixtureForm";
        assertTrue(registry.containsBeanDefinition(formName),
            "Form type must be registered");
        assertEquals("prototype",
            registry.getBeanDefinition(formName).getScope());

        assertFalse(registry.containsBeanDefinition(
                StrutsEngineRegistrar.FORM_BEAN_PREFIX
                    + "org.apache.struts.action.DynaActionForm"),
            "Dynamic forms stay created through the form bean config");

        assertTrue(registry.containsBeanDefinition("messageSource"),
            "The default bundle must become the context messageSource");
        assertTrue(registry.containsBeanDefinition(
                StrutsEngineRegistrar.MESSAGE_SOURCE_BEAN_PREFIX
                    + "org.apache.struts.action.MESSAGE"));
        assertTrue(registry.containsBeanDefinition(
                StrutsEngineRegistrar.MESSAGE_SOURCE_BEAN_PREFIX
                    + "alternate"));
        context.close();
    }

    @Test
    void bridgeResolvesMessagesFromInitializedBundles() {
        GenericWebApplicationContext context =
            new GenericWebApplicationContext();
        MockServletContext servletContext = new MockServletContext();
        org.apache.struts.util.MessageResourcesFactory factory =
            new org.apache.struts.util.PropertyMessageResourcesFactory();
        org.apache.struts.util.MessageResources defaultBundle =
            factory.createResources("test-messages");
        defaultBundle.setReturnNull(false);
        org.apache.struts.util.MessageResources alternateBundle =
            factory.createResources("alternate-messages");
        alternateBundle.setReturnNull(false);
        servletContext.setAttribute("org.apache.struts.action.MESSAGE",
            defaultBundle);
        servletContext.setAttribute("alternate", alternateBundle);
        registryWithRegistrar(context, servletContext);
        context.refresh();

        assertEquals("Welcome!", context.getMessage("welcome.heading", null,
                java.util.Locale.ENGLISH),
            "The default bundle is the context MessageSource");
        org.springframework.context.MessageSource alternate =
            context.getBean(StrutsEngineRegistrar.MESSAGE_SOURCE_BEAN_PREFIX
                + "alternate",
                org.springframework.context.MessageSource.class);
        assertEquals("Greetings from the alternate bundle!",
            alternate.getMessage("alternate.greeting", null,
                java.util.Locale.ENGLISH),
            "Named bundles are reachable under strutsMessageSource names");
        context.close();
    }

    @Test
    void keepsUserMessageSource() {
        GenericWebApplicationContext context =
            new GenericWebApplicationContext();
        context.registerBeanDefinition("messageSource", BeanDefinitionBuilder
            .genericBeanDefinition(StaticMessageSource.class)
            .getBeanDefinition());
        DefaultListableBeanFactory registry =
            registryWithRegistrar(context, new MockServletContext());
        context.refresh();

        StaticMessageSource userSource = context
            .getBean("messageSource", StaticMessageSource.class);
        assertNotNull(userSource,
            "The user messageSource must not be replaced");
        assertTrue(registry.containsBeanDefinition(
                StrutsEngineRegistrar.MESSAGE_SOURCE_BEAN_PREFIX
                    + "org.apache.struts.action.MESSAGE"),
            "Struts bundles stay reachable under dedicated names");
        context.close();
    }

    @Test
    void skipsUserRegisteredActionType() {
        GenericWebApplicationContext context =
            new GenericWebApplicationContext();
        context.registerBeanDefinition("userAction", BeanDefinitionBuilder
            .genericBeanDefinition(FixtureAction.class)
            .getBeanDefinition());
        DefaultListableBeanFactory registry =
            registryWithRegistrar(context, new MockServletContext());

        assertFalse(registry.containsBeanDefinition(
                StrutsEngineRegistrar.ACTION_BEAN_PREFIX
                    + FixtureAction.class.getName()),
            "The user bean must win, no auto-registration happens");
        context.close();
    }

    @Test
    void failsStartupOnTwoUserBeansOfOneActionType() {
        GenericWebApplicationContext context =
            new GenericWebApplicationContext();
        context.registerBeanDefinition("first", BeanDefinitionBuilder
            .genericBeanDefinition(FixtureAction.class).getBeanDefinition());
        context.registerBeanDefinition("second", BeanDefinitionBuilder
            .genericBeanDefinition(FixtureAction.class).getBeanDefinition());
        StrutsEngineRegistrar registrar = new StrutsEngineRegistrar() {
            @Override
            protected URL resourceUrl(String path)
                throws MalformedURLException {
                return Path.of(FIXTURE_CONFIG).toUri().toURL();
            }
        };
        MockServletContext servletContext = new MockServletContext();
        ServletConfig servletConfig = new MockServletConfig(servletContext);
        ((MockServletConfig) servletConfig).addInitParameter("config",
            FIXTURE_CONFIG);
        registrar.setServletConfig(servletConfig);

        assertThrows(IllegalStateException.class,
            () -> registrar.postProcessBeanDefinitionRegistry(
                (DefaultListableBeanFactory) context.getBeanFactory()),
            "Two explicit registrations of one type must fail the startup");
        context.close();
    }
}
