package spiralcraft.service;

public class ServiceAdapter
  implements Service
{

  private Object _selector;

  public void init(ServiceResolver resolver)
    throws ServiceException
  {
  }

  public void destroy()
    throws ServiceException
  {
  }

  public boolean providesInterface(Class serviceClass)
  { return serviceClass.isAssignableFrom(getClass());
  }

  public Object getInterface(Class serviceClass)
  { 
    if (serviceClass.isAssignableFrom(getClass()))
    { return this;
    }
    else
    { return null;
    }
  }

  public Object getSelector()
  { return _selector;
  }

  public void setSelector(Object val)
  { _selector=val;
  }

}
