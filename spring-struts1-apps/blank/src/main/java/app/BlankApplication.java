package app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Spring Boot application of the ported blank application: the engine is
 * configured by the <code>spring-struts1-starter</code> auto-configuration
 * (the classic <code>/WEB-INF/struts-config.xml</code> default applies), the
 * application itself declares no annotated actions or forms — the blank app
 * exercises the classic fallback mode. The class extends
 * <code>SpringBootServletInitializer</code> so the WAR also boots in a
 * standalone container.
 */
@SpringBootApplication
public class BlankApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(BlankApplication.class, args);
    }
}
