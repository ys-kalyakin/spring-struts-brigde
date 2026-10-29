package org.apache.struts.apps.mailreader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Spring Boot application of the mailreader web application: the engine is
 * configured by the <code>spring-struts1-starter</code> auto-configuration
 * (the classic <code>/WEB-INF/struts-config.xml</code> default applies). The
 * class extends <code>SpringBootServletInitializer</code> so the WAR also
 * boots in a standalone container.
 */
@SpringBootApplication
public class MailreaderApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(MailreaderApplication.class, args);
    }
}
