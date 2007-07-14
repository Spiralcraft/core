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

import spiralcraft.lang.spi.Binding;
import spiralcraft.lang.spi.Translator;

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
  public synchronized Binding resolve
    (Binding source
    ,Focus focus
    ,String name
    ,Expression[] params
    )
    throws BindException
  {
    Translator translator=translators.get(name);
    
    if (translator!=null)
    {
      Binding binding=source.getCache().get(translator);
      if (binding==null)
      { 
        // binding=new FieldBinding(source,translator);
        source.getCache().put(translator,binding);
      }
      return binding;      
    }
    
    return null;
  }

  
  @SuppressWarnings("unchecked") // Generic factory method, manip. unknown types
  public Decorator
    decorate(Binding binding,Class decoratorInterface)
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
  { return source.get().iterator();
  }
  
}

