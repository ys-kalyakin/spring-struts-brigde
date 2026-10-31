# Spec Delta

## Purpose

Reads and merges the configuration from `struts-config.xml` — the engine's main source of configuration: action mappings, forms, forwards, exceptions, message-resources and plugins are registered at application startup.

## ADDED Requirements

### Requirement: Registration of configuration elements
At startup, an application with a valid `struts-config.xml` SHALL register all declared elements: action mappings (with their local forwards and exceptions), forms, global forwards, global exceptions, message-resources and plugins.

#### Scenario: Startup with the blank application configuration
- **WHEN** the application starts with a `struts-config.xml` containing an action `path="/Welcome"` with the attribute `forward="/pages/Welcome.jsp"`, a global forward `name="welcome"` with `path="/Welcome.do"` and `<message-resources parameter="MessageResources"/>`
- **THEN** the `/Welcome` mapping, the `welcome` global forward and the message-resources with parameter `MessageResources` are registered and available to request processing

#### Scenario: Form and plugin from the configuration
- **WHEN** `struts-config.xml` declares a `<form-bean>` and a `<plug-in className="...">`
- **THEN** the form is registered by name, and the plugin is included in the configuration's plugin list

### Requirement: Merging the configuration file list
The init-param `config` value SHALL be interpreted as a list of configuration file paths (separator — comma); all files in the list SHALL be read and merged into the default module configuration.

#### Scenario: Multiple files in config
- **WHEN** init-param `config` equals `/WEB-INF/struts-config.xml,/WEB-INF/struts-config-extra.xml` and both files exist
- **THEN** the elements of both files are registered in the default module configuration

### Requirement: Startup error on invalid configuration
If a file from init-param `config` is missing or cannot be parsed, application startup SHALL fail with an error containing that file's name.

#### Scenario: Missing file
- **WHEN** init-param `config` points to a file that does not exist in the application
- **THEN** startup fails with an error, and the error message names the path of the missing file

#### Scenario: Invalid XML
- **WHEN** a configuration file contains syntactically invalid XML or does not conform to the DTD
- **THEN** startup fails with an error naming the file in the message
