package spiralcraft.common;

import java.net.URI;

/**
 * An interface which resolves namespace prefixes to namespace URIs. Used to
 *   share namespace mappings defined in resources (ie. XML, webui) with any
 *   consumer, and allow for programmatic control of available namespaces.
 * 
 * @author mike
 */
public interface NamespaceResolver
{

  /**
   * Resolve a prefix to a namespace URI.
   * 
   * @param prefix
   * @return the URI mapped to the prefix, or null if no URI is mapped
   */
  URI resolvePrefix(String prefix);
  
  /**
   * 
   * @return The "default" namespace mapping, or null if no default
   *   has been defined.
   */
  URI getDefaultURI();
}
