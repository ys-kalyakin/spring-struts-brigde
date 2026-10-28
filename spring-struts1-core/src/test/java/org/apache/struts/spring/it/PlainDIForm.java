package org.apache.struts.spring.it;

import org.springframework.beans.factory.annotation.Autowired;

import org.apache.struts.action.ActionForm;

/**
 * Unannotated form declared only in struts-config: the registrar must
 * register it as a Spring prototype, so the dependency is injected into a
 * fresh instance per request.
 */
public class PlainDIForm extends ActionForm {

    private static final long serialVersionUID = 1L;

    @Autowired
    private transient GreeterService greeterService;

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String greet() {
        return greeterService.greet();
    }
}
