package spiralcraft.stream;

public class AlreadyRegisteredException
  extends Exception
{
  public AlreadyRegisteredException(String scheme)
  { super(scheme);
  }
}
