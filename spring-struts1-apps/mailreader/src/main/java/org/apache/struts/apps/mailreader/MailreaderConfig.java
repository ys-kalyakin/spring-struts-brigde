package org.apache.struts.apps.mailreader;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.struts.spring.StrutsEngineConfiguration;

/**
 * Spring configuration of the mailreader web application (the classic
 * ActionServlet is served by the Spring DispatcherServlet; the struts-config
 * init-params keep their classic names and values).
 */
@Configuration
@ComponentScan("org.apache.struts.apps.mailreader")
@Import(StrutsEngineConfiguration.class)
public class MailreaderConfig {
}
