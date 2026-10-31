package org.apache.struts.upload;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * This interface represents a file that has been uploaded by a client. It is
 * the only interface or class in upload package which is typically referenced
 * directly by a Struts application.
 */
public interface FormFile extends Serializable {

    /**
     * Returns the content type for this file.
     *
     * @return A String representing content type.
     */
    String getContentType();

    /**
     * Sets the content type for this file.
     *
     * @param contentType The content type for the file.
     */
    void setContentType(String contentType);

    /**
     * Returns the size of this file.
     *
     * @return The size of the file, in bytes.
     *
     * @throws IllegalStateException if size is greater than 2GB
     *
     * @see #getFileLength()
     *
     * @deprecated use {@link #getFileLength()}
     */
    @Deprecated
    int getFileSize();

    /**
     * Sets the file size.
     *
     * @param size The size of the file, in bytes.
     *
     * @see #setFileLength(long)
     *
     * @deprecated use {@link #setFileLength(long)}
     */
    @Deprecated
    void setFileSize(int size);

    /**
     * Returns the length of this file.
     *
     * @return The length of the file, in bytes.
     */
    long getFileLength();

    /**
     * Sets the file length.
     *
     * @param fileLength The length of the file, in bytes.
     */
    void setFileLength(long fileLength);

    /**
     * Returns the file name of this file. This is the base name of the file,
     * as supplied by the user when the file was uploaded.
     *
     * @return The base file name.
     */
    String getFileName();

    /**
     * Sets the file name of this file.
     *
     * @param fileName The base file name.
     */
    void setFileName(String fileName);

    /**
     * Returns the data for the entire file as byte array. Care is needed when
     * using this method, since a large upload could easily exhaust available
     * memory. The preferred method for accessing the file data is
     * {@link #getInputStream() getInputStream}.
     *
     * @return The file data as a byte array.
     *
     * @throws FileNotFoundException if the uploaded file is not found. Some
     *                               implementations may not deal with files
     *                               and/or throw this exception.
     * @throws IOException           if an error occurred while reading the
     *                               file.
     */
    byte[] getFileData()
        throws FileNotFoundException, IOException;

    /**
     * Returns an input stream for this file. The caller must close the stream
     * when it is no longer needed.
     *
     * @throws FileNotFoundException if the uploaded file is not found. Some
     *                               implementations may not deal with files
     *                               and/or throw this exception.
     * @throws IOException           if an error occurred while reading the
     *                               file.
     */
    InputStream getInputStream()
        throws FileNotFoundException, IOException;

    /**
     * Destroys all content for the uploaded file, including any underlying
     * data files.
     *
     * @throws IOException           if an error occurred while destroying all
     *                               content.
     */
    void destroy() throws IOException;
}
