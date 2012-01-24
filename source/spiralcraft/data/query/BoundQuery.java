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

import spiralcraft.data.Aggregate;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Identifier;
import spiralcraft.data.Tuple;
import spiralcraft.data.DataException;
import spiralcraft.data.Type;

import spiralcraft.data.access.CursorAggregate;
import spiralcraft.data.access.ScrollableCursor;
import spiralcraft.data.access.SerialCursor;

import spiralcraft.data.kit.EmptyCursor;
import spiralcraft.data.lang.CursorBinding;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;

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
  
  public static final <Tt extends Tuple> Tt fetchUnique(BoundQuery<?,Tt> query)
    throws DataException
  { 
    SerialCursor<Tt> cursor=query.execute();
    Tt ret=null;
    try
    {
      while (cursor.next())
      {
        if (ret==null)
        { ret=cursor.getTuple();
        }
        else
        { 
          throw new DataException
            ("Non unique value encountered for "+ret.getType().getURI());
        }
      }
      return ret;
    }
    finally
    { cursor.close();
    }
  }
  
  public static final <Tt extends Tuple> Aggregate<Tt> fetch(BoundQuery<?,Tt> query)
    throws DataException
  { 
    SerialCursor<Tt> cursor=query.execute();
    try
    { return new CursorAggregate<Tt>(cursor);
    }
    finally
    { cursor.close();
    }
  }  
  protected final ClassLog log
    =ClassLog.getInstance(getClass());
  protected Level debugLevel
    =ClassLog.getInitialDebugLevel(getClass(), Level.INFO);
  
  private final Tq query;
  private boolean resolved;
  protected Type<?> boundType;
  protected final Focus<?> paramFocus;
  protected Channel<Boolean> condition;
  protected EmptyCursor<Tt> emptyCursor;
  
  public BoundQuery(Tq query,Focus<?> paramFocus)
  { 
    this.query=query;
    this.paramFocus=paramFocus;
    
    if (query.getDebugLevel().canLog(debugLevel))
    { debugLevel=query.getDebugLevel();
    }
  }
  
  /**
   * Return the element Type of the relation returned by the bound query.
   * 
   * @return
   */
  public Type<?> getType()
  { return boundType!=null?boundType:query.getType();
  }
  
  /**
   * @return The Query used to create this BoundQuery
   */
  public final Tq getQuery()
  { return query;
  }
  
  public void setDebugLevel(Level debugLevel)
  { this.debugLevel=debugLevel;
  }
  
  
  /**
   * <p>A BoundQuery is often composed of nested BoundQueries, which form a 
   *   data flow  tree. Once the tree is created, the field names and parameter
   *   references must be resolved to various points in this data flow tree.
   *   In some cases, nodes in this data flow tree may be collapsed for 
   *   efficiency.
   *   
   * </p>
   * 
   * @throws DataException
   */
  public void resolve()
    throws DataException
  { 
    if (resolved)
    { return;
    }
    resolved=true;
    
    if (query!=null 
        && paramFocus!=null 
        && condition==null
        && query.conditionX!=null
        )
    { 
      try
      { 
        condition=paramFocus.bind(query.conditionX);
        emptyCursor=new EmptyCursor<Tt>(getType().getFieldSet());
      }
      catch (BindException x)
      { 
        throw new DataException
          ("Error binding query condition "+query.conditionX,x);
      }
    }

  }
  
  /**
   * Performs the Query, resetting the SerialCursor to the beginning. This method
   *   can efficiently be called repeatedly.
   *
   * @throws DataException If anything goes wrong when executing the Query
   */
  public final SerialCursor<Tt> execute()
    throws DataException
  {
    if (!resolved)
    { resolve();
    }
    if (enabled())
    { return doExecute();
    }
    else
    { return emptyCursor;
    }
  }
  
  protected abstract SerialCursor<Tt> doExecute()
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
  
  protected boolean enabled()
  { return condition==null || Boolean.TRUE==condition.get();
  }
  
  protected abstract class BoundQuerySerialCursor
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

    @Override
    public Tt getTuple()
    { return tuple;
    }
    
    @Override
    public FieldSet getFieldSet()
    { return query.getFieldSet();
    }
        
    @Override
    public CursorBinding<Tt,? extends SerialCursor<Tt>> bind()
      throws BindException
    { return new CursorBinding<Tt,SerialCursor<Tt>>(this);
    }

    @Override
    public Identifier getRelationId()
    { return relationId;
    }
    
    @Override
    public Type<?> getResultType()
    { return BoundQuery.this.getType();
    }
  }

  protected abstract class BoundQueryScrollableCursor
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


