package spiralcraft.stream.classpath;

import spiralcraft.stream.ResourceFactory;
import spiralcraft.stream.Resource;
import spiralcraft.stream.UnresolvableURIException;

import java.net.URI;

public class ClasspathResourceFactory
  implements ResourceFactory
{

  public Resource resolve(URI uri)
    throws UnresolvableURIException
  { return new ClasspathResource(uri);
  }



}
