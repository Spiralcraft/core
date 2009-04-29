//
// Copyright (c) 1998,2007 Michael Toth
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

import java.util.HashSet;
import java.util.List;

import spiralcraft.lang.Focus;
import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;
import spiralcraft.lang.TeleFocus;
import spiralcraft.log.ClassLog;
import spiralcraft.util.ArrayUtil;

import spiralcraft.data.Projection;
import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Type;
import spiralcraft.data.access.ScrollableCursor;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.core.ProjectionImpl;
import spiralcraft.data.spi.ArrayTuple;

/**
 * A query which returns a subset of the rows of an original query
 */
public class Distinct
  extends Query
{
  private static final ClassLog log
    =ClassLog.getInstance(Selection.class);
  
  private Projection<Tuple> projection;
  private String[] names;
  
  public Distinct()
  { 
  }
  
  public Distinct
      (Distinct baseQuery
      ,Projection<Tuple> projection
      )
  { 
    super(baseQuery);
    this.projection=projection;
  }
  
  /**
   * Construct a Selection which reads data from the specified source Query and filters
   *   data according to the specified constraints expression.
   */
  public Distinct(Query source,Projection<Tuple> projection)
  { 
    this.projection=projection;
    addSource(source);
  }
  
  public void setNames(String[] names)
  { 
    this.names=names;
    if (names==null)
    { throw new IllegalArgumentException("Names cannot be null: "+toString());
    }
  }
  
  @Override
  public void resolve()
    throws DataException
  { 
    super.resolve();
    List<Query> sources=getSources();
    if (sources==null || sources.size()!=1)
    { throw new DataException
        ("Distinct must have a single source source, not "
        +(sources!=null?sources.size():"0")
        );
    }
    if (debug)
    { log.fine("Creating projection for "+ArrayUtil.format(names,",",""));
    }
    ProjectionImpl<Tuple> projectionImpl
      =new ProjectionImpl<Tuple>(sources.get(0).getFieldSet(),names);
    projectionImpl.resolve();
    projection=projectionImpl;
    this.type=projection.getType();
    
  }
  
  @Override
  public FieldSet getFieldSet()
  { return projection;
  }
    


  
  /**
   * Specify the Expression which constrains the result
   */
  public void setProjection(Projection<Tuple> projection)
  { this.projection=projection;
  }
 
  public void setSource(Query source)
  { 
    addSource(source);
  }
  
  /**
   *@return the Expression which constrains the result
   */
  public Projection<Tuple> getProjection()
  { return projection;
  }
  
  
  @Override
  public <T extends Tuple> BoundQuery<?,T> getDefaultBinding(Focus<?> focus,Queryable<?> store)
    throws DataException
  { return new DistinctBinding<Distinct,T,Tuple>(this,focus,store);
   
  }
  
  @Override
  public String toString()
  { return super.toString()
      +"[projection="+projection+"]: sources="
      +getSources().toString();
  }
    
}

class DistinctBinding<Tq extends Distinct,T extends Tuple,Ts extends Tuple>
  extends UnaryBoundQuery<Tq,T,Ts>
{

  private final Focus<?> paramFocus;
  private Focus<Ts> focus;
  private Channel<T> projectionChannel;
  private boolean resolved;
  
  public DistinctBinding
    (Tq query
    ,Focus<?> paramFocus
    ,Queryable<?> store
    )
    throws DataException
  { 
    super(query.getSources(),paramFocus,store);
    setQuery(query);
    this.paramFocus=paramFocus;
    
  }

  @Override
  @SuppressWarnings("unchecked")
  public void resolve() throws DataException
  { 
    if (!resolved)
    {
      super.resolve();
    

      focus=new TeleFocus<Ts>(paramFocus,sourceChannel);
      if (debug)
      { log.fine("Binding projection "+getQuery().getProjection());
      }
      try
      { 
        projectionChannel
          =(Channel<T>) getQuery().getProjection()
            .bindChannel( (Focus<Tuple>) focus);
        if (debug)
        { projectionChannel.setDebug(true);
        }
      }
      catch (BindException x)
      { throw new DataException("Error binding constraints "+x,x);
      }
      resolved=true;
    }
  }
  

  @Override
  protected SerialCursor<T> newSerialCursor(SerialCursor<Ts> source)
    throws DataException
  { return new DistinctSerialCursor(source);
  }
  
  @Override
  protected ScrollableCursor<T> newScrollableCursor(ScrollableCursor<Ts> source)
  { return null;
  }

  protected class DistinctSerialCursor
    extends UnaryBoundQuerySerialCursor
  {
    private HashSet<T> distinctMap
      =new HashSet<T>();
    
      
    public DistinctSerialCursor(SerialCursor<Ts> source)
      throws DataException
    { super(source);
    }
  
    @Override
    @SuppressWarnings("unchecked")
    protected boolean integrate()
      throws DataException
    { 
      Ts t=sourceChannel.get();
      if (t==null)
      { 
        if (debug)
        { log.fine(toString()+"BoundDistinct: eod ");
        }
        return false;
      }
    
      Tuple projection=projectionChannel.get();
      if (distinctMap.contains(projection))
      { 
        if (debug)
        { log.fine(toString()+"BoundDistinct: duplicate "+t);
        }
        return false;
      }
      
      // XXX This is why generic tuples for queries are strange.
      //
      //      Need to make a copy of the projection to store it in the map 
      //      Consider binding a TupleKeyFunction
      //
      //      Queries should be read-only, but bufferable when required.
      //      Though there is the benefit of being able to use on Deltas
      //      or EditableTuples.
      
      T projected=(T) new ArrayTuple(projection);
      distinctMap.add(projected);
      dataAvailable(projected);
      return true;
    }
    
    @Override
    public Type<?> getResultType()
    { return getQuery().getType();
    }
  }

}