# Design

## Context

`README.md` is currently the only top-level project documentation besides `docs/MIGRATION.md`. Its content inventory: Russian text; CI badge; description; key principles; links to ROADMAP.md (broken — the file was deleted in commit `8a1f671`), the OpenSpec workflow, MIGRATION.md and the demo app; compatibility table; populate-semantics paragraph; modules table; test-apps paragraph; project-structure tree; build command; publication section. Motivation and scope are in `proposal.md` (Why / What Changes).

Facts verified against the repo that constrain the writing:

- Git remote is `ys-kalyakin/spring-struts-brigde` — the badge URL's `brigde` spelling matches the real repository name and must be preserved verbatim.
- Current version: `0.1.0-SNAPSHOT` (root pom), group `org.springstruts`, modules `spring-struts1-core`, `-taglib`, `-tiles`, `-starter`.
- Demo app wiring (the model for "how to connect"): dependencies `spring-struts1-starter` + `spring-struts1-taglib`, property `spring.struts1.config=classpath:struts-config.xml`, `@StrutsAction` / `@StrutsForm` annotations, `tomcat-embed-jasper` for JSP, executable WAR.
- `docs/MIGRATION.md` exists and is the detailed onboarding path.

## Goals / Non-Goals

**Goals:**

- A single English `README.md` targeted at an external evaluator/consumer of the library.
- A target outline fixed before writing, so the content decisions are reviewable (see Decisions).
- Every factual claim in the new README verifiable against the repo (poms, starter properties, docs).

**Non-Goals:**

- No new documentation files (no `README.ru.md`, no architecture doc).
- No changes to `docs/MIGRATION.md`, the OpenSpec tree, CI, or build configuration.
- No publication/deploy documentation — that channel is maintainer-facing and configured in pom profiles; it stays out of the front page.

## Decisions

1. **Full replacement, English only.** Rewrite `README.md` in place; no Russian copy is kept. A bilingual pair would double the maintenance surface, and the user asked for an English README. Alternative considered (side-by-side translation) — rejected.

2. **Target outline.** The new README has exactly these sections, in order:
   1. Title + CI badge (badge URL unchanged, `brigde` spelling preserved);
   2. **What is it** — 1–2 paragraphs: recreation of Struts 1 on modern Spring; migrate by swapping the dependency, adding the Boot starter, and annotating forms/actions; reference implementation weblegacy/struts1 1.5.x (`jakarta.*`) as the compatibility yardstick;
   3. **Key principles** — the three existing bullets, translated (packages preserved; `struts-config.xml` stays the config source; classic taglib URIs/TLDs);
   4. **Compatibility** — the existing table, translated (Java 17+, Boot 4.0.x / Framework 7.0.x, embedded Tomcat 11 / `jakarta.servlet` 6.1, weblegacy 1.5.x parity);
   5. **Modules** — the `org.springstruts` artifact table, translated; one trailing sentence noting the ported weblegacy apps serve as integration tests and `demo` is the migration example;
   6. **Getting started** — new section: Maven dependency snippets (`spring-struts1-starter`, plus `-taglib` / `-tiles` as needed), the minimal `spring.struts1.config` property, a sentence on `@StrutsAction` / `@StrutsForm`, and links to `docs/MIGRATION.md` and `spring-struts1-apps/demo`;
   7. **Notes** — the populate/conversion paragraph condensed to 1–2 sentences: populate runs on Spring `ConversionService` with default commons-beanutils semantics (fixture-tested); app-registered `ConvertUtils` converters do not affect form population; commons-beanutils remains a dependency for the `DynaBean` hierarchy and the validator.

3. **Dropped content** (per the user's "only description and how to connect"): ROADMAP.md link (also broken), OpenSpec workflow references, project-structure tree, `./mvnw verify` build instruction, the entire Publication section. The deploy-channel knowledge remains in the pom `publication` profile, so nothing operational is lost.

4. **Version in dependency snippets.** Show `0.1.0-SNAPSHOT` — the only version that exists in the repo — with the dependency snippets written so the version is in one obvious place. No repository-URL instructions are included (that was the removed publication content); a consumer wiring their own Maven repository knows how to resolve the artifacts. Alternative considered (`<version>` placeholder) — rejected as less informative.

5. **Getting started stays shallow.** The section shows coordinates + one property + one annotation mention and defers everything else to `docs/MIGRATION.md` and the demo app. Duplicating the migration guide in the README would recreate exactly the bloat being removed. If MIGRATION.md's heading anchors are stable, link to its first step; otherwise link the file plainly.

## Risks / Trade-offs

- **Factual drift** (versions, artifact names, property names copied wrong) → during apply, cross-check the table and snippets against the root pom, the demo pom, and `application.properties` before finishing.
- **Losing internal-process discoverability** (OpenSpec, build commands) → accepted trade-off; those live in `openspec/`, `.github/workflows/`, and git history, and the audience for them is the maintainer.
- **Badge renders against the misspelled repo name** → intentional; renaming the repo is out of scope and changing the URL would break the badge.
- **Anchor rot** in the MIGRATION.md deep link → prefer a plain file link if anchors look fragile.

## Migration Plan

Single commit replacing `README.md`; rollback is `git revert`. No other artifact depends on the README's current content.

## Open Questions

None.
