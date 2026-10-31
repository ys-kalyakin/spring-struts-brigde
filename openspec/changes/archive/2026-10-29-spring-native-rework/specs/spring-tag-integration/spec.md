# Spec Delta: spring-tag-integration

## Purpose

Makes the tag libraries part of the Spring JSP stack: tag handlers get access to the Spring context and its beans, shared tag logic runs in Spring components, and Struts tags work on the same page as Spring tags.

## ADDED Requirements

### Requirement: Tag access to the Spring context
A custom tag built on the engine's tag infrastructure SHALL have access to the Spring application context and its beans while processing a page.

#### Scenario: A custom tag uses a Spring bean
- **WHEN** a custom tag built on the engine base obtains a service bean from the context and writes its result into the tag body
- **THEN** the page renders data obtained from the Spring bean

### Requirement: Tag logic runs in Spring components
The shared tag-library logic — message resolution, URL building, form lookup in scope, error and message rendering — SHALL run in Spring components, and tag handlers delegate to them; overriding such a component by the user changes the behavior of the corresponding tags.

#### Scenario: Overriding a component changes tag output
- **WHEN** the application overrides the tag message-resolution Spring component with its own implementation
- **THEN** the output of message and error tags follows the user's implementation

#### Scenario: Dependency injection into a tag
- **WHEN** a custom tag built on the engine base declares a dependency on a context bean
- **THEN** the dependency is available by the time the tag body is processed, without manual lookup by string keys

### Requirement: message-resources bundles as MessageSource
Bundles declared in `<message-resources>` SHALL be available through the Spring `MessageSource`: the default bundle as the context's primary `MessageSource`, named ones under their own keys; Spring tags (for example `spring:message`) render messages from them.

#### Scenario: spring:message reads a Struts bundle
- **WHEN** the configuration declares `<message-resources parameter="MessageResources"/>`, and a JSP uses `spring:message` with a key code from that bundle
- **THEN** the page shows the same message that the classic `bean:message` tag returns

#### Scenario: A named bundle is available by key
- **WHEN** the configuration declares a bundle with key `alternate`, and a page accesses it through the `MessageSource` by that key
- **THEN** the message is obtained from the `alternate` bundle

### Requirement: Coexistence of Struts and Spring tags in JSP
A combination of Struts tags, Spring tags, and EL access to context beans SHALL work correctly on a single JSP page: each construct renders its expected result with no extra configuration.

#### Scenario: A mixed page
- **WHEN** a JSP contains `html:form` with `html:text`, `spring:message`, and an EL expression referencing a Spring bean
- **THEN** all three constructs are rendered: the form with the property value, the message from the bundle, and the bean's data

### Requirement: The classic tag contract is preserved
Moving the tag libraries to the Spring implementation SHALL preserve the external contract of the existing `bean-taglib`, `html-taglib`, `logic-taglib`, `nested-taglib` specs: the same TLDs, URIs, tag names and attributes, and the same HTML output; existing JSPs require no changes.

#### Scenario: Tag-library acceptance scenarios pass
- **WHEN** the acceptance scenarios of the `bean-taglib`, `html-taglib`, `logic-taglib`, and `nested-taglib` specs are run (including the ITs of the `blank` and `examples` apps)
- **THEN** all scenarios pass with unchanged expectations
