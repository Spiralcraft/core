package spiralcraft.stream;

import java.net.URI;

/**
 * Resolve the resource corresponding to the specified URI.
 */
public interface ResourceFactory
{
  public Resource resolve(URI uri)
    throws UnresolvableURIException;

}
