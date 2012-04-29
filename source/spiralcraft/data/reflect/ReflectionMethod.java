//
// Copyright (c) 1998,2007 Michael Toth
// Spiralcraft Inc., All Rights Reserved
//
// This package is part of the Spiralcraft project and is licensed under
// a multiple-license framework.
//
// You may not use this file except in compliance with the terms found in the
// SPIRALCRAFT-LICENSE.txt file at the top of this distribution, or available
// at http://www.spiralcraft.org/licensing/SPIRALCRAFT-LICENSE.txt.
//
// Unless otherwise agreed to in writing, this software is distributed on an
// "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.data.reflect;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;


import spiralcraft.data.core.MethodImpl;
import spiralcraft.data.lang.DataReflector;


import spiralcraft.data.DataException;
import spiralcraft.data.RuntimeDataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.DataComposite;
import spiralcraft.lang.AccessException;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.spi.SourcedChannel;
import spiralcraft.util.ArrayUtil;


public class ReflectionMethod
  extends MethodImpl
{
  
  protected final TypeResolver resolver;
  private java.lang.reflect.Method method;
  private boolean debug;
  
  
  public ReflectionMethod(TypeResolver resolver,java.lang.reflect.Method method)
  {
    this.method=method;
    setName(method.getName());
    this.resolver=resolver;
            
  }
  
  public java.lang.reflect.Method getMethod()
  { return method;
  }
  
  public Object invoke(Object target,Object[]params)
    throws DataException
  { 
    if (debug)
    { 
      log.fine("Invoking "+method+" on "+target+" with "
              +ArrayUtil.format(params,"\r\n  param:","")
              );
    }
    if (target instanceof DataComposite)
    { 
      if (target instanceof Tuple)
      { 
        Object behavior=((Tuple) target).getBehavior();
        if (behavior!=null)
        { target=behavior;
        }
        else
        {
          throw new DataException
            ("Failed to get behavior object from Tuple to invoke "
            +" method "+getQualifiedName()+" on data"
            );
          
        }
      }
      else
      {
        throw new DataException
          ("Can't invoke method "+getQualifiedName()+" on data "+target);
        
      }
    }
    
    
    Object[] behaviorParams=new Object[params.length];
    for (int i=0;i<behaviorParams.length;i++)
    {
      if (params[i] instanceof Tuple)
      {
        behaviorParams[i]=((Tuple) params[i]).getBehavior();
      }
      else
      { behaviorParams[i]=params[i];
      }
    }
           
    try
    { 
      
      
      
      return method.invoke(target,behaviorParams);
    }
    catch (InvocationTargetException x)
    { 
      throw new DataException
        ("Error invoking method "+getQualifiedName()
        ,x.getTargetException()
        );
    }
    catch (IllegalAccessException x)
    {
      throw new DataException
      ("Error invoking method "+getQualifiedName()
      ,x
      );
    }
  }
  
  @Override
  protected void resolveTypes()
  { 
    try
    {
      Type<?> returnType=findType(method.getReturnType());
      Class<?>[] formalTypes=method.getParameterTypes();
      Type<?>[] parameterTypes=new Type<?>[formalTypes.length];
    
      for (int i=0;i<formalTypes.length;i++)
      { parameterTypes[i]=findType(formalTypes[i]);
      }
      updateTypes(returnType,parameterTypes);
    }
    catch (DataException x)
    { 
      throw new RuntimeDataException
        ("Error resolving types for method "+getQualifiedName(),x);
    }
  }
  
  protected Type<?> findType(Class<?> iface)
    throws DataException
  { 
    if (iface==null)
    { return Type.resolve("class:/java/lang/Void");
    }
    URI uri=ReflectionType.canonicalURI(iface);
    return resolver.resolve(uri);
  }
  
  @Override
  public Channel<?> bind(
    Channel<?> source,
    Channel<?>[] params)
    throws BindException
  { 
    Channel<?> binding=source.getCached(this);
    if (binding==null)
    { 
      binding=new ReflectionMethodChannel(source,params);
      source.cache(this,binding);
    }
    return binding;
  }


  @SuppressWarnings({ "unchecked", "rawtypes" })
  class ReflectionMethodChannel
    extends SourcedChannel
  {
    protected final Channel<?>[] params;
    
    public ReflectionMethodChannel
      (Channel<?> source,Channel<?>[] params)
      throws BindException
    { 
      super(DataReflector.getInstance(returnType),source);
      this.params=params;
    }

    @Override
    public boolean isWritable()
    { return false;
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
      { throw new AccessException(x.toString(),x);
      }
      
    }
    
    

    @Override
    protected boolean store(Object val)
    { return false;
    }

  }  
}
