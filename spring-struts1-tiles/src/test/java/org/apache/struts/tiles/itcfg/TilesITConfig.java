package org.apache.struts.tiles.itcfg;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.struts.spring.StrutsEngineConfiguration;

/**
 * Spring configuration of the tiles integration-test web application.
 */
@Configuration
@ComponentScan("org.apache.struts.tiles.it")
@Import(StrutsEngineConfiguration.class)
public class TilesITConfig {
}
