# Spec Delta

## MODIFIED Requirements

### Requirement: Concurrency and run safety
CI SHALL cancel outdated runs for the same pull request or branch when a new commit arrives. The verification workflow SHALL run with minimal permissions (`contents: read`), without secrets and external services, and SHALL have a run time limit so that a hung run does not block the queue forever. Publishing jobs (snapshot publication on `main` and release publication on `v*` tags) are exempt from the secret-free contract: they SHALL use only the publishing credential secret required to reach Maven Central, run with minimal additional permissions, and have their own run time limit.

#### Scenario: New commit in an active PR
- **WHEN** a pull request receives a new commit while the previous run is still in progress
- **THEN** the previous run is cancelled and only the new commit is checked

#### Scenario: Hung run
- **WHEN** a build exceeds the configured time limit
- **THEN** the run fails by timeout and does not remain in the queue forever

#### Scenario: Verification job stays secret-free
- **WHEN** the verification build runs for a pull request or a push to `main`
- **THEN** the job runs with `contents: read` and no secrets, independent of whether publishing jobs are configured

#### Scenario: Publishing job credential scope
- **WHEN** a snapshot or release publishing job runs
- **THEN** it accesses only the Maven Central publishing credential secret and no other secrets
