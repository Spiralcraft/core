package spiralcraft.lang;

import java.net.URI;

/**
 * An interface which allows an Expression container to define a set of
 *   namespace prefixes.
 * 
 * @author mike
 */
public interface NamespaceResolver
{

  public URI resolveNamespace(String prefix);
  
  public URI getDefaultNamespaceURI();
}
