package org.apache.struts.spring;

import jakarta.annotation.PreDestroy;

import org.apache.commons.beanutils.DynaBean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.DynaActionForm;
import org.apache.struts.config.ActionConfig;
import org.apache.struts.config.FormBeanConfig;
import org.apache.struts.config.FormPropertyConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for instance resolution: actions and forms are resolved from
 * the Spring context only — the user-registered bean when the class is
 * registered explicitly, otherwise the definition the engine registrar
 * registered from the configuration. The defensive fallback creates the
 * instance through the autowire-capable bean factory.
 */
class StrutsInstanceResolverTest {

    static class UnregisteredAction extends org.apache.struts.action.Action {
        @Override
        public org.apache.struts.action.ActionForward execute(
            org.apache.struts.action.ActionMapping mapping,
            org.apache.struts.action.ActionForm form,
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response) {
            return null;
        }
    }

    public static class UnregisteredForm extends ActionForm {
    }

    public static class LifecycleAction extends
        org.apache.struts.action.Action {
        boolean destroyed;

        @Override
        public org.apache.struts.action.ActionForward execute(
            org.apache.struts.action.ActionMapping mapping,
            org.apache.struts.action.ActionForm form,
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response) {
            return null;
        }

        @PreDestroy
        void destroy() {
            destroyed = true;
        }
    }

    @Configuration
    static class Ctx {
        @Bean
        public SpringAction springAction() {
            return new SpringAction();
        }
    }

    @Configuration
    static class DoubleCtx {
        @Bean
        public SpringAction springAction() {
            return new SpringAction();
        }

        @Bean
        public SpringAction otherSpringAction() {
            return new SpringAction();
        }
    }

    static class SpringAction extends org.apache.struts.action.Action {
        @Override
        public org.apache.struts.action.ActionForward execute(
            org.apache.struts.action.ActionMapping mapping,
            org.apache.struts.action.ActionForm form,
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response) {
            return null;
        }
    }

    private AnnotationConfigApplicationContext context;

    private StrutsInstanceResolver resolver;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigApplicationContext();
        resolver = new StrutsInstanceResolver();
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    private DefaultListableBeanFactory beanFactory() {
        return (DefaultListableBeanFactory) context.getBeanFactory();
    }

    private void refresh() {
        context.refresh();
        resolver.setApplicationContext(context);
    }

    private ActionConfig actionConfig(Class<?> type) {
        ActionConfig config = new ActionConfig();
        config.setPath("/Test");
        config.setType(type.getName());
        return config;
    }

    private FormBeanConfig formConfig(Class<?> type) {
        FormBeanConfig config = new FormBeanConfig();
        config.setName("form");
        config.setType(type.getName());
        return config;
    }

    @Test
    void resolvesRegisteredActionFromContext() throws Exception {
        context.registerBean(SpringAction.class);
        refresh();

        Action first = resolver.resolveAction(actionConfig(SpringAction.class));
        Action second = resolver.resolveAction(actionConfig(SpringAction.class));
        assertSame(first, second, "Registered actions are singleton beans");
    }

    @Test
    void resolvesAutoRegisteredActionDefinition() throws Exception {
        beanFactory().registerBeanDefinition(
            StrutsEngineRegistrar.ACTION_BEAN_PREFIX
                + SpringAction.class.getName(),
            BeanDefinitionBuilder.genericBeanDefinition(SpringAction.class)
                .getBeanDefinition());
        refresh();

        Action first = resolver.resolveAction(actionConfig(SpringAction.class));
        Action second = resolver.resolveAction(actionConfig(SpringAction.class));
        assertSame(first, second,
            "The auto-registered definition is a singleton");
    }

    @Test
    void createsActionThroughSpringWhenNothingIsRegistered() throws Exception {
        refresh();

        Action first = resolver.resolveAction(
            actionConfig(UnregisteredAction.class));
        Action second = resolver.resolveAction(
            actionConfig(UnregisteredAction.class));
        assertNotSame(first, second,
            "The defensive fallback creates through Spring without a cache");
    }

    @Test
    void multipleBeansOfTypeFailResolution() throws Exception {
        context.register(DoubleCtx.class);
        refresh();

        IllegalStateException e = assertThrows(IllegalStateException.class,
            () -> resolver.resolveAction(actionConfig(SpringAction.class)));
        assertTrue(e.getMessage().contains("Multiple Spring beans"),
            e.getMessage());
    }

    @Test
    void destroysAutoRegisteredSingletonOnContextClose() throws Exception {
        beanFactory().registerBeanDefinition(
            StrutsEngineRegistrar.ACTION_BEAN_PREFIX
                + LifecycleAction.class.getName(),
            BeanDefinitionBuilder.genericBeanDefinition(LifecycleAction.class)
                .getBeanDefinition());
        refresh();

        LifecycleAction action = (LifecycleAction) resolver.resolveAction(
            actionConfig(LifecycleAction.class));
        context.close();
        assertTrue(action.destroyed,
            "@PreDestroy must run for the auto-registered singleton");
    }

    @Test
    void createsFreshFormInstanceEachCall() throws Exception {
        beanFactory().registerBeanDefinition(
            StrutsEngineRegistrar.FORM_BEAN_PREFIX
                + UnregisteredForm.class.getName(),
            BeanDefinitionBuilder.genericBeanDefinition(UnregisteredForm.class)
                .setScope("prototype")
                .getBeanDefinition());
        refresh();

        ActionForm first = resolver.resolveForm(
            formConfig(UnregisteredForm.class));
        ActionForm second = resolver.resolveForm(
            formConfig(UnregisteredForm.class));
        assertNotSame(first, second,
            "Forms are created fresh for every request");
    }

    @Test
    void createsDynamicFormThroughFormBeanConfig() throws Exception {
        FormBeanConfig config = new FormBeanConfig();
        config.setName("dyna");
        config.setType(DynaActionForm.class.getName());
        config.addFormPropertyConfig(
            new FormPropertyConfig("greeting", "java.lang.String", "hello"));
        refresh();

        ActionForm form = resolver.resolveForm(config);
        assertInstanceOf(DynaActionForm.class, form);
        assertEquals("hello", ((DynaBean) form).get("greeting"),
            "Dynamic forms keep their configured initial values");
    }

    @Test
    void createsUnregisteredFormThroughSpring() throws Exception {
        refresh();

        ActionForm first = resolver.resolveForm(
            formConfig(UnregisteredForm.class));
        ActionForm second = resolver.resolveForm(
            formConfig(UnregisteredForm.class));
        assertNotSame(first, second,
            "The defensive fallback creates a fresh form per request");
    }
}
