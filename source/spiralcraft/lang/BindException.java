package spiralcraft.lang;

public class BindException
  extends Exception
{
  public BindException(String message)
  { super(message);
  }

  public BindException(String message,Throwable cause)
  { super(message,cause);
  }
}
