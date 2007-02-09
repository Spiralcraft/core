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
package spiralcraft.data.wrapper;

import java.net.URI;

import org.xml.sax.SAXException;

import java.io.IOException;

import spiralcraft.data.TypeResolver;
import spiralcraft.data.TypeFactory;
import spiralcraft.data.Type;
import spiralcraft.data.Tuple;
import spiralcraft.data.DataException;
import spiralcraft.data.InstanceResolver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


public class ReflectionTypeFactory
  implements TypeFactory
{
  
  public Type createType(TypeResolver resolver,URI uri)
    throws DataException
  {
    String path=uri.getPath().substring(1);

    String className
        =path.replace('/','.');
    
    ClassLoader loader=resolver.getClassLoader();
    
    Class clazz=null;
    try 
    { clazz = loader.loadClass(className);
    }
    catch (ClassNotFoundException x)
    { 
      System.err.println(x);
      return null;
    }
    
    return new ReflectionType(resolver,uri,clazz);
  }

  
}