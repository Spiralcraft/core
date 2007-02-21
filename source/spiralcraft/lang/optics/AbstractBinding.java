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
package spiralcraft.lang.optics;

import spiralcraft.lang.Focus;
import spiralcraft.lang.Optic;
import spiralcraft.lang.Expression;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Decorator;

import java.beans.PropertyChangeSupport;

/**
 * A starting point for building a Binding.
 *
 * Handles PropertyChangeSupport, caching bindings, and keeping a reference
 *   to the Prism.
 *
 * Storage and retrieval is accomplished by abstract methods defined in the
 *   subclass.
 *
 * To summarize, a Binding provides an updateable "view" of a piece of 
 *   information from an underlying data source or data container.
 */
public abstract class AbstractBinding
  implements Binding
{
 
  private final Prism _prism;
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
  protected AbstractBinding(Prism prism)
    throws BindException
  { 
    _prism=prism;
    _static=false;
  }

  /**
   * Construct an AbstractBinding with an initial value
   */
  protected AbstractBinding(Prism prism,boolean isStatic)
    throws BindException
  { 
    _prism=prism;
    _static=isStatic;
  }

  public Class<?> getContentType()
  { return _prism.getContentType();
  }
  
  public Optic resolve(Focus focus,String name,Expression[] params)
    throws BindException
  { 
    Binding binding=_prism.resolve(this,focus,name,params);
    if (binding==null)
    { throw new BindException("'"+name+"' not found in "+_prism.toString());
    }
    return binding;
  }
  
  public Object get()
  { return retrieve();
  }
  
  protected abstract Object retrieve();
  
  protected abstract boolean store(Object val);

  public Decorator decorate(Class decoratorInterface)
  { 
    try
    { return _prism.decorate(this,decoratorInterface);
    }
    catch (BindException x)
    { throw new RuntimeException("Error decorating",x);
    }
  }
  
  public synchronized boolean set(Object value)
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
   * Return the Prism ("type" model/name resolver) for this Binding
   */
  public Prism getPrism()
  { return _prism;
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
