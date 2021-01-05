package spiralcraft.builder;

import java.net.URI;

public class AssemblyNotFoundException
  extends BuildException
{

  private static final long serialVersionUID = -5450877714680650589L;

  public AssemblyNotFoundException(String msg,URI assemblyURI)
  { super(msg,assemblyURI);
  }

  public AssemblyNotFoundException(String msg,URI assemblyURI,Exception cause)
  { super(msg,assemblyURI,cause);
  }
}
