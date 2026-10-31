# spring-boot-starter Specification

## Purpose

Automatic configuration of the Struts 1 engine on Spring Boot: adding the starter dependency replaces manual setup (web.xml with DispatcherServlet, init-params, and importing the engine configurations); the classic init-param contract is carried over into properties.

## Requirements

### Requirement: Automatic engine configuration
Adding the `spring-struts1-starter` dependency to a Spring Boot application SHALL automatically register all beans of the Struts 1 engine — the `ActionServlet` facade, request processing (handler mapping and handler adapter), the instance resolver, and the registrar of types from struts-config — without the application manually importing the engine configurations. Behavior SHALL be equivalent to manually importing `StrutsEngineConfiguration`.

#### Scenario: Boot application without manual configuration
- **WHEN** a Spring Boot application adds `spring-struts1-starter` and sets the struts-config location via starter properties
- **THEN** the engine beans are registered, and a request to an action is handled by the full lifecycle (populate → validation → execute → forward), as with manual configuration

#### Scenario: Classic mode keeps working
- **WHEN** a WAR application configures the engine via web.xml and manual configuration imports without the starter
- **THEN** the engine works as before; the starter is not required by the engine

### Requirement: Front servlet registration and mapping
The starter SHALL register the application's `DispatcherServlet` for the Spring Boot application with a mapping pattern taken from the property `spring.struts1.mapping.url-pattern` (default `*.do`; a list of patterns is supported). The engine's request processing mapping SHALL follow the same property: suffix (`*.do`) and path-mapped (`/action/*`) mappings are consistent between the servlet and the engine.

#### Scenario: Default mapping
- **WHEN** the application does not configure a mapping pattern
- **THEN** requests to `/x.do` are handled by the action with `path="/x"`, and requests to paths outside the mapping do not reach the engine

#### Scenario: Path-mapped mapping via the property
- **WHEN** `spring.struts1.mapping.url-pattern=/action/*` and a request to `/action/x` arrives
- **THEN** the request is handled by the action with `path="/x"`

### Requirement: Classic init-param contract via properties
The starter SHALL pass the classic-contract parameters (`config`, `config/<prefix>`, `validating`) to the `ActionServlet` facade from `spring.struts1.*` properties instead of web.xml init-params. Parameter semantics SHALL match the web.xml init-params.

#### Scenario: Configuration from properties
- **WHEN** the application sets `spring.struts1.config=classpath:struts-config.xml`
- **THEN** the configuration is loaded from the specified resource and `getServlet().getInitParameter("config")` returns the same value

#### Scenario: Module configuration
- **WHEN** the application sets `spring.struts1.config.admin=/WEB-INF/struts-admin.xml`
- **THEN** the module with prefix `/admin` is parsed from the specified file on par with the default module

#### Scenario: Default values
- **WHEN** the application does not set configuration properties
- **THEN** `config` defaults to `/WEB-INF/struts-config.xml`, and XML validation during parsing is enabled

#### Scenario: Missing configuration file
- **WHEN** `spring.struts1.config` points to a non-existent resource
- **THEN** application startup fails with an error naming the file

### Requirement: Conditional tag library services integration
The starter SHALL automatically wire in the tag library services if the `spring-struts1-taglib` module is on the classpath, and SHALL not require any further action from the application for them to work. When the module is absent from the classpath, the starter SHALL work without them.

#### Scenario: Tag library on the classpath
- **WHEN** the application adds `spring-struts1-starter` and `spring-struts1-taglib`
- **THEN** the tag library services are registered and Struts JSP tags work without manually importing their configuration

#### Scenario: Tag library absent
- **WHEN** the classpath has the starter but no `spring-struts1-taglib`
- **THEN** the application starts without the tag library services and without startup errors

### Requirement: Disabling the starter
The starter SHALL support being fully disabled via the property `spring.struts1.enabled=false`: the engine beans and front servlet are not registered, and the Spring Boot application works without starter intervention.

#### Scenario: Disabling via the property
- **WHEN** the application sets `spring.struts1.enabled=false`
- **THEN** the engine beans and the starter's `DispatcherServlet` are not registered, and the MVC application works with its own controllers
