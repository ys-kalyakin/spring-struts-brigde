# Spec Delta

## ADDED Requirements

### Requirement: Migration guide documents functional limitations
The migration guide SHALL contain a functional limitations section that lists every known piece of controller configuration the engine does not implement (custom request processors, commons-chain settings, the controller buffer size), states for each whether migration fails with an error or proceeds with a warning, and gives the remediation path: re-expressing a custom request processor's logic as Spring MVC interceptors, filters, or engine extension points.

#### Scenario: Porting a custom request processor per the guide
- **WHEN** a developer's application fails startup with the unsupported processor class error
- **THEN** the guide's functional limitations section explains what the error means and a concrete remediation path for the processor's logic

#### Scenario: Limitations are discoverable before migration
- **WHEN** the guide is read before starting a migration
- **THEN** every known unimplemented controller configuration is listed with its treatment (startup error or startup warning)
