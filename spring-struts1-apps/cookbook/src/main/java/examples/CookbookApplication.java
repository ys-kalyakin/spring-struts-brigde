package examples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Spring Boot application of the cookbook web application: the engine is
 * configured by the <code>spring-struts1-starter</code> auto-configuration
 * (the default module keeps its classic empty config through properties).
 * The class extends <code>SpringBootServletInitializer</code> so the WAR
 * also boots in a standalone container.
 */
@SpringBootApplication
public class CookbookApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(CookbookApplication.class, args);
    }
}
