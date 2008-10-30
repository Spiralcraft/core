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
public class ChannelScan
  extends Query
{
  
  private Expression<Aggregate<?>> source;

  
  /**
   * Construct an unconfigured Scan
   */
  public ChannelScan()
  { }
  
  public ChannelScan(String source)
  { this.source=Expression.<Aggregate<?>>create(source);
  }
  
  /**
   * Construct a new Scan for the given Type
   */
  public ChannelScan(Expression<Aggregate<?>> source)
  { this.source=source;
  }
  
  public ChannelScan(Query baseQuery)
  { super(baseQuery);
  }
    
  public Expression<Aggregate<?>> getSource()
  { return source;
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
    return (BoundQuery<?,T>) queryable.getAll(type);
  }
  
  @Override
  public String toString()
  { return super.toString()+"[source="+source+"]";
  }
  
  
}

class BoundChannelScan<T extends Tuple>
  extends BoundQuery<ChannelScan,T>
{

  private Channel<Aggregate<T>> source;
  
  @SuppressWarnings("unchecked") // Expression is untyped
  public BoundChannelScan(ChannelScan query,Focus<?> focus)
    throws BindException
  { 
    setQuery(query);
    Channel channel=focus.bind(query.getSource());
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
        =new ListAggregate<T>(getType());
    }
    return new ListCursor<T>(aggregate);
  } 

}

