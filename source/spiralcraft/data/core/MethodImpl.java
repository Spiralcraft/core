package spiralcraft.data.core;

import java.net.URI;

import spiralcraft.data.DataException;
import spiralcraft.data.Method;
import spiralcraft.data.Type;
import spiralcraft.data.lang.DataReflector;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.Signature;
import spiralcraft.log.ClassLog;
import spiralcraft.util.refpool.URIPool;

public abstract class MethodImpl
  implements Method
{
  protected static final ClassLog log
    =ClassLog.getInstance(MethodImpl.class);
  
  private static final Type<?>[] NULL_TYPES=new Type<?>[0];
  
  private Type<?> dataType;
  protected Type<?> returnType;
  protected boolean staticMethod;
  private String name;
  private Type<?>[] parameterTypes;
  private String qualifiedName;
  private boolean locked;
  private URI uri;
  private String description;
  
  protected boolean debug;
  
  @Override
  public URI getURI()
  { return uri;
  }
  
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

  public void setDescription(String description)
  { this.description=description;
  }
  
  @Override
  public String getDescription()
  { return description;
  }
  
  public void setDebug(boolean debug)
  { this.debug=debug;
  }
  
  @Override
  public Type<?>[] getParameterTypes()
  { 
    assertTypes();
    return parameterTypes!=null?parameterTypes:NULL_TYPES;
  }

  public void setParameterTypes(Type<?>[] types)
  { parameterTypes=types;
  }

  @Override
  public Type<?> getReturnType()
  { 
    assertTypes();
    return returnType;
  }

  public void setReturnType(Type<?> type)
  { returnType=type;
  }
  
  @Override
  public boolean isStatic()
  { return staticMethod;
  }
  
  
  public void setStatic(boolean staticMethod)
  { this.staticMethod=staticMethod;
  }
  

  private void lock()
  { locked=true;
  }

  private void assertTypes()
  { 
    if (locked)
    {
      if (returnType==null)
      { resolveTypes();
      }
      if (returnType==null)
      { throw new RuntimeException("Method return type is null for "+qualifiedName);
      }
    }
  }
  
  protected void updateTypes(Type<?> returnType,Type<?>[] parameterTypes)
  {
    this.returnType=returnType;
    this.parameterTypes=parameterTypes;
  }
  
  protected void resolveTypes()
  {
  }
  
  /**
   * Resolve any external dependencies.
   * 
   * @throws DataException
   */
  protected void resolve()
    throws DataException
  {
    if (!locked)
    { lock();
    }
    
    subclassResolve();
    qualifiedName=getDataType().getURI()+"!"+name;
    uri=URIPool.create(qualifiedName);
  }
  
  protected void subclassResolve()
    throws DataException
  { }
  
  @Override
  public Signature getSignature()
    throws BindException
  { 
    
    Reflector<?>[] paramR;
    if (parameterTypes!=null)
    {
      paramR=new Reflector<?> [parameterTypes.length];
      for (int i=0;i<parameterTypes.length;i++)
      { paramR[i]=DataReflector.getInstance(parameterTypes[i]);
      }
    }
    else
    { paramR=new Reflector<?>[0];
    }
    return new Signature
             (getName()
             ,DataReflector.getInstance(getReturnType())
             ,paramR
             );

  }
}



