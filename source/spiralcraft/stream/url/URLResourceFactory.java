package spiralcraft.stream.url;

import spiralcraft.stream.ResourceFactory;
import spiralcraft.stream.Resource;
import spiralcraft.stream.UnresolvableURIException;

import java.net.URI;

public class URLResourceFactory
  implements ResourceFactory

{

  public Resource resolve(URI uri)
    throws UnresolvableURIException
  { return new URLResource(uri);
  }



}
