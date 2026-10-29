package org.apache.struts.webapp.examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot application of the Struts Examples application: the engine is
 * configured by the <code>spring-struts1-starter</code> auto-configuration;
 * the module configurations (<code>config/&lt;prefix&gt;</code> init-params
 * of the classic contract) are set through the
 * <code>spring.struts1.config.*</code> properties. The class extends
 * <code>SpringBootServletInitializer</code> so the WAR also boots in a
 * standalone container.
 */
@SpringBootApplication
public class ExamplesApplication extends SpringBootServletInitializer {

    /**
     * The demo service of the spring-integration showcase.
     */
    @Bean
    public DemoMessageService springDemo() {
        return new DemoMessageService();
    }

    public static void main(String[] args) {
        SpringApplication.run(ExamplesApplication.class, args);
    }
}
