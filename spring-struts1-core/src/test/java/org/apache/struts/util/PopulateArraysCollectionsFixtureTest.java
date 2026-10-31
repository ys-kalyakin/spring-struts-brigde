package org.apache.struts.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import org.apache.struts.fixtures.ArraysForm;
import org.apache.struts.fixtures.MultipartForm;
import org.apache.struts.upload.FormFile;

import jakarta.servlet.ServletException;

/**
 * Fixture tests of array and collection population in
 * {@link RequestUtils#populate}: component-wise arrays with zero-value
 * elements on invalid input, an {@code int[]} multibox, first-element
 * scalar assignment, the unsupported collection target and multipart file
 * lists. These pin the observable behavior of the current implementation.
 */
class PopulateArraysCollectionsFixtureTest {

    private ArraysForm populated(String... params) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        for (int i = 0; i + 1 < params.length; i += 2) {
            request.setParameter(params[i], params[i + 1].split(",", -1));
        }
        ArraysForm form = new ArraysForm();
        RequestUtils.populate(form, request);
        return form;
    }

    @Test
    void stringArrayPopulatedComponentwise() throws Exception {
        ArraysForm form = populated("stringArray", "a,b");
        assertArrayEquals(new String[] {"a", "b"}, form.getStringArray());
    }

    @Test
    void intArrayPopulatedFromMultiboxParameters() throws Exception {
        ArraysForm form = populated("intMultibox", "1,3");
        assertArrayEquals(new int[] {1, 3}, form.getIntMultibox());
    }

    @Test
    void invalidArrayElementGivesZeroValueWithoutError() throws Exception {
        ArraysForm form = populated("intMultibox", "1,abc");
        assertArrayEquals(new int[] {1, 0}, form.getIntMultibox());
    }

    @Test
    void singleScalarPropertyGetsFirstParameterValue() throws Exception {
        ArraysForm form = populated("single", "first,second");
        assertEquals("first", form.getSingle());
    }

    @Test
    void listTargetIsNotPopulatedAndFails() {
        assertThrows(ServletException.class,
            () -> populated("stringList", "a,b"));
    }

    @Test
    void fileListPopulatedFromMultipartRequest() throws Exception {
        MockHttpServletRequest request = rawMultipartRequest("fileList", 2);

        MultipartForm form = new MultipartForm();
        RequestUtils.populate(form, request);

        List<FormFile> files = form.getFileList();
        assertNotNull(files);
        assertEquals(2, files.size());
        assertEquals("f1.txt", files.get(0).getFileName());
        assertEquals("f2.txt", files.get(1).getFileName());
    }

    private MockHttpServletRequest rawMultipartRequest(String fieldName,
        int fileCount) {
        String boundary = "engine-boundary";
        StringBuilder body = new StringBuilder();
        body.append("--").append(boundary).append("\r\n")
            .append("Content-Disposition: form-data; name=\"theText\"\r\n")
            .append("\r\n")
            .append("hello\r\n");
        for (int i = 1; i <= fileCount; i++) {
            body.append("--").append(boundary).append("\r\n")
                .append("Content-Disposition: form-data; name=\"")
                .append(fieldName).append("\"; filename=\"f")
                .append(i).append(".txt\"\r\n")
                .append("Content-Type: text/plain\r\n")
                .append("\r\n")
                .append("content").append(i).append("\r\n");
        }
        body.append("--").append(boundary).append("--\r\n");

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setCharacterEncoding(StandardCharsets.UTF_8.name());
        request.setContentType(
            "multipart/form-data; boundary=" + boundary);
        request.setContent(body.toString().getBytes(StandardCharsets.UTF_8));
        request.setAttribute(org.apache.struts.Globals.MAPPING_KEY,
            new org.apache.struts.action.ActionMapping());
        request.setAttribute(org.apache.struts.Globals.MODULE_KEY,
            new org.apache.struts.config.impl.ModuleConfigImpl(""));
        request.setAttribute(org.apache.struts.Globals.MULTIPART_KEY,
            "org.apache.struts.upload.CommonsMultipartRequestHandler");
        return request;
    }
}
