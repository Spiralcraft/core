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

import java.beans.PropertyChangeSupport;

/**
 * <P>A starting point for building a Binding.</P>
 *
 * <P>Handles PropertyChangeSupport, caching bindings, and keeping a reference
 *   to the Reflector.
 *
 * <P>Storage and retrieval is accomplished by abstract methods defined in the
 *   subclass.
 *
 * <P> To summarize, a Binding provides an updateable "view" of a piece of 
 *   information from an underlying data source or data container. As the underlying
 *   data changes, property changes are propogated through the binding chain, and the
 *   get() method of the Channel will reflect the updated data.
 */
public abstract class AbstractBinding<T>
  implements Binding<T>
{
 
  private final Reflector<T> _reflector;
  private Binding<?> metaBinding;
  private final boolean _static;
  private PropertyChangeSupport _propertyChangeSupport;
  private WeakBindingCache _cache;
  
  public synchronized WeakBindingCache getCache()
  {
    if (_cache==null)
    { _cache=new WeakBindingCache();
    }
    return _cache;
  }
  
  /**
   * Construct an AbstractBinding without an initial value
   */
  public AbstractBinding(Reflector<T> reflector)
  { 
    _reflector=reflector;
    _static=false;
    
    
  }

  /**
   * Construct an AbstractBinding with an initial value
   */
  protected AbstractBinding(Reflector<T> reflector,boolean isStatic)
  {  
    _reflector=reflector;
    _static=isStatic;
  }

  public Class<T> getContentType()
  { return _reflector.getContentType();
  }
  
  @SuppressWarnings("unchecked") // Heterogeneous ops
  public <X> Channel<X> resolve(Focus<?> focus,String name,Expression<?>[] params)
    throws BindException
  { 
    Binding<X> binding=_reflector.<X>resolve(this,focus,name,params);
    if (binding==null)
    {
      if (name.equals("!"))
      { 
        synchronized (this)
        {
          if (metaBinding==null)
          { metaBinding=new SimpleBinding<AbstractBinding>(this,true);
          }
        }
        binding=(Binding<X>) metaBinding;
      }
    }
    
    if (binding==null)
    { throw new BindException("'"+name+"' not found in "+_reflector.toString());
    }
    return binding;
  }
  
  public T get()
  { return retrieve();
  }
  
  protected abstract T retrieve();
  
  protected abstract boolean store(T val);

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
   * Return the Reflector ("type" model/name resolver) for this Binding
   */
  public Reflector<T> getReflector()
  { return _reflector;
  }

  public boolean isStatic()
  { return _static;
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
}
