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

import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Decorator;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.TeleFocus;


import java.util.WeakHashMap;

import java.lang.ref.WeakReference;

import java.lang.reflect.Array;
import java.net.URI;

import spiralcraft.log.ClassLogger;

/**
 * A Reflector which exposes an Array of an extended type.
 */
@SuppressWarnings("unchecked") // Various levels of heterogeneous runtime ops
public class ArrayReflector<I>
  implements Reflector<I[]>
{


  @SuppressWarnings("unused")
  private static final ClassLogger log
    =ClassLogger.getInstance(ArrayReflector.class);
  
 
  private static final WeakHashMap<Reflector,WeakReference<ArrayReflector>> 
    reflectorMap
      =new WeakHashMap<Reflector,WeakReference<ArrayReflector>>();
  

  private static final ArrayLengthTranslator arrayLengthTranslator
    =new ArrayLengthTranslator();
  
  /**
   * Find an ArrayReflector for the specified Component reflector
   */  
  // Map is heterogeneous, T is ambiguous for VoidReflector
  public static final synchronized <I> ArrayReflector<I> 
    getInstance(Reflector<I> componentReflector)
    throws BindException
  { 
    ArrayReflector<I> result=null;
    WeakReference<ArrayReflector> ref=reflectorMap.get(componentReflector);
    
    if (ref!=null)
    { result=ref.get();
    }
    
    if (result==null)
    { 
      result=new ArrayReflector<I>(componentReflector);
      reflectorMap.put(componentReflector,new WeakReference(result));
    }
    return result;
  }
  
  
  private Reflector<I> componentReflector;
  private Class<I[]> targetClass;
  private URI uri;
  
  public ArrayReflector(Reflector<I> componentReflector)
  { 
    
    targetClass
      =(Class<I[]>) Array.newInstance
        (componentReflector.getContentType(),0)
          .getClass();
    
    uri=URI.create(componentReflector.getTypeURI().toString()+".array");
    this.componentReflector=componentReflector;

  }

  public Class<I[]> getContentType()
  { return targetClass;
  }
  
  @Override
  public URI getTypeURI()
  { return uri;
  }

  @Override
  public boolean isAssignableTo(URI typeURI)
  { 
    String uriString=typeURI.toString();
    if (!uriString.endsWith(".array"))
    { return false;
    }
    URI baseURI=URI.create(uriString.substring(0,uriString.length()-6));
      
    return componentReflector.isAssignableTo(baseURI);
  }
  
  public synchronized <X> Channel<X> 
    resolve(Channel<I[]> source
        ,Focus<?> focus
        ,String name
        ,Expression<?>[] params
        )
    throws BindException
  { 
    Channel<X> binding=null;
    if (name.equals("[]"))
    { binding=(Channel<X>) this.subscript(source,focus,params[0]);
    }
    else if (params==null)
    { binding=this.<X>getArrayProperty(source,name);
    }
    return binding;
  }

  public <D extends Decorator<I[]>> D decorate
    (Channel<I[]> source,Class<D> decoratorInterface)
    throws BindException
  { 
    if (decoratorInterface==(Object) IterationDecorator.class)
    { return (D) new ArrayIterationDecorator(source,componentReflector);
    }
    
    // Look up the target class in the map of decorators for 
    //   the specified interface?
    return null;
  }
  

  private synchronized <X> Channel<X> getArrayProperty(Channel<I[]> source,String name)
    throws BindException
  {
    Translator<X,I[]> translator=null;
    if (name.equals("length"))
    { translator=arrayLengthTranslator;
    }
    
    if (translator!=null)
    { 
      Channel<X> binding=source.<X>getCached(translator);
      if (binding==null)
      { 
        binding=new TranslatorChannel<X,I[]>
          (source
          ,translator
          ,null
          );
        source.cache(translator,binding);
      }
      return binding;
    }
    return null;
  }


  
  private Channel<?> subscript
    (Channel<I[]> source
    ,Focus<?> focus
    ,Expression<?> subscript
    )
    throws BindException
  {
    
    
    ThreadLocalChannel<?> componentChannel
      =new ThreadLocalChannel(componentReflector);
    
    TeleFocus teleFocus=new TeleFocus(focus,componentChannel);
    
    Channel<?> subscriptChannel=teleFocus.bind(subscript);
    
    Class subscriptClass=subscriptChannel.getContentType();
    
    if (Integer.class.isAssignableFrom(subscriptClass)
        || Short.class.isAssignableFrom(subscriptClass)
        || Byte.class.isAssignableFrom(subscriptClass)
        )
    {
      return new ArrayIndexChannel<I>
        (componentReflector
        ,source
        ,(Channel<Number>) subscriptChannel
        );
//      return new TranslatorChannel
//        (source
//        ,new ArrayIndexTranslator(componentReflector)
//        ,new Channel[] {subscriptChannel}
//        );
    }
    else if 
      (Boolean.class.isAssignableFrom(subscriptClass)
      || boolean.class.isAssignableFrom(subscriptClass)
      )
    {
      return new ArraySelectChannel
        (source
         ,componentChannel
         ,subscriptChannel
         );
    }
    else
    {
      throw new BindException
        ("Don't know how to apply the [lookup("
        +subscriptChannel.getContentType().getName()
        +")] operator to an Array"
        );
    }
  }
  
  public String toString()
  { return super.toString()+":"+targetClass.getName();
  }


}



