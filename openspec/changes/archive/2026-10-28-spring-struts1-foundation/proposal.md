# Proposal

## Why

The strategic goal of the project is to recreate Struts 1 on top of modern Spring as a drop-in replacement (see the motivation below), but implementing the entire framework is too large a unit of work for a single change. Before splitting the work into a series of small implementation changes, a **single detailed roadmap** is needed in the repository: it fixes the scope of each phase, the target weblegacy test applications, the acceptance criteria, and the "phase → future change" mapping. The roadmap becomes the foundation from which subsequent OpenSpec changes are derived.

## Project Motivation (Roadmap Context)

Struts 1 has been EOL since 2013, and many legacy applications cannot move to a modern stack without rewriting the web layer. The goal is to implement the Struts 1 API on a Spring engine so that an application migrates by swapping the dependency and adding the starter, plus minimal edits (annotations on forms/actions to make them Spring beans). The reference is [weblegacy/struts1](https://github.com/weblegacy/struts1) (the 1.5.x line, already on `jakarta.*`): its API contracts and test applications (`apps/blank`, `apps/examples`, `apps/cookbook`, `apps/mailreader`) are the compatibility criterion.

## What Changes

- A `ROADMAP.md` document is created at the repository root (in Russian — the project's working language): vision, key compatibility principles, and detailed phases.
- Each roadmap phase contains: the scope of work, a target weblegacy test application, acceptance criteria (including core behavioral scenarios: parsing `struts-config.xml`, routing `*.do`, the populate → validate → execute → forward lifecycle, forwards, message-resources, exceptions; actions/forms as Spring beans), and a recommended name for the future OpenSpec change.
- `README.md` is supplemented with the project goal and a link to the roadmap.
- Implementation of the phases is **out of scope** for this change: each phase will be delivered as a separate small OpenSpec change with its own spec deltas and tasks.

## Capabilities

### New Capabilities
(none — the change modifies documentation only)

### Modified Capabilities
(none)

This change is purely documentation; system behavior does not change, so `skip_specs: true` is set in `.openspec.yaml`.

## Impact

- **Files:** new `ROADMAP.md`, updated `README.md`. Code, dependencies, and the build are not affected.
- **Follow-up changes:** implementation changes per phase (tentatively: `struts1-engine-core` — phase 1, `struts1-taglibs` — phase 2, `struts1-tiles-validation` — phase 3, `struts1-starter-migration` — phase 4); the names are fixed in the roadmap.
- **Reference:** a local clone of weblegacy/struts1 (`/home/helloween/projects/struts1-reference`, v1.5.0-rc2) — the source of contracts and test applications.
