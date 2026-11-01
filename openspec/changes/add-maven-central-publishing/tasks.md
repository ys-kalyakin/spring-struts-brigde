# Tasks

## 1. Group rename

- [x] 1.1 Rename the group to `io.github.ys-kalyakin.springstruts` in the root pom and all 11 module poms (`parent` blocks, internal dependencies, demo poms). Verify: `grep -rn "org.springstruts" --include="pom.xml" .` returns nothing and `./mvnw -q validate` resolves the reactor.
- [x] 1.2 Update consumer-facing coordinates in README, `docs/getting-started.md`, `docs/modules.md`, `docs/MIGRATION.md`. Verify: `grep -rn "org.springstruts" README.md docs/` returns nothing (openspec archives are left as historical record).
- [x] 1.3 Full build with quality gates on the renamed reactor. Verify: `./mvnw -B -ntp verify` passes (tests, Checkstyle, SpotBugs).

## 2. Root pom publication metadata

- [x] 2.1 Add `<url>`, Apache 2.0 `<licenses>`, `<developers>` and `<scm>` (connection/developerConnection/url, repository name `spring-struts-brigde`) to the root pom, and fix the stale comment referencing the README publication section. Verify: `./mvnw -B -q org.apache.maven.plugins:maven-help-plugin:3.5.1:evaluate -Dexpression=project.url` (and `project.scm.url`) print the expected values.

## 3. Publishing toolchain in the build

- [x] 3.1 Add `central-publishing-maven-plugin` to the root build with `publishingServerId=central` and the explicit `excludeArtifacts` list (`spring-struts1-apps`, `spring-struts1-blank`, `spring-struts1-examples`, `spring-struts1-cookbook`, `spring-struts1-mailreader-dao`, `spring-struts1-mailreader`, `spring-struts1-demo`, `spring-struts1-benchmarks`). Verify: `./mvnw -B -ntp -DskipTests package` shows the plugin bound in the reactor log and no exclusion entry misses an existing demo artifactId.
- [x] 3.2 Add `maven-source-plugin` (jar-no-fork on package) in root `pluginManagement`, activate it in `core`, `taglib`, `tiles`, `starter` only. Verify: `./mvnw -B -ntp -pl spring-struts1-core,spring-struts1-taglib,spring-struts1-tiles,spring-struts1-starter package` produces `*-sources.jar` in each of the four `target/` dirs and none in demo/benchmark targets.
- [x] 3.3 Add `maven-javadoc-plugin` (jar goal, doclint disabled for the ported legacy HTML — see design D7) in root `pluginManagement`, activate it in the same four modules. Verify: the same build produces `*-javadoc.jar` in the four targets and the build does not fail on javadoc warnings.
- [x] 3.4 Remove the `publication` profile from the root pom. Verify: `./mvnw -B help:effective-pom | grep -c "publication-release"` prints `0`.
- [x] 3.5 Full build with quality gates on the toolchain change. Verify: `./mvnw -B -ntp verify` passes.
- [x] 3.6 Add `maven-gpg-plugin` in root `pluginManagement` and a `release` profile activating it (portal-side signing unavailable — fallback per design D3). Verify: `./mvnw -B -N help:effective-pom -Prelease` shows the plugin activation (`sign-artifacts` execution appears twice vs once in pluginManagement-only without the profile), so everyday builds and `release:prepare` need no GPG.

## 4. CI workflows and release plugin

- [x] 4.1 Append a `publish-snapshots` job to `ci.yml`: `needs: build`, runs `./mvnw -B -ntp deploy` with `CENTRAL_TOKEN`, guarded so it executes only when the project version ends in `-SNAPSHOT`, with its own concurrency key and timeout. Verify: workflow YAML parses (`actionlint` or `gh workflow view`) and the job is skipped for a non-SNAPSHOT version locally via `mvn deploy -Dskip`-style dry check.
- [x] 4.2 Add `release.yml` triggered on `push: tags: v*`: full `./mvnw -B -ntp verify` then `deploy` with `CENTRAL_TOKEN`, minimal permissions, timeout. Verify: `actionlint`/`gh workflow view` parses both workflows and the trigger filter matches `v0.1.0` but not `0.1.0`.
- [x] 4.3 Configure `maven-release-plugin` in the root pom (`tagNameFormat v@{project.version}`). Verify: `./mvnw -B -q release:prepare -DdryRun=true` completes on a clean tree and proposes tag `v0.1.0`.

## 5. Publishing documentation

- [x] 5.1 Write the README "Publishing" section: published coordinates and module list, how to consume from Maven Central (including the snapshot repository URL and when it changes), snapshot policy, and the release runbook (`release:prepare` → CI publishes the tag → verify on central.sonatype.com). Verify: the section exists at the anchor the root pom comment references and matches design decisions D1–D6.

## 6. Portal setup, smoke publication and first release

- [x] 6.1 Generate a Central Portal user token and add it as the `CENTRAL_USERNAME`/`CENTRAL_TOKEN` repository secrets. Verify: the secrets are listed in repository settings (manual check).
- [x] 6.2 Generate a GPG key pair (per design D3 fallback: the Portal has no server-side signing): `gpg --full-generate-key`, publish the public key to `keyserver.ubuntu.com` / `keys.openpgp.org`, and add the repository secrets `GPG_PRIVATE_KEY` (output of `gpg --armor --export-secret-keys <keyid>`) and `GPG_PASSPHRASE`. Verify: the secrets are listed in repository settings and `gpg --keyserver keyserver.ubuntu.com --recv-keys <keyid>` returns the public key (manual checks).
- [ ] 6.3 Snapshot smoke test: push to `main`, confirm the `publish-snapshots` job publishes `io.github.ys-kalyakin.springstruts:*:0.1.0-SNAPSHOT`. Verify: the five artifacts are resolvable from the Central Portal snapshot repository.
- [ ] 6.4 Cut the first release (maintainer confirms timing): `./mvnw -B release:prepare`, confirm the `v0.1.0` CI run publishes. Verify: `io.github.ys-kalyakin.springstruts:spring-struts1-starter:0.1.0` (with sources and javadoc classifiers) is visible on central.sonatype.com and resolves in a fresh local build.

## Workflow follow-up

- After implementation is verified, archive this change (`/opsx:archive`) so the `artifact-publishing` capability and the `ci-pipeline`/`docs-site` modifications land in the main specs.
- Watch the Central Portal validation feedback for the first release bundle; record any signing/snapshot fallback decisions in design.md as required by the spec.
