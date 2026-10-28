package org.apache.struts.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.apache.struts.action.ActionServlet;

/**
 * Convenience Spring configuration declaring the engine beans: the
 * <code>ActionServlet</code> compatibility facade, the initializer that
 * feeds it the front servlet's init-params, the instance resolver and the
 * Spring MVC handler mapping/adapter pair. Applications import this
 * configuration (the Spring Boot starter in phase 4 will do it
 * automatically).
 */
@Configuration
public class StrutsEngineConfiguration {

    @Bean
    public ActionServlet actionServlet() {
        return new ActionServlet();
    }

    @Bean
    public StrutsEngineInitializer strutsEngineInitializer(
        ActionServlet actionServlet) {
        return new StrutsEngineInitializer(actionServlet);
    }

    @Bean
    public StrutsInstanceResolver strutsInstanceResolver() {
        return new StrutsInstanceResolver();
    }

    @Bean
    public StrutsHandlerMapping strutsHandlerMapping(
        ActionServlet actionServlet) {
        return new StrutsHandlerMapping(actionServlet);
    }

    @Bean
    public StrutsHandlerAdapter strutsHandlerAdapter(
        ActionServlet actionServlet, StrutsInstanceResolver resolver) {
        return new StrutsHandlerAdapter(actionServlet, resolver);
    }
}
