package spiralcraft.service;

/**
 * Represents a software subsystem which cooperates with other such
 *   subsystems to implement an application.
 *
 * A Service instance has a life cycle longer than the components
 *   which use it, and normally has a state which involves
 *   external resources. The init() and destroy() methods are used
 *   by a container to give the Service an opportunity to manage
 *   its state after it has been configured and before it is finalized.
 *
 * The Service's container is responsible for providing an implementation
 *   of the ServiceResolver interface, which allows a Service to obtain
 *   references to other Services on which it depends.
 */
public interface Service
{

  
  /**
   * Return the selector which differentiates this Service from others of the
   *   same type.
   */
  public Object getSelector();

  /**
   * Indicate whether the Service provides the specified interface
   */
  public boolean providesInterface(Class serviceInterface)
    throws AmbiguousServiceException;

  /**
   * Obtain an instance of the specified interface from the service
   */
  public Object getInterface(Class serviceInterface)
    throws AmbiguousServiceException;


  /**
   * Initialize the service by resolving all appropriate 
   *   resources and dependent services.
   */
  public void init(ServiceResolver resolver)
    throws ServiceException;

  /**
   * Shut down the service and release all resources
   */
  public void destroy()
    throws ServiceException;
}
