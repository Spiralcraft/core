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
package spiralcraft.tuple.lang;

import spiralcraft.tuple.Field;
import spiralcraft.tuple.Tuple;
import spiralcraft.tuple.Buffer;
import spiralcraft.tuple.Scheme;

import spiralcraft.tuple.spi.ArrayTuple;
import spiralcraft.tuple.spi.ReflectionScheme;

import spiralcraft.lang.BindException;

import spiralcraft.lang.optics.AbstractBinding;
import spiralcraft.lang.optics.Binding;

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
 *
 */
public class TupleDelegate
  extends DelegateBinding
  implements InvocationHandler
{
  
  private final Object _proxy;
  private final TupleBinding _binding;
  
  /**
   * Create a new TupleDelegate which implements the specified interface.
   *
   * The Scheme for the Tuple is determined by reflection the interface.
   *
   * A default Tuple implementation is used.
   */
  public TupleDelegate(Class iface)
    throws BindException
  { 
    super(iface);
    
    Scheme scheme=ReflectionScheme.getInstance(iface);
    _binding=new TupleBinding
      (scheme
      ,new ArrayTuple(scheme)
      );
      
    _proxy=Proxy.newProxyInstance
      (iface.getClassLoader()
      ,new Class[] {iface}
      ,this
      );
  }


  /**
   * Return the Binding for this Tuple
   */
  public Binding getBinding()
  { return _binding;
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
      ((ReflectionScheme) _binding.getScheme()).getField(method);

    if (field!=null)
    {
      if (args!=null && args.length>0)
      {
        System.out.println(args[0]);
        // Write
        ((Buffer) _binding.get()).set(field.getIndex(),args[0]);
        return null;
      }
      else
      {
        // Read
        return ((Tuple) _binding.get()).get(field.getIndex());
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
        return method.invoke(_binding.get(),args);
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
  { return _proxy;
  }
  
  /**
   * We can't change the proxy here, as our implementation is fixed
   */
  public boolean store(Object val)
  { return false;
  }
}
