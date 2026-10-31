# Spec Delta

## MODIFIED Requirements

### Requirement: Populate Value Sources
Values for filling the form SHALL be taken from the request parameters (each name value — as a string array); for a multipart request — from the parsed multipart items (text items as parameters, file items into properties of type `FormFile`); multipart parsing SHALL conform to the `multipart-upload` spec. Parameters whose names begin with `org.apache.struts.` SHALL be skipped. Names with `class` access SHALL be ignored (CVE-2014-0114): a parameter in whose property path `class` (in any case) appears as a property name — on its own, at the start of the path, nested, with an index (`class[0]`), or with a key (`class(key)`) — SHALL be skipped without error and without modifying the form, before any other processing of the name, including resolution of multipart file-field types. The key of a mapped property (for example, `map(class)` or `foo('class')`) is not a property name and does not count as `class` access.

#### Scenario: Request parameters as value sources
- **WHEN** a POST request contains the parameters `name=Alice` and `age=30`, and the form has the properties `name` and `age`
- **THEN** both properties are populated with the corresponding values by the time the action is invoked

#### Scenario: Service parameters are skipped
- **WHEN** the request contains the parameter `org.apache.struts.action.CANCEL`
- **THEN** no attempt is made to populate a property with that name and the form is not modified

#### Scenario: Multipart file field populates a FormFile property
- **WHEN** a POST `multipart/form-data` contains a file field `theFile` and the form has a `FormFile` property
- **THEN** the property contains the parsed file, and the form's text fields are populated from the multipart items

#### Scenario: Bare class name is ignored
- **WHEN** the request contains the parameter `class=value` (or `CLASS=value`)
- **THEN** populate completes without error, the form is not modified, and the remaining parameters are populated

#### Scenario: Nested access through class is ignored
- **WHEN** the request contains the parameters `class.module.classLoader.DefaultAssertionStatus=1`, `a.class.b=1`, `class[0]=1`, `class(key)=1`
- **THEN** each such parameter is skipped without error and without modifying the form; the remaining parameters are populated

#### Scenario: Multipart file field with a class name is ignored
- **WHEN** a multipart request contains a field `class.x` and a text field `name=Alice`, and the form has a `name` property
- **THEN** populate completes without error, the `name` property is populated, and the `class.x` field is ignored before the property type resolution is attempted

### Requirement: Property Name Syntax
Parameter names SHALL be processed in beanutils syntax: a simple name designates a property, `prop[i]` — an indexed property (array or list), `prop(key)` — a mapped property (`Map`), `a.b` — a nested property, as well as their combinations. A name that does not correspond to any property of the form SHALL be skipped without error; an inaccessible nested property SHALL be skipped without error — both when the intermediate value is `null` and when an intermediate property is unknown or unreadable at any step of the nested path; populate SHALL continue processing the remaining parameters. A mapped property SHALL receive the raw parameter value (string array) without conversion.

#### Scenario: Indexed parameter
- **WHEN** the request contains the parameter `intIndexed[0]=5` and the form has an `int[]` array `intIndexed`
- **THEN** the first element of the array equals 5 and the remaining elements are unchanged

#### Scenario: Mapped parameter
- **WHEN** the request contains the parameter `value(color)=red` and the form has a `Map` property `value`
- **THEN** under the key `color` the Map holds the raw parameter value — a string array

#### Scenario: Nested parameter
- **WHEN** the request contains the parameter `nested.intProperty=7` and the form has a nested bean `nested` with the property `intProperty`
- **THEN** the `intProperty` property of the nested bean equals 7

#### Scenario: Unknown parameter name
- **WHEN** the request contains a parameter that does not correspond to any property of the form
- **THEN** populate completes without error and the form is not modified by that parameter

#### Scenario: Combination of indexed and mapped
- **WHEN** the request contains the parameter `nested.maps[0](color)=red`, the element `maps[0]` exists and is a `Map`
- **THEN** under the key `color` that Map holds the first element of the parameter as a string

#### Scenario: Unknown intermediate property of a nested path
- **WHEN** the request contains the parameters `nonExistent.deep.path=1` and `name=Alice`, and the form has a `name` property
- **THEN** populate completes without error, the `name` property is populated, and the `nonExistent.deep.path` parameter is skipped

#### Scenario: Unreadable intermediate property of a nested path
- **WHEN** the request contains the parameter `nested.missing.child=1` and the form has a nested bean `nested` that has no `missing` property
- **THEN** populate completes without error and the parameter is skipped
