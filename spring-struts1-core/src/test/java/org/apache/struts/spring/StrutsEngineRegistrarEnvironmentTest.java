package org.apache.struts.spring;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.mock.web.MockServletContext;

import org.apache.struts.fixtures.FixtureAction;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Boot-mode fallback of the engine registrar: without a classic
 * <code>ServletConfig</code> the registrar builds an
 * {@link EnvironmentServletConfig} from the servlet context and environment;
 * plain non-web contexts stay no-op.
 */
class StrutsEngineRegistrarEnvironmentTest {

    @Test
    void registersFromClasspathPropertyWithoutServletContext() {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        MockEnvironment environment = new MockEnvironment();
        environment.setProperty("spring.struts1.config",
            "classpath:registrar/struts-config.xml");
        StrutsEngineRegistrar registrar = new StrutsEngineRegistrar();
        registrar.setEnvironment(environment);
        registrar.postProcessBeanDefinitionRegistry(factory);

        assertTrue(factory.containsBeanDefinition(
                StrutsEngineRegistrar.ACTION_BEAN_PREFIX
                    + FixtureAction.class.getName()),
            "Action types must be registered in environment-only mode");
        assertTrue(factory.containsBeanDefinition(
                StrutsEngineRegistrar.MESSAGE_SOURCE_BEAN_PREFIX
                    + "org.apache.struts.action.MESSAGE"),
            "Message bundles must be registered in environment-only mode");
    }

    @Test
    void resolvesServletContextResourcesWithContextOnly() {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        // Servlet context present but the classic config resource missing:
        // startup fails with the resource name, like the classic contract.
        StrutsEngineRegistrar registrar = new StrutsEngineRegistrar();
        registrar.setServletContext(new MockServletContext());
        registrar.setEnvironment(new MockEnvironment());
        IllegalStateException error = assertThrows(
            IllegalStateException.class,
            () -> registrar.postProcessBeanDefinitionRegistry(factory));
        assertTrue(error.getMessage().contains("/WEB-INF/struts-config.xml"),
            "The error must name the missing configuration resource");
    }

    @Test
    void plainContextWithoutWebFacilitiesStaysNoOp() {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        StrutsEngineRegistrar registrar = new StrutsEngineRegistrar();
        registrar.setEnvironment(new MockEnvironment());
        registrar.postProcessBeanDefinitionRegistry(factory);
        assertTrue(factory.getBeanDefinitionCount() == 0,
            "A non-web context without configuration must stay empty");
    }
}
