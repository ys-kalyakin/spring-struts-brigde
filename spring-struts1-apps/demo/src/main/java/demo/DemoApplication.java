package demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Spring Boot application of the migration demo: the whole migration of the
 * classic Struts application is the dependency replacement
 * (<code>spring-struts1-starter</code>), this application class and the
 * {@link org.apache.struts.action.StrutsAction}/{@link org.apache.struts.action.StrutsForm}
 * annotations on the action and the form. Everything else —
 * struts-config.xml, JSP, tag libraries, message resources — works as
 * before, on the engine of Spring.
 */
@SpringBootApplication
public class DemoApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
