# Proposal

## Why

The library's English onboarding story currently ends at `README.md`: the only in-depth guide (`docs/MIGRATION.md`) is in Russian, and the remaining reference material — starter properties, taglib coverage, module layout — exists only in source code and the README table. A prospective user migrating a Struts 1 application cannot link a maintainer to a browsable English reference. The project has reached its first usable state (starter, migration guide, demo app) and README is already English, so a public English documentation site is the natural next step; GitHub Pages gives it a permanent front door at `https://ys-kalyakin.github.io/spring-struts-brigde/` at zero cost.

## What Changes

- Add an English documentation site built with **MkDocs Material**, with sources in the existing `docs/` directory (which becomes the MkDocs `docs_dir`) and `mkdocs.yml` at the repository root.
- Site page set (all in English):
  - `index.md` — landing: what the bridge is, key principles, compatibility table (sourced from README);
  - `getting-started.md` — dependency wiring, minimal configuration, pointer to `@StrutsAction`/`@StrutsForm` and the demo app;
  - `migration.md` — English translation of the current Russian `docs/MIGRATION.md` (the file is translated in place so the existing README link keeps working);
  - `taglibs.md` — the five tag libraries (`tags-bean`, `tags-html`, `tags-logic`, `tags-nested`, `tags-tiles`) with their classic URIs, module ownership, and the bundled commons-validator;
  - `configuration.md` — the `spring.struts1.*` starter properties reference (defaults from `Struts1Properties`, including module configs `spring.struts1.config.<prefix>`);
  - `modules.md` — artifacts of group `org.springstruts` and the dependency matrix.
- Add a dedicated GitHub Actions workflow `.github/workflows/pages.yml` that builds the site with pinned MkDocs dependencies on push to `main` (and manual `workflow_dispatch`) and deploys it to GitHub Pages via the official `actions/deploy-pages` flow. The existing `ci.yml` is untouched.
- Link the site from `README.md` (top-level "Documentation" link).

Out of scope: publishing Javadoc/API reference, versioned docs (single "latest" only), any non-English site version.

## Capabilities

### New Capabilities

- `docs-site`: the English-language documentation site of the library — its page set, English-only content rule, MkDocs Material tooling, and content sourcing from README, the migration guide, and the starter properties.
- `pages-deployment`: automated publication of the documentation site to GitHub Pages — a dedicated workflow that builds the site and deploys it on pushes to `main` and manual runs, with least-privilege permissions and superseded-run cancellation.

### Modified Capabilities

None. The `ci-pipeline` capability covers the build/test workflow (`ci.yml`) and its requirements are unchanged; `pages.yml` is a separate workflow with separate concerns.

## Impact

- **New files**: `mkdocs.yml`, `docs/` site pages (`index.md`, `getting-started.md`, `migration.md`, `taglibs.md`, `configuration.md`, `modules.md`), `docs/requirements.txt` (pinned MkDocs Material deps), `.github/workflows/pages.yml`.
- **Modified files**: `docs/MIGRATION.md` (translated to English in place), `README.md` (link to the site).
- **External one-time setup**: GitHub Pages must be enabled once in the repository Settings with "GitHub Actions" as the source — an operator step listed in tasks, not automatable from the workflow file.
- **No changes** to library code, Maven POMs, or the `ci.yml` pipeline.
