package spiralcraft.data.core;

import spiralcraft.data.DataException;
import spiralcraft.data.Method;
import spiralcraft.data.Type;

import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.spi.AbstractChannel;

public abstract class MethodImpl
  implements Method
{

  private Type<?> dataType;
  private Type<?> returnType;
  private Reflector<?> returnReflector;
  private String name;
  private Type<?>[] parameterTypes;
  private String qualifiedName;
  private boolean locked;
  
  
  @Override
  public Type<?> getDataType()
  { return dataType;
  }

  public void setDataType(Type<?> type)
  { dataType=type;
  }
  
  @Override
  public String getName()
  { return name;
  }
  
  public String getQualifiedName()
  { return qualifiedName;
  }
  
  public  void setName(String name)
  { this.name=name;
  }

  @Override
  public Type<?>[] getParameterTypes()
  { return parameterTypes;
  }

  public void setParameterTypes(Type<?>[] types)
  { parameterTypes=types;
  }

  @Override
  public Type<?> getReturnType()
  { return returnType;
  }

  public void setReturnType(Type<?> type)
  { returnType=type;
  }
  

  private void lock()
  { locked=true;
  }

  /**
   * Resolve any external dependencies.
   */
  void resolve()
    throws DataException
  {
    if (!locked)
    { lock();
    }
    subclassResolve();
    qualifiedName=getDataType().getURI()+"!"+name;
  }
  
  protected void subclassResolve()
    throws DataException
  { }
  
  @Override
  public Channel<?> bind(
    Focus<?> focus,
    Channel<?> source,
    Channel<?>[] params)
    throws BindException
  { 
    Channel<?> binding=source.getCached(this);
    if (binding==null)
    { 
      binding=new SimpleChannel(source,params);
      source.cache(this,binding);
    }
    return binding;
  }


  @SuppressWarnings("unchecked")
  class SimpleChannel
    extends AbstractChannel
  {
    protected final Channel<?> source;
    protected final Channel<?>[] params;

    public SimpleChannel(Channel<?> source,Channel<?>[] params)
    { 
      super(returnReflector);
      this.source=source;
      this.params=params;
    }

    @Override
    protected Object retrieve()
    {
      Object o=source.get();
      if (o==null)
      { 
        // Defines x.f() to be null if x is null
        return null;
      }

      Object[] oParams=new Object[params.length];
      for (int i=0;i<params.length;i++)
      { oParams[i]=params[i].get();
      }
      
      try
      { return invoke(o,oParams);
      }
      catch (DataException x)
      { return new AccessException(x.toString(),x);
      }
      
    }

    @Override
    protected boolean store(Object val)
    { return false;
    }

  }
}



