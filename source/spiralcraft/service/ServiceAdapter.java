package spiralcraft.service;

public class ServiceAdapter
  implements Service
{

  private Object _key;

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

  public Object getKey()
  { return _key;
  }

  public void setKey(Object val)
  { _key=val;
  }

}
