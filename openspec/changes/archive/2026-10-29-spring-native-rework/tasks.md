# Tasks

## 1. Forms and actions as Spring beans (core)

- [x] 1.1 Implement `StrutsBeanRegistrar`: after parsing the configuration, registers bean definitions in the `BeanDefinitionRegistry` — actions singleton (lazy-init), forms prototype, names `strutsAction:<fqcn>`/`strutsForm:<fqcn>`; types already registered by the user and `DynaActionForm` are skipped. Verify: unit test — a configuration with an action and a form yields definitions with the expected names and scopes; explicit registration is not duplicated.
- [x] 1.2 Rework `StrutsInstanceResolver`: user bean by type (a single candidate) → auto-registration → fallback `AutowireCapableBeanFactory.createBean`; several candidates — a startup error listing the candidates; the direct-instance cache removed. Verify: updated `StrutsInstanceResolverTest` unit tests — user bean priority, creation through Spring, error on two candidates of one type.
- [x] 1.3 IT: an action and a form without annotations are created by Spring — a dependency is injected, the singleton action is reused across requests, the form is fresh per request, `@PostConstruct` fires before the first execute, `@PreDestroy` fires on context close. Verify: IT tests in `spring-struts1-core` (TomcatTestServer) are green; a full run of `mvn -pl spring-struts1-core test`.

## 2. MessageResources → MessageSource bridge

- [x] 2.1 A `MessageSource` adapter over the classic resources + registration of bundles from `MessageResourcesConfig`: the default bundle becomes the context's `messageSource` if the user has not declared one; named bundles go under dedicated names. Verify: unit test — `MessageSource.getMessage` returns the same as the classic API, including the session locale and the `???ru_RU.key???` format.
- [x] 2.2 Priority test: with a user `messageSource` bean, the primary source is not replaced, and the Struts bundles are available under dedicated names. Verify: unit test with a custom `messageSource` is green.

## 3. Tag base and logic services (taglib)

- [x] 3.1 Introduce `SpringTagSupport`/`SpringBodyTagSupport` (lazy access to the `WebApplicationContext`, bean-lookup helpers) and `StrutsTagServicesConfiguration` with fixed service names. Verify: unit test — a tag on the new base gets the context and a service bean.
- [x] 3.2 Implement `TagMessageResolver` (locale from session/request, the `???ru_RU.key???` format) as a Spring component. Verify: unit tests for locale and the missing-key format.
- [x] 3.3 Implement `TagUrlBuilder`, `TagFormLocator`, `ErrorsMessagesRenderer` as Spring components. Verify: unit tests — URLs with module prefix and encoding, form lookup in request/session, error rendering with header/footer/prefix/suffix.
- [x] 3.4 Migrate the relevant `TagUtils` methods to delegate to services from the current request's context; the rest of the static API unchanged. Verify: existing `TagUtils` tests pass; a test where overriding a service changes output.

## 4. Move tags to the Spring implementation

- [x] 4.1 The `bean` library — tags on the new base and services. Verify: the acceptance scenarios of the `bean-taglib` spec (write, message, define, size, header/cookie/parameter) pass, output identical.
- [x] 4.2 The `html` library — tags on the new base and services (form, widgets, errors/messages, img/link/rewrite). Verify: the acceptance scenarios of the `html-taglib` spec pass, HTML output identical.
- [x] 4.3 The `logic` library — tags on the new base and services (iterate, comparisons, present/empty, redirect/forward). Verify: the acceptance scenarios of the `logic-taglib` spec pass.
- [x] 4.4 The `nested` library — tags on the new base and services. Verify: the acceptance scenarios of the `nested-taglib` spec pass.

## 5. Spring JSP stack

- [x] 5.1 Register the `RequestContextFilter` in the engine configuration. Verify: IT — `spring:message` on an engine page renders a message from a Struts bundle.
- [x] 5.2 Register the `BeanNameResolver` (names from the Spring context) via `JspApplicationContext` once, before the first JSP. Verify: IT — an EL expression over a bean name is rendered; repeated initialization does not fail.
- [x] 5.3 Mixed-page IT: `html:form` with `html:text`, `spring:message`, and EL access to a bean on one JSP. Verify: IT green, all three constructs rendered.

## 6. Demonstration and final integration

- [x] 6.1 Demo page and action in `examples`: an action with an `@Autowired` service, a form with `@Autowired` and `@PostConstruct`, a JSP mixing Struts and Spring tags. Verify: the `ExamplesTest` IT covers the demo scenario.
- [x] 6.2 Full regression: `mvn test` of all modules, existing ITs `blank` and `examples` with unchanged expectations. Verify: the build is green as a whole; `openspec validate --strict` on the change passes.

## Workflow follow-up

- Archive the change after the project's review requirements are satisfied.
- Verify the archived result.
