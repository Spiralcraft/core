package spiralcraft.text.markup;

public class MarkupException
  extends Exception
{
  public MarkupException(String message)
  { super(message);
  }
  
  public MarkupException(String message,Throwable cause)
  { super(message,cause);
  }

  public MarkupException(Throwable cause)
  { super(cause);
  }
}
