# Spec Delta

## MODIFIED Requirements

### Requirement: Merging the configuration file list
The value of the `config` init-param SHALL be interpreted as a list of paths to configuration files (separator — comma) of the default module; all files of the list SHALL be read and merged into the default module configuration. Each init-param of the form `config/<prefix>` (e.g. `config/exercise`) SHALL define the list of files of a separate module with the prefix `/<prefix>` (e.g. `/exercise`).

#### Scenario: Multiple files in config
- **WHEN** the init-param `config` equals `/WEB-INF/struts-config.xml,/WEB-INF/struts-config-extra.xml` and both files exist
- **THEN** the elements of both files are registered in the default module configuration

#### Scenario: Module by the config/exercise init-param
- **WHEN** web.xml declares the init-param `config/exercise` with the value `/WEB-INF/exercise/struts-config.xml`
- **THEN** the file is parsed into the configuration of the module with the prefix `/exercise`, and its elements are registered there independently of the default module

## ADDED Requirements

### Requirement: Registration of configuration modules
All declared modules SHALL be parsed at startup and registered by their prefixes; an unregistered prefix in a request yields the default module. A module file parsing error SHALL lead to a startup error naming the file.

#### Scenario: Multiple modules
- **WHEN** web.xml declares `config/exercise` and `config/dispatch` with existing files
- **THEN** the modules `/exercise` and `/dispatch` are registered alongside the default module

#### Scenario: Broken module file
- **WHEN** the init-param `config/dispatch` points to a missing file
- **THEN** startup ends with an error naming that file
