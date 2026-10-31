# performance-benchmarks Specification

## Purpose

Measures the performance of hot paths of the engine's request pipeline: a benchmark suite reproducibly measures the overhead of request processing, populate, bean resolution, and message formatting, so that optimizations are driven by numbers rather than guesses.

## Requirements

### Requirement: Running benchmarks with a single command
The benchmark suite SHALL be runnable with a single documented command from the repository root in a terminal (without an IDE) and SHALL print measurement results to the console. The run procedure and run configuration SHALL be described in the project documentation.

#### Scenario: Running from the terminal
- **WHEN** a developer executes the documented benchmark run command
- **THEN** the runs execute and measurement results (time-per-operation estimates) are printed to the console

#### Scenario: Run reproducibility
- **WHEN** the benchmark run command is executed again on the same code and environment
- **THEN** measurements are performed with the same configuration (number of warmups, iterations, forks), and results are comparable between runs

### Requirement: Isolation of benchmarks from the regular build
Benchmarks SHALL NOT run during the regular build, test run, or CI: the default project build SHALL complete without running benchmarks, and benchmark dependencies SHALL NOT end up in the library artifacts.

#### Scenario: Regular build without benchmarks
- **WHEN** the regular build and test run of the project are executed
- **THEN** no benchmark is started, and the build and tests pass as before

#### Scenario: Library artifacts without benchmark dependencies
- **WHEN** the publishable library artifacts are built
- **THEN** benchmark dependencies are absent from their transitive dependencies

### Requirement: Coverage of hot paths of the request pipeline
The benchmark suite SHALL cover individually: the full request processing cycle (populate → validate → execute → forward), form populate from parameters, message formatting with arguments, dispatch action method resolution, and resolution of action and form instances.

#### Scenario: Measuring the full request cycle
- **WHEN** the full request processing cycle benchmark is run
- **THEN** the time to process a request from entering the handler to the forward is measured on a prepared configuration with a form and an action

#### Scenario: Measuring populate
- **WHEN** the populate benchmark is run
- **THEN** the time to populate form properties from a set of request parameters is measured

#### Scenario: Measuring instance resolution
- **WHEN** the action and form resolution benchmark is run
- **THEN** the time to obtain instances from the Spring context for the prepared configuration is measured

### Requirement: Documenting measurement results
Measurement results before and after optimizations SHALL be recorded in the project documentation: for each key benchmark — the per-operation value and a description of the measurement environment (processor, JVM, run method), sufficient for reproduction.

#### Scenario: Results recorded
- **WHEN** the work on this proposal is completed
- **THEN** the project documentation lists, for each key benchmark, the values before and after optimizations and a description of the measurement environment
