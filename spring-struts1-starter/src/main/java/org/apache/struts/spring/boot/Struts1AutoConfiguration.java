package org.apache.struts.spring.boot;

import java.util.EnumSet;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.webmvc.autoconfigure.DispatcherServletPath;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;

import org.apache.struts.action.ActionServlet;
import org.apache.struts.spring.StrutsEngineConfiguration;
import org.apache.struts.taglib.spring.StrutsTagServicesConfiguration;

/**
 * Spring Boot auto-configuration of the Struts 1 engine: imports the engine
 * beans, registers the front <code>DispatcherServlet</code> with the classic
 * mapping, feeds the classic init-param contract from
 * <code>spring.struts1.*</code> properties and keeps the multipart handling
 * of the request inside the engine (classic semantics).
 *
 * <p>Runs before Boot's own <code>DispatcherServlet</code>
 * auto-configuration (referenced by name to keep this starter independent of
 * the web starter) so the engine's servlet with the classic mapping wins;
 * tag-library services are imported conditionally — only when
 * <code>spring-struts1-taglib</code> is on the classpath.</p>
 */
@AutoConfiguration
@AutoConfigureBefore(
    name = "org.springframework.boot.webmvc.autoconfigure."
        + "DispatcherServletAutoConfiguration")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnProperty(name = "spring.struts1.enabled",
    havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(Struts1Properties.class)
@Import({StrutsEngineConfiguration.class, TagServicesConfiguration.class})
public class Struts1AutoConfiguration {

    /**
     * Boot-mode configuration of the engine beans (servlet config adapter
     * injection and handler-mapping synchronization).
     */
    @Bean
    public static StrutsBootConfigurer strutsBootConfigurer() {
        return new StrutsBootConfigurer();
    }

    /**
     * The front servlet of the classic contract; as a bean of the Boot root
     * context it uses that context directly (no child context), so the
     * engine's handler mapping and adapter are discovered. Named after the
     * Boot convention: Boot's own auto-configuration backs off by bean NAME
     * (its condition does not check the type).
     */
    @Bean
    @ConditionalOnMissingBean(DispatcherServlet.class)
    public DispatcherServlet dispatcherServlet() {
        return new DispatcherServlet();
    }

    /**
     * Registers the front servlet with the classic servlet-mapping
     * (<code>spring.struts1.mapping.url-pattern</code>, by default
     * <code>*.do</code>) under the classic name, plus the Boot
     * <code>/error</code> path: with a non-root mapping the container's error
     * dispatch would otherwise find no servlet, and unmatched paths would not
     * yield proper 404 responses. A plain registration (the classic mapping
     * may be an extension mapping, which
     * <code>DispatcherServletRegistrationBean</code> forbids); named after
     * the Boot convention: Boot's own registration backs off when this bean
     * exists.
     */
    @Bean
    @ConditionalOnBean(name = "dispatcherServlet")
    public ServletRegistrationBean<DispatcherServlet>
        dispatcherServletRegistration(Struts1Properties properties,
        DispatcherServlet dispatcherServlet) {
        java.util.LinkedHashSet<String> patterns = new java.util.LinkedHashSet<>(
            properties.getMapping().getUrlPattern());
        patterns.add("/error");
        ServletRegistrationBean<DispatcherServlet> registration =
            new ServletRegistrationBean<>(dispatcherServlet,
                patterns.toArray(new String[0]));
        registration.setName(properties.getServletName());
        registration.setLoadOnStartup(2);
        return registration;
    }

    /**
     * Keeps the <code>ActionServlet</code> facade out of the automatic
     * servlet registration: Boot registers every <code>Servlet</code> bean,
     * and an auto-registered facade mapped to <code>/</code> would shadow the
     * default servlet and answer action-less requests with 405. The facade is
     * not a real servlet — it receives its configuration through the engine
     * initializer — so it stays registered without mappings.
     */
    @Bean
    public ServletRegistrationBean<ActionServlet> actionServletFacade(
        ActionServlet actionServlet) {
        ServletRegistrationBean<ActionServlet> registration =
            new ServletRegistrationBean<>(actionServlet);
        registration.setEnabled(false);
        return registration;
    }

    /**
     * Container default servlet for the embedded runtime (classic webapp
     * resources live in the webapp root); skipped when the container
     * provides one. Active only when Tomcat classes are on the classpath.
     */
    @Bean
    @ConditionalOnClass(name = "org.apache.catalina.servlets.DefaultServlet")
    public DefaultServletInitializer defaultServletInitializer() {
        return new DefaultServletInitializer();
    }

    /**
     * Request-context exposure for direct JSP requests, registered the Boot
     * way: the engine initializer's late attempt runs from an
     * <code>InitializingBean</code>, after an embedded container has started
     * and programmatic filter registration is rejected. Under this name the
     * initializer's lazy attempt detects the existing registration and backs
     * off.
     */
    @Bean
    public FilterRegistrationBean<RequestContextFilter>
        springRequestContext() {
        FilterRegistrationBean<RequestContextFilter> registration =
            new FilterRegistrationBean<>(new RequestContextFilter());
        registration.setName("springRequestContext");
        registration.addUrlPatterns("/*");
        registration.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 104);
        return registration;
    }

    /**
     * Boot's error-page facilities need the dispatcher path; the classic
     * servlet is mounted conceptually at the root and requests are selected
     * by the classic servlet mapping.
     */
    @Bean
    public DispatcherServletPath strutsDispatcherServletPath(
        Struts1Properties properties) {
        return new StrutsDispatcherServletPath(properties);
    }

    /**
     * Dispatcher path of the classic front servlet: mounted at the root,
     * requests selected by the classic servlet mapping.
     */
    static final class StrutsDispatcherServletPath
        implements DispatcherServletPath {

        private final String servletUrlMapping;

        StrutsDispatcherServletPath(Struts1Properties properties) {
            this.servletUrlMapping =
                properties.getMapping().getUrlPattern().get(0);
        }

        @Override
        public String getPath() {
            return "/";
        }

        @Override
        public String getServletUrlMapping() {
            return servletUrlMapping;
        }
    }

    /**
     * Keeps multipart requests raw for the engine: the classic contract
     * parses multipart inside the request lifecycle (populate), so the
     * <code>DispatcherServlet</code> must not pre-resolve multipart through
     * the container. This resolver never claims a request; Boot's multipart
     * auto-configuration backs off while this bean exists.
     */
    @Bean
    @ConditionalOnMissingBean(name =
        DispatcherServlet.MULTIPART_RESOLVER_BEAN_NAME)
    public MultipartResolver multipartResolver() {
        return new StrutsMultipartGuard();
    }

    /**
     * Multipart resolver that never claims requests (the engine parses
     * multipart itself, before populate).
     */
    static final class StrutsMultipartGuard implements MultipartResolver {

        @Override
        public boolean isMultipart(HttpServletRequest request) {
            return false;
        }

        @Override
        public MultipartHttpServletRequest resolveMultipart(
            HttpServletRequest request) throws MultipartException {
            throw new MultipartException(
                "Multipart requests are handled by the Struts engine");
        }

        @Override
        public void cleanupMultipart(MultipartHttpServletRequest request) {
            // Nothing to clean up: requests are never claimed.
        }
    }
}
