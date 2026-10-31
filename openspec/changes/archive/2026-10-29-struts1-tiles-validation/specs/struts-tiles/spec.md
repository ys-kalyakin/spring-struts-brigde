# Spec Delta: struts-tiles

## Purpose

A Tiles 1.x-compatible templating system: tile definitions in tiles-defs.xml, wiring via a plugin through struts-config.xml, tile controllers, forwards to definitions, and the `struts-tiles` tag library — with the URI, TLD, and behavior of the weblegacy reference.

## ADDED Requirements

### Requirement: Wiring Tiles as a plugin
`TilesPlugin` SHALL be wired declaratively by a `<plug-in>` element of struts-config.xml with the init-parameters `definitions-config` (a comma-separated list of definition files), `definitions-parser-validate`, `definitions-factory-class`, `moduleAware`. The definitions factory SHALL be initialized at startup and placed in the context under the classic attribute; re-initialization for a module SHALL be a startup error; the plugin's `destroy` SHALL release the factory.

#### Scenario: Declarative wiring
- **WHEN** struts-config.xml declares `TilesPlugin` with `definitions-config=/WEB-INF/tiles-defs.xml`
- **THEN** the definitions factory is initialized at startup, and the definitions are available to tags and forwards without additional code

#### Scenario: Module-aware factories
- **WHEN** the plugin is declared with `moduleAware=true` in two modules with different tiles-defs
- **THEN** each module uses its own definitions factory, and definition names in the modules do not conflict

#### Scenario: A missing definitions file
- **WHEN** `definitions-config` points to a non-existent file
- **THEN** the application startup fails with the file name

### Requirement: Parsing tiles-defs.xml
Definition files SHALL be parsed by the reference syntax: `definition` (name, path, extends, role) with `put` elements (name, value, type, direct) and `putList`/`add`, definition inheritance via `extends` with attribute overriding, DTDs 1.1/1.3/1.4 with local copies; the default factory SHALL support localized variants of definition files (`*_xx_YY.xml`/`*_xx.xml` selected by locale, the base file as fallback).

#### Scenario: Definition inheritance
- **WHEN** tiles-defs.xml declares a definition `.base` with a path and put attributes, and a definition `.page` with `extends=".base"` overriding one attribute
- **THEN** rendering `.page` uses the path and all attributes of `.base` with the overridden value

#### Scenario: putList and add
- **WHEN** a definition contains a `putList` with `add` elements
- **THEN** the attribute is available as a list of values (for example, a list of menu items renderable by iteration)

#### Scenario: Localized definitions
- **WHEN** a variant with a locale suffix exists next to the base tiles-defs.xml, and a request arrives with that locale
- **THEN** the definitions are resolved from the localized file; if there is no localized variant — from the base one

### Requirement: Rendering a definition and nested tiles
Rendering a definition SHALL build the tile attribute context (`ComponentContext`) for the current request, transition to the definition's path template, and let the template read the attributes with the library's tags; nested tiles SHALL see the attributes of their own context, not of the parent page.

#### Scenario: A page on a layout
- **WHEN** a forward leads to a definition with a layout template and header/footer/body attributes
- **THEN** the client receives the page assembled from the layout template: at the `get` tag positions the corresponding attributes are output

#### Scenario: Inserting a nested tile
- **WHEN** the body attribute is itself a definition (or a template with its own put attributes)
- **THEN** the nested tile is rendered inside the parent one, its attributes are not mixed with the parent's

#### Scenario: An attribute in scope
- **WHEN** the template uses `importAttribute` or `useAttribute` with an attribute name
- **THEN** the value is available to the page's subsequent tags (for example, `logic:iterate` over the menu list)

#### Scenario: The library tags work together
- **WHEN** a page uses `definition`+`put` pairs (building a definition in the JSP) and `putList`/`add` (filling lists)
- **THEN** the built definitions and attribute lists are rendered by the `insert`/`get` tags just like those declared in tiles-defs

### Requirement: Tile controllers
A definition SHALL support a controller: a URL controller (including a resource before rendering) and a class controller (an implementation of the classic `Controller` interface, invoked with the tile context before rendering); the classic `TilesAction` SHALL work as a definition controller. The controller SHALL run before the template render and SHALL be able to add attributes to the tile context.

#### Scenario: A class controller adds an attribute
- **WHEN** a definition declares `controllerClass` with a `Controller` implementation that puts a value into the tile context
- **THEN** the definition's template sees the added value among the attributes

#### Scenario: TilesAction as a controller
- **WHEN** a definition declares a class extending `TilesAction` as the controller
- **THEN** its execute is invoked before the render, and after it the template is rendered without an action forward

### Requirement: A forward to a tiles-definition
A forward (from an action, a mapping, or a global-forward) with a path matching a definition name SHALL render the definition instead of looking up a JSP: the attribute context is built, the definition's controller is invoked, then its path is rendered. A path that is not a definition SHALL be processed as a regular forward.

#### Scenario: An action returns a forward to a definition
- **WHEN** the action's execute returns a forward with a path that is a definition name declared in tiles-defs
- **THEN** the client receives the page assembled from that definition

#### Scenario: A regular forward is not broken
- **WHEN** a forward leads to a JSP path that matches no definition
- **THEN** the render proceeds as before (a dispatch to the JSP)

### Requirement: The struts-tiles tag library
The library's TLD SHALL be identical to the weblegacy one (`struts-tiles.tld`, 11 tags): the same URI, the same tag names and attributes; classic access through `TilesUtil` is preserved, and the shared logic (definition resolution, tile context) runs in Spring components overridable by the user.

#### Scenario: Wiring and JSP compilation
- **WHEN** a JSP declares the library by the URI `http://struts.apache.org/tags-tiles` and uses `tiles:insert` with nested `tiles:put`
- **THEN** the JSP compiles and the tile is rendered without additional declarations in web.xml

#### Scenario: Overriding the definition-resolution service
- **WHEN** the application overrides the definition-resolution Spring component with its own implementation
- **THEN** tags and forwards resolve definitions by the user's implementation
