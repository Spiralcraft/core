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

import spiralcraft.lang.optics.Binding;
import spiralcraft.lang.optics.Prism;

import spiralcraft.lang.Focus;
import spiralcraft.lang.Expression;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Decorator;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.Optic;

import spiralcraft.lang.optics.Lense;

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
public class AggregatePrism<T extends Aggregate<I>,I>
  extends DataPrism<T>
{ 
  private final HashMap<String,Lense> lenses
    =new HashMap<String,Lense>();
  
  private final Class<T> contentType;

  @SuppressWarnings("unchecked") // We only create Prism with erased type
  public synchronized static final 
    <T extends Tuple> TuplePrism<T> getInstance(FieldSet fieldSet)
    throws BindException
  { 
    if (fieldSet instanceof Scheme)
    { return (TuplePrism) DataPrism.getInstance( ((Scheme) fieldSet).getType());
    }
    else
    { return new TuplePrism(fieldSet,Tuple.class);
    }
  }
  

  AggregatePrism(Type type,Class<T> contentType)
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
    Lense lense=lenses.get(name);
    
    if (lense!=null)
    {
      Binding binding=source.getCache().get(lense);
      if (binding==null)
      { 
        // binding=new FieldBinding(source,lense);
        source.getCache().put(lense,binding);
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
      Prism prism=DataPrism.getInstance(type.getContentType());
      return new AggregateIterationDecorator(binding,prism);
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

  public AggregateIterationDecorator(Optic<Aggregate<I>> source,Prism<I> prism)
  { super(source,prism);
  }
  
  @Override
  protected Iterator<I> createIterator()
  { return source.get().iterator();
  }
  
}

