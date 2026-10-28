package org.apache.struts.taglib.it;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.struts.action.ActionServlet;
import org.apache.struts.spring.StrutsEngineConfiguration;
import org.apache.struts.spring.StrutsHandlerMapping;

/**
 * Configuration of the path-mapped test servlet: actions are routed by the
 * /action/* prefix instead of the *.do extension.
 */
@Configuration
@Import(StrutsEngineConfiguration.class)
public class TagITPathConfig {

    @Bean
    public StrutsHandlerMapping strutsHandlerMapping(
        ActionServlet actionServlet) {
        StrutsHandlerMapping mapping = new StrutsHandlerMapping(actionServlet);
        mapping.setServletMapping("/action/*");
        return mapping;
    }
}
