package spiralcraft.tuple;

import java.net.URI;


/**
 * Resolves URIs into Scheme objects
 */
public interface SchemeResolver
{
  /**
   * Return the Scheme associated with the specified URI
   */
  Scheme resolveScheme(URI uri);
}
