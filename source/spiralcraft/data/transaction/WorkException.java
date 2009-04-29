package spiralcraft.data.transaction;

public class WorkException
  extends TransactionException
{

  private static final long serialVersionUID = 1324491988433241728L;

  public WorkException(String message,Throwable cause)
  { super(message,cause);
  }
}
