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
package spiralcraft.data.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import spiralcraft.data.DataException;

/**
 * Resolves an Object from a Class by finding a suitable Constructor and
 *   invoking it.
 */
public class ConstructorInstanceResolver
  implements InstanceResolver
{
  private final Class<?>[] formalArgs;
  private final Object[] actualArgs;
  
  public ConstructorInstanceResolver(Class<?>[] formalArgs,Object[] actualArgs)
  { 
    this.formalArgs=formalArgs;
    this.actualArgs=actualArgs;
    
  }
  
  /**
   * Resolve an Object assignable to the specific Class. 
   *
   *@return the Object, or null if none is available that is derived from the
   *  specified class.
   */
  public Object resolve(Class<?> clazz)
    throws DataException
  {
    try
    {
      Constructor<?> constructor=clazz.getConstructor(formalArgs);
      return constructor.newInstance(actualArgs);
    }
    catch (NoSuchMethodException x)
    { return null;
    }
    catch (InstantiationException x)
    { 
      throw new DataException
        ("Error constructing "+clazz.getName()+":"+x.toString(),x);
    }
    catch (IllegalAccessException x)
    { 
      throw new DataException
        ("Error constructing "+clazz.getName()+":"+x.toString(),x);
    }
    catch (InvocationTargetException x)
    { 
      throw new DataException
        ("Error constructing "+clazz.getName()+":"+x.toString(),x);
    }
    
  }
}