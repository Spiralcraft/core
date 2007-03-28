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
import spiralcraft.data.Tuple;
import spiralcraft.data.DataException;



import spiralcraft.data.transport.SerialCursor;

import spiralcraft.data.lang.TupleBinding;

import spiralcraft.lang.BindException;

/**
 * <P>A BoundQuery is a data path that resolves sets of Tuples based on sets of
 *    parameters and criteria. 
 *    
 * <P>It is created when a Query is bound to a Focus
 *    and a Queryable. The Focus provides external parameter values, and the
 *    Queryable provides access to Data.
 *    
 * <P>Tuples are exposed via the SerialCursor interface.
 */
public abstract class BoundQuery<Tq extends Query>
{
  private Tq query;
  private BoundQueryBinding resultBinding;
  private Tuple tuple;
  private boolean resolved;
  
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
  }
  
  /**
   * @return The binding available for downstream components.
   */
  public final TupleBinding getResultBinding()
  { return resultBinding;
  }
  
  /**
   * Implementations must call this when a new Tuple is available for 
   *   processing.
   */
  protected final void dataAvailable(Tuple tuple)
  { 
    this.tuple=tuple;
    // TODO: Notify?
  }
  
  /**
   * <P>A BoundQuery is often composed of nested BoundQueries, which form a data flow
   *   tree. Once the tree is created, the field names and parameter references must
   *   be resolved to various points in this data flow tree. In some cases, nodes in
   *   this data flow tree may be collapsed for efficiency.
   *   
   * <P>
   */
  public void resolve()
    throws DataException
  { 
    assertUnresolved();
    if (!resolved)
    { resolved=true;
    }
  }
  
  /**
   * Performs the Query, resetting the SerialCursor to the beginning. This method
   *   can efficiently be called repeatedly.
   *
   * @throws DataException If anything goes wrong when executing the Query
   */
  public abstract SerialCursor execute()
    throws DataException;
  

  protected void assertUnresolved()
  { 
    if (resolved)
    { throw new IllegalStateException(toString()+" has already been resolved");
    }
  }
  
  class BoundQueryBinding
    extends TupleBinding
  {
    
    public BoundQueryBinding(FieldSet fieldSet)
      throws BindException
    { super(fieldSet,false);
    }
    
    protected Tuple retrieve()
    { return tuple;
    }
    
    protected boolean store(Tuple tuple)
    { throw new UnsupportedOperationException("BoundQueryBinding is read-only");
    }
  }
  
  abstract class BoundQuerySerialCursor
    implements SerialCursor
  { 
    public Tuple dataGetTuple()
    { return tuple;
    }
    
    public FieldSet dataGetFieldSet()
    { return query.getFieldSet();
    }
    
  }
    
}
