package spiralcraft.builder;

public class BuildException
  extends Exception
{
  public BuildException(String message)
  { super(message);
  }

  public BuildException(String message,Exception nested)
  { super(message,nested);
  }
}
