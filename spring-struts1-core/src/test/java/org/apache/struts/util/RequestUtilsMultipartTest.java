package org.apache.struts.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.config.ControllerConfig;
import org.apache.struts.config.impl.ModuleConfigImpl;
import org.apache.struts.fixtures.MultipartForm;
import org.apache.struts.upload.MultipartRequestHandler;
import org.apache.struts.upload.MultipartRequestWrapper;

/**
 * Unit tests for the multipart branch of <code>RequestUtils.populate</code>:
 * the classic handler path (including the skipped populate on limit
 * violations) and the adaptation of a request already resolved by a Spring
 * <code>MultipartResolver</code>.
 */
class RequestUtilsMultipartTest {

    private static final String BOUNDARY = "engine-boundary";

    private MockHttpServletRequest rawMultipartRequest(int fileCount) {
        return rawMultipartRequest(fileCount, "theFile");
    }

    private MockHttpServletRequest rawMultipartRequest(int fileCount,
        String fieldName) {
        StringBuilder body = new StringBuilder();
        body.append("--").append(BOUNDARY).append("\r\n")
            .append("Content-Disposition: form-data; name=\"theText\"\r\n")
            .append("\r\n")
            .append("hello\r\n");
        for (int i = 1; i <= fileCount; i++) {
            body.append("--").append(BOUNDARY).append("\r\n")
                .append("Content-Disposition: form-data; name=\"")
                .append(fieldName).append("\"; filename=\"f")
                .append(i).append(".txt\"\r\n")
                .append("Content-Type: text/plain\r\n")
                .append("\r\n")
                .append("content").append(i).append("\r\n");
        }
        body.append("--").append(BOUNDARY).append("--\r\n");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        request.setContentType(
            "multipart/form-data; boundary=" + BOUNDARY);
        request.setContent(body.toString().getBytes(StandardCharsets.UTF_8));
        request.setAttribute(Globals.MAPPING_KEY, new ActionMapping());
        request.setAttribute(Globals.MODULE_KEY, new ModuleConfigImpl(""));
        return request;
    }

    private ModuleConfigImpl moduleWithMultipartClass() {
        ModuleConfigImpl module = new ModuleConfigImpl("");
        ControllerConfig controller = new ControllerConfig();
        controller.setMultipartClass(
            "org.apache.struts.upload.CommonsMultipartRequestHandler");
        module.setControllerConfig(controller);
        return module;
    }

    @Test
    void populatesTextAndSingleFileFromClassicHandler() throws Exception {
        MockHttpServletRequest request = rawMultipartRequest(1);
        request.setAttribute(Globals.MULTIPART_KEY,
            "org.apache.struts.upload.CommonsMultipartRequestHandler");

        MultipartForm form = new MultipartForm();
        RequestUtils.populate(form, request);

        assertEquals("hello", form.getTheText());
        assertNotNull(form.getTheFile());
        assertEquals("f1.txt", form.getTheFile().getFileName());
        assertArrayEquals("content1".getBytes(StandardCharsets.UTF_8),
            form.getTheFile().getFileData());
        assertNotNull(form.getMultipartRequestHandler());
    }

    @Test
    void populatesFileArrayPropertyWithAllFiles() throws Exception {
        MockHttpServletRequest request = rawMultipartRequest(2, "files");
        request.setAttribute(Globals.MULTIPART_KEY,
            "org.apache.struts.upload.CommonsMultipartRequestHandler");

        MultipartForm form = new MultipartForm();
        RequestUtils.populate(form, request);

        assertEquals(2, form.getFiles().length);
        assertEquals("f2.txt", form.getFiles()[1].getFileName());
    }

    @Test
    void singleFilePropertyGetsFirstFileOfMultiple() throws Exception {
        MockHttpServletRequest request = rawMultipartRequest(2);
        request.setAttribute(Globals.MULTIPART_KEY,
            "org.apache.struts.upload.CommonsMultipartRequestHandler");

        MultipartForm form = new MultipartForm();
        RequestUtils.populate(form, request);

        assertEquals("f1.txt", form.getTheFile().getFileName());
    }

    @Test
    void exceededLimitSkipsPopulateButAttachesHandler() throws Exception {
        MockHttpServletRequest request = rawMultipartRequest(1);
        request.setAttribute(Globals.MULTIPART_KEY,
            "org.apache.struts.upload.CommonsMultipartRequestHandler");
        ModuleConfigImpl module = new ModuleConfigImpl("");
        ControllerConfig controller = new ControllerConfig();
        controller.setMultipartClass(
            "org.apache.struts.upload.CommonsMultipartRequestHandler");
        controller.setMaxFileSize("2");
        module.setControllerConfig(controller);
        request.setAttribute(Globals.MODULE_KEY, module);

        MultipartForm form = new MultipartForm();
        RequestUtils.populate(form, request);

        assertEquals(Boolean.TRUE, request.getAttribute(
            MultipartRequestHandler.ATTRIBUTE_MAX_LENGTH_EXCEEDED));
        assertNull(form.getTheText());
        assertNull(form.getTheFile());
        assertNotNull(form.getMultipartRequestHandler());

        // the application learns about the violation through validate()
        assertTrue(form.validate(new ActionMapping(), request).size() > 0);
        assertTrue(form.isMaxLengthExceeded());
    }

    @Test
    void springMultipartRequestIsAdaptedWithoutReparse() throws Exception {
        MockMultipartHttpServletRequest request =
            new MockMultipartHttpServletRequest();
        request.setMethod("POST");
        request.setContentType("multipart/form-data; boundary=x");
        request.addParameter("theText", "hello");
        request.addFile(new MockMultipartFile("theFile", "s.txt",
            "text/plain", "spring".getBytes(StandardCharsets.UTF_8)));
        request.setAttribute(Globals.MAPPING_KEY, new ActionMapping());
        // no MULTIPART_KEY: the request is already resolved by Spring

        MultipartForm form = new MultipartForm();
        RequestUtils.populate(form, request);

        assertEquals("hello", form.getTheText());
        assertNotNull(form.getTheFile());
        assertEquals("s.txt", form.getTheFile().getFileName());
        assertArrayEquals("spring".getBytes(StandardCharsets.UTF_8),
            form.getTheFile().getFileData());
    }

    @Test
    void plainPostPopulatesLikeBefore() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setParameter("theText", "hello");

        MultipartForm form = new MultipartForm();
        RequestUtils.populate(form, request);

        assertEquals("hello", form.getTheText());
        assertNull(form.getTheFile());
        assertNull(form.getMultipartRequestHandler());
    }

}
