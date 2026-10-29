package org.apache.struts.tiles;


/**
 * Root class for all Tiles-exceptions.
 */
public class TilesException extends Exception
{
  private static final long serialVersionUID = -2512402467536260269L;


  /**
    * Constructor.
    */
  public TilesException()
    {
    super();
  }

  /**
    * Constructor.
    * @param message The error or warning message.
    */
  public TilesException(String message)
    {
    super(message);
  }


  /**
    * Create a new <code>TilesException</code> wrapping an existing exception.
    *
    * @param e The root cause exception
    */
  public TilesException(Exception e)
  {
    super(e);
  }


  /**
    * Create a new <code>TilesException</code> from an existing exception.
    *
    * @param message The detail message.
    * @param e The root cause exception
    */
  public TilesException(String message, Exception e)
  {
    super(message, e);
  }


  /**
    * Return the embedded exception, if any.
    *
    * @return The root cause exception, or <code>null</code> if there is none.
    */
  public Exception getException ()
  {
    return (Exception) getCause();
  }

}
