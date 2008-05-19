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

import spiralcraft.lang.spi.AspectChannel;
import spiralcraft.lang.spi.BeanReflector;
import spiralcraft.lang.spi.ThreadLocalChannel;
import spiralcraft.lang.spi.Translator;
import spiralcraft.lang.spi.TranslatorChannel;

import spiralcraft.data.FieldSet;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.Scheme;
import spiralcraft.data.Aggregate;

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
  

  AggregateReflector(Type<?> type,Class<T> contentType)
    throws BindException
  { 
    super(type);
    this.contentType=contentType;
  }
  

  /**
   * Resolve a Binding that provides access to a member of a Tuple given a 
   *   source that provides Tuples.
   */
  @SuppressWarnings("unchecked") // We haven't genericized the data package yet
  public synchronized Channel resolve
    (final Channel source
    ,Focus focus
    ,String name
    ,Expression[] params
    )
    throws BindException
  {
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
    { return subscript(source,focus,params[0]);
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

  
  @SuppressWarnings("unchecked") // Generic factory method, manip. unknown types
  public Decorator
    decorate(Channel binding,Class decoratorInterface)
    throws BindException
  { 
    if (decoratorInterface==IterationDecorator.class)
    { 
      Reflector reflector=DataReflector.getInstance(type.getContentType());
      return new AggregateIterationDecorator(binding,reflector);
    }
    return null;
  }
  
  public Class<T> getContentType()
  { return contentType;
  }
  
  
  @SuppressWarnings("unchecked") // Reflective subscript type
  private Channel<?> subscript
    (Channel<Aggregate<I>> source
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
       return new TranslatorChannel<I,Aggregate<I>>
         (source
         ,new AggregateIndexTranslator(this)
         ,subscriptChannel
         );
    }
    else if 
      (Boolean.class.isAssignableFrom(subscriptClass)
      || boolean.class.isAssignableFrom(subscriptClass)
      )
    {
       return new AggregateSelectChannel<I>
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

  public String toString()
  { 
    return super.toString()
      +(type!=null
          ?type.toString()
          :"(untyped)"
       );
  }
}

class AggregateIterationDecorator<I>
  extends IterationDecorator<Aggregate<I>,I>
{

  public AggregateIterationDecorator
    (Channel<Aggregate<I>> source,Reflector<I> reflector)
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

