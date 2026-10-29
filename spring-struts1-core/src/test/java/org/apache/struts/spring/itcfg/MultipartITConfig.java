package org.apache.struts.spring.itcfg;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.struts.spring.StrutsEngineConfiguration;

/**
 * Spring configuration of the multipart integration-test web application.
 */
@Configuration
@ComponentScan("org.apache.struts.spring.it")
@Import(StrutsEngineConfiguration.class)
public class MultipartITConfig {
}
