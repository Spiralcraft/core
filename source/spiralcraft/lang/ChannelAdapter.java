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


/**
 * Default implementation of an Channel.
 */
public abstract class ChannelAdapter<T>
  implements Channel<T>
{
  /**
   * Return null. no names exposed
   */
  @Override
  public <X> Channel<X> resolve(Focus<?> focus,String name,Expression<?>[] parameters)
    throws BindException
  { return null;
  }

  /**
   * The target is null
   */
  @Override
  public T get()
  { return null;
  }

  /**
   * The target cannot be modified 
   */
  @Override
  public boolean set(T value)
  { return false;
  }

  /**
   * No immediate decorator support
   */
  @Override
  public <D extends Decorator<T>> D decorate(Class<D> decoratorInterface)
  { return null;
  }
  
  /**
   * The underlying value is not guaranteed to remain unchanged
   */
  @Override
  public boolean isConstant()
  { return false;
  }

  @Override
  public final Class<T> getContentType()
  { return getReflector().getContentType();
  }

  /**
   * Property change not supported by default
   */
  @Override
  public PropertyChangeSupport propertyChangeSupport()
  { return null;
  }

  /**
   * This needs to be implemented by the subclass
   */
  @Override
  public abstract Reflector<T> getReflector();
}
