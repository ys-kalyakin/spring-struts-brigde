# Spec Delta

## Purpose

Dynamic forms (`DynaActionForm`): a form-bean without its own Java class whose properties are declared by `<form-property>` elements in the configuration — creation, initial values, populate, and dynamic access.

## ADDED Requirements

### Requirement: Creating a DynaActionForm from configuration
A form-bean with the type `DynaActionForm` (or a subclass) SHALL create a dynamic form instance with a set of properties from the declared `<form-property>` elements (name, type, writability); the properties SHALL be accessible to the action via dynamic access by name (`get`/`set` by name and index).

#### Scenario: Properties from form-property
- **WHEN** the configuration declares a form-bean of type `DynaActionForm` with `form-property name="testString" type="java.lang.String"` and the mapping uses this form
- **THEN** by the time the action is invoked the form contains the property `testString`, accessible by name

#### Scenario: A form per request
- **WHEN** two requests are processed by a mapping with a dynamic form
- **THEN** each request gets its own instance with the original (not modified by the first request) values

### Requirement: form-property initial values
Dynamic form properties SHALL receive initial values from the `initial` attribute of the `<form-property>` element (for numeric types — the given value, for a missing one — the zero/empty value of the type); initial values are applied when each instance is created.

#### Scenario: Initial value of a string property
- **WHEN** `<form-property name="status" type="java.lang.String" initial="new"/>` is declared in a dynamic form
- **THEN** the created instance contains `status = "new"`

### Requirement: Populating a dynamic form
Request parameters SHALL populate dynamic form properties per commons-beanutils semantics: strings, numbers, indexed (`prop[0]`) and mapped (`prop(key)`) properties; validation by the mapping flag SHALL work with a dynamic form the same as with a static one.

#### Scenario: Parameters populate dynamic properties
- **WHEN** the request contains the parameters `testString=hello` and `language=ru`, and the mapping uses a dynamic form with these properties
- **THEN** the form properties contain the parameter values by the time the action is invoked
