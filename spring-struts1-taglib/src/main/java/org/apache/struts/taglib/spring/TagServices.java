package org.apache.struts.taglib.spring;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.PageContext;

import java.util.function.Supplier;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.support.RequestContextUtils;

/**
 * Resolves the Spring components that carry the shared tag-library logic.
 * The context is looked up through the request first (the engine context of
 * the front <code>DispatcherServlet</code>) and through the servlet context
 * attribute second (the root context). When neither is available — or the
 * application does not import the tag services configuration — the supplied
 * default instance is used, so the tag libraries keep working standalone;
 * an application overrides the behavior of the tags by defining its own
 * beans of the service types.
 */
public final class TagServices {

    private TagServices() {
    }

    /**
     * Return the application context visible to the current page, or
     * <code>null</code> when no Spring context is available.
     */
    public static WebApplicationContext context(PageContext pageContext) {
        WebApplicationContext context = RequestContextUtils
            .findWebApplicationContext(
                (HttpServletRequest) pageContext.getRequest());
        if (context == null) {
            context = WebApplicationContextUtils.getWebApplicationContext(
                pageContext.getServletContext());
        }
        return context;
    }

    /**
     * Return the service bean of the given type, preferring a bean the
     * application registered over the engine default, or the supplied
     * fallback instance when no context or no bean exists.
     */
    public static <T> T service(PageContext pageContext, Class<T> type,
        Supplier<T> fallback) {
        WebApplicationContext context = context(pageContext);
        if (context != null) {
            String[] names = context.getBeanNamesForType(type);
            String chosen = null;
            for (String name : names) {
                Class<?> beanType = context.getType(name);
                if (beanType != null && !beanType.getName()
                    .startsWith("org.apache.struts.taglib.spring.")) {
                    chosen = name;
                    break;
                }
                if (chosen == null) {
                    chosen = name;
                }
            }
            if (chosen != null) {
                try {
                    return context.getBean(chosen, type);
                } catch (NoSuchBeanDefinitionException e) {
                    // Fall through to the default instance.
                }
            }
        }
        return fallback.get();
    }
}
