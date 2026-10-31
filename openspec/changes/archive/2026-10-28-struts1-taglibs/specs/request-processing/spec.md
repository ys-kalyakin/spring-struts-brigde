# Spec Delta

## MODIFIED Requirements

### Requirement: Suffix-based action routing
A request to an action SHALL be routed by the path without the suffix: a request to `/x.do` is processed by the mapping with `path="/x"`. The suffix SHALL be configurable; when the configured suffix changes, routing is performed by it. The mapping SHALL be looked up in the module selected by the request path prefix; for a request without a module prefix — in the default module.

#### Scenario: Request with the .do suffix
- **WHEN** a request `GET /Welcome.do` arrives and the configuration registers a mapping `path="/Welcome"`
- **THEN** the request is processed by this mapping

#### Scenario: Configured suffix
- **WHEN** the suffix is configured as `.act` and a request `GET /Welcome.act` arrives
- **THEN** the request is processed by the mapping `path="/Welcome"`

#### Scenario: Request to a module
- **WHEN** a request `GET /exercise/bean-write.do` arrives and the module `/exercise` is registered with the mapping `path="/bean-write"`
- **THEN** the request is processed by the `/bean-write` mapping of the `/exercise` module

## ADDED Requirements

### Requirement: Module selection by request path
The module for processing a request SHALL be selected by the path prefix: of the registered modules, the one whose prefix matches the beginning of the path after the context is taken; if there is no match — the default module. The selected module SHALL be available to request processing.

#### Scenario: Module prefix
- **WHEN** the default module and the module `/exercise` are registered, and a request `GET /exercise/html-form.do` arrives
- **THEN** the request is processed in the `/exercise` module: mappings and forms are taken from its configuration

#### Scenario: Path without a module prefix
- **WHEN** the module `/exercise` is registered, and a request `GET /Welcome.do` arrives
- **THEN** the request is processed in the default module

### Requirement: Path-mapped actions
Alongside suffix mapping, path-mapped mapping SHALL be supported: with a servlet mapping of the form `/action/*`, a request to `/action/x` is processed by the mapping with `path="/x"` (the `/action` prefix is stripped).

#### Scenario: Request with prefix mapping
- **WHEN** the servlet mapping is configured as `/action/*` and a request `GET /action/Welcome` arrives
- **THEN** the request is processed by the mapping `path="/Welcome"`
