package spiralcraft.service;

/**
 * A group of Services managed as unit
 */
public class ServiceGroup
{
  private Service[] _services;

  public void setServices(Service[] services)
  { _services=services;
  }

  public Service[] getServices()
  { return _services;
  }

  /**
   * Initialize Services. If an exception occurs, all Services
   *   previously initialized will be destroyed before an
   *   exception is thrown from this method.
   */
  public void init()
    throws ServiceException
  { 
    for (int i=0;i<_services.length;i++)
    { 
      try
      { _services[i].init();
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
    for (int i=0;i<_services.length;i++)
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

}
