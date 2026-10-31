# Spec Delta

## Purpose

Measures the performance of the hot paths of the engine's request-processing pipeline: a benchmark suite reproducibly measures the overhead of request processing, populate, bean resolution, and message formatting, so that optimizations are based on numbers rather than guesswork.

## ADDED Requirements

### Requirement: Running benchmarks with a single command
The benchmark suite SHALL be runnable with a single documented command from the repository root in a terminal (without an IDE) and SHALL print the measured results to the console. The run procedure and the run configuration SHALL be described in the project documentation.

#### Scenario: Running from a terminal
- **WHEN** a developer executes the documented benchmark run command
- **THEN** the runs execute and the measured results (time-per-operation scores) are printed to the console

#### Scenario: Run reproducibility
- **WHEN** the benchmark run command is executed again on the same code and environment
- **THEN** the measurements are performed with the same configuration (number of warmups, iterations, forks), and the results are comparable across runs

### Requirement: Isolation of benchmarks from the regular build
Benchmarks SHALL NOT run during the regular build, test run, or CI: the default project build SHALL complete without running benchmarks, and benchmark dependencies SHALL NOT end up in the library artifacts.

#### Scenario: Regular build without benchmarks
- **WHEN** the regular build and test run of the project is executed
- **THEN** no benchmark is started, and the build and tests pass as before

#### Scenario: Library artifacts without benchmark dependencies
- **WHEN** the publishable library artifacts are built
- **THEN** benchmark dependencies are absent from their transitive dependencies

### Requirement: Coverage of request-pipeline hot paths
The benchmark suite SHALL cover individually: the full request-processing cycle (populate → validate → execute → forward), form populate from parameters, message formatting with arguments, dispatch-action method resolution, and resolution of action and form instances.

#### Scenario: Measuring the full request cycle
- **WHEN** the full request-processing cycle benchmark is run
- **THEN** the time to process a request from handler entry to forward is measured on a prepared configuration with a form and an action

#### Scenario: Measuring populate
- **WHEN** the populate benchmark is run
- **THEN** the time to fill form properties from a set of request parameters is measured

#### Scenario: Measuring instance resolution
- **WHEN** the action and form resolution benchmark is run
- **THEN** the time to obtain instances from the Spring context for a prepared configuration is measured

### Requirement: Documenting measurement results
The measurement results taken before and after the optimizations SHALL be recorded in the project documentation: for each key benchmark, the value per operation and a description of the measurement environment (processor, JVM, how it was run) sufficient for reproduction.

#### Scenario: Results recorded
- **WHEN** the changes in this proposal are complete
- **THEN** the project documentation states, for each key benchmark, the values before and after the optimizations and a description of the measurement environment
