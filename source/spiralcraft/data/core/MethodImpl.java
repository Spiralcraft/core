package spiralcraft.data.core;

import spiralcraft.data.DataException;
import spiralcraft.data.Method;
import spiralcraft.data.Type;

import spiralcraft.log.ClassLog;

public abstract class MethodImpl
  implements Method
{
  protected static final ClassLog log
    =ClassLog.getInstance(MethodImpl.class);
  
  private static final Type<?>[] NULL_TYPES=new Type<?>[0];
  
  private Type<?> dataType;
  protected Type<?> returnType;
  private String name;
  private Type<?>[] parameterTypes;
  private String qualifiedName;
  private boolean locked;
  
  protected boolean debug;
  
  
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
  { return parameterTypes!=null?parameterTypes:NULL_TYPES;
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
   * 
   * @throws DataException
   */
  public void resolve()
    throws DataException
  {
    if (!locked)
    { lock();
    }
    subclassResolve();
    qualifiedName=getDataType().getURI()+"!"+name;
  }
  
  @SuppressWarnings("unused")
  protected void subclassResolve()
    throws DataException
  { }
  

}



