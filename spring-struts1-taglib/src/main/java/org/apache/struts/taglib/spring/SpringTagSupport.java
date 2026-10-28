package org.apache.struts.taglib.spring;

import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.TagSupport;

import java.util.function.Supplier;

import org.springframework.web.context.WebApplicationContext;

/**
 * Spring-aware base of the classic tag handlers: extends
 * {@link TagSupport} (so TLD semantics stay identical) and gives subclasses
 * access to the application context and its beans — including the shared
 * tag services, whose beans the application may override.
 */
public abstract class SpringTagSupport extends TagSupport {

    private static final long serialVersionUID = 1L;

    /**
     * The application context visible to the current page.
     * @throws IllegalStateException when no Spring context is available
     */
    protected WebApplicationContext webApplicationContext() {
        WebApplicationContext context = TagServices.context(pageContext);
        if (context == null) {
            throw new IllegalStateException("No Spring WebApplicationContext "
                + "is available for the tag " + getClass().getName());
        }
        return context;
    }

    /** The bean of the given type from the application context. */
    protected <T> T springBean(Class<T> type) {
        return webApplicationContext().getBean(type);
    }

    /** The bean with the given name from the application context. */
    protected Object springBean(String name) {
        return webApplicationContext().getBean(name);
    }

    /**
     * The shared tag service of the given type — the application-defined
     * bean when registered, the engine default otherwise.
     */
    protected <T> T tagService(Class<T> type, Supplier<T> fallback) {
        return TagServices.service(pageContext, type, fallback);
    }

    /** The PageContext of this tag (used by the tag services). */
    protected PageContext pageContext() {
        return pageContext;
    }
}
