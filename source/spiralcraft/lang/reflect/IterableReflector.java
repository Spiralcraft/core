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
import spiralcraft.lang.Expression;
import spiralcraft.lang.Focus;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Decorator;
import spiralcraft.lang.Range;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.TeleFocus;
import spiralcraft.lang.TypeModel;
import spiralcraft.lang.kit.AbstractReflector;
import spiralcraft.lang.spi.ArrayEqualityTranslator;
import spiralcraft.lang.spi.IterableContainsChannel;
import spiralcraft.lang.spi.IterableIndexTranslator;
import spiralcraft.lang.spi.IterableRangeChannel;
import spiralcraft.lang.spi.IterableSelectChannel;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.lang.spi.Translator;
import spiralcraft.lang.spi.TranslatorChannel;


import java.util.Iterator;
import java.util.WeakHashMap;

import java.lang.ref.WeakReference;

import java.net.URI;

import spiralcraft.log.ClassLog;
//import spiralcraft.util.ArrayUtil;

/**
 * A Reflector which exposes an Iterable an extended type.
 */ 
@SuppressWarnings({"unchecked","rawtypes"}) // Various levels of heterogeneous runtime ops
public class IterableReflector<I>
  extends AbstractReflector<Iterable<I>>
{


  @SuppressWarnings("unused")
  private static final ClassLog log
    =ClassLog.getInstance(IterableReflector.class);
  
  
 
  private static final WeakHashMap<Reflector,WeakReference<IterableReflector>> 
    reflectorMap
      =new WeakHashMap<Reflector,WeakReference<IterableReflector>>();
  

  private static final IterableLengthTranslator iterableLengthTranslator
    =new IterableLengthTranslator();

  private static final ArrayEqualityTranslator iterableEqualityTranslator
    =new ArrayEqualityTranslator<Iterable<?>>()
  {
    @Override
    public boolean compare(Iterable<?> source,Iterable<?> target)
    { 
//      log.fine("Comparing "+ArrayUtil.format(source,",",null)
//        +" to "+ArrayUtil.format(target,",",null)
//        +" is "+Arrays.deepEquals(source,target)
//        );
      Iterator sourceIt=source.iterator();
      Iterator targetIt=target.iterator();
      
      boolean failed=false;
      
      while (sourceIt.hasNext())
      {
        if (!targetIt.hasNext())
        { 
          failed=true;
          targetIt=null;
          break;
        }
        
        Object sval=sourceIt.next();
        Object tval=targetIt.next();
        if (sval!=tval && (sval==null || !sval.equals(tval)))
        { 
          failed=true;
          break;
        }
      }
      
      if (failed)
      {
        while (sourceIt.hasNext())
        { sourceIt.next();
        }
      }
      
      if (targetIt!=null)
      {
        while (targetIt.hasNext())
        { 
          targetIt.next();
          failed=true;
        }
      }
      return !failed;
    }
  };
  
  /**
   * Find an ArrayReflector for the specified Component reflector
   */  
  // Map is heterogeneous, T is ambiguous for VoidReflector
  public static final synchronized <I> IterableReflector<I> 
    getInstance(Reflector<I> componentReflector)
  { 
    IterableReflector<I> result=null;
    WeakReference<IterableReflector> ref=reflectorMap.get(componentReflector);
    
    if (ref!=null)
    { result=ref.get();
    }
    
    if (result==null)
    { 
      result=new IterableReflector<I>(componentReflector);
      reflectorMap.put(componentReflector,new WeakReference(result));
    }
    return result;
  }
  
  
  private Reflector<I> componentReflector;
  private Class<Iterable<I>> targetClass;
  private URI uri;
  
  public IterableReflector(Reflector<I> componentReflector)
  { 
    
    targetClass
      =(Class) Iterable.class;
    
    uri=URI.create(componentReflector.getTypeURI().toString()+".iterable");
    this.componentReflector=componentReflector;

  }

  @Override
  public Class<Iterable<I>> getContentType()
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
    if (!uriString.endsWith(".iterable"))
    { return false;
    }
    URI baseURI=URI.create(uriString.substring(0,uriString.length()-6));
      
    return componentReflector.isAssignableTo(baseURI);
  }
  
  @Override
  public synchronized <X> Channel<X> 
    resolve(Channel<Iterable<I>> source
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
      binding=(Channel<X>) new IterableContainsChannel<Iterable<I>,I>
        (source,focus.bind((Expression<I>) params[0]));
    }    
    else if (params==null)
    { binding=this.<X>getIterableProperty(source,name);
    }
    else
    { 
      Channel[] channels=new Channel[params.length];
      for (int i=0;i<channels.length;i++)
      { channels[i]=focus.bind(params[i]);
      }      
      binding=this.<X>getIterableMethod(source,name,channels);
    }
    
    return binding;
  }

  @Override
  public <D extends Decorator<Iterable<I>>> D decorate
    (final Channel<Iterable<I>> source
    ,final Class<D> decoratorInterface
    )
    throws BindException
  { 
    if (decoratorInterface==(Class) IterationDecorator.class)
    { 
      return (D) new IterationDecorator<Iterable<I>,I>
        (source, componentReflector)
      {

        @Override
        protected Iterator createIterator()
        { return source.get().iterator();
        }
      };
    }
    
    // Look up the target class in the map of decorators for 
    //   the specified interface?
    return null;
  }
  
  private synchronized <X> Channel<X> getIterableMethod
  (Channel<Iterable<I>> source,String name,Channel ... params)
  throws BindException
  {
    Translator<X,Iterable<I>> translator=null;
    if (name.equals("equals") || name.equals("==") || name.equals("!="))
    { 
      translator
      =(name.equals("!=")
          ?iterableEqualityTranslator.negate
            :iterableEqualityTranslator
      );
    }

    if (translator!=null)
    { 
      Channel<X> binding=source.<X>getCached(translator);
      if (binding==null)
      { 
        binding=new TranslatorChannel<X,Iterable<I>>
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
  
  private synchronized <X> Channel<X> getIterableProperty
    (Channel<Iterable<I>> source,String name)
  {
    Translator<X,Iterable<I>> translator=null;
    if (name.equals("length"))
    { translator=iterableLengthTranslator;
    }

    if (translator!=null)
    { 
      Channel<X> binding=source.<X>getCached(translator);
      if (binding==null)
      { 
        binding=new TranslatorChannel<X,Iterable<I>>
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
    (Channel<Iterable<I>> source
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
      return new TranslatorChannel
        (source
        ,new IterableIndexTranslator(componentReflector)
        ,new Channel[] {subscriptChannel}
        );
    }
    else if 
      (Boolean.class.isAssignableFrom(subscriptClass)
      || boolean.class.isAssignableFrom(subscriptClass)
      )
    {
      return new IterableSelectChannel
        (source
         ,componentChannel
         ,subscriptChannel
         );
    }
    else if (Range.class.isAssignableFrom(subscriptClass))
    { 
      return new IterableRangeChannel
        (source
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
  { 
    return super.toString()+":"+targetClass.getName()
      +"[component="+componentReflector.getTypeURI()+"]";
  }

  @Override
  public TypeModel getTypeModel()
  { return componentReflector.getTypeModel();
  }

  @Override
  public Reflector<Iterable<I>> subtype(Iterable<I> val)
  { 
    throw new AccessException
       ("Iterable does not support subtyping");
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



