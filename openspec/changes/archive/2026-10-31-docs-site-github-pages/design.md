# Design

## Context

The repository has English `README.md` (recently reworked), a Russian `docs/MIGRATION.md` (170 lines), and a CI workflow (`.github/workflows/ci.yml`) that only builds and tests. There is no Pages setup and no site tooling. The actual remote is `ys-kalyakin/spring-struts-brigde` (note the "brigde" spelling), so the project Pages URL is `https://ys-kalyakin.github.io/spring-struts-brigde/`. Motivation is in [proposal.md](proposal.md); behavior contracts are in the `docs-site` and `pages-deployment` specs.

## Goals / Non-Goals

**Goals:**

- A maintainable single-source Markdown site: one toolchain, sources in the repo next to the code they document.
- Correct rendering under the project sub-path (`/spring-struts-brigde/`), not the domain root.
- A secretless deployment pipeline reusing GitHub's official Pages actions.
- The Russian migration guide becomes English without forking into two divergent copies.

**Non-Goals:**

- Javadoc/API-reference publication (future change if needed).
- Versioned or localized docs (single English "latest" only).
- Any change to `ci.yml`, Maven modules, or library code.

## Decisions

### D1: MkDocs Material, `docs/` as `docs_dir`, `mkdocs.yml` at repo root

The user chose MkDocs Material (modern theme, Markdown sources, trivially scriptable in CI). `mkdocs.yml` sits at the repository root with `docs_dir: docs`, reusing the existing `docs/` directory as the site source tree. The migration guide stays exactly one file: `docs/MIGRATION.md` is translated to English in place, so it is simultaneously the site page, the file README links to, and the browsable copy on GitHub — no duplication to keep in sync.

*Alternatives considered:* Jekyll native build (no control over theme/build, slower, GitHub's default look); a separate `docs-site/` tree (would fork the migration guide or need include-plugins).

### D2: `site_url` carries the project sub-path

`site_url: https://ys-kalyakin.github.io/spring-struts-brigde/` is set explicitly. MkDocs derives the base path from it, so all asset and navigation URLs render correctly under the project path — the common breakage when a project Pages site is built as if it lived at the domain root. The misspelled repo name is intentional and verified against the git remote.

### D3: Pinned build dependencies in a root-level `requirements-docs.txt`

Exact versions of `mkdocs` and `mkdocs-material` (latest stable at implementation time) are pinned in `requirements-docs.txt` at the repository root — not inside `docs/`, so the file is not copied into the published site. The workflow installs with `pip install -r requirements-docs.txt`, making deploys reproducible and upgrades explicit, reviewable edits.

*Alternative:* a Python virtualenv spec or pipx in CI — same effect, more moving parts; a plain pinned requirements file is enough for two packages.

### D4: Official Pages actions, two-job workflow

`.github/workflows/pages.yml` follows the current GitHub recommendation:

- **Triggers**: `push` to `main`, `workflow_dispatch`. No `pull_request` trigger — deploys never run from PRs.
- **Permissions**: `contents: read`, `pages: write`, `id-token: write` (OIDC — no secrets or deploy keys). Converging on least privilege; the top-level `permissions` in `ci.yml` stays untouched since the workflows are independent files.
- **Jobs**: `build` (checkout → setup Python → `pip install -r requirements-docs.txt` → `mkdocs build` → `actions/upload-pages-artifact` with the `site/` directory) and `deploy` (`actions/deploy-pages`, `environment: github-pages`, needs `build`). `actions/configure-pages` runs in the build job to resolve Pages metadata.
- **Concurrency**: `group: pages`, `cancel-in-progress: true` — a newer commit supersedes an in-flight deploy of the same ref.
- **Timeout**: `timeout-minutes` on the deploy job so a hung deployment doesn't sit in the queue.

*Alternatives considered:* legacy `gh-pages` branch pushes (needs a token or deploy key, branch state to manage); third-party actions (extra trust surface). The official actions/`deploy-pages` flow is secretless and requires none of that.

### D5: No paths filter, no CI gate on deploy

The deploy runs on every push to `main` even for code-only commits: an MkDocs build is seconds and a paths filter is easy to get wrong (forgetting `mkdocs.yml` or the workflow file silently stops deployments). The deploy does not wait for the CI run of the same commit — docs publishing is decoupled from build/test validation, and a broken library build cannot break the site build because no Maven is involved.

### D6: Site navigation and content sourcing

`mkdocs.yml` defines an explicit `nav` with the six pages in reading order: `index.md`, `getting-started.md`, `migration.md`, `taglibs.md`, `configuration.md`, `modules.md`. Content is sourced from verified repository facts:

- **index/getting-started/modules** — from `README.md` (principles, compatibility table, artifact table, dependency snippets);
- **migration.md** — English translation of `docs/MIGRATION.md`, preserving the step structure and the demo-app verification section;
- **configuration.md** — from `Struts1Properties` (`enabled=true`, `config=/WEB-INF/struts-config.xml`, `validating=true`, `servlet-name=action`, `mapping.url-pattern=[*.do]`) plus the module-config contract `spring.struts1.config.<prefix>` read from the Environment;
- **taglibs.md** — the five TLD URIs verified in the source TLDs: `http://struts.apache.org/tags-bean`, `-html`, `-logic`, `-nested` (in `spring-struts1-taglib`) and `-tiles` (in `spring-struts1-tiles`), plus the bundled commons-validator/`ValidatorPlugIn`.

Links from site pages into the repository (e.g., the demo app, source files) use absolute `github.com/...` URLs: relative repo paths resolve on GitHub but break on the Pages host, and absolute URLs work in both places.

### D7: README gets a Documentation link

`README.md` gains a prominent link to the Pages site (next to the CI badge area), so GitHub traffic discovers the site and the site becomes the canonical deep-link target.

## Risks / Trade-offs

- [Translation loses coverage or nuance of the Russian MIGRATION.md] → translate section-by-section against the source, keep the step structure and the demo verification section; the diff is reviewable line-by-line in the PR.
- [Everything under `docs/` is copied into the published site] → keep `docs/` site-only (pages + assets); if a non-page file ever needs to live there, use MkDocs `exclude_docs`.
- [Absolute GitHub links in site pages can rot if the repo moves/renames] → acceptable for now; the base URL lives in one place per link and is greppable.
- [Pinned MkDocs versions go stale over time] → intentional: reproducible deploys beat silent upgrades; bumps are deliberate one-line edits.
- [Site deploys even when CI of the same commit fails] → decoupling is deliberate (D5); the site only reflects docs content, never built artifacts.
- [One-time operator setup is easy to forget] → the "Settings → Pages → Source: GitHub Actions" step is an explicit task; without it the first `deploy-pages` run fails with a clear error, which also surfaces the missing step.

## Migration Plan

1. Merge the change (site sources, workflow, translated guide, README link).
2. Operator: enable Pages once in repo Settings with Source: **GitHub Actions**.
3. Run `pages.yml` manually (or push to `main`) and verify the site at `https://ys-kalyakin.github.io/spring-struts-brigde/`.
4. Rollback: redeploy a previous commit via `workflow_dispatch` on the old ref — Pages keeps serving the last successful deployment until a new one succeeds.

## Open Questions

None. The exact pinned `mkdocs`/`mkdocs-material` versions are resolved at implementation time (latest stable), which does not affect the approach.
