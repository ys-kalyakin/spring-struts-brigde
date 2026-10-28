package org.apache.struts.taglib.it;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.struts.spring.StrutsEngineConfiguration;

/**
 * Spring configuration of the taglib integration-test web application.
 */
@Configuration
@Import(StrutsEngineConfiguration.class)
public class TagITConfig {
}
