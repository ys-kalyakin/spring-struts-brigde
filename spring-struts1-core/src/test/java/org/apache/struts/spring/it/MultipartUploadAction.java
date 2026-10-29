package org.apache.struts.spring.it;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.fixtures.MultipartForm;

/**
 * Multipart fixture action: reports the populated form (text, single file,
 * file array) so tests can verify the multipart pipeline end to end.
 */
public class MultipartUploadAction extends Action {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
        HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        MultipartForm upload = (MultipartForm) form;
        StringBuilder body = new StringBuilder("upload:text=")
            .append(upload.getTheText());
        if (upload.getTheFile() != null) {
            body.append(":file=").append(upload.getTheFile().getFileName())
                .append(':').append(upload.getTheFile().getContentType())
                .append(':').append(upload.getTheFile().getFileLength())
                .append(":data=").append(new String(
                    upload.getTheFile().getFileData(), StandardCharsets.UTF_8));
        }
        body.append(":files=").append(upload.getFiles() == null
            ? 0
            : upload.getFiles().length);
        response.getWriter().print(body.toString());
        return null;
    }
}
