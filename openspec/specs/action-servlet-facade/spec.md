# action-servlet-facade Specification

## Purpose

Preserves compatibility with legacy Struts 1 code: a working `ActionServlet` facade with init-params and the `PlugIn` lifecycle (init at startup, destroy at shutdown).

## Requirements

### Requirement: ActionServlet facade
`ActionServlet` SHALL operate as a compatibility facade: configuration is provided either by the `config` init-param from web.xml (classic WAR mode) or by the starter properties `spring.struts1.*` (Boot mode), and actions receive a working servlet through `action.getServlet()`, through which the module configuration and init-params are accessible. In Boot mode, the facade init-params SHALL match the starter properties with the same semantics.

#### Scenario: Init-param config from web.xml
- **WHEN** web.xml declares init-param `config` with value `/WEB-INF/struts-config.xml`
- **THEN** configuration is loaded from that file and `getServlet().getInitParameter("config")` returns the same value

#### Scenario: Starter properties in Boot mode
- **WHEN** a Boot application sets `spring.struts1.config=/WEB-INF/struts-config.xml`
- **THEN** configuration is loaded from that resource and `getServlet().getInitParameter("config")` returns the same value

#### Scenario: Servlet reference in an action
- **WHEN** an action's execute calls `action.getServlet()`
- **THEN** the working ActionServlet of the current application is returned

### Requirement: PlugIn lifecycle
Plugins from the configuration SHALL be initialized at application startup (in declaration order) and stopped at shutdown; a plugin registered as a Spring bean SHALL be created from the context, the rest — by direct instantiation.

#### Scenario: Plugin init and destroy
- **WHEN** an application with a declared plugin starts and then stops
- **THEN** the plugin's init is called at startup and its destroy at shutdown

#### Scenario: Plugin as a Spring bean
- **WHEN** the plugin class is registered in Spring as a bean
- **THEN** the plugin is created from the context and its dependencies are injected
