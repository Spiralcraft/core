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
import spiralcraft.util.tree.LinkedTree;

import java.beans.PropertyChangeSupport;
import java.net.URI;

/**
 * An Channel which delegates to another Channel, usually in order to
 *   decorate the namespace.
 */
public class ProxyChannel<T>
  implements Channel<T>
{

  protected Channel<T> channel;
  private WeakChannelCache cache;
  protected boolean delegateCache;
  //private boolean debug;

  public ProxyChannel(Channel<T> delegate)
  { 
    if (delegate==null)
    { throw new IllegalArgumentException("Delegate cannot be null");
    }
    channel=delegate;
  }
  
  public ProxyChannel()
  {
    
    
  }
  
  
  @SuppressWarnings("unchecked")
  @Override
  public LinkedTree<Channel<?>> trace(Class<Channel<?>> stop)
  { return new LinkedTree<Channel<?>>(this,channel.trace(stop));
  }


  @Override
  public void setDebug(boolean val)
  { //debug=val;
  }
  
  @Override
  public <X> Channel<X> resolve(Focus<?> focus,String name,Expression<?>[] params)
    throws BindException
  { return channel.resolve(focus,name,params);
  }




  /**
   * Resolve contextual metadata for this Channel from an appropriate provider
   *   in the Focus chain.
   * 
   * @param <X>
   * @param focus
   * @param metadataTypeURI
   * @return
   */
  @Override
  public <X> Channel<X> resolveMeta(Focus<?> focus,URI metadataTypeURI)
    throws BindException
  { return channel.resolveMeta(focus,metadataTypeURI);
  }
  
  @Override
  public T get()
  { return channel.get();
  }

  @Override
  public boolean set(T value)
    throws AccessException
  { return channel.set(value);
  }
  
  @Override
  public boolean isWritable()
  { return channel.isWritable();
  }

  @Override
  public Class<T> getContentType()
  { return channel.getContentType();
  }

  @Override
  public <D extends Decorator<T>> D decorate(Class<D> decoratorInterface)
    throws BindException
  { return channel.decorate(decoratorInterface);
  }

  @Override
  public PropertyChangeSupport propertyChangeSupport()
  { return channel.propertyChangeSupport();
  }

  @Override
  public boolean isConstant()
  { return channel.isConstant();
  }

  @Override
  public Reflector<T> getReflector()
  { return channel.getReflector();
  }
  
  @Override
  public String toString()
  { return super.toString()+":"+(channel!=null?channel.toString():"(not bound)");
  }
  
  @Override
  public synchronized void cache(Object key,Channel<?> channel)
  { 
    if (delegateCache)
    { channel.cache(key,channel);
    }
    else
    {
      if (cache==null)
      { cache=new WeakChannelCache();
      }
      cache.put(key,channel);
    }
  }

  
  @Override
  @SuppressWarnings("unchecked")
  public synchronized <X> Channel<X> getCached(Object key)
  { 
    if (delegateCache)
    { return channel.getCached(key);
    }
    else
    { return cache!=null?(Channel<X>) cache.get(key):null;
    }
  }

  @Override
  public Focus<?> getContext()
  { return channel.getContext();
  }
  
  @Override
  public void setContext(Focus<?> context)
  { channel.setContext(context);
  }

  @Override
  public void assertContentType(
    Class<? super T> contentType)
    throws BindException
  { channel.assertContentType(contentType);
  }
  
  
}
