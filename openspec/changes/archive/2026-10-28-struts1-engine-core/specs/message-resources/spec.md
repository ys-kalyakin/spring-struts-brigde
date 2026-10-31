# Spec Delta

## Purpose

Gives the application key-based access to messages with Struts 1 semantics: resources from `<message-resources>`, the locale from the session, and the signature missing-key format.

## ADDED Requirements

### Requirement: Loading message-resources
`<message-resources>` elements from the configuration SHALL be loaded at startup; resources declared without a `key` attribute SHALL be available as the default resources, those declared with a `key` — under that key.

#### Scenario: Default resources
- **WHEN** the configuration declares `<message-resources parameter="MessageResources"/>`
- **THEN** the `MessageResources` bundle is loaded and available to the application as the default resources

#### Scenario: Named bundle
- **WHEN** the configuration declares `<message-resources key="alternate" parameter="AlternateResources"/>`
- **THEN** the `AlternateResources` bundle is available under the key `alternate`

### Requirement: Message by key with the locale from the session
Message retrieval SHALL respect the locale stored in the session (session key `org.apache.struts.action.LOCALE`); if the session has no locale, the request locale SHALL be used.

#### Scenario: Locale from the session
- **WHEN** the locale `ru_RU` is stored in the session, and the bundle has an entry for that locale
- **THEN** the message for the key is returned in the `ru_RU` wording

#### Scenario: Locale not set
- **WHEN** no locale is set in the session
- **THEN** the message is returned by the request locale

### Requirement: Missing-key format
A message for a key missing from the bundle SHALL be returned in the format `???<locale>.<key>???` (for example, `???ru_RU.welcome.missing???`).

#### Scenario: Missing key
- **WHEN** a key that is not in the bundle is requested, with the locale `ru_RU`
- **THEN** the string `???ru_RU.<key>???` is returned instead of an exception
