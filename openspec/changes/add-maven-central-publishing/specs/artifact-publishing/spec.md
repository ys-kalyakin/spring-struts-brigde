# Spec Delta

## Purpose

Publishes the library artifacts to Maven Central so that external projects can consume the drop-in Struts 1 replacement by ordinary Maven coordinates: a fixed published module family under the verified `io.github.ys-kalyakin.springstruts` group, with releases cut through the maven-release-plugin flow and `-SNAPSHOT` builds refreshed on every push to `main`.

## ADDED Requirements

### Requirement: Published coordinates
The library artifacts SHALL be published under the group `io.github.ys-kalyakin.springstruts` with the artifact ids `spring-struts1-parent` (pom), `spring-struts1-core`, `spring-struts1-taglib`, `spring-struts1-tiles` and `spring-struts1-starter`. The parent pom SHALL be published together with the modules, so a consumer resolving any published module resolves its parent from Maven Central as well.

#### Scenario: Consumer dependency declaration
- **WHEN** a consumer declares a dependency on `io.github.ys-kalyakin.springstruts:spring-struts1-starter` from Maven Central
- **THEN** the artifact, its pom and the parent pom are resolvable from Maven Central without extra repositories

#### Scenario: Coordinates consistency across the build
- **WHEN** the reactor builds any module
- **THEN** every pom in the project uses the group `io.github.ys-kalyakin.springstruts` and no pom, build file or documentation references the former `org.springstruts` group

### Requirement: Published module set
Exactly the library family SHALL be published: the parent pom and the `spring-struts1-core`, `spring-struts1-taglib`, `spring-struts1-tiles`, `spring-struts1-starter` jars. The demo applications (`spring-struts1-apps` and its submodules `spring-struts1-blank`, `spring-struts1-examples`, `spring-struts1-cookbook`, `spring-struts1-mailreader-dao`, `spring-struts1-mailreader`, `spring-struts1-demo`) and the benchmarks module (`spring-struts1-benchmarks`) SHALL NOT be published.

#### Scenario: Publishing run
- **WHEN** a publishing run completes for a release or snapshot version
- **THEN** Maven Central contains exactly the five library artifacts of that version and none of the demo, dao or benchmark artifacts

### Requirement: Central validation metadata
The root pom SHALL carry the metadata Maven Central validates: the project `url`, an Apache 2.0 `licenses` entry, a `developers` entry and an `scm` section pointing at the existing GitHub repository (`github.com/ys-kalyakin/spring-struts-brigde`, kept under its current name).

#### Scenario: Publication validation
- **WHEN** a publishing run is validated by Maven Central
- **THEN** the pom metadata (url, license, developers, scm) passes validation and the publication is not rejected for missing metadata

### Requirement: Mandatory artifact kinds
Each published library module SHALL publish, in addition to the main jar, a sources jar and a javadoc jar of the same version. Javadoc generation SHALL succeed on the project's JDK without failing the build on doclet warnings.

#### Scenario: Downloading developer artifacts
- **WHEN** a consumer or IDE fetches the sources or javadoc classifier of any published library artifact
- **THEN** a jar of the same version is available on Maven Central

### Requirement: Artifact signing
Every published artifact SHALL be cryptographically signed before it reaches Maven Central. Signing SHALL be performed by the Central Portal with a portal-generated signing key pair, so no signing key material is stored in the repository, the build or CI secrets. If portal-side signing proves unavailable, signing SHALL fall back to GPG with the key supplied as a CI secret, and this fallback decision SHALL be recorded in the change design.

#### Scenario: Verifying a published artifact
- **WHEN** a consumer or auditor verifies the signature of a published artifact against the publisher's public key
- **THEN** the signature is valid for the artifact, its sources jar, its javadoc jar and its pom

### Requirement: Release flow
Releases SHALL be produced with the maven-release-plugin flow: `release:prepare` runs the full verification build, sets the release version, commits and pushes the tag `v<version>` and resets the working version to the next `-SNAPSHOT`. The tagged commit SHALL be published to Maven Central by CI automatically when the tag lands, and a released version SHALL be immutable — no re-publication of the same version.

#### Scenario: Cutting a release
- **WHEN** a maintainer runs `release:prepare` on a clean `main` with all checks passing
- **THEN** the project version becomes `v<version>` tag plus a bumped next development version, and CI publishes the tagged commit to Maven Central

#### Scenario: Version immutability
- **WHEN** a publishing run attempts to publish a version that already exists on Maven Central
- **THEN** the publication fails and the existing release artifacts remain unchanged

### Requirement: Snapshot flow
Every successful build of `main` SHALL publish the current `-SNAPSHOT` versions of the published module set to the Central Portal snapshot repository. Release versions SHALL NOT be published to the snapshot repository. A failing build SHALL NOT update the snapshots.

#### Scenario: Push to main
- **WHEN** a push to `main` passes the full verification build
- **THEN** the `-SNAPSHOT` artifacts of the five library artifacts are refreshed in the Central Portal snapshot repository

#### Scenario: Failed build
- **WHEN** the verification build of a push to `main` fails
- **THEN** no snapshot artifact is updated

### Requirement: Publishing documentation
The README SHALL contain a "Publishing" section that documents the published coordinates, how to add the library from Maven Central, the snapshot policy and the step-by-step release runbook. The root pom comment that references the publication documentation SHALL point to this section, and the section SHALL exist.

#### Scenario: Following the release runbook
- **WHEN** a maintainer follows the README "Publishing" section without prior knowledge
- **THEN** they can cut and publish a release using only the documented steps and the repository's documented secrets

#### Scenario: Looking up coordinates
- **WHEN** a reader opens the README section on publishing
- **THEN** they see the current group id and the list of published artifacts matching the published module set
