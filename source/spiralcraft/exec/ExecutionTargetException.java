package spiralcraft.exec;

/**
 * Thrown when something goes wrong during execution
 */
public class ExecutionTargetException
  extends Exception
{
  private final Throwable _targetException;
  
  public ExecutionTargetException(Throwable targetException)
  { _targetException=targetException;
  }
  
  public Throwable getTargetException()
  { return _targetException;
  }
}
