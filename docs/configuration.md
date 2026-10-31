# Configuration

The starter is configured with Spring properties in the `spring.struts1.*` namespace. The classic `ActionServlet` init-param contract (`config`, `config/<prefix>`, `validating`) is read directly from the Spring `Environment` — the defaults match the classic values, so an application that relied on the defaults needs no properties at all.

## Properties

| Property | Default | Description |
|---|---|---|
| `spring.struts1.enabled` | `true` | Master switch: when `false`, the starter registers nothing. |
| `spring.struts1.config` | `/WEB-INF/struts-config.xml` | Configuration resource(s) of the default module — the classic `config` init-param, a comma-separated list. |
| `spring.struts1.config.<prefix>` | — | Configuration of a module — the classic `config/<prefix>` init-param, e.g. `spring.struts1.config.admin=/WEB-INF/struts-config-admin.xml`. |
| `spring.struts1.validating` | `true` | Validate the configuration XML while parsing (the classic `validating` init-param). |
| `spring.struts1.servlet-name` | `action` | Name of the registered front servlet. |
| `spring.struts1.mapping.url-pattern` | `*.do` | URL patterns the front servlet is mapped to (the classic servlet-mapping), as a list: `*.do`, `/action/*`. |

## Examples

A config on the classpath — equally available to the executable run and a WAR deployment:

```properties
spring.struts1.config=classpath:struts-config.xml
```

Multiple resources for the default module, plus a module configuration:

```properties
spring.struts1.config=/WEB-INF/struts-config.xml,/WEB-INF/struts-config-legacy.xml
spring.struts1.config.admin=/WEB-INF/struts-config-admin.xml
```

Extended front-servlet mapping:

```properties
spring.struts1.mapping.url-pattern=*.do,/action/*
```

Module configs cannot be bound next to the scalar `config` key as a typed map, so they are read from the `Environment` by property name — the classic `config/<prefix>` contract, one property per module prefix.
