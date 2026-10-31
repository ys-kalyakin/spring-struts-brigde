# Spec Delta: html-taglib

## MODIFIED Requirements

### Requirement: The deferred validator tag
The `html:javascript` tag is implemented in the validation phase: it SHALL generate commons-validator client scripts from the validation.xml rules (the behavior is defined in the `struts-validator` spec); the tag's TLD declaration remains unchanged.

#### Scenario: Using the javascript tag
- **WHEN** a JSP uses `<html:javascript formName="..."/>`, and validation.xml declares the rules of that form
- **THEN** the page contains the generated form validation JavaScript instead of a processing error
