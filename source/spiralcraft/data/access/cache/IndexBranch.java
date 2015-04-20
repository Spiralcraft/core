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
import java.util.LinkedList;

import spiralcraft.data.Aggregate;
import spiralcraft.data.DataException;
import spiralcraft.data.KeyTuple;
import spiralcraft.data.Projection;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.JournalTuple;
import spiralcraft.data.spi.ListCursor;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;
import spiralcraft.util.KeyFunction;

/**
 * A set of CacheEntries associated with a particular keying scheme 
 * 
 * @author mike
 *
 */
public class IndexBranch
{
  final Projection<Tuple> key;

  private final ClassLog log=ClassLog.getInstance(getClass());
  private Level logLevel=ClassLog.getInitialDebugLevel(getClass(),Level.INFO);
  private final CacheBranch cacheBranch;
  private final KeyFunction<KeyTuple,Tuple> keyFunction;
  private final HashMap<KeyTuple,IndexBranchEntry> map
    =new HashMap<KeyTuple,IndexBranchEntry>();

  public IndexBranch(CacheBranch cacheBranch,Projection<Tuple> key)
  { 
    this.cacheBranch=cacheBranch;
    this.key=key;
    this.keyFunction=key.getKeyFunction();
  }
  
  Type<?> getAggregateType()
  { return cacheBranch.cache.getAggregateType();
  }
  
  
  private IndexBranchEntry entry(KeyTuple keyTuple)
  {
    synchronized (map)
    { 
      IndexBranchEntry entry=map.get(keyTuple);
      if (entry==null)
      { 
        if (logLevel.isFine())
        { log.fine("New cache entry for "+key.getType().getURI()+" "+keyTuple);
        }
        entry=new IndexBranchEntry(this);
        map.put(keyTuple,entry);
      }
      return entry;
    }
  }
  
  
  /**
   * Fetch data from the cache and/or the backing store, populating the cache
   *   and normalizing backing store data in the process.
   * 
   * @return
   */
  public SerialCursor<JournalTuple> fetch(KeyTuple keyTuple,KeyedDataProvider backing)
    throws DataException
  {
    try
    {
      IndexBranchEntry entry=entry(keyTuple);
      synchronized (entry)
      {
        Aggregate<JournalTuple> data=entry.get();
        if (data==null)
        {
          if (logLevel.isTrace())
          { log.fine("Cache miss for "+key.getType().getURI()+" "+keyTuple);
          }
          SerialCursor<Tuple> cursor=backing.fetch(keyTuple);
          try
          {
            LinkedList<JournalTuple> list=new LinkedList<JournalTuple>();
            while (cursor.next())
            {
              Tuple tuple=cursor.getTuple();
              JournalTuple normal=cacheBranch.cache(tuple);
              if (normal!=null)
              { list.add(normal);
              }
            }
            if (logLevel.isFine())
            { log.fine("Got "+list+" from store");
            }
            data=entry.fetched(list);
            
            if (logLevel.isFine())
            { log.fine("Normalized to "+data);
            }
          }
          finally
          { cursor.close();
          }
        }
        else
        { 
          if (logLevel.isFine())
          { log.fine("Cache hit for "+key.getType().getURI()+" "+keyTuple+" data: "+data);
          }
        }
        return new ListCursor<JournalTuple>(data);
      }
    }
    finally
    {
    }
  }


    
  void inserted(JournalTuple newValue)
  {
    KeyTuple newKey=keyFunction.key(newValue);
    IndexBranchEntry entry=entry(newKey);
    entry.movedIn(newValue);
    
  }

  void deleted(JournalTuple oldValue)
  {
    KeyTuple newKey=keyFunction.key(oldValue);
    IndexBranchEntry entry=entry(newKey);
    entry.movedOut(oldValue);
    if (logLevel.isFine())
    { log.fine("Moving out "+oldValue);
    }
    
  }
  
  void updated(JournalTuple oldValue,JournalTuple newValue)
  {
    KeyTuple oldKey=keyFunction.key(oldValue);
    KeyTuple newKey=keyFunction.key(newValue);
    if (oldKey.equals(newKey))
    { 
      IndexBranchEntry entry=map.get(oldKey);
      if (entry!=null)
      { entry.updated(oldValue,newValue);
      }
    }
    else
    {
      IndexBranchEntry entry=map.get(oldKey);
      if (entry!=null)
      { entry.movedOut(oldValue);
      }
      
      entry=map.get(newKey);
      if (entry!=null)
      { entry.movedIn(newValue);
      }
    }
  }
  
  
  
}


