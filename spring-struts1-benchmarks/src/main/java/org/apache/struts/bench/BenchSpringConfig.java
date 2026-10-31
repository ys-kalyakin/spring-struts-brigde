package org.apache.struts.bench;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Minimal Spring context of the benchmarks: a singleton action (the engine's
 * auto-registration semantics) and a prototype form (a fresh instance per
 * request), mirroring what {@code StrutsEngineRegistrar} does at startup.
 */
@Configuration
public class BenchSpringConfig {

    @Bean
    public BenchAction benchAction() {
        return new BenchAction();
    }

    @Bean
    @Scope("prototype")
    public BenchForm benchForm() {
        return new BenchForm();
    }
}
