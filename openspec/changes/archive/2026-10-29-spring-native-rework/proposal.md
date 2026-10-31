# Proposal

## Why

Phases 1–2 reproduce Struts 1 "as-is": only classes annotated with `@StrutsAction`/`@StrutsForm` become Spring beans, while the remaining forms and actions are created via `newInstance` bypassing Spring — with no dependency injection and no bean lifecycle. The tag libraries are classic ports on static singletons (`MessageResources.getMessageResources`, `TagUtils`) with no access to the Spring context. This is copying the original, not integration: the project's goal is a Struts 1 → Spring bridge, so the engine must be Spring-native while keeping the classic API.

## What Changes

- **All forms and actions become Spring beans**: types declared in `struts-config.xml` are automatically registered in the context as bean definitions at startup (actions — singleton, forms — prototype). Explicit user registration (component scan of `@StrutsAction`/`@StrutsForm`, Java/XML configuration) takes priority over auto-registration. Direct creation via `newInstance` is gone: any form or action instance is created by Spring (`AutowireCapableBeanFactory`). **BREAKING**: the "direct-creation fallback" requirement from `spring-bean-integration` is replaced by auto-registration — a class without annotations also becomes a Spring bean.
- **Dependency injection works in every form and action** regardless of annotations: `@Autowired`/setter injection and lifecycle callbacks (`@PostConstruct`/`@PreDestroy`, `BeanPostProcessor`) are applied to all instances. The classic lifecycle semantics are preserved: one action instance per class across requests, a fresh form per request.
- **Tag libraries move to a Spring-native implementation**: a Spring-aware tag base gives handlers access to the `WebApplicationContext`; shared tag logic (message resolution, URL building, form lookup in scope, error rendering) is extracted into Spring components; tags get dependency injection. The classic external contract is preserved: the same TLDs/URIs/tag names/attributes and the same HTML output (the `bean-taglib`, `html-taglib`, `logic-taglib`, `nested-taglib` specs remain in force), existing JSPs are not rewritten.
- **MessageResources is bridged to the Spring `MessageSource`**: bundles from `<message-resources>` are registered as `MessageSource` beans; the classic `MessageResources` API is preserved on top of the bridge. Behavior is unchanged: locale from the session, the `???ru_RU.key???` format (the `message-resources` spec remains in force).
- **Coexistence in JSP**: Struts tags, Spring tags (`spring:message`, `spring:eval`, etc.), and EL access to Spring beans all work on the same page — a "Spring JSP stack" on top of the same context.

Assumption (interpretation of the request): the "Spring JSP engine" is understood as the Spring JSP stack — Spring's tag infrastructure (`RequestContext`, `MessageSource`, `spring:*` tags, EL access to beans) — which Struts tags integrate with and are built on the same model (Spring-aware tags + Spring logic components). TLDs/URIs/tag names and HTML output do not change.

## Capabilities

### New Capabilities

- `spring-tag-integration`: Spring-native tag-library infrastructure — a tag base with access to the Spring context, tag logic as Spring components (messages, URLs, form lookup), dependency injection into tags, a `MessageResources` → `MessageSource` bridge, coexistence of Struts and Spring tags in a single JSP.

### Modified Capabilities

- `spring-bean-integration`: instead of "annotated classes are beans, everything else is created directly" — all forms and actions are Spring beans: auto-registration of types from `struts-config.xml` (explicit registration takes priority), every instance created through Spring, dependency injection and lifecycle for all.

## Impact

- **Code:** `spring-struts1-core` — `StrutsInstanceResolver` (creation via `AutowireCapableBeanFactory`), a new registrar of bean definitions from `ModuleConfig`, `StrutsEngineConfiguration`; `spring-struts1-taglib` — tag base classes and shared helpers (`TagUtils`, message resolution); `spring-struts1-apps` — demonstration of injection in the examples.
- **API:** the classic Struts 1 API is preserved (`Action`, `ActionForm`, `execute`/`validate` signatures, TLDs, URIs, tag names and attributes); tag HTML output does not change. **BREAKING** only in the lifecycle sense: a class without Spring registration now goes through Spring (receives bean post-processing).
- **Tests:** existing ITs (`examples`, `blank`) pass with unchanged results — a compatibility regression; new tests for auto-registration, injection into forms/actions/tags, and mixing Struts and Spring tags.
- **Dependencies:** no new ones (Spring context/web, jakarta servlet/jsp are already in place).
- **Out of scope:** replacing JSP with another view technology, struts-el, commons-validator, Tiles, changing TLDs/URIs/tag names, changing HTML output.
