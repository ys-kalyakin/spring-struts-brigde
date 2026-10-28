package org.apache.struts.webapp.examples;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import org.apache.struts.action.ActionForm;

/**
 * Demo form of the spring-integration showcase: an unannotated form bean
 * declared in struts-config — the engine registers it as a Spring prototype,
 * so the dependency is injected and {@code @PostConstruct} runs before
 * populate.
 */
public class SpringDemoForm extends ActionForm {

    private static final long serialVersionUID = 1L;

    @Autowired
    private transient DemoMessageService demoMessageService;

    private boolean postConstructed;

    private String label;

    @PostConstruct
    void postConstruct() {
        postConstructed = true;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getServiceGreeting() {
        return demoMessageService.getMessage()
            + (postConstructed ? ":postconstructed" : ":not-postconstructed");
    }
}
