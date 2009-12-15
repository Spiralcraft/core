package spiralcraft.lang.spi;

import java.net.URI;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Decorator;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;

public class ExtendedReflector<T>
  extends AbstractReflector<T>
{

  protected final Reflector<T> baseReflector;

  public ExtendedReflector(Reflector<T> baseReflector)
  { this.baseReflector=baseReflector;
  }
  
  @Override
  public  <X> Channel<X> resolve
    (Channel<T> source
    ,Focus<?> focus
    ,String name
    ,Expression<?>[] params
    )
    throws BindException
  { 
    if (name.startsWith("@"))
    { 
      Channel<X> ret=this.<X>resolveMeta(source,focus,name,params);
      if (ret!=null)
      { return ret;
      }
    }
    
    Channel<X> ret=baseReflector.resolve(source,focus,name,params);
    if (ret!=null)
    { 
      // Lookup name to find a Functor, which is bindable to
      //  source, focus, params
      
    
    }
    return ret;
    
  }
 

  @Override
  public URI getTypeURI()
  { return baseReflector.getTypeURI();
  }

  @Override
  public boolean isAssignableTo(
    URI typeURI)
  { return baseReflector.isAssignableTo(typeURI);
  }

  @Override
  public <D extends Decorator<T>> D decorate(
    Channel<T> source,
    Class<D> decoratorInterface)
    throws BindException
  { return baseReflector.<D>decorate(source,decoratorInterface);
  }

  @Override
  public Class<T> getContentType()
  { return baseReflector.getContentType();
  }



}
