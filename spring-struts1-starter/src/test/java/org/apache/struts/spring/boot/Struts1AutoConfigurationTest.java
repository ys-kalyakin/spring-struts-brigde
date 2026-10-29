package org.apache.struts.spring.boot;

import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.GenericWebApplicationContext;
import org.springframework.web.context.support.StandardServletEnvironment;
import org.springframework.web.servlet.DispatcherServlet;

import org.apache.struts.spring.StrutsHandlerMapping;
import org.apache.struts.taglib.spring.TagFormLocator;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Auto-configuration of the starter: engine beans without manual import,
 * front servlet registration, conditional tag services, disable switch.
 *
 * <p>The runner uses a {@link GenericWebApplicationContext} (eager bean
 * factory), a servlet environment and a mock servlet context; the engine
 * parses the classpath fixture configuration through the
 * <code>spring.struts1.config</code> property.</p>
 */
class Struts1AutoConfigurationTest {

    private final WebApplicationContextRunner runner =
        new WebApplicationContextRunner(() -> {
            GenericWebApplicationContext context =
                new GenericWebApplicationContext();
            context.setEnvironment(new StandardServletEnvironment());
            return context;
        })
            // MockServletContext does not support programmatic filter
            // registration: the engine initializer's late attempt logs its
            // tolerated warning — real containers get the filter from the
            // starter's FilterRegistrationBean instead.
            .withInitializer(context ->
                context.setServletContext(new MockServletContext()))
            .withPropertyValues(
                "spring.struts1.config=classpath:struts-config.xml")
            .withConfiguration(AutoConfigurations.of(
                Struts1AutoConfiguration.class));

    @Test
    void registersEngineBeansAndFrontServlet() {
        runner.run(context -> {
            assertThat(context).hasBean("actionServlet");
            assertThat(context).hasBean("strutsEngineRegistrar");
            assertThat(context).hasBean("strutsEngineInitializer");
            assertThat(context).hasBean("strutsInstanceResolver");
            assertThat(context).hasBean("strutsHandlerMapping");
            assertThat(context).hasBean("strutsHandlerAdapter");
            assertThat(context).hasBean("dispatcherServlet");
            assertThat(context).hasBean("multipartResolver");
            assertThat(context).hasBean("springRequestContext");

            org.springframework.boot.web.servlet.FilterRegistrationBean<?>
                filter = context.getBean("springRequestContext",
                    org.springframework.boot.web.servlet
                        .FilterRegistrationBean.class);
            assertThat(filter.getFilter())
                .isInstanceOf(org.springframework.web.filter
                    .RequestContextFilter.class);
            assertThat(filter.getUrlPatterns()).containsExactly("/*");

            ServletRegistrationBean<?> registration = context.getBean(
                "dispatcherServletRegistration",
                ServletRegistrationBean.class);
            assertThat(registration.getServletName()).isEqualTo("action");
            assertThat(registration.getUrlMappings()).containsExactly("*.do", "/error");

            assertThat(context.getBean(StrutsHandlerMapping.class)
                .getServletMapping()).isEqualTo("*.do");

            // Tag services: the taglib module is on the test classpath
            assertThat(context).hasSingleBean(TagFormLocator.class);
        });
    }

    @Test
    void mappingPropertySynchronizesServletAndHandler() {
        runner.withPropertyValues(
                "spring.struts1.mapping.url-pattern=/action/*")
            .run(context -> {
                ServletRegistrationBean<?> registration = context.getBean(
                    "dispatcherServletRegistration",
                    ServletRegistrationBean.class);
                assertThat(registration.getUrlMappings())
                    .containsExactly("/action/*", "/error");
                assertThat(context.getBean(StrutsHandlerMapping.class)
                    .getServletMapping()).isEqualTo("/action/*");
            });
    }

    @Test
    void startsWithoutTagServicesWhenTaglibAbsent() {
        runner.withClassLoader(new FilteredClassLoader(
                org.apache.struts.taglib.spring.StrutsTagServicesConfiguration.class))
            .run(context -> {
                assertThat(context).hasNotFailed();
                assertThat(context).hasBean("actionServlet");
                assertThat(context).doesNotHaveBean(TagFormLocator.class);
            });
    }

    @Test
    void registersNothingWhenDisabled() {
        runner.withPropertyValues("spring.struts1.enabled=false")
            .run(context -> {
                assertThat(context).hasNotFailed();
                assertThat(context).doesNotHaveBean("actionServlet");
                assertThat(context).doesNotHaveBean(DispatcherServlet.class);
                assertThat(context).doesNotHaveBean("multipartResolver");
            });
    }

    @Test
    void staysInactiveOutsideServletWebApplications() {
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                Struts1AutoConfiguration.class))
            .run(context -> {
                assertThat(context).hasNotFailed();
                assertThat(context).doesNotHaveBean("actionServlet");
            });
    }
}
