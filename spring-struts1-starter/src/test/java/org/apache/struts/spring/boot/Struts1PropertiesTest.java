package org.apache.struts.spring.boot;

import org.junit.jupiter.api.Test;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Binding of the <code>spring.struts1.*</code> properties.
 */
class Struts1PropertiesTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withUserConfiguration(BindConfig.class);

    @Configuration
    @EnableConfigurationProperties(Struts1Properties.class)
    static class BindConfig {
    }

    @Test
    void bindsDefaults() {
        runner.run(context -> {
            Struts1Properties properties = context.getBean(Struts1Properties.class);
            assertThat(properties.isEnabled()).isTrue();
            assertThat(properties.getConfig()).isEqualTo("/WEB-INF/struts-config.xml");
            assertThat(properties.isValidating()).isTrue();
            assertThat(properties.getServletName()).isEqualTo("action");
            assertThat(properties.getMapping().getUrlPattern())
                .containsExactly("*.do");
        });
    }

    @Test
    void bindsValues() {
        runner.withPropertyValues(
                "spring.struts1.config=classpath:struts-config.xml",
                "spring.struts1.validating=false",
                "spring.struts1.servlet-name=actions",
                "spring.struts1.mapping.url-pattern=*.act,/action/*")
            .run(context -> {
                Struts1Properties properties = context.getBean(Struts1Properties.class);
                assertThat(properties.getConfig()).isEqualTo("classpath:struts-config.xml");
                assertThat(properties.isValidating()).isFalse();
                assertThat(properties.getServletName()).isEqualTo("actions");
                assertThat(properties.getMapping().getUrlPattern())
                    .containsExactly("*.act", "/action/*");
            });
    }
}
