package org.apache.struts.action;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Component;

/**
 * Marks an {@link Action} class as a Spring bean. Annotated actions are
 * registered by Spring component scanning as singletons: one instance is
 * reused between requests and dependencies are injected. Actions without
 * this annotation (and without any Spring registration) are created
 * directly by the engine from the type name in the configuration.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface StrutsAction {
}
