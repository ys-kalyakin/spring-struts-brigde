package org.apache.struts.spring.boot;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.struts.taglib.spring.StrutsTagServicesConfiguration;

/**
 * Tag-library services, imported only when <code>spring-struts1-taglib</code>
 * is on the classpath (the dependency of the starter on the module is
 * optional). Top-level (not nested) because the importing auto-configuration
 * cannot reference its own nested class.
 */
@ConditionalOnClass(StrutsTagServicesConfiguration.class)
@Configuration(proxyBeanMethods = false)
@Import(StrutsTagServicesConfiguration.class)
class TagServicesConfiguration {
}
