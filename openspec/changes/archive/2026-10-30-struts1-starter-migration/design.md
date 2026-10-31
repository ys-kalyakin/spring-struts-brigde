# Design: struts1-starter-migration

## Context

The engine is configured by applications manually: web.xml declares a `DispatcherServlet` with an `AnnotationConfigWebApplicationContext` and classic init-params (`config`, `config/<prefix>`, `validating`), and the application context imports `StrutsEngineConfiguration` (+ `StrutsTagServicesConfiguration`). `StrutsEngineInitializer` and `StrutsEngineRegistrar` receive the `ServletConfig` via `ServletConfigAware` — in classic mode these are processed by the front servlet's child context, where the `ServletConfig` is known. Spring Boot is not used in the project; Spring Framework is pinned at 7.0.10, Tomcat 11.0.27, Java 17. Application integration tests run on the embedded `TomcatTestServer` (a real container).

## Goals / Non-Goals

**Goals:**

- Starter = the single point of engine wiring for a Boot application: beans, front servlet, init-params contract via properties.
- The classic WAR mode (web.xml + `@Import`) keeps working unchanged.
- Migration of the `apps/*` applications and an annotated demo as an executable Boot application.
- Guide, compatibility matrix, publishing infrastructure.

**Non-Goals:**

- Boot 3.5.x support (the matrix declares only 4.0.x; the engine uses the base MVC API, a backport is possible but not verified in this change).
- Publishing to an external repository (Maven Central): requires credentials outside the repository.
- Changes to engine and tag-library semantics; struts-el.

## Decisions

### 1. Starter module and dependencies

New module `spring-struts1-starter` (artifact `spring-struts1-starter`, package `org.apache.struts.spring.boot`). Dependencies: `spring-struts1-core` (mandatory), `spring-struts1-taglib` (optional — for conditional integration), `spring-boot-autoconfigure`. The starter does not pull in Boot web starters (it does not impose a stack on the application); the demo adds `spring-boot-starter-webmvc` (the current name in Boot 4; `spring-boot-starter-web` is declared deprecated). The Boot version is pinned in the parent pom (the 4.0 line, patch 4.0.6 — the current one at planning time; the final version is fixed at implementation time to the current 4.0.x patch release). Auto-configuration registration — `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.

Rejected alternative: keeping the tag services in the starter unconditionally — that would require a hard dependency on taglib and would break applications without the tag libraries.

### 2. ServletConfig in the Boot root context (key problem)

In the Boot root context (`ServletWebServerApplicationContext`), `ServletConfigAware` beans receive `null`: Boot registers a `WebApplicationContextServletContextAwareProcessor`, which resolves the `ServletContext` lazily, but the `ServletConfig` is always `null` (verified against the spring-boot-web-server 4.0.9 bytecode). Classic mode is unaffected: there, the engine bean configuration is processed by the front servlet's child context, where the `ServletConfig` is known.

An additional phase-ordering constraint: `StrutsEngineRegistrar` is a `BeanDefinitionRegistryPostProcessor` and is instantiated during the bean-factory post-processing phase, **before** `BeanPostProcessor` beans are registered; therefore a starter BPP declared via `@Bean` cannot deliver the adapter to the registrar. Meanwhile, `ServletContextAware`/`EnvironmentAware` in the root context are processed by a standard processor registered before the BFPP phase and work even for early beans.

The solution has two levels:

- **Engine (additive, no behavior change)**: a new class `org.apache.struts.spring.EnvironmentServletConfig` — a `ServletConfig` adapter over the `Environment` (lazy property reads) that resolves `classpath:` resources via the classloader and other paths via the `ServletContext`. `StrutsEngineRegistrar` additionally implements `ServletContextAware`/`EnvironmentAware`: if a `ServletConfig` was not injected (Boot mode) but a servlet context is available or the `spring.struts1.config` property is set, it assembles the adapter itself; otherwise it behaves as before (a no-op with a debug log). The normal classic-mode path and resource resolution are unchanged.
- **Starter**: an early `BeanPostProcessor` (a static `@Bean`, registered in the `registerBeanPostProcessors` phase) injects the adapter into the remaining `ServletConfigAware` beans (the initializer and other ordinary beans) and synchronizes the `StrutsHandlerMapping` mapping with the property. Ordinary beans are created after the web server has started, so the `ServletContext` is available by then.

Rejected alternatives: (a) constructor injection of the `ServletConfig` from the auto-configuration — the registrar must be a `BeanDefinitionRegistryPostProcessor`, an ordinary @Bean instance is not supported; (b) a BPP only — it misses the registrar due to phase ordering; (c) overriding the registrar bean in the starter — a name conflict with `StrutsEngineConfiguration` and duplication of the engine bean list.

### 3. The adapter reads the Environment, not a @ConfigurationProperties bean

`StrutsEngineRegistrar` and the starter's `BeanPostProcessor` run at phases where `ConfigurationPropertiesBindingPostProcessor` is not yet guaranteed to be registered; forcing creation of a typed properties bean on that path would yield unbound values. The adapter resolves init-params lazily (on `getInitParameter` calls) directly from the `Environment`: `spring.struts1.config`, `spring.struts1.config.<prefix>` (enumerating property sources), `spring.struts1.validating`. The typed `Struts1Properties` (`spring.struts1.*`) is used only by late beans (servlet registration), where binding is guaranteed.

### 4. Front servlet and mapping

The auto-configuration defines its own `DispatcherServlet` bean (name `dispatcherServlet`) and `ServletRegistrationBean` (name `dispatcherServletRegistration`) with patterns from `spring.struts1.mapping.url-pattern` (default `*.do`, a list) **plus `/error`**: with a non-root mapping, the container error-dispatch otherwise cannot find the servlet, and unregistered paths do not produce correct 404s. The names are canonical for Boot: Boot's `DispatcherServletAutoConfiguration` in Boot 4 backs off by **bean name** (not by type), and its registration backs off when `dispatcherServletRegistration` is present; naming the beans removes the dependency on auto-configuration processing order. `@AutoConfigureBefore` (by Boot class name) is kept for determinism. A servlet created as a root-context bean receives that context via `ApplicationContextAware` (this is how Boot's own auto-configuration works too — verified against the `FrameworkServlet` source), no child context is created, and the engine finds `StrutsHandlerMapping`/`StrutsHandlerAdapter` in the root context. The same property configures the engine's request-handler mapping, and its priority is raised to highest: in the root context, Boot's resource handler (`/**`) would otherwise intercept `*.do` requests before the engine's mapping. For Boot error pages, a `DispatcherServletPath` bean (mounted at root) is additionally published. Rejected alternative: a child `AnnotationConfigWebApplicationContext` as in classic mode — it duplicates the Boot context and cuts the servlet off from application beans.

Additionally: multipart in Boot mode remains with the engine — the starter publishes a no-op `multipartResolver` so that `DispatcherServlet.checkMultipart` does not intercept multipart requests before populate (classic semantics of multipart parsing inside the lifecycle) and so that Boot's auto-configuration backs off.

### 5. Conditional tag-services integration

A nested `@Configuration` inside the auto-configuration: `@ConditionalOnClass(StrutsTagServicesConfiguration.class)` + `@Import`. The taglib dependency is optional, hence the condition is by class, not by bean.

### 6. Migrating the apps/* applications

For each application: the `spring-struts1-starter` dependency; an `<App>Application extends SpringBootServletInitializer` class with `@SpringBootApplication`; web.xml reduced to the minimum (display-name, welcome-file-list) — the servlet, mapping, and init-params are no longer declared; module configs `config/<prefix>` are moved to `spring.struts1.config.<prefix>` properties where they were used. struts-config.xml stays in `/WEB-INF` (the default of the `spring.struts1.config` property matches the classic one). Integration tests (`TomcatTestServer`) are unchanged: the Boot context starts inside the deployed WAR.

### 7. Demo application

Module `spring-struts1-apps/demo` (artifact `spring-struts1-demo`): an executable WAR without web.xml (annotation-based Boot bootstrap) with `tomcat-embed-jasper` — JSPs work in Boot within an executable WAR (JSPs are not supported in an executable jar; the guide records this). Action and form with `@StrutsAction`/`@StrutsForm`, the action's dependency injected via `@Autowired`; struts-config.xml, JSPs, and message-resources are classic. ITs: (a) deploying the built WAR to the `TomcatTestServer` — the full scenario set on par with the other applications; (b) a smoke run via `SpringApplication` (equivalent to `java -jar`) with docBase `src/main/webapp` on a random port — page, form, message.

### 8. Publishing

A `publication` profile in the parent pom: `distributionManagement` with URLs/credentials from user properties (`-Dpublication.release.url=...` etc.), inactive by default. Verification — deploy to a file repository (`file:`) and consuming the published artifacts. The choice of external channel remains an open roadmap question and is resolved by the repository owner outside the code; the README documents how to run it.

### 9. Versions and Spring alignment

Import the `spring-boot-dependencies` BOM (4.0.x) into the parent's `dependencyManagement`; the project's explicit `spring.version` is aligned with the Boot-managed version to avoid a Spring Framework 7.0.x conflict. A full `./mvnw verify` is the arbiter.

## Risks / Trade-offs

- [Boot root context does not inject `ServletConfigAware`] → an engine-side adapter (`EnvironmentServletConfig`) for the registrar + the starter BPP for ordinary beans (decision 2); classic mode is unaffected, application ITs cover both modes.
- [The starter BPP misses `BeanDefinitionRegistryPostProcessor` beans due to phase ordering] → the registrar assembles the adapter itself from `ServletContextAware`/`EnvironmentAware` (decision 2).
- [Early bean initialization when reading properties during the BFPP phase] → the adapter reads the `Environment` lazily; the `@ConfigurationProperties` bean is off the critical path (decision 3).
- [A fallback adapter could change behavior of non-web contexts importing the engine] → the adapter is assembled only when a `ServletContext` is available or `spring.struts1.config` is explicitly set; otherwise the previous no-op (decision 2).
- [Double registration of `DispatcherServlet` with Boot's auto-configuration] → own servlet/registration beans; Boot backs off via `@ConditionalOnMissingBean`; demo ITs check there is no duplicate.
- [JSPs do not work in an executable jar] → demo and guide: executable WAR with jasper (decision 7).
- [Conflict between the project's Spring Framework version and Boot 4.0.x] → import the Boot BOM, full verify (decision 9).
- [Boot 4 renames artifacts and properties] → current names are used (`spring-boot-starter-webmvc`); exact property names (e.g., JSP servlet registration) are verified at implementation time.

## Migration Plan

The change is additive: classic mode is unchanged, applications migrate to the starter within this change. Rollback — revert the branch: the engine and applications return to manual bootstrap.

## Open Questions

- The exact Boot 4.0.x patch release is fixed at implementation time (decision 1); it does not affect the approach, specs, or tasks.
