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

import spiralcraft.data.FieldSet;
import spiralcraft.data.Identifier;
import spiralcraft.data.Tuple;
import spiralcraft.data.DataException;
import spiralcraft.data.Type;

import spiralcraft.data.access.ScrollableCursor;
import spiralcraft.data.access.SerialCursor;

import spiralcraft.data.lang.CursorBinding;
import spiralcraft.lang.BindException;
import spiralcraft.log.ClassLogger;

/**
 * <p>A BoundQuery is a data path that resolves sets of Tuples from a 
 *    Queryable based on sets of parameters and other criteria. 
 * </p>
 * 
 * <p>A BoundQuery is created when a Query is bound to a Focus
 *    and a Queryable. The Focus provides external parameter values, and the
 *    Queryable provides access to Data.
 * </p>
 *    
 * <p>Tuples are exposed via the SerialCursor interface.
 * </p>
 *
 */
public abstract class BoundQuery<Tq extends Query,Tt extends Tuple>
{
  protected static final ClassLogger log
    =ClassLogger.getInstance(BoundQuery.class);
  
  private Tq query;
  // private BoundQueryBinding resultBinding;
  private boolean resolved;
  protected boolean debug;
  
  public Type<?> getType()
  { return query.getType();
  }
  
  /**
   * @return The Query used to create this BoundQuery
   */
  public final Tq getQuery()
  { return query;
  }
  
  /**
   * Set/reset the Query this binding will implement.
   */
  public void setQuery(Tq query)
  { 
    assertUnresolved();
    this.query=query;
    if (query.getDebug())
    { debug=true;
    }
  }
  
  /**
   * @return The binding available for downstream components.
   */
  // public final TupleBinding<Tuple> getResultBinding()
  // { return resultBinding;
  // }
  
  
  /**
   * <P>A BoundQuery is often composed of nested BoundQueries, which form a 
   *   data flow  tree. Once the tree is created, the field names and parameter
   *   references must be resolved to various points in this data flow tree.
   *   In some cases, nodes in this data flow tree may be collapsed for 
   *   efficiency.
   *   
   * <P>
   */
  public void resolve()
    throws DataException
  { 
    if (resolved)
    { return;
    }
    resolved=true;
    
//    try
//    { resultBinding=new BoundQueryBinding(query.getFieldSet());
//    }
//    catch (BindException x)
//    { throw new DataException("Error creating BoundQuery result binding: "+x,x);
//    }
  }
  
  /**
   * Performs the Query, resetting the SerialCursor to the beginning. This method
   *   can efficiently be called repeatedly.
   *
   * @throws DataException If anything goes wrong when executing the Query
   */
  public abstract SerialCursor<Tt> execute()
    throws DataException;
  

  protected void assertUnresolved()
  { 
    if (resolved)
    { throw new IllegalStateException(toString()+" has already been resolved");
    }
  }
  
  @SuppressWarnings("unchecked")
  public QueryChannel bind()
    throws BindException
  { return new QueryChannel((BoundQuery<Query,Tuple>) this);
  }
  
  
  public abstract class BoundQuerySerialCursor
    implements SerialCursor<Tt>
  { 
    private Tt tuple;
    protected Identifier relationId;
    
    /**
     * Implementations must call this when a new Tuple is available for 
     *   processing.
     */
    protected final void dataAvailable(Tt tuple)
    { 
      this.tuple=tuple;
      // TODO: Notify?
    }

    public Tt dataGetTuple()
    { return tuple;
    }
    
    public FieldSet dataGetFieldSet()
    { return query.getFieldSet();
    }
        
    public CursorBinding<Tt,? extends SerialCursor<Tt>> bind()
      throws BindException
    { return new CursorBinding<Tt,SerialCursor<Tt>>(this);
    }

    public Identifier getRelationId()
    { return relationId;
    }
    
    public Type<?> getResultType()
    { return BoundQuery.this.getType();
    }
  }

  public abstract class BoundQueryScrollableCursor
    extends BoundQuerySerialCursor
    implements ScrollableCursor<Tt>
  {
    @Override
    public CursorBinding<Tt,? extends ScrollableCursor<Tt>> bind()
      throws BindException
    { return new CursorBinding<Tt,ScrollableCursor<Tt>>(this);
    }
    
  }


}


