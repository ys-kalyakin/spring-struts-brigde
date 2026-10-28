package org.apache.struts.spring.it;

import org.springframework.stereotype.Component;

/**
 * Simple service bean used to verify dependency injection into actions and
 * forms.
 */
@Component
public class GreeterService {

    public String greet() {
        return "greeted-by-service";
    }
}
