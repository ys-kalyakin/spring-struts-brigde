# Design

## Context

The change has been re-scoped: it delivers only a roadmap document; implementation will proceed via separate changes (see proposal.md). The technical decisions for the future engine (DispatcherServlet + HandlerMapping/HandlerAdapter, commons-digester for parsing, commons-beanutils for populate, the Spring Boot 4.0.x / Framework 7 stack) move out of this change into the roadmap and into the specs/design of the corresponding phase changes — here they are captured only as the content of the document.

## Goals / Non-Goals

**Goals:**
- `ROADMAP.md` at the repository root, detailed enough that each phase can be turned into a separate OpenSpec change without renewed investigation.

**Non-Goals:**
- Any implementation (code, build, dependencies) — outside this change.

## Decisions

**D1. Location and language.** `ROADMAP.md` at the repository root, in Russian (the project's working language; README remains the entry point with a link).
*Alternative:* `docs/ROADMAP.md` — for a single document the root is simpler; move it to `docs/` if the documentation grows.

**D2. Document structure.** Vision → compatibility principles (the `org.apache.struts.*` packages, `struts-config.xml` as the configuration source, tag libraries with the same TLD/URIs, the jakarta stack) → target stack → phases 1–4. Each phase: scope, target weblegacy application, acceptance criteria (these include the core behavioral scenarios from the spec deltas removed when the change was re-scoped), a recommended name for the future change, and known risks.

**D3. Phase-to-change mapping.** A phase maps to one or more small changes; the base mapping is: phase 1 → `struts1-engine-core`, phase 2 → `struts1-taglibs`, phase 3 → `struts1-tiles-validation`, phase 4 → `struts1-starter-migration`. A phase may be split if it turns out to be too large at kickoff — that decision is made when the phase's change is created, not in the roadmap.

**D4. Fate of the removed spec deltas.** The requirements of `struts-request-processing` and `struts-spring-integration` (request lifecycle, action/form beans, fallback compatibility, PlugIn) are not discarded but included in the acceptance criteria of phase 1 of the roadmap; when the phase change is created, they will become its spec deltas.

## Risks / Trade-offs

- [The roadmap will drift out of sync with the reality of the phase changes] → formulate phase acceptance criteria as observable behavior (per scenarios) rather than implementation details; sync the roadmap when a phase change is revised.
- [A document in Russian will limit external contributors] → defer an English translation until the need arises; the document structure is translation-friendly.

## Open Questions

None — the questions recorded earlier (the exact minor version of Boot 4, the scope of the ActionServlet init-param contract, populate via beanutils vs ConversionService, path-mapped actions) have been moved into the roadmap as open questions of the corresponding phases.
