package spiralcraft.registry;

/**
 * Implemented by objects that wish to be referencable
 *   within a global name hierarchy. Registrants are
 *   automatically added to the hierarchy by whichever
 *   container mechanism is responsible for defining 
 *   the component's 'name'.
 */
public interface Registrant
{

  /**
   * Called by containers to supply the Registrant
   *   with its RegistryNode, which the Registrant
   *   can use to obtain references to hierachically
   *   organized application services such as logging,
   *   monitoring and lightweight persistence (ie. preferences)
   */
  public void register(RegistryNode node);

}
