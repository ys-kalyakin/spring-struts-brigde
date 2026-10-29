package org.apache.struts.upload;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionServlet;

import jakarta.servlet.http.HttpServletRequest;

/**
 * A {@link MultipartRequestHandler} over a request that has already been
 * resolved by a user-configured Spring <code>MultipartResolver</code>. The
 * parts of such a request are adapted into the classic contract instead of
 * parsing the request body a second time; limit-exceeded detection is left
 * to the Spring resolver.
 *
 * <p>This handler is not registered through configuration: it is selected
 * automatically by <code>RequestUtils.populate</code> when the incoming
 * request is already a Spring multipart request.</p>
 */
public class SpringMultipartRequestHandler implements MultipartRequestHandler {

    private MultipartHttpServletRequest request;

    private HashMap<String, String[]> elementsText;

    private HashMap<String, FormFile[]> elementsFile;

    private HashMap<String, Object> elementsAll;

    @Override
    public void setServlet(ActionServlet servlet) {
        // not needed: the request has already been parsed
    }

    @Override
    public ActionServlet getServlet() {
        return null;
    }

    @Override
    public void setMapping(ActionMapping mapping) {
        // the mapping is available through the request attributes
    }

    @Override
    public ActionMapping getMapping() {
        return (request != null)
            ? (ActionMapping) request.getAttribute(Globals.MAPPING_KEY)
            : null;
    }

    @Override
    public void handleRequest(HttpServletRequest request) {
        this.request = (MultipartHttpServletRequest) request;

        elementsText = new HashMap<>();
        elementsFile = new HashMap<>();
        elementsAll = new HashMap<>();

        MultiValueMap<String, MultipartFile> files =
            this.request.getMultiFileMap();

        for (Map.Entry<String, List<MultipartFile>> entry : files.entrySet()) {
            FormFile[] adapted = entry.getValue().stream()
                .map(SpringFormFile::new)
                .toArray(FormFile[]::new);
            elementsFile.put(entry.getKey(), adapted);
            elementsAll.put(entry.getKey(), adapted);
        }

        for (Map.Entry<String, String[]> entry
            : this.request.getParameterMap().entrySet()) {
            if (!files.containsKey(entry.getKey())) {
                elementsText.put(entry.getKey(), entry.getValue());
                elementsAll.put(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public HashMap<String, String[]> getTextElements() {
        return elementsText;
    }

    @Override
    public HashMap<String, FormFile[]> getFileElements() {
        return elementsFile;
    }

    @Override
    public HashMap<String, Object> getAllElements() {
        return elementsAll;
    }

    @Override
    public void rollback() {
        // nothing to roll back: the resolver owns the parts
    }

    @Override
    public void finish() {
        // nothing to clean up: the resolver owns the parts
    }

    /**
     * {@link FormFile} adapter over a Spring <code>MultipartFile</code>;
     * read-only, like the classic commons-based implementation.
     */
    static class SpringFormFile implements FormFile {

        private final MultipartFile file;

        SpringFormFile(MultipartFile file) {
            this.file = file;
        }

        @Override
        public String getContentType() {
            return file.getContentType();
        }

        @Override
        public void setContentType(String contentType) {
            throw new UnsupportedOperationException(
                "The setContentType() method is not supported.");
        }

        @Override
        public int getFileSize() {
            long size = file.getSize();
            if (size > Integer.MAX_VALUE) {
                throw new IllegalStateException(
                    "Size is greater than 2 GB; use getFileLength()");
            }
            return (int) size;
        }

        @Override
        public void setFileSize(int filesize) {
            throw new UnsupportedOperationException(
                "The setFileSize() method is not supported.");
        }

        @Override
        public long getFileLength() {
            return file.getSize();
        }

        @Override
        public void setFileLength(long fileLength) {
            throw new UnsupportedOperationException(
                "The setFileLength() method is not supported.");
        }

        @Override
        public String getFileName() {
            return file.getOriginalFilename();
        }

        @Override
        public void setFileName(String fileName) {
            throw new UnsupportedOperationException(
                "The setFileName() method is not supported.");
        }

        @Override
        public byte[] getFileData() throws FileNotFoundException, IOException {
            return file.getBytes();
        }

        @Override
        public InputStream getInputStream()
            throws FileNotFoundException, IOException {
            return file.getInputStream();
        }

        @Override
        public void destroy() throws IOException {
            // the container/Spring resolver owns the underlying resource
        }
    }
}
