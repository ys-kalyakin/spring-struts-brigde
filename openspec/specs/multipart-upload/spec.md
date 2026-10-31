# multipart-upload Specification

## Purpose

Support for multipart requests (file uploads) under the classic Struts 1 contract: the `org.apache.struts.upload` interfaces, request parsing in the populate lifecycle, and graceful handling of limit violations — with the behavior of the weblegacy reference and compatibility with existing applications.

## Requirements

### Requirement: FormFile and MultipartRequestHandler contract
The package `org.apache.struts.upload` SHALL provide the classic reference contract: the `FormFile` interface (name, content type, size, data, stream, destroy) and the `MultipartRequestHandler` interface (`handleRequest`, `getTextElements`, `getFileElements`, `getAllElements`, `rollback`, `finish`, the constant `ATTRIBUTE_MAX_LENGTH_EXCEEDED`). The form SHALL provide access to its multipart handler, and the action SHALL provide access to a file element by field name.

#### Scenario: Accessing an uploaded file from an action
- **WHEN** the form has a property of type `FormFile`, a POST request with `multipart/form-data` has been processed, and the action reads the form's file elements
- **THEN** the element for the field name implements `FormFile` and returns the file name, content type, and data sent by the client

#### Scenario: File data available as a stream
- **WHEN** the action reads the uploaded file via the `FormFile` stream (not the whole byte array at once)
- **THEN** the file content is read completely and correctly

### Requirement: Parsing the multipart request before populate
A multipart POST request (content type `multipart/form-data`) SHALL be parsed before form populate: text fields are included in populate on par with regular request parameters, and files populate form properties of the matching types (a single `FormFile` receives the first file, an array/collection receives all files). Regular (non-multipart) requests SHALL be processed unchanged.

#### Scenario: Text and file fields of one form
- **WHEN** a POST `multipart/form-data` contains a text field and a file, and the form declares a string property and a `FormFile` property
- **THEN** both properties are populated by the time the action is invoked: the string with the text field value, the `FormFile` with the parsed file

#### Scenario: Multi-file property
- **WHEN** the request contains several files under one field, and the form declares an array property `FormFile[]` (or a single `FormFile`)
- **THEN** the array contains all uploaded files (or the single property contains the first of them), as in the reference

#### Scenario: Regular POST unchanged
- **WHEN** a regular POST with `application/x-www-form-urlencoded` arrives for a mapping with a form
- **THEN** populate is performed from the request parameters without multipart parsing, and the behavior is identical to previous phases

### Requirement: Default handler class and configuration from config
By default, multipart requests SHALL be handled by the class `CommonsMultipartRequestHandler`; the handler class SHALL be configurable via the `multipartClass` attribute in `<controller>` or in a specific mapping. Limits and parsing parameters SHALL be taken from `<controller>` attributes: `maxFileSize` (default 250M), `maxSize` (256M), `maxStringLen` (4K), `fileCountMax` (no limit), `memFileSize` (256K), `tempDir`, `bufferSize` (4096).

#### Scenario: Handler class from the mapping
- **WHEN** a mapping declares `multipartClass` with its own handler implementation, and a multipart request arrives
- **THEN** the request is parsed by the specified implementation rather than the default class

#### Scenario: Default values as in the reference
- **WHEN** the configuration does not declare a `<controller>` with multipart attributes
- **THEN** parsing is performed with the reference defaults (maxFileSize 250M, maxSize 256M, maxStringLen 4K)

### Requirement: Graceful handling of limit violations
Exceeding a size limit SHALL NOT lead to a container error: the handler SHALL set the request attribute `ATTRIBUTE_MAX_LENGTH_EXCEEDED` (and the specific attributes for exceeding total size/string length/file count limits, as in the reference), form populate SHALL NOT be performed, and the request SHALL continue through the normal cycle (form validation), where the application learns of the violation via that attribute.

#### Scenario: File larger than maxFileSize
- **WHEN** the uploaded file exceeds the controller's `maxFileSize`, and form validation is enabled and checks the excess attribute
- **THEN** the client receives the input page with the application's size-exceeded error instead of a container 500 response

#### Scenario: Populate skipped on excess
- **WHEN** the request has exceeded a size limit
- **THEN** form properties are not populated with that request's data, and the handler remains available to the form for cleaning up temporary files

### Requirement: Compatibility with the Spring multipart resolver
If the request has already been parsed by Spring's multipart mechanism (the user declared their own `MultipartResolver`, and the request is represented as `MultipartHttpServletRequest`), the engine SHALL adapt its parts to the same contract: text parts into populate, file parts as `FormFile`, without running its own parsing again.

#### Scenario: Request parsed by Spring
- **WHEN** the user has configured their own Spring `MultipartResolver`, and a multipart request arrives at a mapping with a form
- **THEN** the form is populated with both the text and the file parts of the request, and no repeated parsing occurred

### Requirement: Cleanup of temporary resources
Temporary files from parsing SHALL be released: on request processing failure (an exception before or instead of execute) the handler SHALL receive `rollback`, on successful processing — `finish`.

#### Scenario: Rollback on exception
- **WHEN** processing of a multipart request is interrupted by an exception (for example, a validation failure with an input forward or an action failure)
- **THEN** the handler's temporary files are deleted
