# Design

## Context

The reactor (`0.1.0-SNAPSHOT`, Maven 3.9.9 wrapper, JDK 17) builds six modules; four are library jars (`core`, `taglib`, `tiles`, `starter`), `spring-struts1-apps` is a `pom` aggregator of six demo modules (five WARs + one dao jar), `spring-struts1-benchmarks` is a JMH jar. The root pom carries a `publication` profile that feeds `distributionManagement` from external properties (`-Dpublication.release.url` / `-Dpublication.snapshot.url`) — a deliberately repository-agnostic design with no configured channel and no CI wiring. CI (`ci.yml`) runs `verify` (tests, Checkstyle, SpotBugs) with `contents: read` and no secrets; `pages.yml` deploys mkdocs. All Spring/Boot dependencies are release versions (no external SNAPSHOTs), so `release:prepare` is unblocked. The owner has a verified Central Portal namespace `io.github.ys-kalyakin` with one previously published artifact.

## Goals / Non-Goals

**Goals:**
- One publishing channel — Maven Central via the Central Portal — wired end-to-end: build, credentials, CI automation, documentation.
- Coordinates frozen before the first release so consumers never see a rename.
- Release and snapshot publication fully automated from the maintainer's perspective: prepare a release locally, everything else happens in CI.

**Non-Goals:**
- No second publication channel (GitHub Packages, JitPack) — can be added later if a need appears.
- No fully-CI release cutting (`workflow_dispatch` driving `release:prepare`) — the maintainer runs `release:prepare` locally; revisiting later is cheap.
- No repository rename; `scm` freezes the current name (user decision).
- No publishing of demo WARs, dao or benchmarks; no BOM artifact beyond the parent pom.

## Decisions

### D1. Group id: `io.github.ys-kalyakin.springstruts`
Sub-group under the verified namespace rather than the flat `io.github.ys-kalyakin`: it separates this project family from the owner's other artifacts (the existing `gradle-pull-dependencies-plugin`) and follows the Central Portal recommendation of one sub-namespace per project. Keeping `org.springstruts` was rejected — it requires owning and permanently verifying the `springstruts.org` domain. Java packages stay `org.apache.struts.*`; the rename is build-level only (12 poms, README, `docs/*.md` — 53 references).

### D2. Publishing channel: `central-publishing-maven-plugin`, `publication` profile removed
The namespace lives in the Central Portal; legacy OSSRH `distributionManagement` endpoints are closed to new accounts. The plugin attaches to the `deploy` phase: `-SNAPSHOT` versions go to the Portal snapshot repository, release bundles are uploaded for portal validation and publishing. The existing `publication` profile (external `distributionManagement` URLs) is removed — its flexibility assumed channels we deliberately do not support (D: Non-Goals). Credentials move to the CI secret and a local `settings.xml` server entry (id `central`), never into the pom. Alternative (keep the profile for hypothetical extra channels) rejected as YAGNI; re-adding a profile later is trivial.

### D3. Signing: GPG with CI-secrets keys (fallback adopted — recorded per the spec)
The original primary choice — Central Portal server-side signing with a portal-generated key pair — turned out to be unavailable: as of implementation, the official Central documentation (`central.sonatype.org/publish/requirements/gpg/`) describes only self-managed GPG keys, no "Generate Signing Key" feature exists in the Portal UI, and no Sonatype announcement of such a feature could be verified. The spec's fallback branch is therefore adopted: `maven-gpg-plugin` (3.2.8) signs artifacts in the `release` profile (so everyday builds and `release:prepare`'s verification run need no GPG), the CI release workflow passes `-Prelease` and holds `GPG_PRIVATE_KEY` (armored secret key) and `GPG_PASSPHRASE` secrets, imported by `setup-java`. The public key must be distributed to a keyserver (`keyserver.ubuntu.com` or `keys.openpgp.org`) so signatures are verifiable. Snapshot publication (ci.yml) stays unsigned — the portal snapshot repository does not require signatures; if the snapshot smoke test proves otherwise, `-Prelease` moves there too and this decision is updated. Fallback adopted and provisioned on 2026-11-01: dedicated signing key `35B2A208C3C6FF7B` (rsa4096, sign-only primary, uid "ys-kalyakin (maven central) <ys.kalyakin@gmail.com>", expires 2029-10-31, extendable in place), public key distributed to `keyserver.ubuntu.com`, repository secrets `GPG_PRIVATE_KEY` and `GPG_PASSPHRASE` set.

### D4. Release topology: local `release:prepare`, CI publishes the tag
`./mvnw release:prepare` (default `preparationGoals` already run `verify`, so Checkstyle/SpotBugs gate the release) bumps the version, tags `v<version>` (`tagNameFormat` configured) and pushes. A new `release.yml` workflow on `push: tags: v*` re-runs full `verify` and publishes the tagged commit with `CENTRAL_TOKEN`. `release:perform` is not used: it would require the publishing credential on the developer machine. Alternatives: local `release:perform` with `settings.xml` token (secret sprawl, unreproducible environment); CI-driven `release:prepare` (more moving parts, revisitable). If a publication fails after the tag exists, the tag stays and the fix is a new `v<version+patch>` — release versions are immutable.

### D5. Snapshot publication: deploy job on `main`, gated
A job appended to `ci.yml` after the verify job succeeds runs `./mvnw -B -ntp deploy`. The Central Portal plugin routes `-SNAPSHOT` versions to the Portal snapshot repository; the job is skipped when the version does not end in `-SNAPSHOT` (protects against running on an accidentally release-versioned `main`). Concurrency group keyed to `main` prevents two publishes racing.

### D6. Excluding demo and benchmark modules: plugin `excludeArtifacts`
The published set is declared once in the root pom as an explicit exclusion list: `spring-struts1-apps`, `spring-struts1-blank`, `spring-struts1-examples`, `spring-struts1-cookbook`, `spring-struts1-mailreader-dao`, `spring-struts1-mailreader`, `spring-struts1-demo`, `spring-struts1-benchmarks`. Alternative (`maven.deploy.skip` scattered across eight module poms) rejected — one visible list beats eight hidden properties, and adding a demo module cannot silently publish.

### D7. Sources/javadoc jars: root `pluginManagement`, activated in the four library modules
`maven-source-plugin` (attached by default jar packaging rules do not run) and `maven-javadoc-plugin` executions are declared once in the root and enabled only where Central requires them: the four library jars. The parent pom (packaging `pom`) and the demo/benchmark modules attach nothing. Javadoc runs with doclint disabled — during implementation this turned out to be broader than the originally planned "ignore missing tags": the ported Struts 1 javadoc is 2001-era HTML4 (~55 doclint errors across the modules: uppercase headings, `align` attributes, tables without captions, raw `<base>`/`<html>` in text), and an HTML5-strict pass over it would be a dedicated documentation change, not part of the publishing toolchain.

### D8. Metadata and `scm` in the root pom
`<url>`, Apache 2.0 `<licenses>`, `<developers>`, and `<scm>` (connection/developerConnection/url, current repository name) are added to the root pom — they satisfy both the Central pom validation and the maven-release-plugin's scm requirement with one edit. The stale pom comment pointing to the README "Публикация" section is updated to the new English "Publishing" section.

## Risks / Trade-offs

- [Portal snapshot-repository specifics (exact plugin behavior for `-SNAPSHOT` uploads)] → Implementation includes a verification step against current plugin documentation and a live smoke publish of a snapshot before the first release; if every-push snapshots prove unsupported, the spec change would be small but must be made consciously, not silently.
- [Tag exists but publication fails (CI red on the tag)] → Portal validates the complete bundle before making it public, so partial releases are unlikely; a failed release is fixed forward with a new version (immutability), tag remains as audit trail.
- [Javadoc build fails or rots on the recreated legacy API] → `doclint` softened for missing tags only; javadoc jar production is exercised in the first snapshot publish, not discovered at release time.
- [Missed `org.springstruts` reference somewhere] → Final grep gate in tasks; `./mvnw verify` fails the build on any inconsistent groupId via reactor resolution.
- [Parallel in-flight change (`add-unsupported-config-fail-fast`) touches `ci.yml`-adjacent files? It does not touch CI; merge conflicts limited to poms it does not modify] → Land either order; rebase is mechanical.

## Migration Plan

1. Rename group + add metadata/plugins in a single commit; `./mvnw verify` locally.
2. Configure Portal: user token (repo secret `CENTRAL_TOKEN`), portal-generated signing key pair.
3. Cut `0.1.0` with `release:prepare`; watch the first CI publication end-to-end.
4. Rollback before any successful publication: revert the version-bump commit and delete the remote tag. After publication: versions are immutable, fix forward.

## Open Questions

None — the remaining unknowns (portal snapshot behavior, portal signing availability) are verification tasks inside the change, not decisions that alter the approach.
