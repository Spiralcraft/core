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
import spiralcraft.lang.Signature;
import spiralcraft.log.ClassLog;
import spiralcraft.util.tree.LinkedTree;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.net.URI;
import java.util.ArrayList;

/**
 * An Binding which translates get() and source property changes
 *   through a Translator.
 */
public  class TranslatorChannel<T,S>
  implements Channel<T>,PropertyChangeListener
{
  private static final ClassLog log
    =ClassLog.getInstance(TranslatorChannel.class);
  
  private static int _ID=0;
  
  protected final int id;

  protected final Channel<S> source;
  protected final boolean _constant;

  private final Translator<T,? super S> translator;
  private final Channel<?>[] _modifiers;
  private PropertyChangeSupport _propertyChangeSupport;
  private PropertyChangeListener _modifierListener;
  private WeakChannelCache _cache;
  protected boolean debug;
  private Focus<?> context;

  /**
   * Create a Translator binding which translates values bidirectionally,
   *   through the specified Translator.
   */
  public TranslatorChannel
    (Channel<S> source
    ,Translator<T,S> translator
    ,Channel<?> [] modifiers
    )
  { 
    this.source=source;
    this.translator=translator;
    _modifiers=modifiers;
    
    // Determine default static by checking source, modifiers.
    // If all dependencies are static, this is static
    if (translator.isFunction())
    {
      boolean isConstant=source.isConstant();
      
      if (isConstant && _modifiers!=null)
      {
        for (int i=0;i<_modifiers.length;i++)
        { 
          if (!_modifiers[i].isConstant())
          { 
            isConstant=false;
            break;
          }
        }
      }
      _constant=isConstant;
    }
    else
    { _constant=false;
    }
    id=_ID++;  

  }

  @Override
  public void setContext(Focus<?> context)
  { this.context=context;
  }
  
  @Override
  public Focus<?> getContext()
  { 
    if (context!=null)
    { return context;
    }
    else
    { return source.getContext();
    }
  }
  
  @Override
  public void setDebug(boolean val)
  { debug=val;
  }
  
  @Override
  public synchronized void cache(Object key,Channel<?> channel)
  { 
    if (_cache==null)
    { _cache=new WeakChannelCache();
    }
    _cache.put(key,channel);
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public synchronized <X> Channel<X>getCached(Object key)
  { 
    return _cache!=null?(Channel <X>) _cache.get(key):null;
  }
  
  
  @Override
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

  protected final boolean isSourceConstant()
  { return source.isConstant();
  }

  @SuppressWarnings("unchecked")
  @Override
  public LinkedTree<Channel<?>> trace(Class<Channel<?>> stop)
  { 
    
    LinkedTree<Channel<?>>[] children
      =new LinkedTree[ (_modifiers!=null?_modifiers.length:0)+1];
    children[0]=source.trace(stop);
    if (_modifiers!=null)
    {
      for (int i=0;i<_modifiers.length;i++)
      { children[i+1]=_modifiers[i].trace(stop);
      }
    }
    
    return new LinkedTree<Channel<?>>(this,children);
  }
  
  /**
   * Override if value can be set
   */
  @Override
  public boolean set(T value)
    throws AccessException
  { return false;
  }
  
  /**
   * Override if value can be set
   */
  @Override
  public boolean isWritable()
  { return false;
  }

  @Override
  public final Reflector<T> getReflector()
  { return translator.getReflector();
  }

  @Override
  public <X> Channel<X> resolve(Focus<?> focus,String name,Expression<?>[] params)
    throws BindException
  {     
    Channel<X> binding=translator.getReflector().resolve(this,focus,name,params);

    if (binding==null)
    { 
       
      StringBuffer sigs=new StringBuffer();
      sigs.append("\r\n  {[");
      for (Signature sig : translator.getReflector().getSignatures(this))
      { 
        sigs.append(sig.toString());
        sigs.append("\r\n    ");
          
      }
      sigs.append("]\r\n  }");

      Reflector<?>[] pc=null;
      if (params!=null)
      {
        ArrayList<Reflector<?>> pcList=new ArrayList<Reflector<?>>();
        for (int i=0;i<params.length;i++)
        { 
          try
          { 
            Channel<?> channel=focus.bind(params[i]);
            if (!(channel instanceof BindingChannel<?>))
            { pcList.add(channel.getReflector());
            }
          }
          catch (BindException x)
          {
          }
        }
        pc=pcList.toArray(new Reflector<?>[pcList.size()]);
      }
      Signature sig=new Signature
        (name
        ,null
        ,pc
        );
        
      throw new 
        BindException("Member signature '"+sig+"' not found. ("+translator.getReflector()+") "+sigs);      
    }
    return binding;
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
  { return null;
  }
  
  @Override
  public Class<T> getContentType()
  { return translator.getReflector().getContentType();
  }
  
  @Override
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
  @Override
  public boolean isConstant()
  { return _constant;
  }

  @Override
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
      { oldValue=translator.translateForGet((S) event.getOldValue(),_modifiers);
      }
      if (event.getNewValue()!=null)
      { newValue=translator.translateForGet((S) event.getNewValue(),_modifiers);
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

  @Override
  public PropertyChangeSupport propertyChangeSupport()
  { 
    if (isConstant())
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
        @Override
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

  @Override
  public String toString()
  { 
    StringBuilder out=new StringBuilder();
    out.append
      (super.toString()+":"+id+"("+translator.toString()+")<-["
        +source.toString()+"](");
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
