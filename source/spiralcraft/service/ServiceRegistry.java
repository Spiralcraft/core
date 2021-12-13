package spiralcraft.service;

import java.util.ArrayList;

/**
 * A means for application components to find services 
 * 
 * @author mike
 *
 */
public class ServiceRegistry
{
  
  private final ServiceRegistry parent;
  private final ArrayList<Reg> services=new ArrayList<>();
  
  class Reg
  {
    final Class<?> wellKnownClass;
    final String id;
    final Object service;

    Reg(Object service,Class<?> wellKnownClass,String id)
    {
      this.wellKnownClass=wellKnownClass;
      this.id=id;
      this.service=service;
    }
    
  }
  
  public ServiceRegistry()
  { parent=null;
  }
  
  public ServiceRegistry(ServiceRegistry parent)
  { this.parent=parent;
  }
  
  public void register(Object service,Class<?> wellKnownClass,String id)
    throws RegistrationException
  {
    if (getLocal(wellKnownClass,id)==null)
    { services.add(new Reg(service,wellKnownClass,id));
    }
    else
    { throw new RegistrationException
        ("ServiceRegistry already contains a registration for this class/id");
    }
  }
  
  private Object getLocal(Class<?> wellKnownClass,String id)
  { 
    for (Reg reg: services)
    { 
      if (wellKnownClass.equals(reg.wellKnownClass))
      {
        if ( (id==null && reg.id==null) || id.equals(reg.id))
        { return reg.service;
        }
      }
    }
    return null;
  }
  
  @SuppressWarnings("unchecked")
  public <S> S get(Class<S> wellKnownClass,String id)
  {
    Object service=getLocal(wellKnownClass,id);
    if (service==null && parent!=null)
    { service=parent.<S>get(wellKnownClass, id);
    }
    if (service!=null)
    { return (S) service;
    }
    else
    { return null;
    }
  }
}
