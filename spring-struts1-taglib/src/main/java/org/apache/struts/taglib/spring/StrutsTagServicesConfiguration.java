package org.apache.struts.taglib.spring;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares the default Spring components of the tag libraries. Applications
 * import this configuration to make the tag services overridable beans;
 * without the import the tags fall back to the default instances of the
 * same classes.
 */
@Configuration
public class StrutsTagServicesConfiguration {

    @Bean
    public TagMessageResolver tagMessageResolver() {
        return new TagMessageResolver();
    }

    @Bean
    public TagUrlBuilder tagUrlBuilder() {
        return new TagUrlBuilder();
    }

    @Bean
    public TagFormLocator tagFormLocator() {
        return new TagFormLocator();
    }

    @Bean
    public ErrorsMessagesRenderer errorsMessagesRenderer() {
        return new ErrorsMessagesRenderer();
    }
}
