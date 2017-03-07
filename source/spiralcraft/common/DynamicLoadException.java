package spiralcraft.common;

/**
 * A failure to dynamically load or bind a functional component into the
 *   application process.
 */
public class DynamicLoadException
  extends ContextualException
{

  private static final long serialVersionUID = 1L;

  public DynamicLoadException(String message,Object context)
  { super(message,context);
  }

  public DynamicLoadException(String message,Object context,Throwable nested)
  { super(message,context,nested);
  }
}
