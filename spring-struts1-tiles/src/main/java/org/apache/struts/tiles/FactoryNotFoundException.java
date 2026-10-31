package org.apache.struts.tiles;

  /**
   * Exception thrown when definitions factory is not found.
   */
public class FactoryNotFoundException extends DefinitionsFactoryException {
    private static final long serialVersionUID = 2856113784339675593L;

    /**
     * Constructor.
     */
    public FactoryNotFoundException() {
        super();
    }
    /**
     * Constructor.
     * @param msg Message.
     */
    public FactoryNotFoundException(String msg) {
        super(msg);
    }

    /**
     * Constructor.
     *
     * @param msg Message.
     * @param e the root cause exception
     */
    public FactoryNotFoundException(String msg, Exception e) {
        super(msg, e);
    }
}
