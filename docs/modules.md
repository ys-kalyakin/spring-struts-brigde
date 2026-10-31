# Modules

All artifacts are published under the group `org.springstruts`.

| Artifact | Purpose |
|---|---|
| `spring-struts1-core` | the engine: configuration model (commons-digester), request lifecycle (`StrutsHandlerMapping`/`StrutsHandlerAdapter`), the `ActionServlet` facade, `MessageResources` on top of Spring `MessageSource`, the `@StrutsAction`/`@StrutsForm` annotations |
| `spring-struts1-taglib` | the struts-bean/html/logic/nested tag libraries with the classic URIs and TLDs, plus commons-validator (`ValidatorPlugIn`) |
| `spring-struts1-tiles` | Tiles definitions, controllers, and `TilesAction` (the 1.x-compatible stack) |
| `spring-struts1-starter` | Spring Boot auto-configuration of the engine: `spring.struts1.*` properties, front-servlet registration with the classic mapping, `RequestContextFilter`, default-servlet handling for embedded containers |

## Which artifacts do I need?

- **Always**: `spring-struts1-starter` — it brings `spring-struts1-core` transitively and wires the engine into Spring Boot. See [getting started](getting-started.md).
- **JSPs rendered with Struts tags**: `spring-struts1-taglib` (see [tag libraries](taglibs.md)).
- **Tiles in use**: `spring-struts1-tiles`.

## Test applications

The ported weblegacy test applications ([spring-struts1-apps](https://github.com/ys-kalyakin/spring-struts-brigde/tree/main/spring-struts1-apps) — `blank`, `examples`, `cookbook`, `mailreader`) run as the integration-test suite against the assembled starter; [demo](https://github.com/ys-kalyakin/spring-struts-brigde/tree/main/spring-struts1-apps/demo) is a minimal migration example — the migration guide's [checking section](MIGRATION.md#checking-against-the-demo) shows how it is reproduced.
