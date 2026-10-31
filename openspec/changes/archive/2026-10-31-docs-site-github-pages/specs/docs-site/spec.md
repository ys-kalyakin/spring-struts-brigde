# Spec Delta

## Purpose

A public English-language reference for the library: a single browsable place where a user can learn what spring-struts1 is, how to add the starter, how to migrate an existing Struts 1 application, and what the tag libraries and starter configuration support.

## ADDED Requirements

### Requirement: Documentation language is English
All site pages SHALL be written in English. No page SHALL contain untranslated Russian text.

#### Scenario: Viewing any page
- **WHEN** a visitor opens any page of the site
- **THEN** all text on the page is in English

### Requirement: Page set and navigation
The site SHALL contain six pages: home (library overview), getting started, migration guide, tag library reference, starter configuration reference, and modules overview. Each page SHALL be reachable from the site navigation.

#### Scenario: Navigating the site
- **WHEN** a visitor opens the home page
- **THEN** the site navigation lists all sections, and every link leads to an existing page

### Requirement: Home page
The home page SHALL describe the purpose of the library (recreating Struts 1 on top of modern Spring), its key principles (preserving the `org.apache.struts.*` API, `struts-config.xml` as the source of configuration, classic tag libraries) and the stack compatibility table, and SHALL also contain links to getting started and the migration guide.

#### Scenario: First acquaintance with the library
- **WHEN** a visitor opens the home page
- **THEN** they see the library description, principles, compatibility table, and links to getting started and migration

### Requirement: Getting started page
The getting started page SHALL describe adding the `org.springstruts` artifacts (starter, tag libraries, Tiles if needed), minimal `spring.struts1.config` configuration, the `@StrutsAction`/`@StrutsForm` annotations, and SHALL reference the migration guide and the demo application.

#### Scenario: Adding the library by the instructions
- **WHEN** a visitor follows the steps of the getting started page
- **THEN** they get a working application on the starter with minimal configuration

### Requirement: Migration guide in English
The migration guide SHALL exist as a single copy in the repository (`docs/MIGRATION.md`) and SHALL be translated into English while preserving the coverage of the current Russian version (dependency swap, starter configuration, annotations, verification against the demo). The link from README SHALL continue to point to that file, and the site's migration page SHALL publish its content.

#### Scenario: Guide completeness on the site
- **WHEN** a visitor opens the migration page on the site
- **THEN** they see a step-by-step guide equivalent in coverage to the current Russian `docs/MIGRATION.md`

#### Scenario: Link from README
- **WHEN** a README reader follows the link to the migration guide
- **THEN** the link leads to an existing file in the repository, in English

### Requirement: Starter configuration reference
The configuration reference SHALL describe the `spring.struts1.*` properties with their default values: `enabled`, `config` (default `/WEB-INF/struts-config.xml`), `validating`, `servlet-name`, `mapping.url-pattern`, as well as module configurations `spring.struts1.config.<prefix>`.

#### Scenario: Looking up a default value
- **WHEN** a visitor looks on the configuration page for the default value of the `spring.struts1.config` property
- **THEN** they find it along with a description of the property's purpose

### Requirement: Tag library reference
The tag library reference SHALL list the bean, html, logic, nested, and tiles libraries with their classic URIs (`http://struts.apache.org/tags-bean` and the like), state which artifact provides each library, and mention the commons-validator included in the artifact.

#### Scenario: Adding a tag library in a JSP
- **WHEN** a visitor looks for the URI to add a library in a JSP
- **THEN** they find the classic URI, working without changes

### Requirement: Modules overview
The modules overview SHALL describe the `org.springstruts` artifacts (`spring-struts1-core`, `spring-struts1-taglib`, `spring-struts1-tiles`, `spring-struts1-starter`) and the purpose of each.

#### Scenario: Choosing artifacts
- **WHEN** a visitor decides which artifacts to add
- **THEN** the overview describes the purpose and role of each artifact
