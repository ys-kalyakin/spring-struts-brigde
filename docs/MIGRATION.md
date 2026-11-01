# Migrating a Struts 1 application to spring-struts1

A guide for an existing Struts 1 application (the compatibility reference is
[weblegacy/struts1](https://github.com/weblegacy/struts1) 1.5.x, the
`jakarta.*` line). The result of the migration: the application runs on
Spring Boot 4.0.x / Spring Framework 7 / embedded Tomcat 11, while
`struts-config.xml`, the JSPs, the tag libraries and the message-resources
remain unchanged.

A complete live example of the migration is
[spring-struts1-apps/demo](https://github.com/ys-kalyakin/spring-struts-brigde/tree/main/spring-struts1-apps/demo);
the section [Checking against the demo](#checking-against-the-demo) shows
that the demo is reproduced strictly by the steps of this guide.

## Step 1. Swap the dependency

Remove the Struts 1 artifacts (weblegacy: `struts1-core`, `struts1-taglib`,
`struts1-tiles` and companions) and add the `io.github.ys-kalyakin.springstruts` artifacts:

| Artifact | What it replaces | When required |
|---|---|---|
| `spring-struts1-core` | `struts1-core` — the engine and the configuration model | always (comes transitively with the starter) |
| `spring-struts1-taglib` | `struts1-taglib` — the bean/html/logic/nested tag libraries + commons-validator | needed if the application renders JSPs with Struts tags |
| `spring-struts1-tiles` | `struts1-tiles` — Tiles definitions and controllers | needed if the application uses Tiles |
| `spring-struts1-starter` | manual engine setup (ContextLoaderListener, web.xml) | always |

The `org.apache.struts.*` packages and the public API are preserved — the
application code compiles unchanged. The commons dependencies (digester,
beanutils, validator, fileupload2) come transitively.

The minimal set for an application with JSPs:

```xml
<dependency>
  <groupId>io.github.ys-kalyakin.springstruts</groupId>
  <artifactId>spring-struts1-starter</artifactId>
</dependency>
<dependency>
  <groupId>io.github.ys-kalyakin.springstruts</groupId>
  <artifactId>spring-struts1-taglib</artifactId>
</dependency>
```

## Step 2. The Spring Boot application class

Add a single class — it is the `java -jar` entry point and the bootstrap in
a standalone container:

```java
@SpringBootApplication
public class MyApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

`web.xml` shrinks: the `ActionServlet` declaration (or the classic
`servlet-class`), the `servlet-mapping` and all `init-param`s are removed —
the starter configures the engine. Usually only `display-name` and
`welcome-file-list` remain.

**Mandatory**: remove `metadata-complete="true"` from the `<web-app>` root,
if present — this attribute forbids the container from scanning
`ServletContainerInitializer`s, through which Boot deploys the context, and
the application simply does not start.

## Step 3. Configuration properties

The classic init-param contract is read from Spring properties
(`spring.struts1.*`); the defaults match the classic ones, so an empty
`application.properties` is valid:

| Property | Default | Classic counterpart |
|---|---|---|
| `spring.struts1.config` | `/WEB-INF/struts-config.xml` | init-param `config` (comma-separated list) |
| `spring.struts1.config.<prefix>` | — | init-param `config/<prefix>` (modules) |
| `spring.struts1.validating` | `true` | init-param `validating` |
| `spring.struts1.mapping.url-pattern` | `*.do` | `servlet-mapping` (list: `*.do`, `/action/*`) |
| `spring.struts1.servlet-name` | `action` | `<servlet-name>` |
| `spring.struts1.enabled` | `true` | — (disables the starter entirely) |

A `classpath:` path is supported:
`spring.struts1.config=classpath:struts-config.xml` places the config in
`src/main/resources` — this way it is equally available to the executable
run and the WAR deployment (that is how the demo does it). Module configs
are set with properties suffixed by the module prefix, for example
`spring.struts1.config.admin=/WEB-INF/struts-config-admin.xml`.

## Step 4. Annotations on actions and forms (optional)

The annotations are needed only when actions/forms need Spring dependency
injection; without them the application works in the classic fallback —
classes are created directly by name from the config.

```java
@StrutsAction  // org.apache.struts.action.StrutsAction — singleton bean
public class SubscribeAction extends Action {

    private final SubscribeService subscribeService;

    public SubscribeAction(SubscribeService subscribeService) { // @Autowired constructor
        this.subscribeService = subscribeService;
    }
    // execute(...) — the classic contract
}

@StrutsForm  // org.apache.struts.action.StrutsForm — prototype bean
public class SubscribeForm extends ActionForm {
    // set/get pairs for populate, validate(...) — the classic contract
}
```

Semantics: `@StrutsAction` — one instance per application (reused between
requests, like the classic singleton action); `@StrutsForm` — a new instance
per request (form state is not shared). `struts-config.xml` remains the
source of mappings, forms, forwards and message-resources — the annotations
make the classes Spring beans, they do not replace the configuration.

## Common adjustments

- **JSP compilation.** For executable mode add `tomcat-embed-jasper`
  (compile) and `spring-boot-starter-tomcat` (provided — the Boot repackage
  puts it into `WEB-INF/lib-provided`). In a WAR deployment on standalone
  Tomcat, JSP works out of the box.
- **JSPs inside a jar are not supported.** The container compiles JSPs only
  from the webapp directory/WAR: JSP pages must remain in
  `src/main/webapp` (or in the WAR), not in jar libraries.
- **Static resources.** In the embedded container the classic container
  default servlet is not registered automatically; the starter registers it
  itself (`DefaultServletInitializer`, Tomcat 11). Nothing to do — the
  `*.do` mapping does not conflict with static content.
- **404 errors.** The starter adds the `/error` path to the front servlet
  mapping so that unclassified requests return an honest 404 through the
  Boot error mechanism, not a 405.
- **Multipart/upload.** The engine parses multipart itself (the `FormFile`
  contract); the starter keeps the Boot multipart auto-configuration
  switched off — uploads work as in the classic.
- **RequestContextFilter.** The starter registers it as a Boot filter — the
  request context is available to direct JSP requests too, not only to
  forwards.
- **Do not declare `tomcat-embed-*` manually** in compile/test/optional
  scopes: Maven's "nearest" dependency marker shades the transitive one, and
  Tomcat may drop out of the assembled WAR. Let Tomcat come from
  `spring-boot-starter-tomcat`.

## Form beans must not be singletons

If a form class declared in `struts-config.xml` is registered by the
application as a plain Spring bean (a `@Component`/`@Bean` without a scope,
which defaults to singleton), **startup fails** with an error naming the form
type, the bean, and the fix.

Why: a singleton form instance is shared by every session and every user —
two requests would read and write the same fields concurrently. The form
lifecycle assumes fresh state per request (or per session when the mapping
declares `scope="session"`), so a singleton form was never a valid
configuration; the engine now refuses it at startup instead of silently
serving shared mutable state.

How to migrate: scope the form bean as
`@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)` (a new instance per
request), or `@RequestScope`/`@SessionScope` when you want Spring to manage
the lifetime. The auto-registered beans and `@StrutsForm` classes are
prototype already — the failure can only be caused by an explicit user
definition.

## Functional limitations

The engine implements the classic `<controller>` configuration partially.
To keep a migration honest, startup validates every module's `<controller>`
against what the engine implements: a configuration that relies on an
unimplemented setting fails the startup with an actionable error instead of
silently skipping the configured logic.

| `<controller>` setting | Treatment | Why |
|---|---|---|
| `processorClass` other than `org.apache.struts.action.RequestProcessor` or `org.apache.struts.tiles.TilesRequestProcessor` | **startup error** | A custom request processor's logic (authentication, audit, output post-processing) would silently not run |
| `catalog` / `command` other than the classic defaults (`struts` / `servlet-standard`) | **startup error** | commons-chain configuration has no meaning on the engine |
| `bufferSize` other than the classic default (`4096`) | **warning**, the application starts | a tuning knob with no implemented effect; uploads use the container's multipart settings |

`TilesRequestProcessor` is accepted because the engine implements
tiles-forward behavior natively. Every other controller setting — the
multipart limits, `locale`, `contentType`, `nocache`, the forward/page
patterns — is honored.

**Migrating a custom request processor.** When the startup error names your
`processorClass`, re-express the processor's `process*` overrides with the
Spring mechanisms before removing the attribute: request-level logic
(authentication, headers, audit) as a servlet filter or a Spring MVC
interceptor, action-level logic in the action bean itself, and the remaining
processing (populate, validation, exception handling) is already provided by
the engine.

## Checking against the demo

The demo
[spring-struts1-apps/demo](https://github.com/ys-kalyakin/spring-struts-brigde/tree/main/spring-struts1-apps/demo)
is a classic Struts application (a subscription form with validation and
forwards), migrated strictly by the steps above. The migration delta is
four files:

1. `pom.xml` — dependencies: starter + taglib + jasper (+ provided
   starter-tomcat and servlet-api), the Boot repackage plugin (step 1, the
   jasper adjustment);
2. `src/main/java/demo/DemoApplication.java` — the application class
   (step 2);
3. `@StrutsAction` on `SubscribeAction` (with an injected `SubscribeService`)
   and `@StrutsForm` on `SubscribeForm` (step 4);
4. `src/main/resources/application.properties` —
   `spring.struts1.config=classpath:struts-config.xml` (step 3).

Everything else — `struts-config.xml`, the JSPs, the message-resources
(`demo.Messages`) — are classic artifacts left unchanged.

To verify the build and both modes:

```bash
./mvnw -pl spring-struts1-apps/demo verify          # integration tests of the WAR on Tomcat + a Boot run
./mvnw -pl spring-struts1-apps/demo package
java -jar spring-struts1-apps/demo/target/demo.war  # http://localhost:8080/subscribe.do
```

The version compatibility matrix is in the
[README](https://github.com/ys-kalyakin/spring-struts-brigde#compatibility).
