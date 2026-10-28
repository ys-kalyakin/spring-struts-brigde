package app;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import org.apache.struts.spring.StrutsEngineConfiguration;

/**
 * Spring configuration of the ported blank application: imports the engine
 * beans. The application itself declares no annotated actions or forms —
 * the blank app exercises the classic fallback mode.
 */
@Configuration
@Import(StrutsEngineConfiguration.class)
public class BlankConfig {
}
