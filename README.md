# spring-struts-bridge

[![CI](https://github.com/ys-kalyakin/spring-struts-brigde/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/ys-kalyakin/spring-struts-brigde/actions/workflows/ci.yml)
[Documentation](https://ys-kalyakin.github.io/spring-struts-brigde/)

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

## Modules

Group `org.springstruts`:

| Artifact | Purpose |
|---|---|
| `spring-struts1-core` | the engine: configuration model (commons-digester), request lifecycle (`StrutsHandlerMapping`/`StrutsHandlerAdapter`), the `ActionServlet` facade, `MessageResources` on top of Spring `MessageSource`, the `@StrutsAction`/`@StrutsForm` annotations |
| `spring-struts1-taglib` | the struts-bean/html/logic/nested tag libraries with the classic URIs and TLDs, plus commons-validator (`ValidatorPlugIn`) |
| `spring-struts1-tiles` | Tiles definitions, controllers, and `TilesAction` (the 1.x-compatible stack) |
| `spring-struts1-starter` | Spring Boot auto-configuration of the engine: `spring.struts1.*` properties, front-servlet registration with the classic mapping, `RequestContextFilter`, default-servlet handling for embedded containers |

The ported weblegacy test applications (`spring-struts1-apps/` — `blank`, `examples`, `cookbook`, `mailreader`) run as the integration-test suite against the assembled starter; [`spring-struts1-apps/demo`](spring-struts1-apps/demo/) is a minimal migration example.

## Getting started

Add the starter (it brings the whole engine) and the tag libraries your JSPs use:

```xml
<dependency>
  <groupId>org.springstruts</groupId>
  <artifactId>spring-struts1-starter</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
<dependency>
  <groupId>org.springstruts</groupId>
  <artifactId>spring-struts1-taglib</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
<!-- Only if you use Tiles: -->
<dependency>
  <groupId>org.springstruts</groupId>
  <artifactId>spring-struts1-tiles</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Point the starter at your `struts-config.xml`:

```properties
spring.struts1.config=classpath:struts-config.xml
```

Optionally, annotate your actions and forms with `@StrutsAction` / `@StrutsForm` to turn them into Spring beans.

Then follow the step-by-step [migration guide](docs/MIGRATION.md), or look at [spring-struts1-apps/demo](spring-struts1-apps/demo/) — a working migrated application.

## Code quality checks

The build enforces code style (Checkstyle) and bug patterns (SpotBugs) on every module during `./mvnw verify`; any violation or finding fails the build. The ruleset lives in [config/checkstyle.xml](config/checkstyle.xml) (with documented exclusions in [config/checkstyle-suppressions.xml](config/checkstyle-suppressions.xml)), the SpotBugs exclusion policy in [config/spotbugs-exclude.xml](config/spotbugs-exclude.xml) — every exclusion carries a written rationale and is scoped as tightly as possible. CI uploads the Checkstyle and SpotBugs reports as run artifacts.

To run the checks alone:

```bash
./mvnw checkstyle:check spotbugs:check
```

Generated code (the ANTLR validwhen parser, JMH-generated benchmark classes) is not analyzed.

## Notes

Form population runs on the engine's Spring `ConversionService` with the semantics of the default commons-beanutils registry (Boolean literals, zero values for malformed numbers, JDBC formats for `java.sql` dates; the behavior is pinned by fixture tests) — converters an application registers in the static `ConvertUtils` registry do not affect form filling. commons-beanutils remains a dependency for the public API (the `DynaBean` hierarchy of dynamic forms) and the validator.
