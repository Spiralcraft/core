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
package spiralcraft.data.core;

import java.net.URI;

import spiralcraft.data.TypeResolver;
import spiralcraft.data.TypeFactory;
import spiralcraft.data.Type;
import spiralcraft.data.DataException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


public class CoreTypeFactory
  implements TypeFactory
{
  
  @Override
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Type createType(TypeResolver resolver,URI uri)
    throws DataException
  {
    String scheme=uri.getScheme();
    if (scheme!=null && !scheme.equals("java") && !scheme.equals("class"))
    { return null;
    }
    if (uri.getAuthority()!=null)
    { return null;
    }
    
    String path=uri.getPath().substring(1);

    String className
        =path.replace('/','.').concat("Type");
    
    ClassLoader loader=resolver.getClassLoader();
    
    try
    { 
      Class<Type> clazz = (Class<Type>) loader.loadClass(className);
      Constructor<Type> constructor 
        = clazz.getConstructor
          (TypeResolver.class
          ,URI.class
          );
      if (constructor==null)
      { return null;
      }
      return constructor.newInstance(resolver,uri);
    }
    catch (NoSuchMethodException x)
    { // System.err.println(x);
    }
    catch (InvocationTargetException x)
    { 
      throw new DataException
        ("Error instantiating type class "+className+": "+x.toString()
        ,x
        );
    }
    catch (ClassNotFoundException x)
    { // System.err.println(x);
    }
    catch (InstantiationException x)
    { 
      throw new DataException
        ("Error instantiating type class "+className+": "+x.toString()
        ,x
        );
    }
    catch (IllegalAccessException x)
    {
      throw new DataException
        ("Error instantiating type class "+className+": "+x.toString()
        ,x
        );
    }
    return null;
  }

  
}