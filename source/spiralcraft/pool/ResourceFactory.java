package spiralcraft.pool;

/**
 * Provides the application specific resources to be managed
 *   by the Pool.
 */
public interface ResourceFactory
{
  /**
   * Create a new instance of a resource to be added to the Pool.
   */
  public Object createResource();

  /**
   * Discard a resource when no longer needed by the Pool.
   */
  public void discardResource(Object resource);

}
