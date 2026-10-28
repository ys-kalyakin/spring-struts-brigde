package org.apache.struts.spring.itcfg;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.struts.action.ActionServlet;
import org.apache.struts.spring.StrutsEngineConfiguration;
import org.apache.struts.spring.StrutsHandlerMapping;

/**
 * Configuration of the custom-suffix test application: actions are routed
 * by the <code>*.act</code> suffix instead of the default <code>*.do</code>.
 */
@Configuration
@Import(StrutsEngineConfiguration.class)
public class SuffixITConfig {

    @Bean
    public StrutsHandlerMapping strutsHandlerMapping(
        ActionServlet actionServlet) {
        StrutsHandlerMapping mapping = new StrutsHandlerMapping(actionServlet);
        mapping.setExtension(".act");
        return mapping;
    }
}
