package spiralcraft.stream;

import java.net.URI;
import java.io.IOException;

public class UnresolvableURIException
  extends IOException
{
  public UnresolvableURIException(URI uri,String message)
  { super(message+": "+uri.toString());
  }
}
