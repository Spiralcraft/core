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
   * Return the name of this node relative to its parent node.
   * The name of the root node is ""
   */
  public String getName();

  /**
   * Return the absolute path of this node relative to the ClassLoader
   *   to which the Registry package is scoped.
   */
  public String getAbsolutePath();

  /**
   * Obtain the instance of the specified class 
   *   that has been registered with this RegistryNode or
   *   its ancestors. 
   */
  public Object findInstance(Class instanceClass);

  /**
   * Register an instance that will be visible from
   *   this RegistryNode and its descendants.
   */
  public void registerInstance(Class instanceClass,Object instance);


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
