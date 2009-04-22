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

import java.net.URI;


import spiralcraft.data.TypeResolver;
import spiralcraft.data.TypeFactory;
import spiralcraft.data.Type;
import spiralcraft.data.DataException;

public class ReflectionTypeFactory
  implements TypeFactory
{
  
  @SuppressWarnings("unchecked")
  public Type<?> createType(TypeResolver resolver,URI uri)
    throws DataException
  {
    String path=uri.getPath().substring(1);

    String className
        =path.replace('/','.').replace(ReflectionType.INNER_CLASS_SEPARATOR,"$");
    
    ClassLoader loader=resolver.getClassLoader();
    
    Class<Object> clazz=null;
    try 
    { clazz = (Class<Object>) loader.loadClass(className);
    }
    catch (ClassNotFoundException x)
    { return null;
    }
    
    return new ReflectionType<Object>(resolver,uri,clazz);
  }

  
}