package spiralcraft.tuple;

public class TupleException
  extends Exception
{
  public TupleException()
  { }
  
  public TupleException(String message)
  { super(message);
  }
  
  public TupleException(String message,Throwable cause)
  { super(message,cause);
  }
}
