# Spec Delta: request-processing

## MODIFIED Requirements

### Requirement: Form populate from request parameters
If the mapping declares a form, its properties SHALL be filled from the request parameters with commons-beanutils semantics reproduced by the engine on Spring `ConversionService` (conversion rules are defined in the `form-populate` spec); a mapping without a form SHALL be processed without populate. A multipart request SHALL fill the form from the parsed multipart elements (text fields — as parameters, file fields — into properties of the corresponding types) before the action is invoked; the parsing behavior and the handling of limit overruns SHALL conform to the `multipart-upload` spec.

#### Scenario: Parameters fill form properties
- **WHEN** a POST arrives with parameters whose names match the properties of the form declared in the mapping
- **THEN** by the time the action is invoked, the form properties contain the parameter values

#### Scenario: Mapping without a form
- **WHEN** the mapping does not declare a `name` attribute with a form
- **THEN** populate is not performed and the action is invoked immediately

#### Scenario: Multipart request fills a file property
- **WHEN** a POST `multipart/form-data` arrives with a file field on a mapping with a form containing a `FormFile` property
- **THEN** by the time the action is invoked, the property contains the parsed file, and the form's text fields are filled from the multipart elements
