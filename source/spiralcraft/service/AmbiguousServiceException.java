package spiralcraft.service;

/**
 * Exception thrown when multiple services with a requested interface and key 
 *   exist within the same context.
 */
public class AmbiguousServiceException
  extends Exception
{
  private Class _serviceInterface;
  private Object _key;

  public AmbiguousServiceException(Class serviceInterface,Object key)
  { 
    _serviceInterface=serviceInterface;
    _key=key;
  }

  public String toString()
  { return super.toString()+": "+_serviceInterface.getName()+"["+_key.toString()+"]";
  }

}
