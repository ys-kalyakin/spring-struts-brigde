package app;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.server.servlet.ConfigurableServletWebServerFactory;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.DispatcherServlet;

import org.apache.struts.spring.StrutsHandlerMapping;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The starter switch on a real Boot run (task 6.2): with
 * <code>spring.struts1.enabled=false</code> the engine starts nothing and
 * the application boots anyway (Boot's own front servlet serves it); with
 * the default enabled state there is exactly one <code>DispatcherServlet</code>
 * — the engine's, no duplicate registration.
 */
class BlankBootSwitchTest {

    @SpringBootApplication
    static class App {

        @Bean
        WebServerFactoryCustomizer<ConfigurableServletWebServerFactory>
            docRoot() {
            return factory -> factory.setDocumentRoot(
                Path.of("src/main/webapp").toFile());
        }
    }

    @Test
    void disabledLeavesTheEngineOutAndTheApplicationAlive() {
        try (ConfigurableApplicationContext context = new SpringApplication(
                App.class).run("--server.port=0",
                "--spring.struts1.enabled=false")) {
            assertFalse(context.getBeanFactory()
                    .containsBeanDefinition("actionServlet"),
                "The engine must register nothing when disabled");
            assertTrue(context.getBeansOfType(StrutsHandlerMapping.class)
                    .isEmpty(),
                "No engine handler mapping when disabled");
            assertEquals(1, context.getBeanNamesForType(
                    DispatcherServlet.class).length,
                "Boot's own front servlet takes over when disabled");
        }
    }

    @Test
    void enabledRegistersExactlyOneFrontServlet() {
        try (ConfigurableApplicationContext context = new SpringApplication(
                App.class).run("--server.port=0")) {
            assertTrue(context.getBeanFactory()
                    .containsBeanDefinition("actionServlet"),
                "The engine must be present when enabled");
            String[] names = context.getBeanNamesForType(
                DispatcherServlet.class);
            assertEquals(1, names.length,
                "Exactly one DispatcherServlet when enabled: "
                    + String.join(", ", names));
            assertEquals("dispatcherServlet", names[0],
                "The engine's servlet answers under the Boot convention name");
            assertTrue(context.getBeanFactory().containsBeanDefinition(
                    "dispatcherServletRegistration"),
                "The classic registration backs Boot's own one off");
        }
    }
}
