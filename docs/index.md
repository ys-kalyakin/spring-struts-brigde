# spring-struts-bridge

A recreation of the **Struts 1** framework on top of modern Spring. An existing Struts 1 application migrates to a current stack by swapping the dependency, adding the Spring Boot starter, and making minimal code changes — annotations on forms and actions so they become Spring beans. The compatibility reference is [weblegacy/struts1](https://github.com/weblegacy/struts1) (the 1.5.x line, `jakarta.*`): its API, TLDs, and test applications serve as the yardstick for this implementation.

## Key principles

- the `org.apache.struts.*` packages and the public Struts 1 API are preserved — replacing the artifact keeps your code compiling;
- `struts-config.xml` remains the source of configuration; actions and forms become Spring beans;
- the Struts tag libraries (bean, html, logic, nested, tiles) work with the same URIs and TLDs on the Spring engine.

## Compatibility

| Component | Version |
|---|---|
| Java | 17+ |
| Spring Boot | 4.0.x (build and tests verified on 4.0.9) |
| Spring Framework | 7.0.x — aligned with Boot via the imported `spring-boot-dependencies` BOM |
| Container | embedded Tomcat 11 (standalone Tomcat 11 for WAR deployment), `jakarta.servlet` 6.1 |
| API compatibility | weblegacy/struts1 1.5.x (`jakarta.*`) |

## Where to go next

- [Getting started](getting-started.md) — wire the starter into a Spring Boot application in a few lines.
- [Migration guide](MIGRATION.md) — step-by-step migration of an existing Struts 1 application, verified against a working [demo app](https://github.com/ys-kalyakin/spring-struts-brigde/tree/main/spring-struts1-apps/demo).
- [Tag libraries](taglibs.md), [configuration reference](configuration.md), and [modules](modules.md) — the details behind the principles above.
