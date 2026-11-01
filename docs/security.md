# Security notes

The engine recreates Struts 1 on modern Spring. This page records the
security posture against the verified vulnerabilities of the historical
framework, the hardening the engine adds on top, and the upstream behaviors
that remain the application's responsibility.

## Historical Struts 1 CVEs

| CVE | Class | Status in this engine |
|---|---|---|
| CVE-2005-3745 | XSS via the query string in error messages | Not reproduced — request data never enters framework error messages; URIs reach output only through filtered tags |
| CVE-2006-1546 | Validation bypass via the cancel parameter | Not reproduced — validation never short-circuits on a cancel parameter |
| CVE-2006-1547 | DoS through the `multipartRequestHandler` property | Closed — the reserved-name rule rejects it before any resolution |
| CVE-2006-1548 | XSS through the dispatch parameter name in error messages | Not reproduced — request data goes to logs only; thrown messages carry configuration values |
| CVE-2014-0114 | ClassLoader traversal via `class.classLoader...` form parameters | Closed — two independent name filters plus populate-level guard, pinned by tests |
| CVE-2016-1181 | Race on session-scoped forms (reset/populate vs concurrent request) | Closed — the session form lifecycle window is serialized per session (see below) |
| CVE-2016-1182 | Overwriting validator-internal form properties from the request | Closed — the reserved-name rule rejects `validatorResults`/`resultValueMap`/`servlet`; the `page` clamp prevents narrowing the validated scope |

Deliberate deviations from the reference behavior are limited to the
reserved-name rule (five property names are never assignable from a request)
and the class-conversion refusal (a `Class`-typed form property is a populate
error — request data cannot trigger class loading).

## Session-form concurrency

A mapping with `scope="session"` (the default) shares one form instance
between the requests of that session — classic Struts semantics. The engine
serializes the mutation window of that instance per session: acquiring or
creating the instance, `reset()`, and populate (including the multipart parse
and handler attachment) run under a per-session mutex, so concurrent requests
of one session cannot race a `reset()` against a populate, and a page render
cannot swap the stored instance under a populate in progress (the
CVE-2016-1181 window).

Validation and action execution still run outside the window: passing the
populated form to validate and execute remains shared-state semantics, as in
the reference framework. Applications that need full isolation use
`scope="request"` — every request gets and populates its own instance in
parallel.

## Form beans and Spring scopes

A form class registered by the application as a Spring singleton bean fails
startup: a singleton form instance would be shared across sessions and
users, which no form lifecycle assumes. See the
[migration guide](MIGRATION.md#form-beans-must-not-be-singletons) for the
remediation. Auto-registered forms and `@StrutsForm` classes are prototype;
`@RequestScope` and `@SessionScope` definitions are accepted and resolved
through the same session mutex protection.

## The application's responsibility

Two upstream behaviors are intentionally preserved (applications relying on
them migrate unchanged, and they are not engine bugs):

- `html:errors` / `html:messages` render message text (and, unless
  `filterArgs="true"` is set, replacement arguments) without HTML escaping.
  If an application puts request data into an `ActionMessage`, it must escape
  it itself — the same rule as the original framework.
- `ExceptionHandler` prints the exception to an already-committed response in
  the rare committed-response edge case; exception messages therefore should
  not contain request data.

## Reporting

Report security issues through the project's GitHub security advisories
rather than public issues.
