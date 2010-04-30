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
import spiralcraft.data.Type;

import spiralcraft.data.reflect.AssemblyType;
import spiralcraft.data.reflect.ReflectionScheme;
import spiralcraft.data.reflect.ReflectionType;

import spiralcraft.data.spi.EditableArrayTuple;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Decorator;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Channel;
import spiralcraft.lang.SimpleFocus;

import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.AbstractChannel;
import spiralcraft.lang.spi.SimpleChannel;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
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
  { this(iface,null);
  }
  
  /**
   * Create a new TupleDelegate which implements the specified interface.
   *
   * The Scheme for the Tuple is determined by reflection the interface.
   *
   * If provided, the provided Tuple is used, otherwise a default Tuple 
   *   will be created.
   */
  public TupleDelegate(Class<T> iface,Tuple backing)
    throws BindException,DataException
  { 
    super(new TupleDelegateReflector<T>(iface),true);
    
    Scheme scheme
      =TypeResolver.getTypeResolver().resolve
        (ReflectionType.canonicalURI(iface))
          .getScheme();
          
    binding=new StaticTupleBinding
      (scheme
      ,backing!=null?backing:new EditableArrayTuple(scheme)
      );
      
    proxy=(T) Proxy.newProxyInstance
      (iface.getClassLoader()
      ,new Class[] {iface}
      ,this
      );
  }


  public Tuple getTuple()
  { return (Tuple) binding.get();
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
    
    Field field=null;
    
    if (binding.getFieldSet() instanceof ReflectionScheme)
    { field=((ReflectionScheme) binding.getFieldSet()).getField(method);
    }
    else
    { 
      Type archetype=binding.getFieldSet().getType();
      while (archetype instanceof AssemblyType)
      { archetype=archetype.getArchetype();
      }
      if (archetype instanceof ReflectionType)
      { 
        field=((ReflectionScheme) ((ReflectionType) archetype).getScheme())
          .getField(method);
      }
    }

    if (field!=null)
    {
      if (args!=null && args.length>0)
      {
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
        // Run a data method
        
        Channel<Object> methodChannel=binding.getCached(method);
        if (methodChannel==null)
        {
        
          
          Type<?> type=binding.getFieldSet().getType();
          Type<?>[] params=new Type<?>[method.getParameterTypes().length];
          Channel<?>[] paramChannels=new Channel[params.length];
          
          
          // Shortcut Object methods
          if (params.length==0 && method.getName().equals("toString"))
          { return this.toString();
          }
          else if (params.length==1 
                   && method.getName().equals("equals")
                   )
          { 
            if (args[0]==null)
            { return false;
            }
            Object arg=args[0];
            if (Proxy.class.isAssignableFrom(arg.getClass()))
            {
              InvocationHandler handler=Proxy.getInvocationHandler(arg);
              if (handler instanceof TupleDelegate<?>)
              { return getTuple().equals(((TupleDelegate) handler).getTuple());
              }
            }
          }
          else if (params.length==0
                   && method.getName().equals("hashCode")
                   )
          { return getTuple().hashCode();
          }
          
          int i=0;
          for (Class<?> clazz : method.getParameterTypes())
          { 
            
            params[i]=ReflectionType.canonicalType(clazz);
            paramChannels[i]=new SimpleChannel(args[i],true);
            i++;
          }
        
          spiralcraft.data.Method dataMethod
            =type.findMethod(method.getName(),params);

          if (dataMethod!=null)
          { 
            methodChannel
              =(Channel<Object>) 
                dataMethod.bind
                  (new SimpleFocus(null).chain(binding)
                  ,binding
                  ,paramChannels
                  );
            if (paramChannels.length==0)
            { binding.cache(method,methodChannel);
            }
            
          }
          else
          { 
          
          }
        }
        
        if (methodChannel!=null)
        { return methodChannel.get();          
        }
        else
        {  throw new UnsupportedOperationException("Method "+method+" not implemented");
        }
      }
      finally
      { 
      }
    }
  }
  
  /**
   * Return the proxy interface
   */ 
  @Override
  protected T retrieve()
  { return proxy;
  }
  
  @Override
  public boolean isWritable()
  { return false;
  }
  
  /**
   * We can't change the proxy here, as our implementation is fixed
   */
  @Override
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
  @Override
  @SuppressWarnings("unchecked")
  public <X> Channel<X> 
    resolve(Channel<T> source
        ,Focus<?> focus
        ,String name
        ,Expression<?>[] params
        )
    throws BindException
  { 
    if (name.startsWith("@"))
    { return this.<X>resolveMeta(source,focus,name,params);
    }
    Channel<T> binding=((TupleDelegate) source).getTupleBinding();
    return binding.getReflector().<X>resolve(binding,focus,name,params);
  }
  
  // We haven't genericized the data package builder yet
  // XXX TODO- this gets pretty hacked up using generics- figure out something cleaner
  @Override
  @SuppressWarnings("unchecked")
  public <D extends Decorator<T>> D 
    decorate(Channel<T> source,Class<D> decoratorInterface)
  { 
    try
    {
      Channel<T> binding=((TupleDelegate) source).getTupleBinding();
      return binding.getReflector().decorate(binding,decoratorInterface);
    }
    catch (BindException x)
    { throw new RuntimeException("Error Decorating",x);
    }
    
  }

}

