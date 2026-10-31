# Spec Delta

## MODIFIED Requirements

### Requirement: Page set and navigation
The site SHALL contain seven pages: the home page (library overview), getting started, the migration guide, the tag library reference, the starter configuration reference, the modules overview, and a performance page with instructions for running the benchmarks and recorded results. Each page SHALL be reachable from the site navigation.

#### Scenario: Site navigation
- **WHEN** a visitor opens the home page
- **THEN** the site navigation lists all sections, and every link leads to an existing page

#### Scenario: Performance page in navigation
- **WHEN** a visitor opens the performance page from the site navigation
- **THEN** the page opens and contains instructions for running the benchmarks and a table of measured results
