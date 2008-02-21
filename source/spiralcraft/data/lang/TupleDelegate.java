//
// Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.data.lang;

import spiralcraft.data.Field;
import spiralcraft.data.Tuple;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.Scheme;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.DataException;

import spiralcraft.data.reflect.ReflectionScheme;
import spiralcraft.data.reflect.ReflectionType;

import spiralcraft.data.spi.EditableArrayTuple;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Decorator;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Channel;

import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.lang.spi.BeanReflector;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationTargetException;

/**
 * Implements the Java Beans 'properties' portion of a Java interface via a 
 *   Proxy, delegating to a Tuple for data storage.
 *
 * Effectively allows a Tuple to automatically implement a "data container" 
 *  Java interface.
 */
@SuppressWarnings("unchecked") // Proxy is not generic
public class TupleDelegate<T>
  extends AbstractChannel<T>
  implements InvocationHandler
{
  
  private final T proxy;
  private final TupleBinding binding;
  
  /**
   * Create a new TupleDelegate which implements the specified interface.
   *
   * The Scheme for the Tuple is determined by reflection the interface.
   *
   * A default Tuple implementation is used.
   */
  public TupleDelegate(Class<T> iface)
    throws BindException,DataException
  { 
    super(new TupleDelegateReflector<T>(iface),true);
    
    Scheme scheme
      =TypeResolver.getTypeResolver().resolve
        (ReflectionType.canonicalURI(iface))
          .getScheme();
          
    binding=new StaticTupleBinding
      (scheme
      ,new EditableArrayTuple(scheme)
      );
      
    proxy=(T) Proxy.newProxyInstance
      (iface.getClassLoader()
      ,new Class[] {iface}
      ,this
      );
  }


  /**
   * Return the Binding for the raw Tuple, so we can manipulate the
   *   'backing-data' object.
   */
  public TupleBinding getTupleBinding()
  { return binding;
  }
 
  /**
   * InvocationHandler.invoke
   * 
   * Looks up the field associated with the bean method and performs the
   *   appropriate set() or get() operation.
   *
   * If no 'field' is associated with the method...
   *   XXX We need to implement 'methods' by delegating to some functionality
   *       associated with the Tuple.
   */
  public Object invoke
    (Object proxy,
    Method method,
    Object[] args
    )
    throws Throwable
  {
    Field field=
      ((ReflectionScheme) binding.getFieldSet()).getField(method);

    if (field!=null)
    {
      if (args!=null && args.length>0)
      {
        System.out.println(args[0]);
        // Write
        ((EditableTuple) binding.get()).set(field.getIndex(),args[0]);
        return null;
      }
      else
      {
        // Read
        return ((Tuple) binding.get()).get(field.getIndex());
      }
    }
    else
    { 
      try
      { 
        // We should run this on a 'peer' of the Tuple
        //   instead of on the Tuple itself
        //
        // Also consider when methods/actions are implemented in Schemes/Tuples.
        return method.invoke(binding.get(),args);
      }
      catch (InvocationTargetException x)
      { throw x.getTargetException();
      }
    }
  }
  
  /**
   * Return the proxy interface
   */ 
  protected T retrieve()
  { return proxy;
  }
  
  /**
   * We can't change the proxy here, as our implementation is fixed
   */
  public boolean store(T val)
  { return false;
  }
}

/**
 * 
 */
class TupleDelegateReflector<T>
  extends BeanReflector<T>
{
  
  
  public TupleDelegateReflector(Class<T> iface)
  { super(iface);
  }

  // We haven't genericized the data package builder yet
  // XXX TODO- this gets pretty hacked up using generics- figure out something cleaner
  @SuppressWarnings("unchecked")
  public <X> Channel<X> 
    resolve(Channel<T> source
        ,Focus<?> focus
        ,String name
        ,Expression<?>[] params
        )
    throws BindException
  { 
    Channel<T> binding=(Channel<T>) ((TupleDelegate) source).getTupleBinding();
    return binding.getReflector().<X>resolve(binding,focus,name,params);
  }
  
  // We haven't genericized the data package builder yet
  // XXX TODO- this gets pretty hacked up using generics- figure out something cleaner
  @SuppressWarnings("unchecked")
  public Decorator<T> decorate(Channel source,Class decoratorInterface)
  { 
    try
    {
      Channel<T> binding=(Channel<T>) ((TupleDelegate) source).getTupleBinding();
      return binding.getReflector().decorate(binding,decoratorInterface);
    }
    catch (BindException x)
    { throw new RuntimeException("Error Decorating",x);
    }
    
  }

}

