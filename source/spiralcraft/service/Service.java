package spiralcraft.service;

/**
 * Represents a software subsystem which cooperates with other such
 *   subsystems to implement an application.
 */
public interface Service
{

  /**
   * Initialize the service by resolving all appropriate 
   *   resources.
   */
  public void init()
    throws ServiceException;

  /**
   * Shut down the service and release all resources
   */
  public void destroy()
    throws ServiceException;
}
