package spiralcraft.stream.file;

import spiralcraft.stream.ResourceFactory;
import spiralcraft.stream.Resource;
import spiralcraft.stream.UnresolvableURIException;

import java.net.URI;

public class FileResourceFactory
  implements ResourceFactory

{

  public Resource resolve(URI uri)
    throws UnresolvableURIException
  { return new FileResource(uri);
  }



}
