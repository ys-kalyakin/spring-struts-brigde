package org.apache.struts.config;

/**
 * Thrown at startup when a module's controller configuration relies on
 * controller behavior this engine does not implement. Without the check the
 * configured behavior would be silently skipped; the message names the
 * property, the offending value, the module, and the supported values.
 */
public class UnsupportedControllerConfigException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public UnsupportedControllerConfigException(String message) {
        super(message);
    }
}
