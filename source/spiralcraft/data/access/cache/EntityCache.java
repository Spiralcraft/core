//
// Copyright (c) 2013 Michael Toth
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
package spiralcraft.data.access.cache;

import java.util.HashMap;

import spiralcraft.data.DataException;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.Projection;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.spi.ArrayJournalTuple;
import spiralcraft.data.transaction.Transaction;
import spiralcraft.data.transaction.Transaction.Nesting;
import spiralcraft.data.transaction.Transaction.Requirement;
import spiralcraft.data.transaction.WorkException;
import spiralcraft.data.transaction.WorkUnit;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;



/**
 * <p>Caches keyed DataComposites queried from external sources
 * </p>
 * 
 * <p>An EntityCache manages a CacheIndex for each key in the Entity
 * </p>
 * 
 * @author mike
 *
 */
public class EntityCache
{
  private final ClassLog log=ClassLog.getInstance(getClass());
  private final Type<?> type;
  private final Type<?> aggregateType;
  private final CacheResourceManager resourceManager
    =new CacheResourceManager(this);
  
  final PrimarySet primary;
  final HashMap<Projection<?>,CacheIndex> indices
    =new HashMap<Projection<?>,CacheIndex>();
  Object monitor=new Object();

  private Level logLevel=Level.INFO;
  
  @SuppressWarnings("unchecked")
  public EntityCache(Type<?> t) 
    throws DataException
  { 
    this.type=t;
    this.aggregateType=Type.getAggregateType(t);
    this.primary=new PrimarySet(this);
  }
  
  Type<?> getAggregateType()
  { return aggregateType;
  }
  
  Type<?> getType()
  { return type;
  }
  
  public ArrayJournalTuple cache(Tuple tuple)
    throws DataException
  {
    CacheBranch branch=getBranch();
    if (branch!=null)
    { return branch.cache(tuple);
    }
    
    synchronized (monitor)
    { return primary.cache(tuple);
    }
  }
  
  public SerialCacheCursor cache(SerialCursor<?> cursor)
    throws DataException
  { return new SerialCacheCursor(cursor,this);
  }
  
  public CacheIndex getIndex(Projection<Tuple> key)
    throws DataException
  { 
    synchronized (monitor)
    {
      CacheIndex ret=indices.get(key);
      if (ret==null)
      { 
        if (logLevel.isFine())
        { log.fine("Creating cache index for "+key);
        }
        ret
          =new CacheIndex
            (this,key,primary);
        indices.put(key,ret);
      }
      return ret;
    }
  }

  
  public ArrayJournalTuple update(final DeltaTuple delta)
    throws DataException
  {
    return new WorkUnit<ArrayJournalTuple>
      (Requirement.REQUIRED,Nesting.PROPOGATE)
    {

      @Override
      protected ArrayJournalTuple run()
        throws WorkException
      {
        try
        {
          return resourceManager.branch(Transaction.getContextTransaction())
            .update(delta);
        }
        catch (DataException x)
        { throw new WorkException("Error updating cache",x);
        }
        
      }
      
    }.work();
    
    
  }

  CacheBranch getBranch()
  { return resourceManager.getBranch();
  }
}
