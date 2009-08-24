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


import spiralcraft.lang.Focus;
import spiralcraft.lang.Expression;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Decorator;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.TeleFocus;

import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.AspectChannel;
import spiralcraft.lang.spi.IterableContainsChannel;
import spiralcraft.lang.spi.IterationProjector;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.lang.spi.Translator;
import spiralcraft.lang.spi.TranslatorChannel;

import spiralcraft.data.FieldSet;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.Scheme;
import spiralcraft.data.Aggregate;
import spiralcraft.data.spi.ListAggregate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Maps a Scheme into the spiralcraft.lang binding mechanism
 *
 * This allows object models of Tuples (defined by Schemes) to be
 *   fully utilized by the language facilities.
 */
public class AggregateReflector<T extends Aggregate<I>,I>
  extends DataReflector<T>
{ 
  private final HashMap<String,Translator<?,?>> translators
    =new HashMap<String,Translator<?,?>>();
  
  private final Class<T> contentType;

  @SuppressWarnings("unchecked") // We only create Reflector with erased type
  public synchronized static final 
    <T extends Tuple> TupleReflector<T> getInstance(FieldSet fieldSet)
    throws BindException
  { 
    if (fieldSet instanceof Scheme)
    { return (TupleReflector) DataReflector.getInstance( ((Scheme) fieldSet).getType());
    }
    else
    { return new TupleReflector(fieldSet,Tuple.class);
    }
  }
  

  AggregateReflector(Type<T> type,Class<T> contentType)
  { 
    super(type);
    this.contentType=contentType;
  }
  
  /**
   * Resolve a meta name
   */
  @SuppressWarnings("unchecked") // We haven't genericized the data package yet
  @Override
  public synchronized <X> Channel<X> resolveMeta
    (final Channel<T> source
    ,Focus<?> focus
    ,String name
    ,Expression<?>[] params
    )
    throws BindException
  {  
    Channel<X> channel=super.<X>resolveMeta(source,focus,name,params);
    if (channel!=null)
    { return channel;
    }
    if (name.equals("@aggregate"))
    { 
      // Provide access to 
      Channel binding=source.getCached("_aggregate");
      if (binding==null)
      { 
        binding=new AspectChannel
          (BeanReflector.getInstance(contentType)
          ,source
          );
        source.cache("_aggregate",binding);
      }
      return binding;
    }
    else if (name.startsWith("@"))
    { 
      // Map bean properties of aggregate into meta space.
      return this.<Aggregate>resolveMeta(source,focus,"@aggregate",null)
        .resolve(focus,name.substring(1),params);
    }
    return null;
  }
  
  /**
   * Resolve a Binding that provides access to a member of a Tuple given a 
   *   source that provides Tuples.
   */
  @SuppressWarnings("unchecked") // We haven't genericized the data package yet
  @Override
  public synchronized <X> Channel<X> resolve
    (final Channel<T> source
    ,Focus<?> focus
    ,String name
    ,Expression<?>[] params
    )
    throws BindException
  {
    if (name.startsWith("@"))
    { return this.<X>resolveMeta(source,focus,name,params);
    }
    
    if (name.equals("_aggregate"))
    { 
      // Provide access to 
      Channel binding=source.getCached("_aggregate");
      if (binding==null)
      { 
        binding=new AspectChannel
          (BeanReflector.getInstance(contentType)
          ,source
          );        
        source.cache("_aggregate",binding);
      }
      return binding;
    }
    else if (name.equals("[]"))
    { return (Channel<X>) subscript(source,focus,params[0]);
    }    
    else if (name.equals("?="))
    { 
      return new IterableContainsChannel
        (source,focus.bind((Expression<I>) params[0]));
    }
    else if (name.equals("#"))
    { 
      return 
        new IterationProjector
          (source,focus,params[0]).result;
    }    
    else
    {
    
      Translator translator=translators.get(name);
    
      if (translator!=null)
      {
        Channel binding=source.getCached(translator);
        if (binding==null)
        { 
          // binding=new FieldBinding(source,translator);
          source.cache(translator,binding);
        }
        return binding;      
      }
    
      return null;
    }
  }
  
  @SuppressWarnings("unchecked")
  public Aggregate<T> fromArray(Object[] array)
  { 
    if (array==null)
    { return null;
    }
    return new ListAggregate(this.getType(),Arrays.asList(array));
  }

  
  @SuppressWarnings("unchecked") // Generic factory method, manip. unknown types
  @Override
  public <D extends Decorator<T>> D
    decorate(Channel<T> binding,Class<D> decoratorInterface)
    throws BindException
  { 
    if (decoratorInterface.equals(IterationDecorator.class))
    { 
      Reflector reflector=DataReflector.getInstance(type.getContentType());
      return (D) new AggregateIterationDecorator<T,I>(binding,reflector);
    }
    return null;
  }
  
  @Override
  public Class<T> getContentType()
  { return contentType;
  }
  
  
  @SuppressWarnings("unchecked") // Reflective subscript type
  private Channel<?> subscript
    (Channel<T> source
    ,Focus<?> focus
    ,Expression<?> subscript
    )
    throws BindException
  {
        
    Reflector<I> componentReflector
      =DataReflector.getInstance(type.getContentType());
    
    ThreadLocalChannel<I> componentChannel
      =new ThreadLocalChannel<I>(componentReflector);
    
    TeleFocus<I> teleFocus=new TeleFocus<I>(focus,componentChannel);
    
    Channel<?> subscriptChannel=teleFocus.bind(subscript);
    
    Class<?> subscriptClass=subscriptChannel.getContentType();
    
    if (Integer.class.isAssignableFrom(subscriptClass)
        || Short.class.isAssignableFrom(subscriptClass)
        || Byte.class.isAssignableFrom(subscriptClass)
        )
    {
       return new TranslatorChannel<I,T>
         (source
         ,new AggregateIndexTranslator(this)
         ,new Channel[] {subscriptChannel}
         ); 
    }
    else if 
      (Boolean.class.isAssignableFrom(subscriptClass)
      || boolean.class.isAssignableFrom(subscriptClass)
      )
    {
       return new AggregateSelectChannel<T,I>
         (source
         ,componentChannel
         ,(Channel<Boolean>) subscriptChannel
         );
    }
    else
    {
      throw new BindException
        ("Can't apply the [lookup("
        +subscriptChannel.getContentType().getName()
        +")] operator to an Aggregate");
      
    }
  }

  @Override
  public String toString()
  { 
    return super.toString()
      +(type!=null
          ?type.toString()
          :"(untyped)"
       );
  }



}

class AggregateIterationDecorator<T extends Aggregate<I>,I>
  extends IterationDecorator<T,I>
{

  public AggregateIterationDecorator
    (Channel<T> source,Reflector<I> reflector)
  { super(source,reflector);
  }
  
  @Override
  protected Iterator<I> createIterator()
  { 
    Aggregate<I> aggregate=source.get();
    if (aggregate!=null)
    { return aggregate.iterator();
    } 
    else
    { return null;
    }
  }
  
}

