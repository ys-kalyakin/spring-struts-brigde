# Tasks

## 1. Taglib module and core utilities

- [x] 1.1 Create the `spring-struts1-taglib` module (dependencies: core, jakarta.servlet.jsp-api provided, beanutils) with TLDs copied byte-for-byte from the reference (`META-INF/tld/struts-{bean,html,logic,nested}.tld`); verify that `./mvnw -q -pl spring-struts1-taglib -am validate` passes
- [x] 1.2 Port into core `org.apache.struts.util.ResponseUtils` (filter/render/write) and `ModuleUtils` (module map, selectModule by request path) from the reference; verify with a unit test of filter (escaping) and module selection by path
- [x] 1.3 Port `org.apache.struts.taglib.TagUtils` from the reference with adaptation to our core (MessageResources/ModuleConfig from the request); verify with a unit test of URL building with context and value filtering

## 2. struts-bean library

- [x] 2.1 Port the 11 tags of `org.apache.struts.taglib.bean` (write, message, define, size, header, cookie, parameter, include, resource, struts, page?) file-by-file from the reference; verify the module compiles
- [x] 2.2 Write ITs on embedded Tomcat 11 (a dedicated test page in the test webapp): bean:write values and filter=true/false, bean:message by key and with arg0..arg3 and bundle, bean:define + bean:write of the variable, bean:parameter/header/cookie, bean:size, bean:include; verify the tests are green and match the scenarios of the `bean-taglib` spec

## 3. struts-html library

- [x] 3.1 Port the 29 tags of `org.apache.struts.taglib.html` (form, text, password, hidden, textarea, checkbox, multibox, radio, select, option, options, optionsCollection, submit, reset, button, cancel, image, file, errors, messages, img, link, rewrite, base, html, xhtml; no javascript stub — the tag is not ported); verify the module compiles
- [x] 3.2 Write ITs: html:form with action and without (as the html-form exercise), widgets text/select/checkbox/multibox with values from the form, html:errors (header/prefix/suffix defaults and filtering by property), html:messages, html:link by forward and with paramId/paramName, html:rewrite, html:img, html:base; verify they match the scenarios of the `html-taglib` spec
- [x] 3.3 Implement the xhtml mode (flag, self-closing elements, `checked="checked"`/`selected="selected"`) and IT: output with the mode enabled and disabled; verify the `html-taglib` spec scenario (xhtml)
- [x] 3.4 Pin with an IT: a JSP with `html:javascript` produces a processing error (the tag is deferred to phase 3); verify the spec scenario

## 4. struts-logic library

- [x] 4.1 Port the 17 tags of `org.apache.struts.taglib.logic` (iterate, equal/notEqual, greaterEqual/lessEqual/greaterThan/lessThan, match/notMatch, present/notPresent, empty/notEmpty, messagesPresent/messagesNotPresent, redirect, forward); verify the module compiles
- [x] 4.2 Write ITs: iterate (collection, indexId, offset/length), comparisons by parameter and property, match with location, present/notPresent (including an application attribute — as in the reference Welcome.jsp), empty, messagesPresent, redirect by forward (302) and forward; verify they match the scenarios of the `logic-taglib` spec

## 5. struts-nested library

- [x] 5.1 Port the 40 tags of `org.apache.struts.taglib.nested` (including NestedPropertyHelper/NestedReferenceHelper) file-by-file; verify the module compiles
- [x] 5.2 Write ITs on dedicated pages: nested:form + nested:text with a full property path, nested:iterate + nested:write over list elements, nested:root/nested:reference, output equivalence to the base tags; verify they match the scenarios of the `nested-taglib` spec

## 6. Multi-modularity and path-mapped

- [x] 6.1 Extend `ActionServlet.initModuleConfig`: parsing `config/<prefix>` init-params into a module map (startup error with the file name on a broken/missing module file); verify with unit tests (module by `config/exercise`, error with the file name)
- [x] 6.2 Introduce module selection in `StrutsHandlerMapping` (by request path prefix, otherwise the default module) and pass the module into the pipeline/adapter (request attribute); verify with an IT: a request to `/exercise/…` is served by the mappings of the `/exercise` module, a request without a prefix — by the default module
- [x] 6.3 Add the `servletMapping` mode to `StrutsHandlerMapping` (`*.do` / `/action/*`): path-mapped routing with prefix stripping; verify with an IT: a request to `/action/Welcome` is served by the `/Welcome` mapping

## 7. DynaActionForm and dispatch actions

- [x] 7.1 Port a minimal `DynaActionForm` (DynaBean over a HashMap, properties from `<form-property>`, initial values) and the creation branch in `FormBeanConfig.createActionForm`; verify with unit tests: properties per configuration, initial values, a fresh instance per request
- [x] 7.2 Verify with an IT: populate of the dynamic form from request parameters and validation by the mapping flag on a dynamic form; verify the scenarios of the `dynamic-form-beans` spec
- [x] 7.3 Port the extras subset into core (`org.apache.struts.extras.actions`: DispatchAction, LookupDispatchAction, MappingDispatchAction, EventDispatchAction, ActionDispatcher, EventActionDispatcher, ForwardAction, IncludeAction); verify with unit tests/ITs: dispatch by parameter, LookupDispatch by button from the bundle, MappingDispatch by the mapping's parameter, EventDispatch by event, ActionDispatcher without inheritance, unspecified when the parameter is absent; verify the scenarios of the `dispatch-actions` spec

## 8. Porting apps/examples and blank

- [x] 8.1 Create the `spring-struts1-apps/examples` module (war): web.xml with the config, config/exercise, config/dispatch modules (without upload/validator), struts-config files and application sources from the reference without edits, exercise/dispatch JSPs — from the reference; verify the war builds with `./mvnw -pl spring-struts1-apps/examples -am package`
- [x] 8.2 Switch `blank` to the original reference JSPs (html:html/bean:message/logic:notPresent) instead of the phase 1 adaptation; verify the blank ITs are green on the original pages
- [x] 8.3 Write ITs for the examples run on embedded Tomcat 11: all exercise pages bean-*/html-*/logic-* respond 200 with key HTML structure markers (attributes, nesting), dispatch scenarios (actionDispatcher, eventAction, lookup, mapping) are executed with POST requests selecting the method; verify the tests are green. Note: html-link.jsp and bean-include.jsp reference the /upload and /validator modules excluded in 8.1, so they are excluded from the ITs with a comment (the behavior of their tags is covered by the taglib module ITs)

## 9. End-to-end phase 2 verification

- [x] 9.1 Full `./mvnw verify` green; go through the phase 2 acceptance criteria in ROADMAP.md (TLD/URIs identical, examples renders and passes exercise-taglib and dispatch, HTML structure compatible) and make sure each is covered by a test; verify the final run report

## Workflow follow-up

- Sync the specs and archive the change (`/opsx:sync`, `/opsx:archive`) after review.
- Check the archived result (`openspec list`).
