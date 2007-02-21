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
package spiralcraft.lang;

import java.util.HashMap;

import spiralcraft.lang.optics.SimpleBinding;
import spiralcraft.lang.optics.Prism;
import spiralcraft.lang.optics.BeanPrism;
import spiralcraft.lang.optics.VoidPrism;

/**
 * Creates Optics
 */
public class OpticFactory
{
  private static final OpticFactory _INSTANCE = new OpticFactory();

  public static final OpticFactory getInstance()
  { return _INSTANCE;
  }

  private final HashMap<Class,Prism> _prismMap
    =new HashMap<Class,Prism>();

  /**
   * Create an Optic which provides view of an arbitrary Java object.
   */
  public Optic createOptic(Object object)
    throws BindException
  { 
    if (object instanceof Optic)
    { return (Optic) object;
    }
    else
    { return new SimpleBinding(object,true);
    }
  }

  
  /**
   * Find a Prism which provides an interface into the specified Java class
   */
  public synchronized Prism findPrism(Class clazz)
    throws BindException
  { 
    Prism result=(Prism) _prismMap.get(clazz);
    if (result==null)
    {
      if (clazz==Void.class)
      { result=new VoidPrism();
      }
      else
      { result=new BeanPrism(clazz);
      }
      _prismMap.put(clazz,result);
    }
    return result;
  }
}


