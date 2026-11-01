# struts-config-parsing Specification

## Purpose

Reads and merges the configuration from `struts-config.xml` — the engine's primary configuration source: action mappings, forms, forwards, exceptions, message-resources, and plugins are registered at application startup.

## Requirements

### Requirement: Registration of configuration elements
At startup, an application with a valid `struts-config.xml` SHALL register all declared elements: action mappings (with their local forwards and exceptions), forms, global forwards, global exceptions, message-resources, and plugins.

#### Scenario: Startup with the blank application configuration
- **WHEN** the application starts with a `struts-config.xml` containing the action `path="/Welcome"` with the attribute `forward="/pages/Welcome.jsp"`, the global forward `name="welcome"` with `path="/Welcome.do"`, and `<message-resources parameter="MessageResources"/>`
- **THEN** the `/Welcome` mapping, the `welcome` global forward, and the message-resources with parameter `MessageResources` are registered and available to request processing

#### Scenario: Form and plugin from the configuration
- **WHEN** `struts-config.xml` declares a `<form-bean>` and a `<plug-in className="...">`
- **THEN** the form is registered by name, and the plugin is included in the configuration's plugin list

### Requirement: Merging the list of configuration files
The value of the `config` init-param SHALL be interpreted as a list of configuration file paths (comma-separated) for the default module; all files in the list SHALL be read and merged into the default module's configuration. Each init-param of the form `config/<prefix>` (for example `config/exercise`) SHALL specify the list of files for a separate module with the prefix `/<prefix>` (for example `/exercise`).

#### Scenario: Multiple files in config
- **WHEN** the init-param `config` equals `/WEB-INF/struts-config.xml,/WEB-INF/struts-config-extra.xml` and both files exist
- **THEN** elements of both files are registered in the default module's configuration

#### Scenario: Module via the config/exercise init-param
- **WHEN** web.xml declares the init-param `config/exercise` with the value `/WEB-INF/exercise/struts-config.xml`
- **THEN** the file is parsed into the configuration of the module with prefix `/exercise`, and its elements are registered there independently of the default module

### Requirement: Startup error on invalid configuration
If a file from the `config` init-param is missing or cannot be parsed, application startup SHALL fail with an error containing that file's name.

#### Scenario: Missing file
- **WHEN** the init-param `config` points to a file that does not exist in the application
- **THEN** startup fails, and the error message names the path of the missing file

#### Scenario: Invalid XML
- **WHEN** a configuration file contains syntactically invalid XML or does not conform to the DTD
- **THEN** startup fails with the file name in the error message

### Requirement: Registration of configuration modules
All declared modules SHALL be parsed at startup and registered under their prefixes; an unregistered prefix in a request falls back to the default module. A module file parsing error SHALL lead to a startup error naming the file.

#### Scenario: Multiple modules
- **WHEN** web.xml declares `config/exercise` and `config/dispatch` with existing files
- **THEN** the modules `/exercise` and `/dispatch` are registered alongside the default module

#### Scenario: Broken module file
- **WHEN** the init-param `config/dispatch` points to a missing file
- **THEN** startup fails with an error naming that file

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
