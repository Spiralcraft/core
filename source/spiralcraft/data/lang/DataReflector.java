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
package spiralcraft.data.lang;

import java.util.WeakHashMap;

import java.lang.ref.WeakReference;
import java.net.URI;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Reflector;

import spiralcraft.lang.spi.BeanReflector;

import spiralcraft.data.DataComposite;
import spiralcraft.data.Tuple;
import spiralcraft.data.Aggregate;
import spiralcraft.data.Type;
import spiralcraft.data.TypeNotFoundException;

/**
 * Abstract base class maps a DataComposite into the spiralcraft.lang namespace
 *
 * @author mike
 *
 * @param <T> The type of DataComposite we are mapping
 */
public abstract class DataReflector<T extends DataComposite>
  implements Reflector<T>
{
  // 
  // XXX Use weak map
  private static final WeakHashMap<Type<?>,WeakReference<Reflector<?>>>
    SINGLETONS=new WeakHashMap<Type<?>,WeakReference<Reflector<?>>>();
  
  protected final Type<?> type;
  
  @SuppressWarnings("unchecked") // We only create Reflector with erased type
  public synchronized static final 
    <T> Reflector<T> getInstance(Type type)
    throws BindException
  { 
    
    if (type==null)
    { throw new IllegalArgumentException("Type cannot be null");
    }
    Reflector broker=null;
    
    if (type.isPrimitive())
    { 
      broker
        =BeanReflector.getInstance(type.getNativeClass());
    }
    else
    {
      WeakReference<Reflector<?>> ref=SINGLETONS.get(type);
      if (ref!=null)
      { broker=ref.get();
      }
      if (broker==null)
      {
        if (type.isAggregate())
        { broker=new AggregateReflector(type,Aggregate.class);
        }
        else
        { broker=new TupleReflector(type,Tuple.class);
        }
        SINGLETONS.put(type,new WeakReference(broker));
      }
    }
    return broker;
  }
  
  public DataReflector(Type<?> type)
  { this.type=type;;
  }
  
  
  
  public Type<?> getType()
  { return type;
  }
  
  @Override
  public URI getTypeURI()
  {
    if (type!=null)
    { return type.getURI();
    }
    return null;
  }


  
  @Override
  public boolean isAssignableTo(URI typeURI)
  {
    if (type==null)
    { return false;
    }
    try
    {
      Type<?> requestedType=Type.resolve(typeURI);
      return requestedType.isAssignableFrom(type);
    }
    catch (TypeNotFoundException x)
    { return false;
    }
    

  }
  
  public String toString()
  { return super.toString();
  }
}
