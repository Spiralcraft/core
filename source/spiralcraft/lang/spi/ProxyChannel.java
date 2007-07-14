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
package spiralcraft.lang.spi;

import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.WriteException;
import spiralcraft.lang.Decorator;

import java.beans.PropertyChangeSupport;

/**
 * An Channel which delegates to another Channel, usually in order to
 *   decorate the namespace.
 */
public class ProxyChannel<T>
  implements Channel<T>
{

  private final Channel<T> _optic;

  public ProxyChannel(Channel<T> delegate)
  { 
    if (delegate==null)
    { throw new IllegalArgumentException("Delegate cannot be null");
    }
    _optic=delegate;
  }

  public <X> Channel<X> resolve(Focus<?> focus,String name,Expression<?>[] params)
    throws BindException
  { return _optic.resolve(focus,name,params);
  }

  public T get()
  { return _optic.get();
  }

  public boolean set(T value)
    throws WriteException
  { return _optic.set(value);
  }

  public Class<T> getContentType()
  { return _optic.getContentType();
  }

  public <D extends Decorator<T>> D decorate(Class<D> decoratorInterface)
    throws BindException
  { return _optic.decorate(decoratorInterface);
  }

  public PropertyChangeSupport propertyChangeSupport()
  { return _optic.propertyChangeSupport();
  }

  public boolean isStatic()
  { return _optic.isStatic();
  }

  public Reflector<T> getReflector()
  { return _optic.getReflector();
  }
  
  public String toString()
  { return super.toString()+":"+_optic.toString();
  }
}
