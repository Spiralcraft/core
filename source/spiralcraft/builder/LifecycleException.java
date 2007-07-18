package spiralcraft.builder;

/**
 * An exception related to the failure of a Lifecycle implementation to start
 *   or stop.
 */
public class LifecycleException
  extends Exception
{

  private static final long serialVersionUID = 1L;

  public LifecycleException(String message)
  { super(message);
  }

  public LifecycleException(String message,Throwable nested)
  { super(message,nested);
  }
}
