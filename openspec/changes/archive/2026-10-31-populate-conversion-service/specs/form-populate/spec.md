# Spec Delta: form-populate

## Purpose

Defines the semantics of filling (populating) Struts forms with request values: value sources, property name syntax, conversion rules, and error handling — with behavior compatible with commons-beanutils and the weblegacy/struts1 1.5.x reference.

## ADDED Requirements

### Requirement: Populate value sources
The values for filling a form SHALL be taken from the request parameters (all values of a name — as a string array); for a multipart request — from the parsed multipart elements (text fields — as parameters, file fields — into properties of `FormFile` types); multipart parsing SHALL conform to the `multipart-upload` spec. Parameters whose names start with `org.apache.struts.` SHALL be skipped; names with access to `class` SHALL be ignored (CVE-2014-0114).

#### Scenario: Request parameters as value sources
- **WHEN** a POST request contains the parameters `name=Alice` and `age=30`, and the form has the properties `name` and `age`
- **THEN** both properties are filled with the corresponding values by the time the action is invoked

#### Scenario: Service parameters are skipped
- **WHEN** a request contains the parameter `org.apache.struts.action.CANCEL`
- **THEN** no attempt to fill a property with that name is made and the form is not modified

#### Scenario: Multipart file field fills a FormFile property
- **WHEN** a POST `multipart/form-data` contains a file field `theFile` and the form has a `FormFile` property
- **THEN** the property contains the parsed file, and the form's text fields are filled from the multipart elements

### Requirement: Property name syntax
Parameter names SHALL be processed in beanutils syntax: a simple name targets a property, `prop[i]` — an indexed property (array or list), `prop(key)` — a mapped property (`Map`), `a.b` — a nested property, as well as their combinations. A name matching no property of the form SHALL be skipped without error; an inaccessible nested property (an intermediate value of `null`) SHALL be skipped without error. A mapped property SHALL receive the raw parameter value (a string array) without conversion.

#### Scenario: Indexed parameter
- **WHEN** a request contains the parameter `intIndexed[0]=5` and the form has an `int[]` array `intIndexed`
- **THEN** the first element of the array equals 5, the remaining elements are unchanged

#### Scenario: Mapped parameter
- **WHEN** a request contains the parameter `value(color)=red` and the form has a `Map` property `value`
- **THEN** the key `color` in the Map holds the raw parameter value — a string array

#### Scenario: Nested parameter
- **WHEN** a request contains the parameter `nested.intProperty=7` and the form has a nested bean `nested` with the property `intProperty`
- **THEN** the `intProperty` property of the nested bean equals 7

#### Scenario: Unknown parameter name
- **WHEN** a request contains a parameter matching no property of the form
- **THEN** populate completes without error, the form is not modified by that parameter

#### Scenario: Combination of indexed and mapped
- **WHEN** a request contains the parameter `nested.maps[0](color)=red`, the element `maps[0]` exists and is a `Map`
- **THEN** the key `color` of that Map holds the first element of the parameter as a string

### Requirement: Scalar value conversion
A string value SHALL be converted to the declared property type with the semantics of the default commons-beanutils registry: number primitives and wrappers (the value is trimmed; incorrect input, an empty string, or overflow yield the zero value of the type without an error), `Character` (the first character; an empty string yields a space without an error), `BigDecimal`/`BigInteger` (incorrect input or an empty string — a populate error), `String` (no conversion). The conversion SHALL be performed by the engine's conversion service (Spring `ConversionService`); the static global commons-beanutils registry does not participate in populate.

#### Scenario: String into a numeric property
- **WHEN** the parameter `age=30` fills an `int`/`Integer` property `age`
- **THEN** the property value equals the number 30

#### Scenario: Character from a string
- **WHEN** the parameter `grade=A` fills a `char`/`Character` property `grade`
- **THEN** the property value equals the character `A`

#### Scenario: Incorrect number yields the zero value
- **WHEN** the parameter `age=abc` fills the numeric property `age` that held the value 11
- **THEN** there is no error, the property value is replaced with the zero value (0), just as for an empty string and for overflow

#### Scenario: BigDecimal with incorrect input
- **WHEN** the parameter `amount=abc` fills the `BigDecimal` property `amount`
- **THEN** populate fails with an error (see the requirement on populate errors)

#### Scenario: A type without a converter
- **WHEN** a parameter with a string value fills a property of a type that has no converter in the default registry (`java.util.Date`, enum, `Calendar`)
- **THEN** populate fails with an error — the type has no converter (compatible with the default registry)

### Requirement: Boolean literals
A value SHALL be converted to `Boolean`/`boolean` by the literals `true`, `yes`, `y`, `on`, `1` (true) and `false`, `no`, `n`, `off`, `0` (false), case-insensitively; any other input SHALL yield the value `false` (the zero value of the type) without an error.

#### Scenario: Truth literal
- **WHEN** the parameter `confirm=yes` fills the `boolean` property `confirm`
- **THEN** the property value equals `true`

#### Scenario: Falsehood literal
- **WHEN** the parameter `confirm=Off` fills the `Boolean` property `confirm`
- **THEN** the property value equals `Boolean.FALSE`

#### Scenario: Invalid input yields false
- **WHEN** the parameter `confirm=maybe` fills the `boolean` property `confirm` that held the value `true`
- **THEN** there is no error, the property value is replaced with `false`

### Requirement: Arrays and collections
A parameter's array of values SHALL fill an array property component by component (each element is converted to the component type by the scalar conversion rules; an incorrect element yields the zero value of the component without an error); a single value SHALL fill a scalar property with the first element of the parameter array; a single `FormFile` SHALL fill a `FormFile` property, and a `FormFile` array — an array or list property of files. Collections (for example, `List`) SHALL NOT be filled from a parameter array — populate fails with an error.

#### Scenario: String array component by component
- **WHEN** a request contains the parameter `langs=java&langs=go` and the form has a `String[]` property `langs`
- **THEN** the property array equals `[java, go]`

#### Scenario: Number array from multibox
- **WHEN** a request contains the parameter `intMultibox=1&intMultibox=3` and the form has an `int[]` property `intMultibox`
- **THEN** the property array equals `[1, 3]`

#### Scenario: Incorrect element of a number array
- **WHEN** a request contains the parameter `intMultibox=1&intMultibox=abc` and the form has an `int[]` property
- **THEN** there is no error, the property array equals `[1, 0]`

#### Scenario: A collection is not filled from parameters
- **WHEN** a request contains the parameter `langs=java&langs=go` and the form has a `List<String>` property `langs`
- **THEN** populate fails with an error (no converter to a collection)

#### Scenario: Single file into a FormFile property
- **WHEN** a multipart request delivers a single file in the field `theFile`, resolved by the form as a single `FormFile` property
- **THEN** the property contains the single parsed file

### Requirement: Populate of dynamic and lazy forms
Dynamic forms SHALL be filled according to the `form-property` types declared in the configuration (including mapped ones) with the same conversion as static forms, including zero values on incorrect input; a mapped property of a dynamic form SHALL receive the raw parameter value. For lazy forms (for example, `LazyValidatorForm`), properties not declared in the configuration SHALL be added with the value of the first element of the parameter as a string, while declared ones SHALL be converted according to the declared type.

#### Scenario: Parameters fill a dynamic form
- **WHEN** a request contains the parameters `testString=hello` and `confirmed=true`, and the dynamic form declares properties of `java.lang.String` and `java.lang.Boolean`
- **THEN** the dynamic form properties contain the string `hello` and `Boolean.TRUE`

#### Scenario: Incorrect input of a dynamic form
- **WHEN** a request contains the parameter `count=abc` and the dynamic form declares a `java.lang.Integer` property
- **THEN** there is no error, the property equals `Integer` 0

#### Scenario: Mapped parameter of a dynamic form
- **WHEN** a request contains the parameter `map(key)=value` and the dynamic form declares the mapped property `map`
- **THEN** the key `key` of the dynamic property holds the raw parameter value (a string array)

#### Scenario: Undeclared property of a lazy form
- **WHEN** a request contains the parameter `extra=1&extra=2`, and the lazy form does not declare the property `extra`
- **THEN** the property `extra` is added to the form as the string `"1"` (the first element)

### Requirement: Populate errors
An unrecoverable populate error SHALL abort request processing with a servlet error whose message is compatible with the reference (`BeanUtils.populate`), and the action SHALL NOT be invoked. An error SHALL be produced by: a property type without a converter (`java.util.Date`, enum, `Calendar`), a failed conversion of `BigDecimal`/`BigInteger`, a collection as the fill target, an invalid value format for `java.sql.Date`/`Time`/`Timestamp` (JDBC formats), an invalid class name for `Class`, an invalid `URL`. Assignments made before the first error SHALL be preserved.

#### Scenario: The action is not invoked on a conversion error
- **WHEN** a request contains a parameter with a value that cannot be parsed into the JDBC format for a `java.sql.Date` property
- **THEN** populate fails with a servlet error and the execute method of the action is not invoked

#### Scenario: Partial filling is preserved
- **WHEN** a request contains a correct parameter for a string property and a parameter that causes a populate error for `BigDecimal`
- **THEN** populate fails with an error, but the string property remains filled
