package org.apache.struts.spring;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.config.ActionConfig;
import org.apache.struts.config.FormBeanConfig;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Unit tests for instance resolution (task 4.2): Spring-registered beans are
 * resolved from the context (respecting scope), unregistered classes are
 * created directly (fallback mode).
 */
class StrutsInstanceResolverTest {

    static class FallbackAction extends org.apache.struts.action.Action {
        @Override
        public org.apache.struts.action.ActionForward execute(
            org.apache.struts.action.ActionMapping mapping,
            org.apache.struts.action.ActionForm form,
            jakarta.servlet.http.HttpServletRequest request,
            jakarta.servlet.http.HttpServletResponse response) {
            return null;
        }
    }

    public static class FallbackForm extends ActionForm {
    }

    @Configuration
    static class Ctx {
        @Bean
        public SpringAction springAction() {
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
        context.registerBean(SpringAction.class);
        context.refresh();
        resolver = new StrutsInstanceResolver();
        resolver.setApplicationContext(context);
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void resolvesRegisteredActionFromContext() throws Exception {
        ActionConfig config = new ActionConfig();
        config.setPath("/Spring");
        config.setType(SpringAction.class.getName());

        Action first = resolver.resolveAction(config);
        Action second = resolver.resolveAction(config);
        assertSame(first, second, "Registered actions are singleton beans");
    }

    @Test
    void fallsBackToDirectCreationForUnregisteredAction() throws Exception {
        ActionConfig config = new ActionConfig();
        config.setPath("/Fallback");
        config.setType(FallbackAction.class.getName());

        Action first = resolver.resolveAction(config);
        Action second = resolver.resolveAction(config);
        assertSame(first, second,
            "Classic mode caches directly created actions");
    }

    @Test
    void createsFreshFormInstanceEachCall() throws Exception {
        FormBeanConfig config = new FormBeanConfig();
        config.setName("form");
        config.setType(FallbackForm.class.getName());

        ActionForm first = resolver.resolveForm(config);
        ActionForm second = resolver.resolveForm(config);
        assertNotSame(first, second,
            "Forms are created fresh for every request");
    }
}
