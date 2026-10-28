package org.apache.struts.spring.it;

/**
 * Subclass of the declared TestException: must be handled by the same
 * configuration through the superclass walk.
 */
public class SubTestException extends TestException {

    private static final long serialVersionUID = 1L;

    public SubTestException(String message) {
        super(message);
    }
}
