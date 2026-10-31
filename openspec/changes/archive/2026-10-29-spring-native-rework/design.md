# Design

## Context

The engine (phases 1–2) already runs on Spring MVC: `StrutsHandlerMapping`/`StrutsHandlerAdapter` drive the request pipeline, `StrutsInstanceResolver` takes `@StrutsAction`/`@StrutsForm`-annotated classes from the context, and everything else is created directly (`newInstance`, per-class cache; for forms — `FormBeanConfig.createActionForm`). The tag libraries are classic ports: tags extend `TagSupport`/`BodyTagSupport`, messages come from static `MessageResources` singletons, shared operations are static `TagUtils` methods. See proposal.md (Why) — the motivation is not repeated here.

Constraints that shape the approach: the external contract is fixed by the `*-taglib`, `message-resources`, `request-processing` specs — their behavior does not change; the JSP container instantiates tag handlers itself, so "a tag as a Spring bean" can only mean Spring access and delegation to components, not a bean lifecycle for the tag.

## Goals / Non-Goals

**Goals:**
- Every form and action instance is created by the Spring bean factory; types from `struts-config.xml` are visible in the context as bean definitions.
- Full bean lifecycle (`@PostConstruct`/`@PreDestroy`, `BeanPostProcessor`) for forms and actions.
- Spring-native tag infrastructure: context access from tags, tag logic in Spring components, a `MessageResources` → `MessageSource` bridge, EL access to beans, coexistence with `spring:*` tags.
- Zero changes to HTML output and TLDs — the regression is closed by existing ITs.

**Non-Goals:**
- Replacing JSP with another view technology; struts-el; Tiles; commons-validator.
- Moving `TagUtils` entirely to services — only the areas named in the `spring-tag-integration` spec are migrated (messages, URLs, form lookup, error/message rendering).
- Changing the request-processing pipeline (`request-processing` is untouched).

## Decisions

### D1. Auto-registration of types from the configuration as bean definitions
`StrutsEngineRegistrar` (implemented as `BeanDefinitionRegistryPostProcessor` + `ServletConfigAware`) reads the classic init-params, parses the configuration with the parser shared with the facade (`StrutsConfigParser`, extracted from `ActionServlet`) and registers definitions in the `BeanDefinitionRegistry`: actions — singleton with lazy-init, forms — prototype; names — `strutsAction:<fqcn>` / `strutsForm:<fqcn>`.
- *Why the bean-definition phase*: the engine context lives in the `DispatcherServlet`; its `initMessageSource` runs before regular bean creation, so the `messageSource` bridge (D5) must already exist at that phase — registering from the initializer would be too late. The `ActionServlet` facade keeps its classic init (parsing, message-resources into the ServletContext, plugins); they share the parsing rules.
- *Why not on-the-fly creation via `AutowireCapableBeanFactory.createBean` without registration*: such beans would be invisible in the context (cannot be overridden, inspected, subscribed to events) and would have a weaker lifecycle. Registration gives the "real" bean status that is requested.
- *Alternative — component-scanning all classes from the configuration*: rejected; scanning works by packages and would duplicate definitions for every unannotated class.
- `DynaActionForm` is not registered: it is an internal dynamic class, created via `FormBeanConfig.createActionForm` (`DynaActionFormClass` binding, initial values) as before.

### D2. Resolver: Spring only
`StrutsInstanceResolver` is simplified: explicit user registration wins (`getBeanNamesForType`; a single candidate — it is used; several — a startup error listing the candidates instead of silently picking the first); otherwise — the auto-registered definition; the fallback path (a class that appears outside the configuration) — `AutowireCapableBeanFactory.createBean` with no direct-instance cache. The `actionCache` is removed: the bean's singleton scope reproduces the classic "one instance per class".

### D3. Scopes and form storage
The scope from the mapping (`request`/`session`) remains where the attribute is stored (classic `request-processing` behavior); the bean scope is the instance's lifetime. A user who registers a form as a session-scoped bean gets deliberately shared state at `scope="session"`; at `scope="request"` the prototype guarantees a fresh instance. No separate "mapping scope → bean scope" mapping is introduced, to avoid changing observable behavior.

### D4. Tag base and logic services
A new package `org.apache.struts.taglib.spring` in `spring-struts1-taglib`:
- `SpringTagSupport` / `SpringBodyTagSupport` — re-parents for existing tags; they provide lazy access to the `WebApplicationContext` via `WebApplicationContextUtils` (the ServletContext attribute is reliable even without `RequestContextHolder`) and helpers for getting beans/dependencies.
- Service components (singleton, stateless, request data passed as arguments): `TagMessageResolver` (locale from session/request, the `???ru_RU.key???` format), `TagUrlBuilder` (context-relative/action URLs, module prefix), `TagFormLocator` (form lookup in request/session), `ErrorsMessagesRenderer` (prefix/suffix/header/footer). Tags and `TagUtils` delegate to them.
- `TagUtils` remains a static facade (classic API), but migrated methods delegate to services found in the current request's context — overriding a bean changes tag output (a spec requirement); the rest of the static API is untouched.
- The `StrutsTagServicesConfiguration` declares the service beans under fixed names; applications import it explicitly (the phase 4 starter will wire it automatically).

### D5. MessageResources → MessageSource bridge
The registrar maps `MessageResourcesConfig` to `MessageSource` beans: the classic `PropertyMessageResources` is wrapped in a `MessageSource` adapter. If the user has no `messageSource` bean, the default bundle becomes the context's primary `MessageSource` (the `spring:message` scenario from the spec); if there is one, the Struts bundles are registered under dedicated names, the classic tags keep resolving via `TagMessageResolver` against the Struts bundles, and `spring:message` follows the user's primary source. The static `MessageResources.getMessageResources` is kept for backward compatibility; tags no longer use it.

### D6. The "Spring JSP stack" in JSP
- `spring:message`/`spring:eval` run on top of the same context (`RequestContextAwareTag` builds the `RequestContext` from the `WebApplicationContext` itself); the engine additionally registers a `RequestContextFilter` so the request locale/context are available to filter-level code.
- EL access to beans: an initializer registers, once per context via `JspFactory.getJspApplicationContext(...)`, its own `ELResolver` (`BeanNameELResolver` over `SpringBeanNameResolver`, the EL 6.0 contract: `isNameResolved`/`getBean`) that resolves singleton bean names from the Spring context; prototype beans are not exposed to EL. Registration happens before the first JSP is compiled (after that — `IllegalStateException`); re-registration is guarded by a flag.
- Mixing is verified by an IT: a page with `html:form`+`html:text`, `spring:message`, and EL over a bean.

### D7. Demonstration and regression
The `examples` app gains a demo page: an action with an `@Autowired` service, a form with `@Autowired` and `@PostConstruct`, and a JSP mixing Struts and Spring tags. The existing ITs (`blank`, `examples`) are the regression for unchanged HTML output; their changes are limited to the new demo page.

## Risks / Trade-offs

- [Several user beans of one action/form type] → a startup error listing the candidates instead of silently picking the first (currently the first is picked — a latent bug).
- [Static `TagUtils` as a facade hides that the logic lives in services] → kept only as a transitional API; the spec fixes service overridability; new code must use the services.
- [The EL resolver cannot be registered after the first JSP] → registration at the initializer stage, before serving requests; an integration test catches the ordering.
- [Unannotated classes get bean post-processing — BREAKING] → autowiring by annotations only (not byName/byType-everything), default constructor as in the classic behavior; documented in the proposal.
- [Re-parenting rework of ~98 tags] → migration in batches per library with acceptance scenarios run after each; the base classes are signature-compatible (inheritance from `TagSupport` preserved).

## Migration Plan

The change is internal and seamless with respect to configuration: user artifacts (struts-config, JSP, web.xml) do not change; the new `StrutsTagServicesConfiguration` import is added to the demo apps. Rollback is a commit revert. Implementation order: auto-registration/resolver (core) → MessageSource bridge → tag base and services → tag migration per library (bean → html → logic → nested) → EL/RequestContextFilter → demo and ITs.

## Open Questions

- Should `Action` implement `ServletContextAware`/`ApplicationContextAware` to obtain the servlet environment the Spring-native way (currently the adapter calls the classic `setServlet`)? Does not affect the specs — to be decided during implementation.
