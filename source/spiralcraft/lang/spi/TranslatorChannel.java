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
import spiralcraft.log.ClassLogger;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * An Binding which translates get() and source property changes
 *   through a Translator.
 */
public  class TranslatorChannel<T,S>
  implements Channel<T>,PropertyChangeListener
{
  private static final ClassLogger log
    =ClassLogger.getInstance(TranslatorChannel.class);
  
  private static int _ID=0;
  
  protected final int id;

  protected final Channel<S> source;
  protected final boolean _static;

  private final Translator<T,? super S> translator;
  private final Channel<?>[] _modifiers;
  private PropertyChangeSupport _propertyChangeSupport;
  private PropertyChangeListener _modifierListener;
  private WeakChannelCache _cache;
  private Channel<?> metaBinding;
  protected boolean debug;

  /**
   * Create a Translator binding which translates values bidirectionally,
   *   through the specified Translator.
   */
  public TranslatorChannel
    (Channel<S> source
    ,Translator<T,S> translator
    ,Channel<?>[] modifiers
    )
  { 
    this.source=source;
    this.translator=translator;
    _modifiers=modifiers;
    
    // Determine default static by checking source, modifiers.
    // If all dependencies are static, this is static
    boolean isStatic=true;
    if (!source.isStatic())
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

  public void setDebug(boolean val)
  { debug=val;
  }
  
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
    return _cache!=null?(Channel <X>) _cache.get(key):null;
  }
  
  
  public final T get()
  { 
    if (debug)
    {
      T val=translator.translateForGet(source.get(),_modifiers);
      log.fine(toString()+"\r\n.get() returning: "+val);
      return val;
    }
    else
    { return translator.translateForGet(source.get(),_modifiers);
    }
  }

  /**
   * Indicates that downstream bindings have requested property change
   *   notifications
   */
  protected boolean isPropertyChangeSupportActive()
  { return _propertyChangeSupport!=null;
  }
  
  protected final S getSourceValue()
  { return source.get();
  }

  protected final boolean isSourceStatic()
  { return source.isStatic();
  }

  /**
   * Override if value can be set
   */
  public boolean set(T value)
    throws AccessException
  { return false;
  }
  
  /**
   * Override if value can be set
   */
  public boolean isWritable()
  { return false;
  }

  public final Reflector<T> getReflector()
  { return translator.getReflector();
  }

  @SuppressWarnings("unchecked") // Heterogeneous metadata
  public <X> Channel<X> resolve(Focus<?> focus,String name,Expression<?>[] params)
    throws BindException
  {     
    Channel<X> binding=translator.getReflector().resolve(this,focus,name,params);
    if (binding==null)
    {
      if (name.equals("!"))
      { 
        synchronized (this)
        {
          if (metaBinding==null)
          { metaBinding=new SimpleChannel<TranslatorChannel>(this,true);
          }
        }
        binding=(Channel<X>) metaBinding;
      }
    }
    if (binding==null)
    { throw new BindException("'"+name+"' not found in "+translator.getReflector().toString());
    }
    return binding;
  }
  
  public Class<T> getContentType()
  { return translator.getReflector().getContentType();
  }
  
  public <D extends Decorator<T>> D decorate(Class<D> decoratorInterface)
  { 
    try
    { return translator.getReflector().decorate(this,decoratorInterface);
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
      { oldValue=(T) translator.translateForGet((S) event.getOldValue(),_modifiers);
      }
      if (event.getNewValue()!=null)
      { newValue=(T) translator.translateForGet((S) event.getNewValue(),_modifiers);
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

      S sourceValue=source.get();
      if (sourceValue!=null)
      { newValue=translator.translateForGet(sourceValue,_modifiers);
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
      PropertyChangeSupport pcs=source.propertyChangeSupport();
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
      (super.toString()+":"+id+"("+translator.toString()+"):"+source.toString()+"(");
    boolean first=true;
    if (_modifiers!=null)
    {
      for (Channel<?> o: _modifiers)
      { 
        if (!first)
        { out.append(",");
        }
        else
        { first=false;
        }
        out.append(o.toString());
      }
    }
    out.append(")");
    return out.toString();
    
  }
}
