# form-populate Specification

## Purpose

Defines the semantics of populating Struts forms with request values: value sources, property name syntax, conversion rules and error handling — with behavior compatible with commons-beanutils and the weblegacy/struts1 1.5.x reference.

## Requirements

### Requirement: Populate value sources
Values for populating the form SHALL be taken from request parameters (all values of a name — as an array of strings); for a multipart request — from parsed multipart items (text ones as parameters, file ones into properties of `FormFile` types); multipart parsing SHALL conform to the `multipart-upload` spec. Parameters whose names start with `org.apache.struts.` SHALL be skipped. Names with access to `class` SHALL be ignored (CVE-2014-0114): a parameter whose property path contains `class` (in any case) as a property name — by itself, at the start of the path, nested, with an index (`class[0]`) or with a key (`class(key)`) — SHALL be skipped without error and without modifying the form, before any other processing of the name, including resolution of multipart file field types. A mapped property key (for example, `map(class)` or `foo('class')`) is not a property name and does not count as access to `class`.

#### Scenario: Request parameters as value sources
- **WHEN** a POST request contains parameters `name=Alice` and `age=30`, and the form has properties `name` and `age`
- **THEN** both properties are populated with the corresponding values by the time the action is invoked

#### Scenario: Service parameters are skipped
- **WHEN** the request contains parameter `org.apache.struts.action.CANCEL`
- **THEN** no attempt to populate a property with that name is made and the form is unchanged

#### Scenario: Multipart file field populates a FormFile property
- **WHEN** a POST `multipart/form-data` contains file field `theFile` and the form has a `FormFile` property
- **THEN** the property contains the parsed file, and the form's text fields are populated from the multipart items

#### Scenario: Bare class name is ignored
- **WHEN** the request contains parameter `class=value` (or `CLASS=value`)
- **THEN** populate completes without error, the form is unchanged, the remaining parameters are populated

#### Scenario: Nested access through class is ignored
- **WHEN** the request contains parameters `class.module.classLoader.DefaultAssertionStatus=1`, `a.class.b=1`, `class[0]=1`, `class(key)=1`
- **THEN** each such parameter is skipped without error and without modifying the form; the remaining parameters are populated

#### Scenario: Multipart file field with a class name is ignored
- **WHEN** a multipart request contains field `class.x` and text field `name=Alice`, and the form has property `name`
- **THEN** populate completes without error, property `name` is populated, field `class.x` is ignored before the attempt to resolve the property type

### Requirement: Property name syntax
Parameter names SHALL be processed in beanutils syntax: a simple name designates a property, `prop[i]` — an indexed property (array or list), `prop(key)` — a mapped property (`Map`), `a.b` — a nested property, as well as combinations thereof. A name that matches no form property SHALL be skipped without error; an inaccessible nested property SHALL be skipped without error — both when an intermediate value is `null` and when an intermediate property is unknown or unreadable at any step of the nested path; populate SHALL continue processing the remaining parameters. A mapped property SHALL receive the raw parameter value (array of strings) without conversion.

#### Scenario: Indexed parameter
- **WHEN** the request contains parameter `intIndexed[0]=5` and the form has an `int[]` array `intIndexed`
- **THEN** the first element of the array is 5, the other elements are unchanged

#### Scenario: Mapped parameter
- **WHEN** the request contains parameter `value(color)=red` and the form has a `Map` property `value`
- **THEN** under key `color` the Map holds the raw parameter value — an array of strings

#### Scenario: Nested parameter
- **WHEN** the request contains parameter `nested.intProperty=7` and the form has nested bean `nested` with property `intProperty`
- **THEN** property `intProperty` of the nested bean is 7

#### Scenario: Unknown parameter name
- **WHEN** the request contains a parameter that matches no form property
- **THEN** populate completes without error, the form is unchanged for that parameter

#### Scenario: Combination of indexed and mapped
- **WHEN** the request contains parameter `nested.maps[0](color)=red`, element `maps[0]` exists and is a `Map`
- **THEN** under key `color` of this Map holds the first element of the parameter as a string

#### Scenario: Unknown intermediate property of a nested path
- **WHEN** the request contains parameters `nonExistent.deep.path=1` and `name=Alice`, and the form has property `name`
- **THEN** populate completes without error, property `name` is populated, parameter `nonExistent.deep.path` is skipped

#### Scenario: Unreadable intermediate property of a nested path
- **WHEN** the request contains parameter `nested.missing.child=1`, and the form has nested bean `nested` which has no property `missing`
- **THEN** populate completes without error, the parameter is skipped

### Requirement: Scalar value conversion
A string value SHALL be converted to the declared property type with the semantics of the default commons-beanutils registry: primitives and numeric wrappers (the value is trimmed; invalid input, an empty string or overflow produce the zero value of the type without error), `Character` (the first character; an empty string yields a space without error), `BigDecimal`/`BigInteger` (invalid input or an empty string — a populate error), `String` (no transformation). Conversion SHALL be performed by the engine's conversion service (Spring `ConversionService`); the static global commons-beanutils registry does not participate in populate.

#### Scenario: String into a numeric property
- **WHEN** parameter `age=30` populates an `int`/`Integer` property `age`
- **THEN** the property value is the number 30

#### Scenario: Character from a string
- **WHEN** parameter `grade=A` populates a `char`/`Character` property `grade`
- **THEN** the property value is character `A`

#### Scenario: Invalid number produces the zero value
- **WHEN** parameter `age=abc` populates numeric property `age` that held value 11
- **THEN** there is no error and the property value is replaced with the zero value (0), as with an empty string and overflow

#### Scenario: BigDecimal with invalid input
- **WHEN** parameter `amount=abc` populates `BigDecimal` property `amount`
- **THEN** populate fails (see the populate errors requirement)

#### Scenario: Type without a converter
- **WHEN** a parameter with a string value populates a property of a type without a converter in the default registry (`java.util.Date`, enum, `Calendar`)
- **THEN** populate fails — the type has no converter (compatible with the default registry)

### Requirement: Boolean literals
A value SHALL be converted to `Boolean`/`boolean` with the literals `true`, `yes`, `y`, `on`, `1` (true) and `false`, `no`, `n`, `off`, `0` (false) case-insensitively; any other input SHALL produce the value `false` (the zero value of the type) without error.

#### Scenario: True literal
- **WHEN** parameter `confirm=yes` populates `boolean` property `confirm`
- **THEN** the property value is `true`

#### Scenario: False literal
- **WHEN** parameter `confirm=Off` populates `Boolean` property `confirm`
- **THEN** the property value is `Boolean.FALSE`

#### Scenario: Invalid input yields false
- **WHEN** parameter `confirm=maybe` populates `boolean` property `confirm` that held value `true`
- **THEN** there is no error and the property value is replaced with `false`

### Requirement: Arrays and collections
An array of parameter values SHALL populate an array property element-wise (each element is converted to the component type per the scalar conversion rules; an invalid element yields the zero component value without error); a single value SHALL populate a scalar property with the first element of the parameter array; a single `FormFile` SHALL populate a `FormFile` property, and an array of `FormFile` — an array or list of files property. Collections (for example, `List`) SHALL not be populated from a parameter array — populate fails.

#### Scenario: String array element-wise
- **WHEN** the request contains parameter `langs=java&langs=go` and the form has a `String[]` property `langs`
- **THEN** the property array is `[java, go]`

#### Scenario: Numeric array from multibox
- **WHEN** the request contains parameter `intMultibox=1&intMultibox=3` and the form has an `int[]` property `intMultibox`
- **THEN** the property array is `[1, 3]`

#### Scenario: Invalid element of a numeric array
- **WHEN** the request contains parameter `intMultibox=1&intMultibox=abc` and the form has an `int[]` property
- **THEN** there is no error and the property array is `[1, 0]`

#### Scenario: Collection is not populated from parameters
- **WHEN** the request contains parameter `langs=java&langs=go` and the form has a `List<String>` property `langs`
- **THEN** populate fails (no converter into a collection)

#### Scenario: Single file into a FormFile property
- **WHEN** a multipart request submits one file in field `theFile`, resolved by the form as a single `FormFile` property
- **THEN** the property contains the single parsed file

### Requirement: Populate of dynamic and lazy forms
Dynamic forms SHALL be populated according to the `form-property` types declared in the configuration (including mapped ones) with the same conversion as static forms, including zero values on invalid input; a mapped property of a dynamic form SHALL receive the raw parameter value. For lazy forms (for example, `LazyValidatorForm`), properties not declared in the configuration SHALL be added with the value of the first element of the parameter as a string, and declared ones SHALL be converted according to the declared type.

#### Scenario: Parameters populate a dynamic form
- **WHEN** the request contains parameters `testString=hello` and `confirmed=true`, and the dynamic form declares properties `java.lang.String` and `java.lang.Boolean`
- **THEN** the dynamic form properties contain string `hello` and `Boolean.TRUE`

#### Scenario: Invalid input of a dynamic form
- **WHEN** the request contains parameter `count=abc` and the dynamic form declares a `java.lang.Integer` property
- **THEN** there is no error and the property is `Integer` 0

#### Scenario: Mapped parameter of a dynamic form
- **WHEN** the request contains parameter `map(key)=value` and the dynamic form declares mapped property `map`
- **THEN** under key `key` the dynamic property holds the raw parameter value (array of strings)

#### Scenario: Undeclared property of a lazy form
- **WHEN** the request contains parameter `extra=1&extra=2`, and the lazy form does not declare property `extra`
- **THEN** property `extra` is added to the form as string `"1"` (the first element)

### Requirement: Populate errors
An unrecoverable populate error SHALL abort request processing with a servlet error with a message compatible with the reference (`BeanUtils.populate`), and the action SHALL not be invoked. An error SHALL be produced by: a property type without a converter (`java.util.Date`, enum, `Calendar`), a failed `BigDecimal`/`BigInteger` conversion, a collection as the populate target, an invalid value format for `java.sql.Date`/`Time`/`Timestamp` (JDBC formats), an invalid class name for `Class`, an invalid `URL`. Assignments made before the first error SHALL be preserved.

#### Scenario: Action not invoked on a conversion error
- **WHEN** the request contains a parameter with a value not parseable into a JDBC format for a `java.sql.Date` property
- **THEN** populate fails with a servlet error and the action's execute method is not invoked

#### Scenario: Partial population is preserved
- **WHEN** the request contains a valid parameter for a string property and a parameter that causes a populate error for `BigDecimal`
- **THEN** populate fails, but the string property remains populated
