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
import spiralcraft.lang.WriteException;
import spiralcraft.lang.Decorator;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * An Binding which translates get() and source property changes
 *   through a lense.
 */
public  class LenseBinding<T,S>
  implements Binding<T>,PropertyChangeListener
{
  private static int _ID=0;
  
  protected final int id;

  private final Optic<S> _source;
  private final Lense<T,? super S> _lense;
  private final Optic[] _modifiers;
  private final boolean _static;
  private PropertyChangeSupport _propertyChangeSupport;
  private PropertyChangeListener _modifierListener;
  private WeakBindingCache _cache;

  /**
   * Create a Lense binding which translates values bidirectionally,
   *   through the specified lense.
   */
  public LenseBinding(Optic<S> source,Lense<T,S> lense,Optic[] modifiers)
  { 
    _source=source;
    _lense=lense;
    _modifiers=modifiers;
    
    // Determine default static by checking source, modifiers.
    // If all dependencies are static, this is static
    boolean isStatic=true;
    if (!_source.isStatic())
    { isStatic=false;
    }
    if (_modifiers!=null)
    { 
      for (int i=0;i<_modifiers.length;i++)
      { 
        if (!_modifiers[i].isStatic())
        { 
          isStatic=false;
          break;
        }
      }
    }
    _static=isStatic;
    id=_ID++;  

  }

  public synchronized WeakBindingCache getCache()
  {
    if (_cache==null)
    { _cache=new WeakBindingCache();
    }
    return _cache;
  }
  
  public final T get()
  { return _lense.translateForGet(_source.get(),_modifiers);
  }

  /**
   * Indicates that downstream bindings have requested property change
   *   notifications
   */
  protected boolean isPropertyChangeSupportActive()
  { return _propertyChangeSupport!=null;
  }
  
  protected final S getSourceValue()
  { return _source.get();
  }

  protected final boolean isSourceStatic()
  { return _source.isStatic();
  }

  /**
   * Override if value can be set
   */
  public boolean set(T value)
    throws WriteException
  { return false;
  }

  public final Prism<T> getPrism()
  { return _lense.getPrism();
  }

  public <X> Optic<X> resolve(Focus<?> focus,String name,Expression[] params)
    throws BindException
  { 
    Binding<X> binding=_lense.getPrism().resolve(this,focus,name,params);
    if (binding==null)
    { throw new BindException("'"+name+"' not found in "+_lense.getPrism().toString());
    }
    return binding;
  }
  
  public Class<T> getContentType()
  { return _lense.getPrism().getContentType();
  }
  
  public Decorator<T> decorate(Class decoratorInterface)
  { 
    try
    { return _lense.getPrism().decorate(this,decoratorInterface);
    }
    catch (BindException x)
    { throw new RuntimeException("Error decorating",x);
    }
  }
  
  /**
   * Override if standard definition of isStatic is false
   */
  public boolean isStatic()
  { return _static;
  }

  @SuppressWarnings("unchecked") // PropertyChange is not generic- values are Object type
  public void propertyChange(PropertyChangeEvent event)
  {
    // System.out.println(toString()+".propertyChange");
    if (_propertyChangeSupport!=null)
    {
      // System.out.println(toString()+".propertyChange2");
      T oldValue=null;
      T newValue=null;


      if (event.getOldValue()!=null)
      { oldValue=(T) _lense.translateForGet((S) event.getOldValue(),_modifiers);
      }
      if (event.getNewValue()!=null)
      { newValue=(T) _lense.translateForGet((S) event.getNewValue(),_modifiers);
      }

      if (oldValue!=newValue)
      { 
        // System.out.println(toString()+".propertyChange3");
        _propertyChangeSupport.firePropertyChange("",oldValue,newValue);
      }
    }
  }

  public void modifierChanged()
  {
    // System.out.println(toString()+".modifierChanged");
    if (_propertyChangeSupport!=null)
    {
      T newValue=null;

      S sourceValue=_source.get();
      if (sourceValue!=null)
      { newValue=_lense.translateForGet(sourceValue,_modifiers);
      }
      // System.out.println(toString()+".modifierChanged:"+newValue);
      _propertyChangeSupport.firePropertyChange("",null,newValue);
    }
  }

  public PropertyChangeSupport propertyChangeSupport()
  { 
    if (isStatic())
    { return null;
    }
    
    if (_propertyChangeSupport==null)
    { 
      // System.out.println(toString()+" propertyChangeSupport");
      
      _propertyChangeSupport=new PropertyChangeSupport(this);
      PropertyChangeSupport pcs=_source.propertyChangeSupport();
      if (pcs!=null)
      { pcs.addPropertyChangeListener(this);
      }
      
      _modifierListener
        =new PropertyChangeListener()
      {
        public void propertyChange(PropertyChangeEvent event)
        { modifierChanged();
        }
      };

      if (_modifiers!=null)
      {
        for (int i=0;i<_modifiers.length;i++)
        { 
          pcs=_modifiers[i].propertyChangeSupport();
          if (pcs!=null)
          { 
            // System.out.println(toString()+" added modifier listener");
            pcs.addPropertyChangeListener(_modifierListener);
          }
        }
      }
    }
    return _propertyChangeSupport;
  }

  protected final void firePropertyChange(String name,Object oldValue,Object newValue)
  {
    // System.out.println(toString()+".firePropertyChange: "+newValue);
    if (_propertyChangeSupport!=null)
    { 
      // System.out.println(toString()+".firePropertyChange2: "+newValue);
      _propertyChangeSupport.firePropertyChange(name,oldValue,newValue);
    }
  }

  protected final void firePropertyChange(PropertyChangeEvent event)
  {
    if (_propertyChangeSupport!=null)
    { _propertyChangeSupport.firePropertyChange(event);
    }
  }

  public String toString()
  { 
    StringBuilder out=new StringBuilder();
    out.append
      (super.toString()+":"+id+"("+_lense.toString()+"):"+_source.toString()+"(");
    boolean first=true;
    for (Optic o: _modifiers)
    { 
      if (!first)
      { out.append(",");
      }
      else
      { first=false;
      }
      out.append(o.toString());
    }
    out.append(")");
    return out.toString();
    
  }
}
