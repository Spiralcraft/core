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

import spiralcraft.data.spi.EditableArrayTuple;
import spiralcraft.data.wrapper.ReflectionScheme;
import spiralcraft.data.wrapper.ReflectionType;

import spiralcraft.lang.BindException;

import spiralcraft.lang.optics.AbstractBinding;
import spiralcraft.lang.optics.Binding;
import spiralcraft.lang.optics.Prism;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Decorator;
import spiralcraft.lang.Expression;

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
public class TupleDelegate
  extends AbstractBinding
  implements InvocationHandler
{
  
  private final Object proxy;
  private final TupleBinding binding;
  
  /**
   * Create a new TupleDelegate which implements the specified interface.
   *
   * The Scheme for the Tuple is determined by reflection the interface.
   *
   * A default Tuple implementation is used.
   */
  public TupleDelegate(Class iface)
    throws BindException,DataException
  { 
    super(new TuplePrism(iface),true);
    
    Scheme scheme
      =TypeResolver.getTypeResolver().resolve
        (ReflectionType.canonicalUri(iface))
          .getScheme();
          
    binding=new TupleBinding
      (scheme
      ,new EditableArrayTuple(scheme)
      );
      
    proxy=Proxy.newProxyInstance
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
      ((ReflectionScheme) binding.getScheme()).getField(method);

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
  protected Object retrieve()
  { return proxy;
  }
  
  /**
   * We can't change the proxy here, as our implementation is fixed
   */
  public boolean store(Object val)
  { return false;
  }
}

/**
 * 
 */
class TuplePrism
  implements Prism
{
  private final Class iface;
  
  public TuplePrism(Class iface)
  { this.iface=iface;
  }
  
  public Binding resolve(Binding source,Focus focus,String name,Expression[] params)
    throws BindException
  { 
    Binding binding=((TupleDelegate) source).getTupleBinding();
    return binding.getPrism().resolve(binding,focus,name,params);
  }
  
  public Decorator decorate(Binding source,Class decoratorInterface)
  { 
    try
    {
      Binding binding=((TupleDelegate) source).getTupleBinding();
      return binding.getPrism().decorate(binding,decoratorInterface);
    }
    catch (BindException x)
    { throw new RuntimeException("Error Decorating",x);
    }
    
  }
  
  public Class getContentType()
  { return iface;
  }
}

