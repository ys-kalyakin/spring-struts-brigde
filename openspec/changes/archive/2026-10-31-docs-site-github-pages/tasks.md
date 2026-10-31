# Tasks

## 1. Site scaffolding

- [x] 1.1 Create `requirements-docs.txt` at the repo root pinning exact latest-stable versions of `mkdocs` and `mkdocs-material`; install locally with `pip install -r requirements-docs.txt` and verify `mkdocs --version` runs.
- [x] 1.2 Create `mkdocs.yml` at the repo root: `docs_dir: docs`, `site_url: https://ys-kalyakin.github.io/spring-struts-brigde/`, Material theme, and an explicit `nav` listing the six pages (index, getting-started, migration, taglibs, configuration, modules). Verify with a placeholder `docs/index.md` that `mkdocs build --strict` succeeds.

## 2. Content pages

- [x] 2.1 Translate `docs/MIGRATION.md` to English in place, preserving the step structure, artifact tables, code samples, and the demo-app verification section; replace repo-relative links (e.g. the demo app link) with absolute `github.com/ys-kalyakin/spring-struts-brigde/...` URLs so they resolve both on GitHub and on the site. Verify: no Cyrillic remains (`grep -P '[\p{IsCyrillic}]' docs/MIGRATION.md` is empty) and every section of the Russian source has an English counterpart.
- [x] 2.2 Write `docs/index.md` — landing page: what the bridge is, key principles, compatibility table, links to getting-started and migration. Verify the facts match `README.md` (compatibility rows, principles wording).
- [x] 2.3 Write `docs/getting-started.md` — dependency wiring (starter + taglib + tiles), `spring.struts1.config` property, `@StrutsAction`/`@StrutsForm` mention, links to the migration guide and the demo app (absolute GitHub URL). Verify the dependency snippets and property defaults match README.
- [x] 2.4 Write `docs/configuration.md` — reference for `spring.struts1.enabled`, `.config`, `.validating`, `.servlet-name`, `.mapping.url-pattern` with defaults, plus module configurations `spring.struts1.config.<prefix>`. Verify every default against `spring-struts1-starter/src/main/java/org/apache/struts/spring/boot/Struts1Properties.java`.
- [x] 2.5 Write `docs/taglibs.md` — the bean/html/logic/nested/tiles libraries with their classic URIs, which artifact provides each, and the bundled commons-validator. Verify the five URIs against the `.tld` files in `spring-struts1-taglib` and `spring-struts1-tiles`.
- [x] 2.6 Write `docs/modules.md` — the four `org.springstruts` artifacts and each one's role (what it provides, what it replaces). Verify the artifact list matches the README modules table.

## 3. Site-wide verification

- [x] 3.1 Run `mkdocs build --strict` and verify it succeeds with zero warnings (broken internal links and missing nav entries fail the build).
- [x] 3.2 Check English-only across the site: `grep -rP '[\p{IsCyrillic}]' docs/` returns nothing. Verify `docs/MIGRATION.md` still exists so the README link keeps resolving.

## 4. Deployment pipeline and README

- [x] 4.1 Create `.github/workflows/pages.yml` per design D4: triggers `push` to `main` + `workflow_dispatch` (no `pull_request`), permissions `contents: read` / `pages: write` / `id-token: write`, two jobs (build: checkout → setup Python → `pip install -r requirements-docs.txt` → `mkdocs build` → configure-pages → upload-pages-artifact; deploy: `actions/deploy-pages` with `environment: github-pages`), `concurrency` with `cancel-in-progress`, and a per-job timeout. Verify the YAML parses (e.g. `python3 -c "import yaml; yaml.safe_load(open('.github/workflows/pages.yml'))"`) and that `ci.yml` is unmodified.
- [x] 4.2 Add a "Documentation" link to the Pages URL in `README.md` near the CI badge. Verify the link renders and points to `https://ys-kalyakin.github.io/spring-struts-brigde/`.

## 5. Deployment verification

- [x] 5.1 Operator one-time setup: enable GitHub Pages in repo Settings → Pages with Source: **GitHub Actions** (document this step in the PR description). Verify: the setting shows GitHub Actions as the build source.
- [x] 5.2 On the change's pull request, verify `pages.yml` does not run for the PR while `ci.yml` runs as usual (Checks tab).
- [x] 5.3 After merge to `main`, verify the `pages.yml` run succeeds and the site is live at `https://ys-kalyakin.github.io/spring-struts-brigde/`: styles load, navigation reaches all six pages, and internal links return no 404s.

## Workflow follow-up

- Archive the change after the project's review requirements are satisfied.
- Verify the archived result.
