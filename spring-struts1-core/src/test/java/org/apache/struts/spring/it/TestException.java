package org.apache.struts.spring.it;

/**
 * Exception declared in the fixture global-exceptions.
 */
public class TestException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public TestException(String message) {
        super(message);
    }
}
