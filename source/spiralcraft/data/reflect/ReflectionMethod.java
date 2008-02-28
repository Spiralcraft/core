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


import spiralcraft.data.DataException;
import spiralcraft.data.Type;
import spiralcraft.data.TypeNotFoundException;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.DataComposite;


public class ReflectionMethod
  extends MethodImpl
{
  protected final TypeResolver resolver;
  private java.lang.reflect.Method method;

  
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
    if (target instanceof DataComposite)
    { 
      throw new DataException
        ("Can't invoke method "+getQualifiedName()+" on data");
    }
    
    try
    { return method.invoke(target,params);
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
  
  public void resolveType()
    throws DataException
  {
    try
    { 
      setReturnType(findType(method.getReturnType()));
      Class<?>[] formalTypes=method.getParameterTypes();
      Type<?>[] parameterTypes=new Type[formalTypes.length];
      
      for (int i=0;i<formalTypes.length;i++)
      { parameterTypes[i]=findType(formalTypes[i]);
      }
      setParameterTypes(parameterTypes);
    }
    catch (TypeNotFoundException x)
    { 
      // This should NEVER happen- there always exists a Type for
      //   every java class
      x.printStackTrace();
    }
  }
  

  protected Type<?> findType(Class<?> iface)
    throws TypeNotFoundException
  { 
    URI uri=ReflectionType.canonicalURI(iface);
    return resolver.resolve(uri);
  }
}
