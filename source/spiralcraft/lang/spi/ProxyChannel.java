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
import spiralcraft.lang.AccessException;
import spiralcraft.lang.Decorator;

import java.beans.PropertyChangeSupport;

/**
 * An Channel which delegates to another Channel, usually in order to
 *   decorate the namespace.
 */
public class ProxyChannel<T>
  implements Channel<T>
{

  private final Channel<T> channel;
  private WeakChannelCache cache;

  public ProxyChannel(Channel<T> delegate)
  { 
    if (delegate==null)
    { throw new IllegalArgumentException("Delegate cannot be null");
    }
    channel=delegate;
  }

  public <X> Channel<X> resolve(Focus<?> focus,String name,Expression<?>[] params)
    throws BindException
  { return channel.resolve(focus,name,params);
  }

  public T get()
  { return channel.get();
  }

  public boolean set(T value)
    throws AccessException
  { return channel.set(value);
  }

  public Class<T> getContentType()
  { return channel.getContentType();
  }

  public <D extends Decorator<T>> D decorate(Class<D> decoratorInterface)
    throws BindException
  { return channel.decorate(decoratorInterface);
  }

  public PropertyChangeSupport propertyChangeSupport()
  { return channel.propertyChangeSupport();
  }

  public boolean isStatic()
  { return channel.isStatic();
  }

  public Reflector<T> getReflector()
  { return channel.getReflector();
  }
  
  public String toString()
  { return super.toString()+":"+channel.toString();
  }
  
  public synchronized void cache(Object key,Channel<?> channel)
  { 
    if (cache==null)
    { cache=new WeakChannelCache();
    }
    cache.put(key,channel);
  }
  
  @SuppressWarnings("unchecked")
  public synchronized <X> Channel<X> getCached(Object key)
  { 
    return cache!=null?(Channel<X>) cache.get(key):null;
  }
  
}
