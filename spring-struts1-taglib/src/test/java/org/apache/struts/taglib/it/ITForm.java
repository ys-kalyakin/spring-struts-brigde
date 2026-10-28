package org.apache.struts.taglib.it;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts.action.ActionForm;

/**
 * Form fixture for the taglib integration tests.
 */
public class ITForm extends ActionForm {

    private static final long serialVersionUID = 1L;

    private String name;

    private String color;

    private String html;

    private String status = "open";

    private String path;

    private String note = "note";

    private String token;

    private Boolean active = Boolean.FALSE;

    private Integer key = 0;

    private List<ITForm> items = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Integer getKey() {
        return key;
    }

    public void setKey(Integer key) {
        this.key = key;
    }

    public List<ITForm> getItems() {
        return items;
    }

    public void setItems(List<ITForm> items) {
        this.items = items;
    }
}
