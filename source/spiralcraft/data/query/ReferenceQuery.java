//
// Copyright (c) 1998,2008 Michael Toth
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
package spiralcraft.data.query;

import spiralcraft.data.DataException;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Aggregate;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.lang.DataReflector;

import spiralcraft.data.reflect.ReflectionType;
import spiralcraft.data.spi.IteratorCursor;
import spiralcraft.data.spi.ListCursor;
import spiralcraft.data.spi.ListAggregate;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Expression;
import spiralcraft.lang.IterationDecorator;
import spiralcraft.lang.Reflector;

/**
 * A Query which provides access to all instances of a given Type. This is usually the 
 *   eventual upstream source for all Queries. 
 */
public class ReferenceQuery<T extends Tuple>
  extends Query
{
  
  private Expression<Aggregate<T>> reference;

  
  /**
   * Construct an unconfigured ReferenceQuery
   */
  public ReferenceQuery()
  { }
  
  public ReferenceQuery(String reference)
  { this.reference=Expression.<Aggregate<T>>create(reference);
  }
  
  /**
   * Construct a new Scan for the given Type
   */
  public ReferenceQuery(Expression<Aggregate<T>> reference)
  { this.reference=reference;
  }
  
  public ReferenceQuery(Query baseQuery)
  { super(baseQuery);
  }
    
  public Expression<Aggregate<T>> getReference()
  { return reference;
  }
  
  public void setReference(Expression<Aggregate<T>> reference)
  { this.reference=reference;
  }
  
  public void setType(Type<?> type)
  { this.type=type;
  }
  
  @Override
  public FieldSet getFieldSet()
  { 
    if (type!=null)
    { return type.getScheme();
    }
    else
    { return null;
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <X extends Tuple> BoundQuery<?,X> 
    getDefaultBinding(Focus<?> focus,Queryable<?> queryable)
    throws DataException
  { 
    try
    { return (BoundQuery<?,X>) new BoundReferenceQuery<T>(this,focus);
    }
    catch (BindException x)
    { throw new DataException("Error binding ReferenceQuery",x);
    }
  }
  
  @Override
  public String toString()
  { return super.toString()+"[source="+reference+"]";
  }
  
  
  
  
}

class BoundReferenceQuery<T extends Tuple>
  extends BoundQuery<ReferenceQuery<T>,T>
{

  private Channel<Aggregate<T>> source;
  private IterationDecorator<?,T> iterationDecorator;
  private Type<T> type;
  
  @SuppressWarnings("unchecked") // Expression is untyped
  public BoundReferenceQuery(ReferenceQuery query,Focus<?> focus)
    throws BindException
  { 
    setQuery(query);
    Channel channel=focus.<T>bind(query.getReference());
    
    Reflector<T> reflector=channel.getReflector();
    if (reflector instanceof DataReflector)
    { 
      type=((DataReflector) reflector).getType().getContentType();
      source=channel;
    }
    else 
    {
      iterationDecorator
        =(IterationDecorator<?,T>) channel.decorate(IterationDecorator.class);
      if (iterationDecorator!=null)
      { 
        reflector=iterationDecorator.getComponentReflector();
        if (reflector instanceof DataReflector)
        { type=((DataReflector) reflector).getType();
        }
        else
        { 
          try
          { type=ReflectionType.canonicalType(reflector.getContentType());
          }
          catch (DataException x)
          { throw new BindException
              ("Error binding reference query to generic iterable source",x);
          }
             
        }
      }
      else
      { 
        throw new BindException
          ("Error binding reference query- reference "
            +query.getReference().getText()+" does not point to an " +
          		"Aggregate or anything that is iterable"
          );
      }
    }
    if (query.getType()!=null)
    {
      if (query.getType().isAssignableFrom(type))
      { type=(Type<T>) query.getType();
      }
      else
      { 
        throw new BindException
          ("Declared query type "+query.getType().getURI()
           +" cannot be assigned from "+type.getURI()+" as resolved by "
           +" reference to expression "+query.getReference().getText()
          );
      }
    }
  }
  
  @Override
  public Type<T> getType()
  { return type;
  }


  @Override
  public SerialCursor<T> execute() throws DataException
  { 
    if (source!=null)
    {
      Aggregate<T> aggregate=source.get();
      if (aggregate==null)
      { 
        aggregate
          =new ListAggregate<T>(Type.getAggregateType(getType()));
      }
      return new ListCursor<T>(aggregate);
    }
    else
    {
      return new IteratorCursor<T>
        (type.getFieldSet(),iterationDecorator.iterator());
    }
  } 

}

