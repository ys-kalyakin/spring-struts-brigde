package org.apache.struts.tiles;

  /**
   * Exception thrown when an error occurs while the factory tries to
   * create a new instance mapper.
   */
public class DefinitionsFactoryException extends TilesException {
    private static final long serialVersionUID = -5162822984380885363L;

  /**
    * Constructor.
    */
    public DefinitionsFactoryException() {
        super();
    }

  /**
    * Constructor.
    * @param message The error or warning message.
    */
    public DefinitionsFactoryException(String message) {
        super(message);
    }


  /**
    * Create a new <code>DefinitionsFactoryException</code> wrapping an existing exception.
    *
    * @param e The exception to be wrapped.
    */
    public DefinitionsFactoryException(Exception e) {
        super();
    }


  /**
    * Create a new <code>DefinitionsFactoryException</code> from an existing exception.
    *
    * @param message The detail message.
    * @param e The root cause exception
    */
    public DefinitionsFactoryException(String message, Exception e) {
        super(message, e);
    }
}
