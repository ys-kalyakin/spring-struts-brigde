# Tasks

## 1. Validator in `spring-struts1-core`

- [x] 1.1 Implement `ControllerConfigValidator` in `org.apache.struts.config`: string-based allowlist for `processorClass` (classic default, `org.apache.struts.tiles.TilesRequestProcessor`; empty/absent normalizes to the default), failure on non-default `catalog`/`command`, SLF4J warning for non-default `bufferSize`; every message names the property, the offending value, and the module prefix (empty prefix reported as the default module). Unit tests cover: custom processor class fails naming property/value/prefix, both supported names pass, empty and absent values pass, chain settings fail, default chain values pass, buffer size warns without failing. Verify: unit tests pass.
- [x] 1.2 Invoke the validator from `ActionServlet.initModuleConfig` right after each module's parse, wrapping the validator's runtime exception into `ServletException` like existing parse errors. Verify: a unit test starting the facade with a configuration carrying `processorClass="com.example.SecureRequestProcessor"` fails startup with the property named.
- [x] 1.3 Invoke the validator from `StrutsEngineRegistrar.parseModule` right after its parse, wrapping the exception the way the registrar already wraps parse failures. Verify: a unit test through the registrar path fails startup with the property named.

## 2. Integration coverage

- [x] 2.1 Audit the IT suite's `struts-config.xml` files for `processorClass`, `catalog`, `command`, `bufferSize` values other than the supported ones; migrate any offending application configuration within this change before validation is active. Verify: recorded audit output shows no app in the suite relies on an unsupported value.
- [x] 2.2 Add an integration scenario asserting an application whose configuration declares a custom `processorClass` fails startup with the actionable error, and one declaring `org.apache.struts.tiles.TilesRequestProcessor` starts. Verify: the integration tests pass.

## 3. Migration guide

- [x] 3.1 Add a "Functional limitations" section to `docs/MIGRATION.md` after "Common adjustments": a table of unimplemented controller properties (`processorClass`, `catalog`, `command` — startup error; `bufferSize` — startup warning) and the remediation path for a custom `RequestProcessor` (Spring MVC interceptors, servlet filters, engine extension points). Verify: the section's content matches the delta-spec scenarios for `app-migration` and renders in the site build.

## 4. Final integration

- [x] 4.1 Run the full `./mvnw verify` (tests, Checkstyle, SpotBugs) and `openspec validate --strict` on the change. Verify: the build is green and validation passes.

## Workflow follow-up

- Archive the change after the project's review requirements are satisfied.
- Verify the archived result.
