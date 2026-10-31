# Tasks: struts1-starter-migration

## 1. Parent pom: Boot version and starter module

- [x] 1.1 Pin the Spring Boot 4.0.x version (current patch of the 4.0 line) in the parent pom, import the `spring-boot-dependencies` BOM into `dependencyManagement`, align `spring.version` with the Boot-managed version; `./mvnw -pl spring-struts1-core -am verify` green after alignment
- [x] 1.2 Add the `spring-struts1-starter` module to `<modules>` and create the module skeleton: pom with dependencies on `spring-struts1-core`, optional `spring-struts1-taglib`, `spring-boot-autoconfigure`, provided `jakarta.servlet-api`; `./mvnw -pl spring-struts1-starter -am compile` passes

## 2. Starter auto-configuration

- [x] 2.1 `Struts1Properties` (`spring.struts1.*`): `enabled` (true), `config` (`/WEB-INF/struts-config.xml`), `configModules` (Map → `config/<prefix>`), `validating` (true), `mapping.urlPattern` (`*.do`, list), `servletName` (`action`); property binding unit test (`./mvnw -pl spring-struts1-starter test`)
- [x] 2.2 `EnvironmentServletConfig` adapter in `spring-struts1-core` (implements ServletConfig): lazy init-param reads from the `Environment` (`config`, `config/<prefix>` from `spring.struts1.config.*`, `validating`, defaults `/WEB-INF/struts-config.xml` and `true`), resolution of `classpath:` resources and via `ServletContext`; unit test: default values, property overriding a default, module parameter `spring.struts1.config.admin` → `getInitParameter("config/admin")`, enumeration of parameter names
- [x] 2.3 Registrar fallback: `StrutsEngineRegistrar` implements `ServletContextAware`/`EnvironmentAware` and, when no `ServletConfig` is present, assembles the adapter itself (if a `ServletContext` is available or `spring.struts1.config` is set), resource resolution via the adapter; unit test: plain context without a ServletConfig with `spring.struts1.config=classpath:fixture` registers the types from struts-config; starter BPP `StrutsBootConfigurer`: injects the adapter into the remaining `ServletConfigAware` beans and synchronizes the `StrutsHandlerMapping` mapping from the property
- [x] 2.4 `Struts1AutoConfiguration`: import of `StrutsEngineConfiguration`, own `DispatcherServlet` and `ServletRegistrationBean` beans from `Struts1Properties` (mapping kept consistent with the engine's request handler), conditional nested `@Configuration` with `@ConditionalOnClass` for tag services; `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` file; test with `ApplicationContextRunner`: engine beans registered, with taglib on the classpath — tag services too, without taglib — startup without errors, `spring.struts1.enabled=false` disables everything, Boot's own `DispatcherServlet` registration is not duplicated
- [x] 2.5 Starter integration test on embedded Tomcat (modeled on `TomcatTestServer`): mini Boot application with the starter and struts-config in resources — a `*.do` request passes the full lifecycle populate → validation → execute → forward; path-mapped variant `/action/*` via `spring.struts1.mapping.url-pattern`

## 3. Migrating the apps/* applications to the starter

- [x] 3.1 blank: starter dependency, a `BlankApplication extends SpringBootServletInitializer` class with `@SpringBootApplication`, web.xml reduced to display-name and welcome-file-list (servlet, mapping, and init-params removed); the `BlankAppTest` integration test passes with unchanged scenarios — `./mvnw -pl spring-struts1-apps/blank verify`
- [x] 3.2 examples: the same migration; module configs `config/<prefix>` from web.xml moved to `spring.struts1.config.<prefix>` properties; `ExamplesTest` passes — `./mvnw -pl spring-struts1-apps/examples verify`
- [x] 3.3 cookbook and mailreader: the same migration; `CookbookAppTest`, `MailreaderAppTest`, and the mailreader-dao tests pass — `./mvnw -pl spring-struts1-apps/cookbook,spring-struts1-apps/mailreader,spring-struts1-apps/mailreader-dao verify`
- [x] 3.4 Verify classic mode is not broken: `spring-struts1-core` tests (manual engine configuration) pass — `./mvnw -pl spring-struts1-core verify`

## 4. Annotated demo application

- [x] 4.1 Module `spring-struts1-apps/demo` (artifact `spring-struts1-demo`, packaging war, executable via `spring-boot-maven-plugin`): dependencies on the starter, taglib, `spring-boot-starter-webmvc`, `tomcat-embed-jasper`; an annotated `@StrutsAction` (singleton, `@Autowired` dependency) and `@StrutsForm` (prototype), struts-config.xml, JSPs, message-resources — `./mvnw -pl spring-struts1-apps/demo package` produces an executable WAR
- [x] 4.2 Integration test deploying the WAR to `TomcatTestServer`: page renders, form is filled and validated (errors → input), locale-based messages, singleton action across requests, a fresh form per request — `./mvnw -pl spring-struts1-apps/demo verify`
- [x] 4.3 Boot-run smoke test: `SpringApplication` on a random port with docBase `src/main/webapp` (equivalent to `java -jar`) — page, form, and message accessible

## 5. Guide, matrix, publishing

- [x] 5.1 `docs/MIGRATION.md`: migration steps (artifact swap, starter, application class, properties, annotations), typical adjustments (jasper, static resources, jar/WAR JSP limitation), link to the demo; verification — the demo migration is reproducible strictly by following the guide's steps
- [x] 5.2 Compatibility matrix in README: Boot 4.0.x, Java 17+, Spring Framework (aligned version), Tomcat 11 / jakarta servlet 6.1, module and artifact list; update the project structure section
- [x] 5.3 `publication` profile in the parent pom (`distributionManagement` from properties `-Dpublication.release.url` etc.) and a "Publishing" section in README; verification — `./mvnw -Ppublication deploy -Dpublication.release.url=file:target/pubrepo -DskipTests` publishes all modules, and a test consumption of the artifacts resolves

## 6. Final integration

- [x] 6.1 Full `./mvnw verify` run green across all modules, including the starter and demo
- [x] 6.2 Check double servlet registration and starter disabling on a real application: `spring.struts1.enabled=false` on a blank Boot run — the engine does not start, the application does not fail; when enabled — a single `DispatcherServlet`

## Workflow follow-up

- OpenSpec: update task statuses during implementation (`openspec status --change struts1-starter-migration`), finish with `openspec archive` and the phase 4 commit.
- Record in README/ROADMAP the resolution of phase 4 open questions: publishing channel (documented as external configuration), Boot version matrix (4.0.x declared).
