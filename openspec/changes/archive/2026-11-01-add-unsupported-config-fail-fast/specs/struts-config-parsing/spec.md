# Spec Delta

## ADDED Requirements

### Requirement: Startup failure on an unsupported processor class
At startup, each module's controller configuration SHALL be validated against the processor classes the engine supports. A `processorClass` other than a supported processor class SHALL fail application startup with an error naming the property, the offending value, and the module prefix. The supported processor classes are the classic default request processor and the Tiles request processor, whose forwarding behavior the engine implements natively.

#### Scenario: Custom processor class fails startup
- **WHEN** a module's `struts-config.xml` declares `<controller processorClass="com.example.SecureRequestProcessor"/>`
- **THEN** startup fails with an error naming `processorClass`, the value `com.example.SecureRequestProcessor`, and the module prefix

#### Scenario: Supported processor classes start
- **WHEN** the default module declares `processorClass="org.apache.struts.action.RequestProcessor"` and a module declares `processorClass="org.apache.struts.tiles.TilesRequestProcessor"`
- **THEN** both modules start without errors

#### Scenario: Empty and absent processor class start
- **WHEN** a module declares `<controller/>` without `processorClass`, and another declares `processorClass=""`
- **THEN** the classic default processor class applies and both modules start without errors

### Requirement: Startup failure on commons-chain controller settings
A controller configuration whose `catalog` or `command` attribute differs from the classic default value SHALL fail application startup with an error naming the property, the offending value, and the module prefix, because commons-chain configuration has no meaning on the engine and the chain logic it selects would be silently skipped.

#### Scenario: Custom chain command fails startup
- **WHEN** a module's `struts-config.xml` declares `<controller command="custom-chain"/>`
- **THEN** startup fails with an error naming `command`, the value `custom-chain`, and the module prefix

#### Scenario: Default chain values start
- **WHEN** a module declares `<controller catalog="struts" command="servlet-standard"/>`
- **THEN** the module starts without errors

### Requirement: Startup warning for the controller buffer size
A controller configuration with a `bufferSize` other than the classic default SHALL NOT fail startup; the engine SHALL log a startup warning naming the property, the offending value, and the module prefix, because the buffer size is a tuning knob with no implemented effect rather than a behavior difference.

#### Scenario: Non-default buffer size warns and starts
- **WHEN** a module declares `<controller bufferSize="8192"/>`
- **THEN** the application starts and a warning naming `bufferSize`, the value `8192`, and the module prefix is logged

#### Scenario: Default buffer size starts silently
- **WHEN** a module declares `<controller bufferSize="4096"/>` or omits the attribute
- **THEN** the module starts without a buffer-size warning
