package org.apache.struts.upload;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.config.ControllerConfig;
import org.apache.struts.config.impl.ModuleConfigImpl;

/**
 * Unit tests for the classic multipart handler contract: parsing, element
 * maps and the graceful limit-exceeded semantics. The request body is
 * hand-crafted: the fileupload2 parser consumes the raw servlet input
 * stream, not the container's Part API.
 */
class CommonsMultipartRequestHandlerTest {

    private static final String BOUNDARY = "engine-boundary";

    private MockHttpServletRequest multipartRequest(String... extraFileParts) {
        StringBuilder body = new StringBuilder();
        body.append("--").append(BOUNDARY).append("\r\n")
            .append("Content-Disposition: form-data; name=\"theText\"\r\n")
            .append("\r\n")
            .append("hello\r\n");
        int files = 0;
        for (String ignored : extraFileParts) {
            body.append("--").append(BOUNDARY).append("\r\n")
                .append("Content-Disposition: form-data; name=\"theFile\"; filename=\"test")
                .append(++files).append(".txt\"\r\n")
                .append("Content-Type: text/plain\r\n")
                .append("\r\n")
                .append("file-content").append(files).append("\r\n");
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

    @Test
    void parsesTextAndFileElements() throws Exception {
        MockHttpServletRequest request = multipartRequest("one");
        CommonsMultipartRequestHandler handler =
            new CommonsMultipartRequestHandler();
        handler.handleRequest(request);

        assertArrayEquals(new String[] { "hello" },
            handler.getTextElements().get("theText"));

        FormFile[] files = handler.getFileElements().get("theFile");
        assertEquals(1, files.length);
        assertEquals("test1.txt", files[0].getFileName());
        assertEquals("text/plain", files[0].getContentType());
        assertEquals("file-content1".length(), files[0].getFileLength());
        assertArrayEquals("file-content1".getBytes(StandardCharsets.UTF_8),
            files[0].getFileData());
        assertTrue(files[0].getInputStream().readAllBytes().length > 0);

        assertTrue(handler.getAllElements().containsKey("theText"));
        assertTrue(handler.getAllElements().containsKey("theFile"));
    }

    @Test
    void multiFileFieldProducesArrayOfFormFiles() throws Exception {
        MockHttpServletRequest request = multipartRequest("one", "two");
        CommonsMultipartRequestHandler handler =
            new CommonsMultipartRequestHandler();
        handler.handleRequest(request);

        assertEquals(2, handler.getFileElements().get("theFile").length);
        assertEquals("test2.txt",
            handler.getFileElements().get("theFile")[1].getFileName());
    }

    @Test
    void exceededFileSizeSetsAttributeAndSkipsElements() throws Exception {
        MockHttpServletRequest request = multipartRequest("big");
        ModuleConfigImpl module = new ModuleConfigImpl("");
        ControllerConfig controller = new ControllerConfig();
        controller.setMaxFileSize("2");
        module.setControllerConfig(controller);
        request.setAttribute(Globals.MODULE_KEY, module);

        CommonsMultipartRequestHandler handler =
            new CommonsMultipartRequestHandler();
        handler.handleRequest(request);

        assertEquals(Boolean.TRUE, request.getAttribute(
            MultipartRequestHandler.ATTRIBUTE_MAX_LENGTH_EXCEEDED));
        assertFalse(handler.getAllElements().containsKey("theFile"));
        assertTrue(handler.getAllElements().isEmpty());
    }

    @Test
    void exceededRequestSizeSetsByteCountAttribute() throws Exception {
        MockHttpServletRequest request = multipartRequest("big");
        ModuleConfigImpl module = new ModuleConfigImpl("");
        ControllerConfig controller = new ControllerConfig();
        controller.setMaxSize("1");
        module.setControllerConfig(controller);
        request.setAttribute(Globals.MODULE_KEY, module);

        CommonsMultipartRequestHandler handler =
            new CommonsMultipartRequestHandler();
        handler.handleRequest(request);

        assertEquals(Boolean.TRUE, request.getAttribute(
            MultipartRequestHandler.ATTRIBUTE_MAX_LENGTH_EXCEEDED));
        assertTrue(handler.getAllElements().isEmpty());
    }

    @Test
    void finishAndRollbackDestroyFiles() throws Exception {
        MockHttpServletRequest request = multipartRequest("one");
        // tiny threshold forces the item onto disk so cleanup is observable
        ModuleConfigImpl module = new ModuleConfigImpl("");
        ControllerConfig controller = new ControllerConfig();
        controller.setMemFileSize("2");
        module.setControllerConfig(controller);
        request.setAttribute(Globals.MODULE_KEY, module);

        CommonsMultipartRequestHandler handler =
            new CommonsMultipartRequestHandler();
        handler.handleRequest(request);

        org.apache.commons.fileupload2.core.DiskFileItem item =
            (org.apache.commons.fileupload2.core.DiskFileItem)
                ((CommonsMultipartRequestHandler.CommonsFormFile)
                    handler.getFileElements().get("theFile")[0]).fileItem;
        assertTrue(java.nio.file.Files.exists(item.getPath()));
        handler.finish();
        assertFalse(java.nio.file.Files.exists(item.getPath()));
    }

    @Test
    void wrapperExposesMultipartTextParameters() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("plain", "value");
        MultipartRequestWrapper wrapper = new MultipartRequestWrapper(request);
        wrapper.setParameter("theText", "hello");
        wrapper.setParameter("theText", "second");

        assertEquals("hello", wrapper.getParameter("theText"));
        assertArrayEquals(new String[] { "hello", "second" },
            wrapper.getParameterValues("theText"));
        assertEquals("value", wrapper.getParameter("plain"));
        assertTrue(wrapper.getParameterNames().hasMoreElements());
    }

}
