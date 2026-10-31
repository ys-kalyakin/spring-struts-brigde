# Tasks

## 1. ROADMAP.md Document

- [x] 1.1 Create `ROADMAP.md` at the root: sections "Vision", "Compatibility Principles" (the `org.apache.struts.*` packages, `struts-config.xml`, tag libraries, the jakarta stack), "Target Stack" (Spring Boot 4.x / Framework 7, Java 17+, Tomcat 11 — with a note to verify at the start of phase 1); verify that the file exists and the sections are present
- [x] 1.2 Describe phases 1–4, each with: scope of work, target weblegacy application, acceptance criteria (including core behavioral scenarios: config parsing, `*.do` routing, the populate → validate → execute → forward lifecycle, forwards, message-resources, exceptions, actions/forms as Spring beans, fallback compatibility, PlugIn), a recommended name for the future change, and open questions; verify that each phase contains all elements
- [x] 1.3 Include in the roadmap the "phase → change" mapping (`struts1-engine-core`, `struts1-taglibs`, `struts1-tiles-validation`, `struts1-starter-migration`) and the rule that a phase may be split into several changes; verify that the names do not clash with existing ones (`openspec list`)

## 2. README

- [x] 2.1 Supplement `README.md`: the project goal (Struts 1 on a Spring engine, a drop-in replacement), a link to `ROADMAP.md`, a link to the weblegacy/struts1 reference; verify that the links are valid and the text reads well

## 3. Consistency

- [x] 3.1 End-to-end consistency check: the phase scopes in `ROADMAP.md` match proposal.md, and the acceptance criteria cover all scenarios of the former spec deltas; verify by reading both documents
- [x] 3.2 Run `openspec validate spring-struts1-foundation`; verify that validation passes

## Workflow follow-up

- After the tasks are complete, sync and archive the change (`/opsx:sync`, `/opsx:archive`).
- Kick off phase 1 implementation as a new change (`openspec new change "struts1-engine-core"` or `/opsx:propose`).
