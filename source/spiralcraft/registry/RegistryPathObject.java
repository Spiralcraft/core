package spiralcraft.registry;

/**
 * An interface which allows objects that provide services through the Registry
 *   to create a child associated with a specific RegistryNode.
 */
public interface RegistryPathObject
{
  /**
   * Create a new RegistryPathObject for the path element named
   *   as specifed (relative to this RegistryPathObject). This method will be called
   *   once as each node in the Registry is traversed in descending order.
   */
  public RegistryPathObject registryPathObject(RegistryNode childNode);
}
