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


import spiralcraft.lang.AccessException;
import spiralcraft.lang.CollectionDecorator;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Expression;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Decorator;
import spiralcraft.lang.Functor;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.Channel;
import spiralcraft.lang.ListDecorator;
import spiralcraft.lang.Range;
import spiralcraft.lang.Reflector;
import spiralcraft.lang.TeleFocus;

import spiralcraft.lang.reflect.BeanReflector;
import spiralcraft.lang.spi.AspectChannel;
import spiralcraft.lang.spi.BindingChannel;
import spiralcraft.lang.spi.GatherChannel;
import spiralcraft.lang.spi.IterableContainsChannel;
import spiralcraft.lang.spi.ListRangeChannel;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.lang.spi.Translator;
import spiralcraft.lang.spi.TranslatorChannel;

import spiralcraft.data.FieldSet;
import spiralcraft.data.RuntimeDataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.Scheme;
import spiralcraft.data.Aggregate;
import spiralcraft.data.spi.EditableArrayListAggregate;
import spiralcraft.data.spi.ListAggregate;
import spiralcraft.data.EditableAggregate;

import java.util.ArrayList;
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
  implements Functor<T>
{ 
  private final HashMap<String,Translator<?,?>> translators
    =new HashMap<String,Translator<?,?>>();
  
  private final Class<T> contentType;
  private final Functor<T> constructor;
  

  @SuppressWarnings({ "unchecked", "rawtypes" }) // We only create Reflector with erased type
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
  

  @SuppressWarnings("unchecked")
  AggregateReflector(Type<T> type,Class<T> contentType)
  { 
    super(type);
    this.contentType=contentType;
    if (this.type!=null && this.type instanceof Functor)
    { this.constructor=(Functor<T>) this.type;
    }
    else
    { this.constructor=null;
    }
    
  }
  
  /**
   * Resolve a meta name
   */
  @SuppressWarnings({ "unchecked", "rawtypes" }) // We haven't genericized the data package yet
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
  @SuppressWarnings({ "unchecked", "rawtypes" }) // We haven't genericized the data package yet
  @Override
  public synchronized <X> Channel<X> resolve
    (final Channel<T> source
    ,Focus<?> focus
    ,String name
    ,Expression<?>[] params
    )
    throws BindException
  {
    if (getType()!=null)
    { getType().link();
    }
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
  
  @SuppressWarnings({ "unchecked", "rawtypes" })
  public Aggregate<T> fromArray(Object[] array)
  { 
    if (array==null)
    { return null;
    }
    return new ListAggregate(this.getType(),Arrays.asList(array));
  }

  
  @SuppressWarnings({ "unchecked", "rawtypes" }) // Generic factory method, manip. unknown types
  @Override
  public <D extends Decorator<T>> D
    decorate(Channel<T> binding,Class<D> decoratorInterface)
    throws BindException
  { 
    if (decoratorInterface.equals(IterationDecorator.class)
       || decoratorInterface.equals(CollectionDecorator.class) 
       || decoratorInterface.equals(ListDecorator.class) 
       )
    { 
      Reflector reflector=DataReflector.getInstance(type.getContentType());
      return (D) new AggregateListDecorator<T,I>(binding,reflector);
    }
    return null;
  }
  
  @Override
  public Class<T> getContentType()
  { return contentType;
  }
  
  
  @SuppressWarnings({ "unchecked", "rawtypes" }) // Reflective subscript type
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
    else if (Range.class.isAssignableFrom(subscriptClass))
    {
      return new ListRangeChannel<T,I>
        (source
        ,(Channel<Range>) subscriptChannel
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

  
  /**
   * Constructor functor
   */
  @Override
  public Channel<T> bindChannel(Focus<?> focus,Channel<?>[] arguments)
    throws BindException
  {    
    if (constructor!=null)
    { return constructor.bindChannel(focus,arguments);
    }
      
    ArrayList<Channel<?>> indexedParamList=new ArrayList<Channel<?>>();
    ArrayList<Channel<?>> namedParamList=new ArrayList<Channel<?>>();
      
    boolean endOfParams=false;
    for (Channel<?> chan : arguments)
    { 
      if (chan instanceof BindingChannel<?>)
      { 
        endOfParams=true;
        namedParamList.add(chan);
      }
      else
      {
        if (endOfParams)
        { 
          throw new BindException
            ("Positional parameters must preceed named parameters");
        }
        indexedParamList.add(chan);
        
      }
      
    }      
          
        
    Channel<?>[] indexedParams
      =indexedParamList.toArray(new Channel[indexedParamList.size()]);      

    Channel<T> constructorChannel;
    if (indexedParams.length==0)
    { 
      constructorChannel  
        =new AggregateConstructorChannel<T,I>(this,focus,null);      
    }
    else if (indexedParams.length==1)
    { 
      constructorChannel
        =new AggregateConstructorChannel<T,I>(this,focus,indexedParams[0]);
    }
    else
    {
      throw new BindException
        ("Wrong number of indexed parameters for Aggregate constructor");
    }
      
     
    if (namedParamList.size()>0)
    { 
      constructorChannel
        =new GatherChannel<T>
          (constructorChannel
          ,namedParamList.toArray
            (new BindingChannel[namedParamList.size()])
          );
    }
        
    return constructorChannel;    

  }



}

class AggregateListDecorator<T extends Aggregate<I>,I>
  extends ListDecorator<T,I>
{

  public AggregateListDecorator
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

  @Override
  public T add(
    T collection,
    I item)
  { 
    ((EditableAggregate<I>) collection).add(item);
    return collection;
  }
  
  @Override
  public T addAll(T collection,Iterator<I> items)
  {
    ((EditableAggregate<I>) collection).addAll(items);
    return collection;
  }

  @Override
  public T addAll(T collection,T items)
  {
    ((EditableAggregate<I>) collection).addAll(items);
    return collection;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public T newCollection()
  { 
    return (T) new EditableArrayListAggregate<I>
      ( ((AggregateReflector<T,I>) source.getReflector()).getType());

  }
  
  @Override
  public int size(T collection)
  { 
    if (collection==null)
    { return 0;
    }
    return collection.size();
  }
  
  @Override
  public I get(T collection,int index)
  { 
    try
    { return collection.get(index);
    }
    catch (IndexOutOfBoundsException x)
    { return null;
    }
    catch (RuntimeDataException x)
    { throw new AccessException("Error retrieving data from Aggregate",x);
    }
  }
}


