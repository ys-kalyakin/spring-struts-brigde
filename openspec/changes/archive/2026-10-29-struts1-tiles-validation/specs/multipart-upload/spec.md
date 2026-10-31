# Spec Delta: multipart-upload

## Purpose

Support for multipart requests (file uploads) in the classic Struts 1 contract: the `org.apache.struts.upload` interfaces, request parsing in the populate lifecycle, graceful handling of limit excess — with weblegacy reference behavior and compatibility with existing applications.

## ADDED Requirements

### Requirement: The FormFile and MultipartRequestHandler contract
The `org.apache.struts.upload` package SHALL provide the classic reference contract: the `FormFile` interface (name, content type, size, data, stream, destroy) and the `MultipartRequestHandler` interface (`handleRequest`, `getTextElements`, `getFileElements`, `getAllElements`, `rollback`, `finish`, the `ATTRIBUTE_MAX_LENGTH_EXCEEDED` constant). The form SHALL provide access to its multipart handler, and the action — to a file element by field name.

#### Scenario: Access to an uploaded file from an action
- **WHEN** the form has a property of type `FormFile`, a POST request with `multipart/form-data` has been processed, and the action reads the form's file elements
- **THEN** the element by field name implements `FormFile` and returns the file name, content type, and data sent by the client

#### Scenario: File data available as a stream
- **WHEN** the action reads the uploaded file through the `FormFile` stream (not the whole byte array at once)
- **THEN** the file content is read completely and correctly

### Requirement: Parsing a multipart request before populate
A multipart POST request (content-type `multipart/form-data`) SHALL be parsed before form populate: text fields go into populate on a par with regular request parameters, and file ones fill the form properties of the corresponding types (a single `FormFile` — the first file, an array/collection — all files). Regular (non-multipart) requests SHALL be processed unchanged.

#### Scenario: Text and file fields of one form
- **WHEN** a `multipart/form-data` POST contains a text field and a file, and the form declares a string property and a `FormFile` property
- **THEN** both properties are populated by the time the action is invoked: the string — with the text field's value, the `FormFile` — with the parsed file

#### Scenario: Multi-file property
- **WHEN** the request contains several files in one field, and the form declares a `FormFile[]` array property (or a single `FormFile`)
- **THEN** the array contains all uploaded files (or the single property — the first of them), as in the reference

#### Scenario: A regular POST is unchanged
- **WHEN** a regular POST with `application/x-www-form-urlencoded` arrives at a mapping with a form
- **THEN** populate runs over the request parameters without multipart parsing, the behavior identical to previous phases

### Requirement: The default handler class and configuration from the config
By default, multipart requests SHALL be handled by the `CommonsMultipartRequestHandler` class; the handler class SHALL be configurable with the `multipartClass` attribute in `<controller>` or in a specific mapping. Limits and parsing parameters SHALL come from the `<controller>` attributes: `maxFileSize` (default 250M), `maxSize` (256M), `maxStringLen` (4K), `fileCountMax` (no limit), `memFileSize` (256K), `tempDir`, `bufferSize` (4096).

#### Scenario: The handler class from the mapping
- **WHEN** a mapping declares `multipartClass` with its own handler implementation, and a multipart request arrives
- **THEN** the request is parsed by the specified implementation, not by the default class

#### Scenario: Default values as in the reference
- **WHEN** the configuration does not declare a `<controller>` with multipart attributes
- **THEN** parsing runs with the reference default limits (maxFileSize 250M, maxSize 256M, maxStringLen 4K)

### Requirement: Graceful handling of limit excess
Exceeding a size limit SHALL not lead to a container error: the handler SHALL set the request attribute `ATTRIBUTE_MAX_LENGTH_EXCEEDED` (and the specific total-size/string-length/file-count excess attributes, as in the reference), form populate SHALL not run, and the request SHALL continue through the normal cycle (form validation), where the application learns about the excess through this attribute.

#### Scenario: A file larger than maxFileSize
- **WHEN** the uploaded file exceeds the controller's `maxFileSize`, and form validation is enabled and checks the excess attribute
- **THEN** the client receives the input page with the application's size-excess error, not a container 500 response

#### Scenario: Populate skipped on excess
- **WHEN** the request exceeded a size limit
- **THEN** the form properties are not populated with such a request's data, and the handler is available to the form for cleaning up temp files

### Requirement: Compatibility with the Spring multipart parser
If the request has already been parsed by the Spring multipart mechanism (the user declared their own `MultipartResolver`, and the request is a `MultipartHttpServletRequest`), the engine SHALL adapt its parts into the same contract: text parts — into populate, file parts — as `FormFile`, without running its own parsing again.

#### Scenario: A request parsed by Spring
- **WHEN** the user configured their own Spring `MultipartResolver`, and a multipart request arrived at a mapping with a form
- **THEN** the form is populated with both the text and the file parts of the request, and no re-parsing happened

### Requirement: Cleanup of temporary resources
The temp files of parsing SHALL be released: on a request-processing error (an exception before/instead of execute) the handler SHALL get `rollback`, on successful processing — `finish`.

#### Scenario: Rollback on an exception
- **WHEN** multipart request processing is interrupted by an exception (for example, a validation error with the input transition or an action failure)
- **THEN** the handler's temp files are removed
