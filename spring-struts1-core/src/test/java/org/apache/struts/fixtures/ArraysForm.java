package org.apache.struts.fixtures;

import java.util.List;

import org.apache.struts.action.ActionForm;

/**
 * Fixture form covering array and collection population: component-wise
 * arrays, an {@code int[]} multibox, a string list and a scalar property
 * receiving multiple parameter values.
 */
public class ArraysForm extends ActionForm {

    private String[] stringArray;

    private List<String> stringList;

    private int[] intMultibox;

    private String single;

    public String[] getStringArray() {
        return stringArray;
    }

    public void setStringArray(String[] stringArray) {
        this.stringArray = stringArray;
    }

    public List<String> getStringList() {
        return stringList;
    }

    public void setStringList(List<String> stringList) {
        this.stringList = stringList;
    }

    public int[] getIntMultibox() {
        return intMultibox;
    }

    public void setIntMultibox(int[] intMultibox) {
        this.intMultibox = intMultibox;
    }

    public String getSingle() {
        return single;
    }

    public void setSingle(String single) {
        this.single = single;
    }
}
