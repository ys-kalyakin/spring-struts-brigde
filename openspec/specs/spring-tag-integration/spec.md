# spring-tag-integration Specification

## Purpose

Makes the tag libraries part of the Spring JSP stack: tag handlers get access to the Spring context and its beans, shared tag logic is executed by Spring components, and Struts tags work on the same page as Spring tags.

## Requirements

### Requirement: Tag access to the Spring context
A custom tag built on the engine's tag infrastructure SHALL have access to the Spring application context and its beans during page processing.

#### Scenario: Custom tag uses a Spring bean
- **WHEN** a custom tag built on the engine retrieves a service bean from the context and outputs its result into the tag body
- **THEN** the page renders the data obtained from the Spring bean

### Requirement: Tag logic executed by Spring components
The shared logic of the tag libraries — message resolution, URL building, form lookup in scope, rendering of errors and messages — SHALL be executed by Spring components, with tag handlers delegating to them; overriding such a component by the user changes the behavior of the corresponding tags.

#### Scenario: Overriding a component changes tag output
- **WHEN** the application overrides the Spring component for tag message resolution with its own implementation
- **THEN** the output of the message and error tags is built by the user's implementation

#### Scenario: Dependency injection into a tag
- **WHEN** a custom tag built on the engine declares a dependency on a context bean
- **THEN** the dependency is available by the time the tag body is processed, without manual lookup by string keys

### Requirement: message-resources bundles as MessageSource
Bundles declared in `<message-resources>` SHALL be available through the Spring `MessageSource`: the default bundle as the context's primary `MessageSource`, named ones under their keys; Spring tags (for example `spring:message`) render messages from them.

#### Scenario: spring:message reads the Struts bundle
- **WHEN** the configuration declares `<message-resources parameter="MessageResources"/>`, and the JSP uses `spring:message` with a code that is a key from that bundle
- **THEN** the page displays the same message that the classic `bean:message` tag returns

#### Scenario: Named bundle available by key
- **WHEN** the configuration declares a bundle with the key `alternate`, and the page accesses it through the `MessageSource` under that key
- **THEN** the message is obtained from the `alternate` bundle

### Requirement: Coexistence of Struts and Spring tags in JSP
A combination of Struts tags, Spring tags, and EL access to context beans SHALL work correctly on a single JSP page: each construct renders the expected result without additional configuration.

#### Scenario: Mixed page
- **WHEN** the JSP contains `html:form` with `html:text`, `spring:message`, and an EL expression referencing a Spring bean
- **THEN** all three constructs are rendered: the form with the property value, the message from the bundle, and the bean's data

### Requirement: Preserving the classic tag contract
Moving the tag libraries to a Spring implementation SHALL preserve the external contract of the existing `bean-taglib`, `html-taglib`, `logic-taglib`, `nested-taglib` specifications: the same TLDs, URIs, tag names and attributes, and the same HTML output; existing JSPs require no changes.

#### Scenario: Tag library acceptance scenarios pass
- **WHEN** the acceptance scenarios of the `bean-taglib`, `html-taglib`, `logic-taglib`, and `nested-taglib` specifications are run (including the ITs of the `blank` and `examples` applications)
- **THEN** all scenarios pass without changes to expectations
