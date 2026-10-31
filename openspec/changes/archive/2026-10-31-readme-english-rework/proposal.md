# Proposal

## Why

The README is the project's front page, but it is written in Russian and is oriented at the project's own developers rather than at prospective users: it links a deleted ROADMAP.md, references the internal OpenSpec workflow, and spends most of its length on the project tree, build commands, and publication channel. An external user cannot determine from it what the library does, whether their stack is supported, or how to wire it in. The project has reached its first usable state (starter, migration guide, demo app), so the front page should now sell and onboard in English.

## What Changes

- Rewrite `README.md` entirely in English (full replacement, no Russian version retained).
- Keep and translate the consumer-valuable content:
  - one-paragraph description (what the bridge is, compatibility reference weblegacy/struts1 1.5.x `jakarta.*`);
  - key principles (same `org.apache.struts.*` packages, `struts-config.xml` stays the config source, classic taglib URIs/TLDs);
  - compatibility table (Java 17+, Spring Boot 4.0.x / Framework 7.0.x, embedded Tomcat 11, `jakarta.servlet` 6.1);
  - modules & artifacts table for group `org.springstruts`.
- Add a missing "Getting started" section that shows how to connect: Maven dependency on `spring-struts1-starter` (plus `spring-struts1-taglib` / `spring-struts1-tiles` as needed), the minimal `spring.struts1.config` property, a pointer to `@StrutsAction` / `@StrutsForm`, and links to `docs/MIGRATION.md` and the `spring-struts1-apps/demo` example.
- Remove developer-internal content:
  - link to ROADMAP.md (file no longer exists — the link is broken);
  - OpenSpec workflow references (`/opsx:*`, `openspec/` directory listing);
  - project structure tree and the `./mvnw verify` build instruction;
  - the whole "Publication" section (maintainer-facing deploy channel configuration);
  - the CI badge is kept only if verified to render for the public repo, otherwise dropped.
- Condense to one or two sentences each: the form-populate conversion semantics note and the ported test-applications paragraph.

## Capabilities

### New Capabilities

None. This is a documentation-only change; no runtime behavior of the framework is affected.

### Modified Capabilities

None. The change's `.openspec.yaml` declares `skip_specs: true` because no spec-level behavior changes — all existing capabilities describe framework behavior, and the README rework touches none of it.

## Impact

- Files: `README.md` only.
- No code, API, dependency, build, or CI changes.
- Risk: none functional; the only care point is factual accuracy of the kept content (versions, artifact names, property names) against the actual poms and starter code.
