# Spec Delta

## Purpose

Automatic publication of the English-language documentation to GitHub Pages: every time the documentation changes in the main branch, the site is rebuilt and published without manual actions, by a separate workflow that does not affect CI.

## ADDED Requirements

### Requirement: Automatic site deployment
Site publication SHALL run on push to `main` and on manual workflow runs (`workflow_dispatch`); a deployment run SHALL not be triggered for pull requests. After a successful deploy, the updated version of the site SHALL be available on GitHub Pages.

#### Scenario: Push to main
- **WHEN** a commit with documentation changes lands in `main`
- **THEN** the site is built and deployed, and the updated version is available on GitHub Pages

#### Scenario: Manual run
- **WHEN** a user runs the workflow manually from GitHub Actions
- **THEN** the site is built and deployed for the selected branch

#### Scenario: Site build failure
- **WHEN** the site build fails
- **THEN** no deploy is performed, and the previous version of the site remains published

### Requirement: Correct operation at the project URL
The site SHALL open correctly at the project's GitHub Pages URL (`https://ys-kalyakin.github.io/spring-struts-brigde/`): static assets and internal links SHALL use the project's base path, not the domain root.

#### Scenario: Opening the home page
- **WHEN** a visitor opens `https://ys-kalyakin.github.io/spring-struts-brigde/`
- **THEN** the home page is displayed with styles, scripts, and navigation

#### Scenario: Following an internal link
- **WHEN** a visitor follows an internal link from any page of the site
- **THEN** the target page opens without a 404 error

### Requirement: Reproducible build
The build tool versions for the site SHALL be pinned in the repository, and the deploy SHALL use exactly those pinned versions, not the latest ones available at run time.

#### Scenario: Redeploying the same commit
- **WHEN** deploying the same commit is triggered again (including much later)
- **THEN** the site is built with the same pinned tool versions

### Requirement: Permissions and concurrency
The deploy workflow SHALL run with the minimal required permissions (`contents: read`, `pages: write`, `id-token: write`) and without secrets, SHALL cancel an in-progress deploy of the same ref when a new run appears, and SHALL have a run-time limit. The deploy execution SHALL not affect the CI run of a pull request.

#### Scenario: New push during a deploy
- **WHEN** a new commit lands in `main` while the previous deploy is still running
- **THEN** the previous deploy is cancelled and the new commit's version is published

#### Scenario: Pull request unaffected
- **WHEN** a pull request changing documentation is opened
- **THEN** the CI run proceeds as usual, and the site deploy is not triggered
