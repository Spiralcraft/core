package spiralcraft.builder;

public class PersistenceException
  extends BuildException
{
  public PersistenceException(String message)
  { super(message);
  }

  public PersistenceException(String message,Exception nested)
  { super(message,nested);
  }
}
