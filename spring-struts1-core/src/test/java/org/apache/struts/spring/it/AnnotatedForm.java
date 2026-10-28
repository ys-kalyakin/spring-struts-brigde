package org.apache.struts.spring.it;

import org.springframework.beans.factory.annotation.Autowired;

import org.apache.struts.action.StrutsForm;

/**
 * Annotated form fixture: a prototype Spring bean with an injected
 * dependency. Every request must receive its own instance.
 */
@StrutsForm
public class AnnotatedForm extends org.apache.struts.action.ActionForm {

    private static final long serialVersionUID = 1L;

    @Autowired
    private transient GreeterService greeterService;

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String describe() {
        return "annotated-form:" + greeterService.greet();
    }
}
