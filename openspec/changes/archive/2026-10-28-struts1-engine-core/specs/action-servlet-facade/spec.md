# Spec Delta

## Purpose

Preserves compatibility with legacy Struts 1 code: a working `ActionServlet` facade with init-params and a `PlugIn` lifecycle (init at startup, destroy at shutdown).

## ADDED Requirements

### Requirement: ActionServlet facade
`ActionServlet` SHALL work as a compatibility facade: configuration is given by the init-param `config` from web.xml, and actions through `action.getServlet()` receive a working servlet through which the module configuration and init-params are accessible.

#### Scenario: Init-param config from web.xml
- **WHEN** web.xml declares the init-param `config` with the value `/WEB-INF/struts-config.xml`
- **THEN** the configuration is loaded from that file and `getServlet().getInitParameter("config")` returns the same value

#### Scenario: Servlet reference in an action
- **WHEN** the action's execute calls `action.getServlet()`
- **THEN** the working ActionServlet of the current application is returned

### Requirement: PlugIn lifecycle
Plugins from the configuration SHALL be initialized at application startup (in declaration order) and stopped at its shutdown; a plugin registered as a Spring bean SHALL be created from the context, the rest — by direct creation.

#### Scenario: Plugin init and destroy
- **WHEN** an application with a declared plugin starts and then stops
- **THEN** the plugin's init is called at startup and its destroy — at shutdown

#### Scenario: Plugin as a Spring bean
- **WHEN** the plugin class is registered in Spring as a bean
- **THEN** the plugin is created from the context and its dependencies are injected
