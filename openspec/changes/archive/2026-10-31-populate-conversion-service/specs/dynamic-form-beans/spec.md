# Spec Delta: dynamic-form-beans

## MODIFIED Requirements

### Requirement: Dynamic form populate
Request parameters SHALL fill the properties of a dynamic form with commons-beanutils semantics reproduced by the engine on Spring `ConversionService` (conversion rules are defined in the `form-populate` spec): strings, numbers, indexed (`prop[0]`) and mapped (`prop(key)`) properties; validation driven by the mapping flag SHALL work with a dynamic form the same way as with a static one.

#### Scenario: Parameters fill dynamic properties
- **WHEN** a request contains the parameters `testString=hello` and `language=ru`, and the mapping uses a dynamic form with these properties
- **THEN** the form properties contain the parameter values by the time the action is invoked
