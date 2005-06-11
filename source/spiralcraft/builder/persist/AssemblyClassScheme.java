//
// Copyright (c) 1998,2005 Michael Toth
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
package spiralcraft.builder.persist;

import java.net.URI;

import spiralcraft.tuple.spi.ReflectionScheme;
import spiralcraft.tuple.spi.SchemeImpl;
import spiralcraft.tuple.spi.FieldListImpl;

import spiralcraft.builder.AssemblyClass;

/**
 * A Scheme derived from an AssemblyClass definition.
 *
 * This Scheme implementation wraps a ReflectionScheme, and decorates the
 *   associated Fields with information specified in the AssemblyClass
 */
public class AssemblyClassScheme
  extends SchemeImpl
{
  private final ReflectionScheme reflectionScheme;
  
  public AssemblyClassScheme(URI uri,AssemblyClass assemblyClass)
  {
    setURI(uri);
    if (assemblyClass==null)
    { throw new IllegalArgumentException("assemblyClass cannot be null");
    }
    Class javaClass=assemblyClass.getJavaClass();
    reflectionScheme=ReflectionScheme.getInstance(javaClass);
    setFields(new FieldListImpl(reflectionScheme.getFields()));
  }
  
  public String toString()
  { return super.toString()+":"+reflectionScheme.toString();
  }
}