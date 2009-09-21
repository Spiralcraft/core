//
// Copyright (c) 2009 Michael Toth
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
package spiralcraft.lang.reflect;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.WeakHashMap;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.CollectionDecorator;
import spiralcraft.lang.Decorator;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.ListDecorator;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.spi.GenericCollectionDecorator;
import spiralcraft.lang.spi.GenericListDecorator;
import spiralcraft.log.ClassLog;

/**
 * Reflects a Collection which provides an extended component type
 * 
 * @author mike
 *
 * @param <C>
 * @param <T>
 */
public class CollectionReflector<C extends Collection<T>,T>
  extends BeanReflector<C>
{

  private static final ClassLog log
    =ClassLog.getInstance(CollectionReflector.class);
  
  
  @SuppressWarnings("unchecked")
  private static final 
    WeakHashMap<Type,WeakHashMap<Reflector,WeakReference<CollectionReflector>>> reflectorMap
      =new WeakHashMap<Type,WeakHashMap<Reflector,WeakReference<CollectionReflector>>>();
  
  /**
   * Obtain an instance of a reflector for a parameterized collection type
   * 
   * @param <C>
   * @param <T>
   * @param clazz
   * @param reflector
   * @return
   */
  @SuppressWarnings("unchecked")
  public static synchronized final 
    <C extends Collection<T>,T> CollectionReflector<C,T>
    getInstance(Class<?> clazz,Reflector<T> componentReflector)
  { 
    CollectionReflector<C,T> result=null;
    
    WeakHashMap<Reflector,WeakReference<CollectionReflector>> subMap
      =reflectorMap.get(clazz);
    if (subMap==null)
    { 
      subMap=new WeakHashMap<Reflector,WeakReference<CollectionReflector>>();
      
    }
    
    WeakReference<CollectionReflector> ref=subMap.get(componentReflector);
    
    if (ref!=null)
    { result=ref.get();
    }
    
    if (result==null)
    { 
      result=new CollectionReflector<C,T>(clazz,componentReflector);
      subMap.put(componentReflector,new WeakReference(result));
      reflectorMap.put(clazz,subMap);
    }
    return result;
    
    
    
  }
  

  
  
  protected final Reflector<T> componentReflector;
  
  
  public CollectionReflector(Type type,Reflector<T> componentReflector)
  { 
    super(type);
    this.componentReflector=componentReflector;
    log.fine
      ("Created new CollectionReflector for "
      +type+":"+componentReflector.getTypeURI()
      );
  }

  @SuppressWarnings("unchecked")
  @Override
  public <D extends Decorator<C>> D decorate
    (Channel<C> source,Class<D> decoratorInterface)
    throws BindException
  { 
    if (List.class.isAssignableFrom(source.getReflector().getContentType()))
    {
      if (decoratorInterface==(Class) IterationDecorator.class
          || decoratorInterface==(Class) CollectionDecorator.class
          || decoratorInterface==(Class) ListDecorator.class
          )
      { return (D) new GenericListDecorator(source,componentReflector);
      }
    }
    else if (decoratorInterface==(Class) IterationDecorator.class
        || decoratorInterface==(Class) CollectionDecorator.class
        )
    { return (D) new GenericCollectionDecorator<C,T>(source,componentReflector);
    }
    return super.<D>decorate(source,decoratorInterface);
  }
  
}
