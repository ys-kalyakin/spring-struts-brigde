package org.apache.struts.webapp.examples;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.struts.spring.StrutsEngineConfiguration;

/**
 * Spring configuration of the Struts Examples application on the
 * spring-struts1 engine.
 */
@Configuration
@Import(StrutsEngineConfiguration.class)
public class ExamplesConfig {
}
