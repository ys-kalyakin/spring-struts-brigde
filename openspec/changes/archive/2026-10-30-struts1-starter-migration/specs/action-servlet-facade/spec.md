# Spec Delta: action-servlet-facade

## MODIFIED Requirements

### Requirement: ActionServlet facade
The `ActionServlet` SHALL work as a compatibility facade: configuration is given by the `config` init-param from web.xml (classic WAR mode) or by the starter properties `spring.struts1.*` (Boot mode), and actions obtain a working servlet via `action.getServlet()`, through which module configuration and init-params are available. In Boot mode, the facade's init-params SHALL match the starter properties with the same semantics.

#### Scenario: config init-param from web.xml
- **WHEN** web.xml declares the init-param `config` with the value `/WEB-INF/struts-config.xml`
- **THEN** configuration is loaded from that file and `getServlet().getInitParameter("config")` returns the same value

#### Scenario: Starter properties in Boot mode
- **WHEN** a Boot application sets `spring.struts1.config=/WEB-INF/struts-config.xml`
- **THEN** configuration is loaded from that resource and `getServlet().getInitParameter("config")` returns the same value

#### Scenario: Servlet reference in an action
- **WHEN** an action's execute calls `action.getServlet()`
- **THEN** the working ActionServlet of the current application is returned
