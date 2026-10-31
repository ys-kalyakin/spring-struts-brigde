package org.apache.struts.tiles;

  /**
   * Exception thrown when a definition is not found.
   */
public class NoSuchDefinitionException extends DefinitionsFactoryException {
    private static final long serialVersionUID = -4188770898592969226L;

    /**
     * Constructor.
     */
    public NoSuchDefinitionException() {
        super();
    }
    /**
     * Constructor.
     * @param msg Message.
     */
    public NoSuchDefinitionException(String msg) {
        super(msg);
    }
}
