package spiralcraft.service;

import spiralcraft.util.ArrayUtil;

/**
 * A group of Services managed as unit
 */
public class ServiceGroup
  implements ServiceResolver
{
  private Service[] _services;
  private ServiceResolver _parentResolver;
  private Object _selector;

  private static boolean serviceHasSelector(Service service,Object selector)
  {
    if (selector==null)
    { return service.getSelector()==null;
    }
    else
    { return selector.equals(service.getSelector());
    }
  }

  public void setServices(Service[] services)
  { _services=services;
  }

  public Service[] getServices()
  { return _services;
  }

  public Service[] findServices(Class serviceInterface)
    throws AmbiguousServiceException
  { 
    Service[] locals=new Service[_services.length];
    int j=0;
    for (int i=0;i<_services.length;i++)
    { 
      if (_services[i].providesInterface(serviceInterface))
      { locals[j++]=_services[i];
      }
    }
    locals=(Service[]) ArrayUtil.truncate(locals,j);
    
    if (_parentResolver!=null)
    { return combineServices(locals,_parentResolver.findServices(serviceInterface));
    }
    else
    { return locals;
    }
  }


  private Service[] combineServices(Service[] locals,Service[] parents)
  {
    Service[] inherited=new Service[parents.length];
    int k=0;
    for (int i=0;i<parents.length;i++)
    {
      boolean masked=false;
      Object parentSelector=parents[i].getSelector();
      for (int j=0;j<locals.length;j++)
      { 
        if (serviceHasSelector(locals[j],parentSelector))
        {   
          masked=true;
          break;
        }
      }
      if (!masked)
      { inherited[k++]=parents[i];
      }
    }
    return (Service[]) ArrayUtil.appendArrays(locals,ArrayUtil.truncate(inherited,k));    
  }

  public Service findService(Class serviceInterface,Object selector)
    throws AmbiguousServiceException
  { 
    Service ret=null;
    for (int i=0;i<_services.length;i++)
    { 
      if (_services[i].providesInterface(serviceInterface)
          && serviceHasSelector(_services[i],selector)
         )
      {
        if (ret!=null)
        { throw new AmbiguousServiceException(serviceInterface,selector);
        }
        else
        { ret=_services[i];
        }
      }
    }
    if (ret==null && _parentResolver!=null)
    { ret=_parentResolver.findService(serviceInterface,selector);
    }
    return ret;
  }

  /**
   * Initialize Services. If an exception occurs, all Services
   *   previously initialized will be destroyed before an
   *   exception is thrown from this method.
   */
  public void init(ServiceResolver resolver)
    throws ServiceException
  { 
    if (resolver!=this)
    { _parentResolver=resolver;
    }

    for (int i=0;i<_services.length;i++)
    { 
      try
      { _services[i].init(this);
      }
      catch (ServiceException x)
      { 
        for (int j=i-1;j>=0;j--)
        { 
          try
          { _services[j].destroy();
          }
          catch (ServiceException y)
          { y.printStackTrace();
          }
        }
        throw x;
      }
    }
  }

  /**
   * Destroy Services. If an exception occurs, it will be
   *   logged and remaining Services will be destroyed before
   *   an exception is thrown. If multiple exceptions occur,
   *   the last exception will be thrown and the rest will be
   *   logged.
   */
  public void destroy()
    throws ServiceException
  {
    ServiceException exception=null;
    for (int i=_services.length;i-->0;)
    { 
      try
      { _services[i].destroy();
      }
      catch (ServiceException x)
      { 
        if (exception!=null)
        { exception.printStackTrace();
        }
        exception=x;
      }
    }
    if (exception!=null)
    { throw exception;
    }
  }

  public Object getSelector()
  { return _selector;
  }
  
  public void setSelector(Object val)
  { _selector=val;
  }

  public boolean providesInterface(Class serviceInterface)
    throws AmbiguousServiceException
  {
    boolean found=false;
    for (int i=0;i<_services.length;i++)
    { 
      if (_services[i].providesInterface(serviceInterface))
      { 
        if (found)
        { throw new AmbiguousServiceException(serviceInterface,null);
        }
        else
        { found=true;
        }
      }
    }
    return found;
  }

  public Object getInterface(Class serviceInterface)
    throws AmbiguousServiceException
  {
    Object ret=null;
    for (int i=0;i<_services.length;i++)
    { 
      if (_services[i].providesInterface(serviceInterface))
      { 
        if (ret!=null)
        { throw new AmbiguousServiceException(serviceInterface,null);
        }
        else
        { ret=_services[i].getInterface(serviceInterface);
        }
      }
    }
    return ret;
  }
}
