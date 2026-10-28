package org.apache.struts.webapp.examples;

/**
 * Demo service of the spring-integration showcase: a plain singleton Spring
 * bean resolved by EL through its bean name and injected into the demo
 * action and form.
 */
public class DemoMessageService {

    public String getMessage() {
        return "injected-service-ok";
    }
}
