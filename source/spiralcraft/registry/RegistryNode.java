package spiralcraft.registry;

/**
 * Provides access to hierarchically organized application
 *   services for a single Registrant, such as logging,
 *   monitoring and lightweight persistence (ie. preferences).
 *
 * A hierarchy of RegistryNodes provides a unified, externally
 *   referencable namespace which external services can use to
 *   associate service instances with individual components.
 *
 * For example, using the fully qualified component path, logging
 *   services can tailor logging preferences for individual components,
 *   monitoring services can be activated for indivudual components
 *   and configuration services can key configuration details to
 *   individual components.
 */
public interface RegistryNode
{
  /**
   * Obtain the node associated with this RegistryNode of
   *   a specific service.
   */
  public Object obtainServiceNode(Class serviceInterface);

  /** 
   * Create a child RegistryNode and register it with this node
   *   under the specified name
   */
  public RegistryNode createChild(String name);
  
  /**
   * Return the child RegistryNode with the specified name
   */
  public RegistryNode getChild(String name);
}
