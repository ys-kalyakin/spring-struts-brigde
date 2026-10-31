package org.apache.struts.fixtures;

import java.util.HashMap;
import java.util.Map;

import org.apache.struts.action.ActionForm;

/**
 * Fixture form covering the parameter-name syntax of populate: simple,
 * indexed, mapped, nested names and their combinations.
 */
public class NamesForm extends ActionForm {

    /**
     * Nested bean reachable through the {@code nested} property: indexed and
     * mapped properties of its own, including a mapped-array combination.
     */
    public static class Nested {

        private int intProperty;

        private String[] strings = {"a", "b"};

        @SuppressWarnings("unchecked")
        private Map<String, String>[] maps = new HashMap[2];

        public int getIntProperty() {
            return intProperty;
        }

        public void setIntProperty(int intProperty) {
            this.intProperty = intProperty;
        }

        public String[] getStrings() {
            return strings;
        }

        public void setStrings(String[] strings) {
            this.strings = strings;
        }

        public Map<String, String>[] getMaps() {
            return maps;
        }

        public void setMaps(Map<String, String>[] maps) {
            this.maps = maps;
        }
    }

    private String name;

    private int[] intIndexed = {0, 10, 20, 30, 40};

    private Map<String, String> mapValue = new HashMap<>();

    private Nested nested = new Nested();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int[] getIntIndexed() {
        return intIndexed;
    }

    public void setIntIndexed(int[] intIndexed) {
        this.intIndexed = intIndexed;
    }

    public Map<String, String> getMapValue() {
        return mapValue;
    }

    public void setMapValue(Map<String, String> mapValue) {
        this.mapValue = mapValue;
    }

    public Nested getNested() {
        return nested;
    }

    public void setNested(Nested nested) {
        this.nested = nested;
    }
}
