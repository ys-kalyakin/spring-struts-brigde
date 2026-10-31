# Tasks

## 1. Write the English README

- [x] 1.1 Replace `README.md` with the English opening: title, CI badge (URL copied verbatim from the current README, keeping the `brigde` repo spelling), the "What is it" description paragraphs, and the "Key principles" bullets, following design.md Decision 2 (sections 1–3). Verify: rendered markdown is valid and the badge link target matches `.github/workflows/ci.yml` on the remote repo.
- [x] 1.2 Add "Compatibility" and "Modules" sections: the version table (Java 17+, Boot 4.0.x / Framework 7.0.x, embedded Tomcat 11 / `jakarta.servlet` 6.1, weblegacy 1.5.x parity) and the `org.springstruts` artifact table, plus the one-sentence note on ported test apps and the `demo` example. Verify: each version row against the root pom / starter code, each artifact row against the actual module names.
- [x] 1.3 Add the "Getting started" section: Maven dependency snippets for `spring-struts1-starter` (plus `-taglib` / `-tiles` as needed) at version `0.1.0-SNAPSHOT`, the `spring.struts1.config=classpath:struts-config.xml` property, a sentence on `@StrutsAction` / `@StrutsForm`, and links to `docs/MIGRATION.md` and `spring-struts1-apps/demo`. Verify: snippet group/artifact/version match `spring-struts1-apps/demo/pom.xml` and the property matches the demo's `application.properties`; both links resolve to existing files.
- [x] 1.4 Add the condensed "Notes" section: form population runs on Spring `ConversionService` with default commons-beanutils semantics (fixture-tested); converters registered by the app in `ConvertUtils` do not affect form population; commons-beanutils stays a dependency for the `DynaBean` hierarchy and the validator. Verify: statements consistent with the current README's populate paragraph and the `form-populate` spec in `openspec/specs/`.

## 2. Removal and final checks

- [x] 2.1 Confirm the dropped content is gone: no references to ROADMAP.md, the OpenSpec workflow (`/opsx:`, `openspec/`), the project-structure tree, `./mvnw verify`, or the publication/deploy channel remain in `README.md`. Verify: `grep -inE 'roadmap|opsx|openspec|mvnw|publication|deploy' README.md` returns no content hits.
- [x] 2.2 Read the finished README end-to-end as an external consumer: English throughout (no Russian remnants), no maintainer-internal content, and every internal link resolves. Verify: manual read-through plus a link/path existence check for all relative links in the file.

## Workflow follow-up

- Archive the change after the project's review requirements are satisfied.
