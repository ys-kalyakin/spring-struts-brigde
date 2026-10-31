# dynamic-form-beans Specification

## Purpose

Dynamic forms (`DynaActionForm`): a form-bean without its own Java class whose properties are declared by `<form-property>` elements in the configuration — creation, initial values, populate and dynamic access.

## Requirements

### Requirement: Creating DynaActionForm from configuration
A form-bean with type `DynaActionForm` (or a subclass) SHALL create a dynamic form instance with the set of properties from the declared `<form-property>` elements (name, type, writability); the properties SHALL be accessible to the action via dynamic access by name (`get`/`set` by name and index).

#### Scenario: Properties from form-property
- **WHEN** the configuration declares a form-bean of type `DynaActionForm` with `form-property name="testString" type="java.lang.String"` and the mapping uses this form
- **THEN** by the time the action is invoked, the form contains property `testString`, accessible by name

#### Scenario: Form per request
- **WHEN** two requests are processed by a mapping with a dynamic form
- **THEN** each request receives its own instance with the initial (not modified by the first request) values

### Requirement: Initial values of form-property
Properties of a dynamic form SHALL receive initial values from the `initial` attribute of the `<form-property>` element (for numeric types — the given value, if absent — the zero/empty value of the type); initial values are applied at the creation of each instance.

#### Scenario: Initial of a string property
- **WHEN** `<form-property name="status" type="java.lang.String" initial="new"/>` is declared in a dynamic form
- **THEN** the created instance contains `status = "new"`

### Requirement: Populate of a dynamic form
Request parameters SHALL populate the properties of a dynamic form with commons-beanutils semantics, reproduced by the engine on Spring `ConversionService` (conversion rules are defined in the `form-populate` spec): strings, numbers, indexed (`prop[0]`) and mapped (`prop(key)`) properties; validation by the mapping flag SHALL work with a dynamic form the same as with a static one.

#### Scenario: Parameters populate dynamic properties
- **WHEN** the request contains parameters `testString=hello` and `language=ru`, and the mapping uses a dynamic form with these properties
- **THEN** the form properties contain the parameter values by the time the action is invoked
