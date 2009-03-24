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
import spiralcraft.data.access.ScrollableCursor;
import spiralcraft.data.lang.DataReflector;

import spiralcraft.data.spi.ListCursor;
import spiralcraft.data.spi.ListAggregate;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Expression;

/**
 * A Query which provides access to all instances of a given Type. This is usually the 
 *   eventual upstream source for all Queries. 
 */
public class ReferenceQuery
  extends Query
{
  
  private Expression<Aggregate<?>> reference;

  
  /**
   * Construct an unconfigured ReferenceQuery
   */
  public ReferenceQuery()
  { }
  
  public ReferenceQuery(String reference)
  { this.reference=Expression.<Aggregate<?>>create(reference);
  }
  
  /**
   * Construct a new Scan for the given Type
   */
  public ReferenceQuery(Expression<Aggregate<?>> reference)
  { this.reference=reference;
  }
  
  public ReferenceQuery(Query baseQuery)
  { super(baseQuery);
  }
    
  public Expression<Aggregate<?>> getReference()
  { return reference;
  }
  
  public void setReference(Expression<Aggregate<?>> reference)
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
  public <T extends Tuple> BoundQuery<?,T> 
    getDefaultBinding(Focus<?> focus,Queryable<?> queryable)
    throws DataException
  { 
    try
    { return new BoundReferenceQuery(this,focus);
    }
    catch (BindException x)
    { throw new DataException("Error binding Shuffle",x);
    }
  }
  
  @Override
  public String toString()
  { return super.toString()+"[source="+reference+"]";
  }
  
  
}

class BoundReferenceQuery<T extends Tuple>
  extends BoundQuery<ReferenceQuery,T>
{

  private Channel<Aggregate<T>> source;
  
  @SuppressWarnings("unchecked") // Expression is untyped
  public BoundReferenceQuery(ReferenceQuery query,Focus<?> focus)
    throws BindException
  { 
    setQuery(query);
    Channel channel=focus.bind(query.getReference());
    source=channel;
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public Type<T> getType()
  { 
    return (Type<T>) ((DataReflector<Aggregate<T>>) source.getReflector())
      .getType().getContentType();
  }


  @Override
  public ScrollableCursor<T> execute() throws DataException
  { 
    Aggregate<T> aggregate=source.get();
    if (aggregate==null)
    { 
      aggregate
        =new ListAggregate<T>(Type.getAggregateType(getType()));
    }
    return new ListCursor<T>(aggregate);
  } 

}

