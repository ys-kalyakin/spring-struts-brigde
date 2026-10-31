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
