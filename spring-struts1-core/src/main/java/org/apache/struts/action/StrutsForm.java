package org.apache.struts.action;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Marks an {@link ActionForm} class as a Spring bean. Annotated forms are
 * registered by Spring component scanning as prototypes: every request
 * receives its own instance (state is never shared) and dependencies are
 * injected. Forms without this annotation (and without any Spring
 * registration) are created directly by the engine from the type name in
 * the configuration.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public @interface StrutsForm {
}
