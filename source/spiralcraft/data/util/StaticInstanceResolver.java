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

import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;

/**
 * An InstanceResolver that simply holds an Object
 */
public class StaticInstanceResolver
  implements InstanceResolver
{
  private static final ClassLog log
    =ClassLog.getInstance(StaticInstanceResolver.class);
 
  private static final Level logLevel
    =ClassLog.getInitialDebugLevel(StaticInstanceResolver.class,Level.INFO);
  
  private Object object;
  
  public StaticInstanceResolver(Object object)
  { this.object=object;
  }
  
  /**
   * Resolve an Object assignable to the specific Class. 
   *
   *@return the Object, or null if none is available that is derived from the
   *  specified class.
   */
  @Override
  public Object resolve(Class<?> clazz)
  {
    if (object==null)
    { return null;
    }
    if (clazz.isAssignableFrom(object.getClass()))
    { return object;
    }
    else
    { 
      if (logLevel.isFine())
      { log.fine(clazz.getName()+" is not assignable from "+object.getClass());
      }
    }
    return null;
  }

  public void setInstance(
    Object newInstance)
  { this.object=newInstance;
    // TODO Auto-generated method stub
    
  }

  public Object getInstance()
  { return object;
  }
  
  @Override
  public String toString()
  { return super.toString()+": "+object;
  }
}