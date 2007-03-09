package spiralcraft.data;

public class RuntimeDataException
    extends RuntimeException
{
  public static final long serialVersionUID=0;
  
  public RuntimeDataException(String message,DataException cause)
  { super(message,cause);
  }
}
