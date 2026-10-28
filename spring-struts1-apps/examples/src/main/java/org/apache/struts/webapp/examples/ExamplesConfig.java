package org.apache.struts.webapp.examples;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.struts.spring.StrutsEngineConfiguration;
import org.apache.struts.taglib.spring.StrutsTagServicesConfiguration;

/**
 * Spring configuration of the Struts Examples application on the
 * spring-struts1 engine: the engine beans, the overridable tag services and
 * the demo service of the spring-integration showcase.
 */
@Configuration
@Import({StrutsEngineConfiguration.class,
    StrutsTagServicesConfiguration.class})
public class ExamplesConfig {

    @Bean
    public DemoMessageService springDemo() {
        return new DemoMessageService();
    }
}
