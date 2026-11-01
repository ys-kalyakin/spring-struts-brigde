# Getting started

Add the starter (it brings the whole engine) and the tag libraries your JSPs use:

```xml
<dependency>
  <groupId>io.github.ys-kalyakin.springstruts</groupId>
  <artifactId>spring-struts1-starter</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
<dependency>
  <groupId>io.github.ys-kalyakin.springstruts</groupId>
  <artifactId>spring-struts1-taglib</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
<!-- Only if you use Tiles: -->
<dependency>
  <groupId>io.github.ys-kalyakin.springstruts</groupId>
  <artifactId>spring-struts1-tiles</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Point the starter at your `struts-config.xml`:

```properties
spring.struts1.config=classpath:struts-config.xml
```

That is the whole wiring — the starter auto-configures the engine: it registers the front servlet with the classic `*.do` mapping, reads the configuration, and connects `MessageResources` to Spring. See the [configuration reference](configuration.md) for every `spring.struts1.*` property (all defaults match the classic init-params, so an empty `application.properties` with a WAR-style config path is also valid).

Optionally, annotate your actions and forms with `@StrutsAction` / `@StrutsForm` to turn them into Spring beans and get dependency injection:

```java
@StrutsAction
public class SubscribeAction extends Action {

    private final SubscribeService subscribeService;

    public SubscribeAction(SubscribeService subscribeService) {
        this.subscribeService = subscribeService;
    }
    // execute(...) — the classic contract
}
```

Without the annotations the application works in the classic fallback — action and form classes are created directly by name from `struts-config.xml`.

## Next steps

- Migrating an existing Struts 1 application? Follow the [migration guide](MIGRATION.md) — it covers the full swap, `web.xml` reduction, and the common adjustments.
- Prefer to see it working? The [demo app](https://github.com/ys-kalyakin/spring-struts-brigde/tree/main/spring-struts1-apps/demo) is a minimal migrated application reproduced strictly by the steps of the guide.
- Rendering JSPs with Struts tags? Check the [tag libraries](taglibs.md) your pages can use.
