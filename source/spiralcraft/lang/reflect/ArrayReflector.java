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

import spiralcraft.lang.AccessException;
import spiralcraft.lang.CollectionDecorator;
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Decorator;
import spiralcraft.lang.ListDecorator;
import spiralcraft.lang.Range;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.TeleFocus;
import spiralcraft.lang.TypeModel;
import spiralcraft.lang.spi.AbstractReflector;
import spiralcraft.lang.spi.ArrayContainsChannel;
import spiralcraft.lang.spi.ArrayEqualityTranslator;
import spiralcraft.lang.spi.ArrayIndexChannel;
import spiralcraft.lang.spi.ArrayListDecorator;
import spiralcraft.lang.spi.ArrayRangeChannel;
import spiralcraft.lang.spi.ArraySelectChannel;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.lang.spi.Translator;
import spiralcraft.lang.spi.TranslatorChannel;


import java.util.Arrays;
import java.util.WeakHashMap;

import java.lang.ref.WeakReference;

import java.lang.reflect.Array;
import java.net.URI;

import spiralcraft.log.ClassLog;
//import spiralcraft.util.ArrayUtil;

/**
 * A Reflector which exposes an Array of an extended type.
 */
@SuppressWarnings({"unchecked","rawtypes"}) // Various levels of heterogeneous runtime ops
public class ArrayReflector<I>
  extends AbstractReflector<I[]>
{


  @SuppressWarnings("unused")
  private static final ClassLog log
    =ClassLog.getInstance(ArrayReflector.class);
  
  
 
  private static final WeakHashMap<Reflector,WeakReference<ArrayReflector>> 
    reflectorMap
      =new WeakHashMap<Reflector,WeakReference<ArrayReflector>>();
  

  private static final ArrayLengthTranslator arrayLengthTranslator
    =new ArrayLengthTranslator();

  private static final ArrayEqualityTranslator objectArrayEqualityTranslator
    =new ArrayEqualityTranslator<Object[]>()
  {
    @Override
    public boolean compare(Object[] source,Object[] target)
    { 
//      log.fine("Comparing "+ArrayUtil.format(source,",",null)
//        +" to "+ArrayUtil.format(target,",",null)
//        +" is "+Arrays.deepEquals(source,target)
//        );
      return Arrays.deepEquals(source,target);
    }
  };
  
  /**
   * Find an ArrayReflector for the specified Component reflector
   */  
  // Map is heterogeneous, T is ambiguous for VoidReflector
  public static final synchronized <I> ArrayReflector<I> 
    getInstance(Reflector<I> componentReflector)
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
    try
    {
      Class<I> contentType=componentReflector.getContentType();
      if (contentType==Void.TYPE)
      { contentType=(Class<I>) Object.class;
      }
      
      targetClass
        =(Class<I[]>) Array.newInstance(contentType,0).getClass();
    
      uri=URI.create(componentReflector.getTypeURI().toString()+".array");
      this.componentReflector=componentReflector;
    }
    catch (IllegalArgumentException x)
    { 
      throw new IllegalArgumentException
        ("Failed to determine array type for "
        +componentReflector.getContentType()
        +" provided by "+componentReflector
        ,x
        );
    }

  }

  @Override
  public Class<I[]> getContentType()
  { return targetClass;
  }
  
  @Override
  public URI getTypeURI()
  { return uri;
  }

  @Override
  public Reflector<?> disambiguate(Reflector<?> alternate)
  {
    if (alternate==this || alternate.getTypeModel()!=getTypeModel())
    { return this;
    }
    else
    { return alternate.disambiguate(this);
    }
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

  @Override
  public boolean isAssignableFrom(Reflector<?> reflector)
  { 
    if (reflector.getContentType().equals(getContentType()))
    { return true;
    }
    return super.isAssignableFrom(reflector);
  }
  
  @Override
  public synchronized <X> Channel<X> 
    resolve(Channel<I[]> source
        ,Focus<?> focus
        ,String name
        ,Expression<?>[] params
        )
    throws BindException
  { 
    if (name.startsWith("@"))
    { return this.<X>resolveMeta(source,focus,name,params);
    }
        
    Channel<X> binding=null;
    if (name.equals("[]"))
    { binding=(Channel<X>) this.subscript(source,focus,params[0]);
    }
    else if (name.equals("?="))
    { 
      return (Channel<X>) new ArrayContainsChannel<I>
        (source,focus.bind((Expression<I>) params[0]));
    }    
    else if (params==null)
    { binding=this.<X>getArrayProperty(source,name);
    }
    else
    { 
      Channel[] channels=new Channel[params.length];
      for (int i=0;i<channels.length;i++)
      { channels[i]=focus.bind(params[i]);
      }      
      binding=this.<X>getArrayMethod(source,name,channels);
    }
    return binding;
  }

  @Override
  public <D extends Decorator<I[]>> D decorate
    (Channel<I[]> source,Class<D> decoratorInterface)
    throws BindException
  { 
    if (decoratorInterface==(Class) IterationDecorator.class
        || decoratorInterface==(Class) CollectionDecorator.class
        || decoratorInterface==(Class) ListDecorator.class
        )
    { return (D) new ArrayListDecorator(source,componentReflector);
    }
    
    // Look up the target class in the map of decorators for 
    //   the specified interface?
    return null;
  }
  
  private synchronized <X> Channel<X> getArrayMethod
    (Channel<I[]> source,String name,Channel ... params)
    throws BindException
  {
    Translator<X,I[]> translator=null;
    if (name.equals("equals") || name.equals("==") || name.equals("!="))
    { 
      translator
        =(name.equals("!=")
         ?objectArrayEqualityTranslator.negate
         :objectArrayEqualityTranslator
         );
    }
    
    if (translator!=null)
    { 
      Channel<X> binding=source.<X>getCached(translator);
      if (binding==null)
      { 
        binding=new TranslatorChannel<X,I[]>
          (source
          ,translator
          ,params
          );
        source.cache(translator,binding);
      }
      return binding;
    }
    return null;
  }
  private synchronized <X> Channel<X> getArrayProperty(Channel<I[]> source,String name)
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
    else if (Range.class.isAssignableFrom(subscriptClass))
    { 
      return new ArrayRangeChannel
        (source
        ,componentReflector
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
  
  @Override
  public String toString()
  { return super.toString()+":"+targetClass.getName();
  }

  @Override
  public TypeModel getTypeModel()
  { return componentReflector.getTypeModel();
  }

  @Override
  public Reflector<I[]> subtype(I[] val)
  { 
    throw new AccessException
       ("Array does not support subtyping");
  }

  @Override
  public boolean accepts(Object val)
  {
    if (val==null)
    { return true;
    }
    
    if (!(val.getClass().isArray()))
    { return false;
    }
    else
    { 
      return componentReflector.getContentType()
        .isAssignableFrom(val.getClass().getComponentType());
    }
    
  }
  
}



