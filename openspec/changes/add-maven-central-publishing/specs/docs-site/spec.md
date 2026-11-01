# Spec Delta

## MODIFIED Requirements

### Requirement: Getting started page
The getting started page SHALL describe adding the `io.github.ys-kalyakin.springstruts` artifacts (starter, tag libraries, Tiles if needed), minimal `spring.struts1.config` setup, the `@StrutsAction`/`@StrutsForm` annotations, and SHALL reference the migration guide and the demo application.

#### Scenario: Adding the library per the instructions
- **WHEN** a visitor follows the steps of the getting started page
- **THEN** they get a working application on the starter with minimal configuration

#### Scenario: Copying the dependency snippet
- **WHEN** a visitor copies the dependency coordinates from the getting started page into their build
- **THEN** the group id matches the artifacts published on Maven Central (`io.github.ys-kalyakin.springstruts`)

### Requirement: Modules overview
The modules overview SHALL describe the artifacts of the `io.github.ys-kalyakin.springstruts` group (`spring-struts1-core`, `spring-struts1-taglib`, `spring-struts1-tiles`, `spring-struts1-starter`) and the purpose of each of them.

#### Scenario: Choosing artifacts
- **WHEN** a visitor decides which artifacts to add
- **THEN** the overview describes the purpose and role of each artifact

#### Scenario: Group id on the modules page
- **WHEN** a visitor reads the modules overview
- **THEN** the group id shown matches the artifacts published on Maven Central
