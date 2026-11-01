# Proposal

## Why

The project is a public drop-in replacement for weblegacy/struts1, but its artifacts cannot be consumed: there is no publication channel, and the coordinates use the group `org.springstruts`, which is not verifiable on Maven Central (the group implies ownership of the `springstruts.org` domain). The owner has a verified Maven Central namespace `io.github.ys-kalyakin`; at the `0.1.0-SNAPSHOT` stage a coordinate rename costs nothing, while after the first release it would be a breaking change for every consumer. In addition, the root pom comment references a README "Публикация" section that no longer exists, so the publication process is undocumented.

## What Changes

- **BREAKING** Rename the Maven group from `org.springstruts` to `io.github.ys-kalyakin.springstruts` across all 12 poms, README and `docs/`. Java packages stay `org.apache.struts.*` (drop-in compatibility is unchanged).
- Add Maven Central validation metadata to the root pom: project `url`, `licenses` (Apache 2.0), `developers`, `scm` (existing repository `github.com/ys-kalyakin/spring-struts-brigde` kept as is — no repository rename).
- Wire the publication toolchain into the build: `maven-source-plugin` and `maven-javadoc-plugin` attach sources and javadoc jars for the four published library modules; `central-publishing-maven-plugin` becomes the publishing channel to the Central Portal, with signing done by the Central Portal (server-side key pair; no GPG plugin in the build).
- Publish exactly the library family: `spring-struts1-parent` (pom), `spring-struts1-core`, `spring-struts1-taglib`, `spring-struts1-tiles`, `spring-struts1-starter`. Demo and tooling artifacts (`spring-struts1-apps` and its six submodules, `spring-struts1-benchmarks`) are excluded from publication.
- Release flow via `maven-release-plugin`: `release:prepare` performs the version bump and tags `v<version>`; the tagged commit is published by CI. Releases are immutable on Maven Central.
- Snapshot flow: every successful push to `main` publishes `-SNAPSHOT` artifacts to the Central Portal snapshot repository via a CI job guarded by the `CENTRAL_TOKEN` secret.
- Remove the `publication` profile from the root pom: the external-URL `distributionManagement` design is superseded by the Central Portal plugin; credentials live in CI secrets / local `settings.xml`, not in the build.
- Document publication: restore a "Publishing" section in the README (the pom comment references it), covering coordinates, repositories, snapshot policy and the release runbook.

## Capabilities

### New Capabilities
- `artifact-publishing`: publication of the library artifacts to Maven Central — published coordinates and module set, Central validation metadata, mandatory sources/javadoc artifacts, signing, the release flow through `maven-release-plugin` and tag-triggered CI publishing, the snapshot flow on every push to `main`, and the publishing documentation.

### Modified Capabilities
- `ci-pipeline`: the run-safety requirement currently forbids secrets and external services for the workflow; publishing jobs on `main` and on `v*` tags legitimately need the `CENTRAL_TOKEN` secret and reach the Central Portal. The verification job keeps its secret-free contract.
- `docs-site`: the getting-started page and the modules overview reference the `org.springstruts` group; both requirements change to the new group id so consumer-facing instructions show the real published coordinates.

## Impact

- **Build**: root pom (metadata, plugins, profile removal) and all 11 module poms (groupId); Checkstyle/SpotBugs gate stays in place for the renamed build.
- **CI**: `ci.yml` gains a snapshot-publishing job on `main`; new `release.yml` publishes on `v*` tags; both use the `CENTRAL_TOKEN` secret.
- **External services**: Maven Central Portal — the `io.github.ys-kalyakin` namespace (already verified and owned), a user token stored as the `CENTRAL_TOKEN` secret, and a portal-generated signing key pair.
- **Consumers**: dependency coordinates change to `io.github.ys-kalyakin.springstruts`; acceptable now because no version has ever been published.
- **Docs**: README and `docs/getting-started.md`, `docs/modules.md`, `docs/MIGRATION.md` show the new coordinates; the README gains the Publishing runbook section.
