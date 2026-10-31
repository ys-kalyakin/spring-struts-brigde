# Tasks

## 1. Multipart upload in the core

- [x] 1.1 Add the commons-fileupload2-core/`-jakarta` dependencies (newest stable 2.x; base check against the reference's 2.0.0-M1) to `spring-struts1-core`; document the version choice in a pom comment. Verify: `mvn -pl spring-struts1-core dependency:resolve` passes, the version is documented.
- [x] 1.2 Port the `org.apache.struts.upload` packages from the reference: `FormFile`, `MultipartRequestHandler` (with the limit-exceeded constants), `CommonsMultipartRequestHandler` (+`CommonsFormFile`), `MultipartRequestWrapper`, related classes; `Globals.MULTIPART_KEY`, handler access on `ActionForm`, `ActionServletWrapper`, the `ControllerConfig.maxStringLen=4K` default + the new `maxSize`/`fileCountMax`. Verify: contract unit tests — the FormFile adapter, limits from ControllerConfig, attribute constants; `mvn -pl spring-struts1-core test` green.
- [x] 1.3 Integration into the request loop: `StrutsHandlerAdapter.processMultipart` (the request wrapper), `processPopulate` sets `Globals.MULTIPART_KEY` from `mapping.multipartClass`/ControllerConfig, `RequestUtils.populate` — the handler-elements branch with `rationalizeMultipleFileProperty`, `rollback`/`finish` in the error/success branches, adaptation of `MultipartHttpServletRequest` without re-parsing. Verify: unit tests of populate over handler elements (a single FormFile, an array, skipping populate on limit excess).
- [x] 1.4 Multipart ITs in `spring-struts1-core` (the TomcatTestServer fixture): a file upload + a text field in one form, multi-file into an array, exceeding `maxFileSize` → the input page with an error via a request attribute, temp files removed. Verify: IT tests green, `mvn -pl spring-struts1-core test` as a whole.

## 2. commons-validator in the core

- [x] 2.1 Add the commons-validator 1.10.1 and antlr4-runtime 4.13.2 dependencies to core; move the reference resources: `validator-rules.xml`, `LocalStrings.properties`, the commons-validator JS utilities; vendor validwhen (ANTLR4 sources or the plugin — per design D4). Verify: `mvn -pl spring-struts1-core test` green, the resources available at the classic classpath paths.
- [x] 2.2 Port `org.apache.struts.validator`: `ValidatorPlugIn` (pathnames/stopOnFirstError, module context keys, a re-initialization error), `Resources` (initValidator, arguments 0–3, bundles, locale), `ValidatorForm`, `DynaValidatorForm`, `FieldChecks` (the full rule set), `validwhen`; `ValidatorActionForm`/`DynaValidatorActionForm` in `org.apache.struts.extras.validator` (the package as in the reference). Verify: unit tests — initValidator by form key, converting violations into ActionMessages, page filtering, validwhen expressions; a startup error on a bad pathnames.
- [x] 2.3 Validation unit/ITs on the engine: validation.xml with required/mask/minlength/intRange/validwhen over a dynamic and a static form, messages via message-resources (the `???` format for missing keys), the input transition on errors, DynaValidatorForm with `extends` form-bean. Verify: IT tests in the core fixture green.

## 3. html:javascript in taglib

- [x] 3.1 Port `JavascriptValidatorTag` from the reference onto the Spring tag base (JS resources from the core, the static/dynamic/c methods, formName/submitMethod and the TLD attributes). Verify: render unit tests — the form script per validation.xml, the static mode; the "tag without a validator" scenario from TagRenderingTest is flipped to working.
- [x] 3.2 IT on the taglib fixture: a page with `<html:javascript formName="..."/>` renders the validation functions of the declared rules. Verify: IT green, `mvn -pl spring-struts1-taglib test` as a whole.

## 4. The spring-struts1-tiles module

- [x] 4.1 Module skeleton: pom (the core+taglib dependency), wiring into the root aggregator; moving the `org.apache.struts.tiles` core API from the reference: the ComponentDefinition/ComponentContext/AttributeDefinition hierarchy, DefinitionsFactory(+Config), TilesUtil(+Impl/Struts/Modules), the controllers (Controller, ControllerSupport, UrlController, ActionController), TilesAction, the beans (MenuItem/SimpleMenuItem). Verify: `mvn -pl spring-struts1-tiles test-compile` green.
- [x] 4.2 XML parsing of definitions: moving `xmlDefinition` (XmlParser on digester with local DTDs 1.1/1.3/1.4, XmlDefinition/XmlAttribute/XmlListAttribute, I18nFactorySet with localized file variants, FactorySet). Verify: unit tests — parsing tiles-defs with extends/put/putList, localized file variants, an error on a missing file.
- [x] 4.3 `TilesPlugin` via the plugin mechanism: the reference init-params (definitions-config, parser-validate, factory-classname, moduleAware), the factory in the servlet context under the classic keys (+module prefix), registering the definition-resolver as a Spring component, a re-initialization error, factory destroy. Verify: init/destroy/moduleAware unit tests.
- [x] 4.4 The forward hook in the core: the `DefinitionForwardResolver` interface in `org.apache.struts.spring`, polling beans of the type in `StrutsHandlerAdapter.processForwardConfig` before the regular render; the tiles implementation (the attribute context, invoking the controller, rendering the path definition, a fallback include when committed). Verify: adapter unit tests — a forward to a definition is rendered, the regular path untouched; the core does not depend on the tiles module (a pom check).
- [x] 4.5 The tag library: the reference `struts-tiles.tld` + 11 tags on the SpringTagSupport base (insert/definition/put/putList/add/get/getAsString/useAttribute/importAttribute/initComponentDefinitions); the definition-resolution and context logic — overridable Spring services. Verify: render unit tests of definition/layout pairs, nested tiles, importAttribute/useAttribute.
- [x] 4.6 Tiles ITs on the module fixture: an application with tiles-defs (inheritance, putList, localized files), an action forward to a definition, a controller class adding an attribute, TilesAction, moduleAware in two modules. Verify: ITs green, `mvn -pl spring-struts1-tiles test` as a whole.

## 5. examples: upload and validator

- [x] 5.1 Enable the modules in `examples`: the init-params `config/upload`, `config/validator` (+ the -type/-validwhen/-bundles/-i18nVariables variants per the reference) in web.xml; port the section classes from the reference (UploadForm/UploadAction, RegistrationForm/CityStateZip/the multiRegistration actions, TypeTestForm, etc.) and the JSPs (upload.jsp, display.jsp, the registration pages). Verify: the application starts with all modules, `mvn -pl spring-struts1-apps -am test-compile` green.
- [x] 5.2 Upload-section ITs: a multipart POST upload-submit with a file and text → the display page with the name/type/content; exceeding the 2M limit → the maxLengthExceeded message; the multi-file scenarios of upload-multi. Verify: the IT tests in ExamplesTest green.
- [x] 5.3 Validator-section ITs: a registration-submit with required/mask/minlength violations → errors on input; a successful submit → confirmation; multiRegistration (page=1/2); the validwhen config; jsRegistration renders the script; locale/bundles (`bundle="alternate"`). Verify: IT tests green.

## 6. cookbook

- [x] 6.1 A new `cookbook` war module under `spring-struts1-apps`: porting the reference (the configs + Wildcard, DynaValidatorForm, the custom `twofields` validator, the jsp sections simple/dyna/options/multibox/bean/logic/links/validator/messages/localization/token). Verify: the application starts, `mvn -pl spring-struts1-apps -am test-compile` green.
- [x] 6.2 Cookbook ITs: DynaValidatorForm validation (byte/integer/range/creditCard/email), the custom twofields rule, the basic scenarios of the sections (multibox, localization, token). Verify: CookbookAppTest green.

## 7. mailreader (+dao)

- [x] 7.1 Vendoring the dao: the `mailreader-dao` module under apps from the reference (the org.apache.struts.apps.mailreader.dao.* packages, the memory impl, digester loading of database.xml). Verify: the dao unit tests (creating/reading a user/subscription) green.
- [x] 7.2 Port the extras plugins into the core: `ModuleConfigVerifier`, `DigestingPlugIn` (+ the related classes of the reference package). Verify: init/destroy unit tests of both plugins with set-property.
- [x] 7.3 A new `mailreader` war module: porting the reference (a struts-config with wildcard and extends form-bean, DynaValidatorForm forms, MemoryDatabasePlugIn in the application sources, validation.xml, JSPs, the DigestingPlugIn server-types config). Verify: the application starts, `mvn -pl spring-struts1-apps -am test-compile` green.
- [x] 7.4 Mailreader ITs: logon (required/minlength validation), registering a new user → logon, subscription create/edit/delete, tokens, the ExpiredPasswordException forward. Verify: MailreaderAppTest green.

## 8. blank and full regression

- [x] 8.1 blank: replace `TestPlugin` with a real `ValidatorPlugIn` (as in the reference blank), validation.xml and message keys from the reference; TestPlugin either stays as additional regression of the plugin mechanism or is removed — per the actual reference. Verify: BlankAppTest green with validation.
- [x] 8.2 Full regression: `mvn test` of all modules; the existing blank/examples ITs (exercise/dispatch) with unchanged expectations; `openspec validate --strict` on the change passes. Verify: the build green as a whole.

## Workflow follow-up

- Archive the change after the project's review requirements are satisfied.
- Verify the archived result.
