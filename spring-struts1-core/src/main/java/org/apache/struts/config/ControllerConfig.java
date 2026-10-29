package org.apache.struts.config;

/**
 * A JavaBean representing the configuration information of a
 * <code>&lt;controller&gt;</code> element from a Struts configuration file.
 *
 * <p>Ported from weblegacy/struts1 1.5.x (org.apache.struts.config.ControllerConfig);
 * chain-specific fields are retained as attributes only.</p>
 */
public class ControllerConfig extends BaseConfig {

    private static final long serialVersionUID = -4777554808429817708L;

    /** The input buffer size for file uploads. */
    protected int bufferSize = 4096;

    /** The content type and character encoding to be set on each response. */
    protected String contentType = "text/html";

    /** The name of the catalog (chain). */
    protected String catalog = "struts";

    /** The name of the command (chain). */
    protected String command = "servlet-standard";

    /** The pattern for forward paths. */
    protected String forwardPattern;

    /** Should the input property of ActionConfig be treated as the name of a forward? */
    protected boolean inputForward = false;

    /** Should a Locale be stored in the user's session if not present? */
    protected boolean locale = true;

    /** The maximum size of an entire multipart request. */
    protected String maxSize = "256M";

    /** The maximum size of a file to be accepted in an upload. */
    protected String maxFileSize = "250M";

    /** The maximum length of a string parameter in a multipart request. */
    protected String maxStringLen = "4K";

    /** The maximum number of files accepted in an upload (-1 = no limit). */
    protected long fileCountMax = -1L;

    /** The size threshold above which uploaded files will be written to disk. */
    protected String memFileSize = "256K";

    /** The fully qualified Java class name of the MultipartRequestHandler. */
    protected String multipartClass =
        "org.apache.struts.upload.CommonsMultipartRequestHandler";

    /** Should we set the no-cache HTTP headers on each response? */
    protected boolean nocache = false;

    /** The pattern for page paths. */
    protected String pagePattern;

    /** The fully qualified Java class name of the RequestProcessor class. */
    protected String processorClass =
        "org.apache.struts.action.RequestProcessor";

    /** The temporary working directory for file uploads. */
    protected String tempDir = null;

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getForwardPattern() {
        return forwardPattern;
    }

    public void setForwardPattern(String forwardPattern) {
        this.forwardPattern = forwardPattern;
    }

    public boolean getInputForward() {
        return inputForward;
    }

    public void setInputForward(boolean inputForward) {
        this.inputForward = inputForward;
    }

    public boolean getLocale() {
        return locale;
    }

    public void setLocale(boolean locale) {
        this.locale = locale;
    }

    public String getMaxFileSize() {
        return maxFileSize;
    }

    public void setMaxFileSize(String maxFileSize) {
        this.maxFileSize = maxFileSize;
    }

    public String getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(String maxSize) {
        this.maxSize = maxSize;
    }

    public String getMaxStringLen() {
        return maxStringLen;
    }

    public void setMaxStringLen(String maxStringLen) {
        this.maxStringLen = maxStringLen;
    }

    public long getFileCountMax() {
        return fileCountMax;
    }

    public void setFileCountMax(long fileCountMax) {
        this.fileCountMax = fileCountMax;
    }

    public String getMemFileSize() {
        return memFileSize;
    }

    public void setMemFileSize(String memFileSize) {
        this.memFileSize = memFileSize;
    }

    public String getMultipartClass() {
        return multipartClass;
    }

    public void setMultipartClass(String multipartClass) {
        this.multipartClass = multipartClass;
    }

    public boolean getNocache() {
        return nocache;
    }

    public void setNocache(boolean nocache) {
        this.nocache = nocache;
    }

    public String getPagePattern() {
        return pagePattern;
    }

    public void setPagePattern(String pagePattern) {
        this.pagePattern = pagePattern;
    }

    public String getProcessorClass() {
        return processorClass;
    }

    public void setProcessorClass(String processorClass) {
        this.processorClass = processorClass;
    }

    public String getTempDir() {
        return tempDir;
    }

    public void setTempDir(String tempDir) {
        this.tempDir = tempDir;
    }

    @Override
    public String toString() {
        return "ControllerConfig[contentType=" + contentType + ",locale="
            + locale + ",nocache=" + nocache + ",processorClass="
            + processorClass + "]";
    }
}
