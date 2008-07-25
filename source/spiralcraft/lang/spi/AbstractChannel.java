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
import spiralcraft.lang.Decorator;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.AccessException;

import java.beans.PropertyChangeSupport;

/**
 * <P>A starting point for building a Channel.</P>
 *
 * <P>Handles PropertyChangeSupport, caching bindings, and keeping a reference
 *   to the Reflector.
 *
 * <P>Storage and retrieval is accomplished by abstract methods defined in the
 *   subclass.
 *
 * <P> To summarize, a Channel provides an updateable "view" of a piece of 
 *   information from an underlying data source or data container. As the underlying
 *   data changes, property changes are propogated through the binding chain, and the
 *   get() method of the Channel will reflect the updated data.
 */
public abstract class AbstractChannel<T>
  implements Channel<T>
{
 
  private final Reflector<T> _reflector;
  protected Channel<?> metaChannel;
  private final boolean _static;
  private PropertyChangeSupport _propertyChangeSupport;
  private WeakChannelCache _cache;
  protected boolean debug;
  
  
  public synchronized void cache(Object key,Channel<?> channel)
  { 
    if (_cache==null)
    { _cache=new WeakChannelCache();
    }
    _cache.put(key,channel);
  }
  
  @SuppressWarnings("unchecked")
  public synchronized <X> Channel<X>getCached(Object key)
  { 
    return _cache!=null?(Channel<X>) _cache.get(key):null;
  }
  
  /**
   * Construct an AbstractChannel without an initial value
   */
  public AbstractChannel(Reflector<T> reflector)
  { 
    _reflector=reflector;
    _static=false;
    
    
  }

  public void setDebug(boolean val)
  { this.debug=val;
  }
  
  /**
   * Construct an AbstractChannel with an initial value
   */
  protected AbstractChannel(Reflector<T> reflector,boolean isStatic)
  {  
    _reflector=reflector;
    _static=isStatic;
  }

  public Class<T> getContentType()
  { return _reflector.getContentType();
  }
  
  public <X> Channel<X> resolve
    (Focus<?> focus
    ,String name
    ,Expression<?>[] params
    )
    throws BindException
  { 
    Channel<X> binding=_reflector.<X>resolve(this,focus,name,params);    
    if (binding==null)
    { throw new BindException("'"+name+"' not found. ("+toString()+")");
    }
    return binding;
  }
  
  public T get()
  { return retrieve();
  }
  
  protected abstract T retrieve();
  
  protected abstract boolean store(T val)
    throws AccessException;

  public <D extends Decorator<T>> D decorate(Class<D> decoratorInterface)
  { 
    try
    { return _reflector.decorate(this,decoratorInterface);
    }
    catch (BindException x)
    { throw new RuntimeException("Error decorating",x);
    }
  }
  
  public synchronized boolean set(T value)
    throws AccessException
  { 
    if (_static)
    { return false;
    }
    else
    {
      Object oldValue=retrieve();
      if (store(value))
      {
        if (_propertyChangeSupport!=null)
        { _propertyChangeSupport.firePropertyChange("",oldValue,value);
        }
        return true;
      }
      else
      { return false;
      }
    }
  }

  /**
   * Return the Reflector ("type" model/name resolver) for this Channel
   */
  public Reflector<T> getReflector()
  { return _reflector;
  }

  public boolean isStatic()
  { return _static;
  }

  public boolean isWritable()
  { return true;
  }
  
  public synchronized PropertyChangeSupport propertyChangeSupport()
  { 
    if (_static)
    { return null;
    }
    else if (_propertyChangeSupport==null)
    { _propertyChangeSupport=new PropertyChangeSupport(this);
    }
    return _propertyChangeSupport;
  }
  
  public String toString()
  { 
    return super.toString()
      +":"+_reflector;
  }
  
}
