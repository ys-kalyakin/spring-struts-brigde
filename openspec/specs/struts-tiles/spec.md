# struts-tiles Specification

## Purpose

A Tiles 1.x-compatible templating system: tile definitions in tiles-defs.xml, wiring via a plugin through struts-config.xml, tile controllers, transitions via forwards to definitions, and the `struts-tiles` tag library — with the URI, TLD, and behavior of the weblegacy reference.

## Requirements

### Requirement: Wiring Tiles as a plugin
`TilesPlugin` SHALL be wired declaratively with a `<plug-in>` element of struts-config.xml with the init parameters `definitions-config` (comma-separated list of definition files), `definitions-parser-validate`, `definitions-factory-class`, `moduleAware`. The definitions factory SHALL be initialized at startup and placed in the context under the classic attribute; re-initialization for a module SHALL be a startup error; the plugin's `destroy` SHALL release the factory.

#### Scenario: Declarative wiring
- **WHEN** struts-config.xml declares `TilesPlugin` with `definitions-config=/WEB-INF/tiles-defs.xml`
- **THEN** the definitions factory is initialized at startup, and definitions are available to tags and forwards without additional code

#### Scenario: Module-aware factories
- **WHEN** the plugin is declared with `moduleAware=true` in two modules with different tiles defs
- **THEN** each module uses its own definitions factory, and definition names in the modules do not conflict

#### Scenario: Missing definitions file
- **WHEN** `definitions-config` points to a non-existent file
- **THEN** application startup fails with an error naming the file

### Requirement: Parsing tiles-defs.xml
Definition files SHALL be parsed by the reference syntax: `definition` (name, path, extends, role) with `put` elements (name, value, type, direct) and `putList`/`add`, definition inheritance via `extends` with attribute overriding, DTD 1.1/1.3/1.4 with local copies; the default factory SHALL support localized variants of definition files (`*_xx_YY.xml`/`*_xx.xml` are selected by locale, with the base file as fallback).

#### Scenario: Definition inheritance
- **WHEN** tiles-defs.xml declares a definition `.base` with a path and put attributes, and a definition `.page` with `extends=".base"` overriding one attribute
- **THEN** rendering `.page` uses the path and all attributes of `.base` with the overridden value

#### Scenario: putList and add
- **WHEN** a definition contains a `putList` with `add` elements
- **THEN** the attribute is available as a list of values (for example, a list of menu items rendered by an iteration)

#### Scenario: Localized definitions
- **WHEN** a variant with a locale suffix exists next to the base tiles-defs.xml, and the request arrives with that locale
- **THEN** definitions are resolved from the localized file, falling back to the base file when no localized variant exists

### Requirement: Rendering a definition and nested tiles
Rendering a definition SHALL build the tile attribute context (`ComponentContext`) for the current request, transition to the definition's path template, and let the template read the attributes with the library tags; nested tiles SHALL see the attributes of their own context, not of the parent page.

#### Scenario: Page on a layout
- **WHEN** a forward leads to a definition with a layout template and header/footer/body attributes
- **THEN** the client receives the page assembled from the layout template: the corresponding attributes are output at the `get` tag locations

#### Scenario: Inserting a nested tile
- **WHEN** the body attribute is itself a definition (or a template with its own put attributes)
- **THEN** the nested tile is rendered inside the parent, and its attributes are not mixed with the parent's

#### Scenario: Attribute in scope
- **WHEN** the template uses `importAttribute` or `useAttribute` with an attribute name
- **THEN** the value is available to subsequent tags of the page (for example, `logic:iterate` over the menu list)

#### Scenario: Library tags work in combination
- **WHEN** a page uses the `definition`+`put` pairs (building a definition in JSP) and `putList`/`add` (filling lists)
- **THEN** the built definitions and attribute lists are rendered by the `insert`/`get` tags just like those declared in tiles-defs

### Requirement: Tile controllers
A definition SHALL support a controller: a URL controller (including a resource before rendering) and a class controller (an implementation of the classic `Controller` interface, invoked with the tile context before rendering); the classic `TilesAction` SHALL work as a definition controller. The controller SHALL run before the template is rendered and SHALL be able to add attributes to the tile context.

#### Scenario: Class controller adds an attribute
- **WHEN** a definition declares a `controllerClass` with a `Controller` implementation that puts a value into the tile context
- **THEN** the definition's template sees the added value among the attributes

#### Scenario: TilesAction as a controller
- **WHEN** a definition declares as its controller a class extending `TilesAction`
- **THEN** its execute is invoked before rendering, and after it the template is rendered without an action forward

### Requirement: Forward to a tiles-definition
A forward (from an action, mapping, or global-forward) whose path matches a definition name SHALL render the definition instead of looking up a JSP: the attribute context is built, the definition's controller is invoked, then its path is rendered. A path that is not a definition SHALL be handled as a regular forward.

#### Scenario: Action returns a forward to a definition
- **WHEN** the action's execute returns a forward whose path is the name of a definition declared in tiles-defs
- **THEN** the client receives the page assembled from that definition

#### Scenario: Regular forward unchanged
- **WHEN** a forward leads to a JSP path that does not match any definition
- **THEN** rendering proceeds as before (dispatch to the JSP)

### Requirement: struts-tiles tag library
The library TLD SHALL be identical to the weblegacy reference (`struts-tiles.tld`, 11 tags): the same URI, the same tag names and attributes; the classic access via `TilesUtil` is preserved, while the shared logic (definition resolution, tile context) is executed by Spring components with the option of user override.

#### Scenario: Connecting and compiling a JSP
- **WHEN** a JSP declares the library by URI `http://struts.apache.org/tags-tiles` and uses `tiles:insert` with nested `tiles:put`
- **THEN** the JSP compiles and the tile is rendered without additional declarations in web.xml

#### Scenario: Overriding the definition resolution service
- **WHEN** the application overrides the Spring component for definition resolution with its own implementation
- **THEN** tags and forwards resolve definitions by the user's implementation
