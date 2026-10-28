package org.apache.struts.spring.it;

/**
 * Exception with no configuration anywhere: must propagate to the container.
 */
public class UncaughtException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UncaughtException(String message) {
        super(message);
    }
}
