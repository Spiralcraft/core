package spiralcraft.exec;

/**
 * Thrown when something goes wrong during execution
 */
public class ExecutionTargetException
  extends ExecutionException
{
  private final Throwable _targetException;
  
  public ExecutionTargetException(Throwable targetException)
  { 
    super("");
    _targetException=targetException;
    if (_targetException!=null)
    { initCause(_targetException);
    }
  }
  
  public Throwable getTargetException()
  { return _targetException;
  }
  
  public String toString()
  { return super.toString()+" caused by "+_targetException.toString();
  }
}
