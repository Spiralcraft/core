//
// Copyright (c) 1998,2009 Michael Toth
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

import spiralcraft.common.declare.DeclarationInfo;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Expression;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Decorator;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.AccessException;
import spiralcraft.lang.Signature;
import spiralcraft.log.ClassLog;
import spiralcraft.util.tree.LinkedTree;

import java.beans.PropertyChangeSupport;
import java.net.URI;
import java.util.ArrayList;

/**
 * <p>A starting point for building a Channel.</p>
 *
 * <p>Handles PropertyChangeSupport, caching bindings, and keeping a reference
 *   to the Reflector.
 * </p>
 *
 * <p>Storage and retrieval is accomplished by abstract methods defined in the
 *   subclass.
 * </p>
 *
 * <p>To summarize, a Channel provides an updateable "view" of a piece of 
 *   information from an underlying data source or data container. As the 
 *   underlying data changes, property changes are propogated through the 
 *   binding chain, and the get() method of the Channel will reflect the 
 *   updated data.
 * </p>
 */
public abstract class AbstractChannel<T>
  implements Channel<T>
{
 
  public static Channel<?>[] bind
    (Expression<?>[] expressions,Focus<?> focus)
    throws BindException
  {
    if (expressions==null)
    { return null;
    }
  
    Channel<?>[] ret=new Channel<?>[expressions.length];
    int i=0;
    for (Expression<?> x:expressions)
    { ret[i++]=focus.bind(x);
    }
    return ret;
  }
  protected static final ClassLog log
    =ClassLog.getInstance(AbstractChannel.class);
  
  private static final boolean traceCache
    ="true".equals
      (System.getProperty("spiralcraft.lang.spi.AbstractChannel.traceCache"));
  
  private final Reflector<T> _reflector;
  private final boolean _static;
  private PropertyChangeSupport _propertyChangeSupport;
  private WeakChannelCache _cache;
  protected boolean debug;
  protected Focus<?> context;
  protected Channel<?> origin;
  protected DeclarationInfo declarationInfo;
  
  

  
  /**
   * Construct an AbstractChannel without an initial value
   */
  public AbstractChannel(Reflector<T> reflector)
  { 
    _reflector=reflector;
    _static=false;
    if (reflector==null)
    { throw new IllegalArgumentException("Reflector cannot be null");
    }
  }


  
  /**
   * Construct an AbstractChannel with an initial value
   */
  protected AbstractChannel(Reflector<T> reflector,boolean isStatic)
  {  
    _reflector=reflector;
    _static=isStatic;
    if (reflector==null)
    { throw new IllegalArgumentException("Reflector cannot be null");
    }
  }
  
  @Override
  public void setDebug(boolean val)
  { this.debug=val;
  }
  
  @Override
  public Focus<?> getContext()
  { return context;
  }

  @Override
  public void setContext(
    Focus<?> context)
  { this.context = context;
  }

  @Override
  public synchronized void cache(Object key,Channel<?> channel)
  { 
    if (_cache==null)
    { _cache=new WeakChannelCache();
    }
    _cache.put(key,channel);
    if (traceCache)
    { log.fine(toString()+" cached "+channel+" as "+key);
    }
    if (channel.getContext()==null)
    { 
      if (traceCache)
      { log.fine("Set context to "+context);
      }
      channel.setContext(context);
    }
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public synchronized <X> Channel<X>getCached(Object key)
  { 
    final Channel<X> ret= _cache!=null?(Channel<X>) _cache.get(key):null;
    if (traceCache && ret!=null)
    { log.fine(toString()+" Hit "+ret+" for "+key);
    }
    return ret;
  }

  @Override
  public Class<T> getContentType()
  { return _reflector.getContentType();
  }
  
  @Override
  public <X> Channel<X> resolve
    (Focus<?> focus
    ,String name
    ,Expression<?>[] params
    )
    throws BindException
  { 
    Channel<X> binding=_reflector.<X>resolve(this,focus,name,params);    
    if (binding==null)
    { 
     
      StringBuffer sigs=new StringBuffer();
      sigs.append("\r\n  {[");
      for (Signature sig : _reflector.getSignatures(this))
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
        BindException(
          ((name==null || name.isEmpty())?"Constructor":"Member")
          +" signature '"+sig+"' not found. ("+toString()+") "+sigs);
    }
    return binding;
  }
  
  @Override
  public T get()
  { 
    try
    { return retrieve();
    }
    catch (AccessException x)
    { throw new AccessException("Access error in "+getDeclarationInfo(),x);
    }
  }
  
  protected DeclarationInfo getDeclarationInfo()
  { return declarationInfo!=null?declarationInfo:new DeclarationInfo(null,URI.create(getClass().getName()),null);
  }
  
  public void setDeclarationInfo(DeclarationInfo declarationInfo)
  { this.declarationInfo=declarationInfo;
  }
  
  protected abstract T retrieve();
  
  protected abstract boolean store(T val)
    throws AccessException;

  @Override
  public <D extends Decorator<T>> D decorate(Class<D> decoratorInterface)
    throws BindException
  { 
    try
    { return _reflector.decorate(this,decoratorInterface);
    }
    catch (BindException x)
    { throw new RuntimeException("Error decorating",x);
    }
  }
  
  @Override
  public synchronized boolean set(T value)
    throws AccessException
  { 
    if (_static || !isWritable())
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
  @Override
  public Reflector<T> getReflector()
  { return _reflector;
  }

  @Override
  public boolean isConstant()
  { return _static;
  }

  @Override
  public boolean isWritable()
  { return true;
  }
  
  /**
   * Resolve contextual metadata for this Channel from an appropriate provider
   *   in the Focus chain.
   * 
   * @param <X>
   * @param focus
   * @param metadataTypeURI
   * @return
   * @throws BindException 
   */
  @Override
  public <X> Channel<X> resolveMeta(Focus<?> focus,URI metadataTypeURI) 
    throws BindException
  { 
    if (origin!=null)
    { return origin.<X>resolveMeta(focus,metadataTypeURI);
    }
    else
    { return null;
    }
  }
  
  @Override
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
  
  @Override
  public String toString()
  { 
    return super.toString()
      +": "+_reflector.getClass().getName()+": "+_reflector.getTypeURI();
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public LinkedTree<Channel<?>> trace(Class<Channel<?>> stop)
  { 
    
    if (origin==null || (stop!=null && stop.isAssignableFrom(getClass())))
    { return new LinkedTree<Channel<?>>(this);
    }
    else
    { return new LinkedTree<Channel<?>>(this,origin.trace(stop));
    }
  }
  
  @Override
  public void assertContentType(Class<? super T> contentType)
    throws BindException
  { 
    if (!contentType.isAssignableFrom(getContentType()))
    { 
      throw new BindException
        ("Type assertion failed: Channel type "+getContentType().getName()
        +" is not assignable to specified type "+contentType.getName()
        );
    }
  }
}
