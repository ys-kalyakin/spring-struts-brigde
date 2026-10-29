package org.apache.struts.fixtures;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;
import org.apache.struts.upload.MultipartRequestHandler;

/**
 * Fixture form with multipart-aware properties: a single file, a file array
 * and a text field, plus the classic limit-exceeded validation.
 */
public class MultipartForm extends ActionForm {

    private String theText;

    private FormFile theFile;

    private FormFile[] files;

    private boolean maxLengthExceeded;

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        theText = null;
        theFile = null;
        files = null;
        maxLengthExceeded = false;
    }

    @Override
    public ActionErrors validate(ActionMapping mapping,
        HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        if (Boolean.TRUE.equals(request.getAttribute(
            MultipartRequestHandler.ATTRIBUTE_MAX_LENGTH_EXCEEDED))) {
            maxLengthExceeded = true;
            errors.add("maxLengthExceeded",
                new org.apache.struts.action.ActionMessage(
                    "maxLengthExceeded"));
        }
        return errors;
    }

    public String getTheText() {
        return theText;
    }

    public void setTheText(String theText) {
        this.theText = theText;
    }

    public FormFile getTheFile() {
        return theFile;
    }

    public void setTheFile(FormFile theFile) {
        this.theFile = theFile;
    }

    public FormFile[] getFiles() {
        return files;
    }

    public void setFiles(FormFile[] files) {
        this.files = files;
    }

    public boolean isMaxLengthExceeded() {
        return maxLengthExceeded;
    }
}
