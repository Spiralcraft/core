package spiralcraft.stream.batch;

public class OperationException
  extends Exception
{
  private final Operation _operation;
  
  public OperationException(Operation operation,String message)
  { 
    super(message);
    _operation=operation;
  }
  
  public OperationException(Operation operation,String message,Throwable cause)
  { 
    super(message,cause);
    _operation=operation;
  }
}
