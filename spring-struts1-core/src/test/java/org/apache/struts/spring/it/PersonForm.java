package org.apache.struts.spring.it;

import org.apache.struts.action.ActionForm;

/**
 * Plain form fixture (no Spring registration): created directly by the
 * engine, one instance per request in request scope.
 */
public class PersonForm extends ActionForm {

    private static final long serialVersionUID = 1L;

    private String name;

    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
