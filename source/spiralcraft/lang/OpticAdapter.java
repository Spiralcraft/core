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

import java.beans.PropertyChangeSupport;

import spiralcraft.lang.optics.Prism;

/**
 * Default implementation of an Optic.
 */
public abstract class OpticAdapter<T>
  implements Optic<T>
{
  /**
   * Return null. no names exposed
   */
  public <X> Optic<X> resolve(Focus focus,String name,Expression[] parameters)
    throws BindException
  { return null;
  }

  /**
   * The target is null
   */
  public T get()
  { return null;
  };

  /**
   * The target cannot be modified 
   */
  public boolean set(T value)
  { return false;
  }

  /**
   * No immediate decorator support
   */
  public Decorator<T> decorate(Class decoratorInterface)
  { return null;
  }
  
  /**
   * The underlying value is not guaranteed to remain unchanged
   */
  public boolean isStatic()
  { return false;
  }

  public final Class<T> getContentType()
  { return getPrism().getContentType();
  }

  /**
   * Property change not supported by default
   */
  public PropertyChangeSupport propertyChangeSupport()
  { return null;
  }

  /**
   * This needs to be implemented by the subclass
   */
  public abstract Prism<T> getPrism();
}
