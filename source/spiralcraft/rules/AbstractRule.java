package spiralcraft.rules;

public abstract class AbstractRule<C, T>
  implements Rule<C, T>
{
  private String message;
  protected C context;
  protected boolean debug;
  
  public void setMessage(String message)
  { this.message=message;
  }
  
  public String getMessage()
  { return message;
  }
  
  public C getContext()
  { return context;
  }
  
  public void setContext(C context)
  { this.context=context;
  }
  
  public void setDebug(boolean debug)
  { this.debug=debug;
  }

}
