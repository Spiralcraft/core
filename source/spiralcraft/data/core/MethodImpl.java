package spiralcraft.data.core;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import spiralcraft.common.declare.Declarable;
import spiralcraft.common.declare.DeclarationInfo;
import spiralcraft.data.DataException;
import spiralcraft.data.Method;
import spiralcraft.data.Type;
import spiralcraft.data.lang.DataReflector;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.Signature;
import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.SimpleChannel;
import spiralcraft.log.ClassLog;

import spiralcraft.util.refpool.URIPool;

public abstract class MethodImpl
  implements Method,Declarable
{
  protected static final ClassLog log
    =ClassLog.getInstance(MethodImpl.class);
  
  private static final Type<?>[] NULL_TYPES=new Type<?>[0];
  
  private Type<?> dataType;
  protected Type<?> returnType;
  protected boolean derivedReturnType;
  protected boolean staticMethod;
  private String name;
  private Type<?>[] parameterTypes;
  private String qualifiedName;
  private boolean locked;
  private URI uri;
  private String description;
  protected Focus<MethodImpl> selfFocus;
  protected boolean generic;
  
  protected boolean debug;
  
  protected DeclarationInfo declarationInfo;
  
  @Override
  public void setDeclarationInfo(DeclarationInfo di)
  { this.declarationInfo=di;
  }
  
  @Override
  public DeclarationInfo getDeclarationInfo()
  { return declarationInfo;
  }
  
  @Override
  public URI getURI()
  { 
    if (uri!=null)
    { return uri;
    }
    else if (dataType!=null)
    { 
      String qualifiedName=getDataType().getURI()+"!"+name;
      return URIPool.create(qualifiedName);
    }
    else return URIPool.create("?!"+name);
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
  public boolean isGeneric()
  { return generic;
  }
  
  public void setGeneric(boolean generic)
  { this.generic=generic;
  }

  
  @Override
  public boolean isStatic()
  { return staticMethod;
  }
  
  
  public void setStatic(boolean staticMethod)
  { this.staticMethod=staticMethod;
  }
  
  @Override
  public boolean isReturnTypeDerived()
  { return derivedReturnType;
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
      if (returnType==null && !derivedReturnType)
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
  
  protected MethodImpl extend()
  {
    try
    {
      MethodImpl m=getClass().getDeclaredConstructor().newInstance();
      m.returnType=returnType;
      m.staticMethod=staticMethod;
      m.name=name;
      m.parameterTypes=parameterTypes;
      m.description=description;
      m.debug=debug;
      m.declarationInfo=declarationInfo;
      return m;
    }
    catch (IllegalAccessException x)
    { throw new RuntimeException(x);
    }
    catch (InstantiationException x)
    { throw new RuntimeException(x);
    }
    catch (InvocationTargetException x)
    { throw new RuntimeException(x);
    }
    catch (NoSuchMethodException x)
    { throw new RuntimeException(x);
    }
    
    
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
    
    qualifiedName=getDataType().getURI()+"!"+name;
    uri=URIPool.create(qualifiedName);
    try
    {
      selfFocus=getDataType().getSelfFocus().chain
       (new SimpleChannel<MethodImpl>
         (BeanReflector.<MethodImpl>getInstance(getClass()),this,true)
       );
      selfFocus.addAlias(URIPool.create("class:/spiralcraft/data/types/meta/Method"));
    }
    catch (BindException x)
    { throw new DataException("Error binding "+getURI(),getDeclarationInfo(),x);
    }
    subclassResolve();
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
    if (getReturnType()==null)
    { throw new BindException("Method "+getURI()+" does not specify a return type");
    }
    return new Signature
             (getName()
             ,DataReflector.getInstance(getReturnType())
             ,paramR
             );

  }
  
  @Override
  public String toString()
  { return super.toString()+": "+getURI();
  }
  
}



