# Spec Delta: spring-boot-starter

## Purpose

Automatic configuration of the Struts 1 engine on Spring Boot: adding the starter dependency replaces manual setup (web.xml with DispatcherServlet, init-params, and importing engine configurations), and the classic init-params contract moves into properties.

## ADDED Requirements

### Requirement: Automatic engine configuration
Adding the `spring-struts1-starter` dependency to a Spring Boot application SHALL automatically register all Struts 1 engine beans — the `ActionServlet` facade, request handling (handler mapping and handler adapter), the instance resolver, and the struts-config types registrar — without manual import of engine configurations by the application. Behavior SHALL be equivalent to manually importing `StrutsEngineConfiguration`.

#### Scenario: Boot application without manual configuration
- **WHEN** a Spring Boot application adds `spring-struts1-starter` and sets the struts-config location via starter properties
- **THEN** the engine beans are registered and a request to an action is processed by the full lifecycle (populate → validation → execute → forward), as with manual configuration

#### Scenario: Classic mode keeps working
- **WHEN** a WAR application configures the engine via web.xml and manual import of configurations without the starter
- **THEN** the engine works as before; the starter is not required by the engine

### Requirement: Front servlet registration and mapping
The starter SHALL register the `DispatcherServlet` of the Spring Boot application with a mapping pattern from the `spring.struts1.mapping.url-pattern` property (default `*.do`; a list of patterns is supported). The engine's request-handling mapping SHALL follow the same property: suffix (`*.do`) and path-mapped (`/action/*`) mappings are consistent between the servlet and the engine.

#### Scenario: Default mapping
- **WHEN** the application does not configure a mapping pattern
- **THEN** requests to `/x.do` are handled by the action with `path="/x"`, and requests to paths outside the mapping do not reach the engine

#### Scenario: Path-mapped mapping via property
- **WHEN** `spring.struts1.mapping.url-pattern=/action/*` and a request arrives at `/action/x`
- **THEN** the request is handled by the action with `path="/x"`

### Requirement: Classic init-param contract via properties
The starter SHALL pass the classic contract parameters (`config`, `config/<prefix>`, `validating`) to the `ActionServlet` facade from the `spring.struts1.*` properties instead of web.xml init-params. Parameter semantics SHALL match the web.xml init-params.

#### Scenario: Configuration from properties
- **WHEN** the application sets `spring.struts1.config=classpath:struts-config.xml`
- **THEN** configuration is loaded from the specified resource and `getServlet().getInitParameter("config")` returns the same value

#### Scenario: Module configuration
- **WHEN** the application sets `spring.struts1.config.admin=/WEB-INF/struts-admin.xml`
- **THEN** the module with the `/admin` prefix is parsed from the specified file on par with the default module

#### Scenario: Default values
- **WHEN** the application does not set the configuration properties
- **THEN** `config` defaults to `/WEB-INF/struts-config.xml` and XML validation during parsing is enabled

#### Scenario: Missing configuration file
- **WHEN** `spring.struts1.config` points to a non-existent resource
- **THEN** application startup fails with an error naming the file

### Requirement: Conditional tag-services integration
The starter SHALL automatically wire in the tag library services when the `spring-struts1-taglib` module is on the classpath, and SHALL not require any further action from the application for them to work. When the module is absent from the classpath, the starter SHALL work without them.

#### Scenario: Tag library on the classpath
- **WHEN** the application adds `spring-struts1-starter` and `spring-struts1-taglib`
- **THEN** the tag library services are registered and Struts JSP tags work without manual import of their configuration

#### Scenario: Tag library absent
- **WHEN** the classpath contains the starter but not `spring-struts1-taglib`
- **THEN** the application starts without the tag library services and without startup errors

### Requirement: Disabling the starter
The starter SHALL support being fully disabled with the `spring.struts1.enabled=false` property: engine beans and the front servlet are not registered, and the Spring Boot application runs without starter interference.

#### Scenario: Disabling via property
- **WHEN** the application sets `spring.struts1.enabled=false`
- **THEN** the engine beans and the starter's `DispatcherServlet` are not registered, and the MVC application works with its own controllers
