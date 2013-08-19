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

import java.lang.ref.WeakReference;
import java.util.HashMap;

import spiralcraft.data.DataException;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.Identifier;
import spiralcraft.data.Tuple;
import spiralcraft.data.spi.ArrayJournalTuple;
import spiralcraft.log.ClassLog;

/**
 * Holds canonical references to Tuples referenced in 
 *   app memory.
 * 
 * @author mike
 *
 */
public class ReferenceSet
{
  final ClassLog log=ClassLog.getInstance(getClass());
  private final EntityCache cache;
  private final HashMap<Identifier,TupleReference> map
    =new HashMap<Identifier,TupleReference>();
  
  ReferenceSet(EntityCache cache)
  { this.cache=cache;
  }
  
  TupleReference entry(Identifier id)
  {
    synchronized (map)
    { 
      TupleReference entry=map.get(id);
      if (entry==null)
      { 
        log.fine("New tuple reference for "+cache.getType().getURI()+" "+id);
        entry=new TupleReference(this);
        map.put(id,entry);
      }
      return entry;
    }
  }  
  
  /**
   * Cache the specified tuple (if not already present) and return the 
   *   normalized version.
   * 
   * @param tuple
   * @return
   * @throws DataException
   */
  ArrayJournalTuple cache(Tuple tuple)
    throws DataException
  { return entry(tuple.getId()).cache(tuple);
  }  
  
  ArrayJournalTuple insert(DeltaTuple delta)
    throws DataException
  { 
    ArrayJournalTuple newData=ArrayJournalTuple.freezeDelta(delta);
    return entry(newData.getId()).insert(newData);
  }

  ArrayJournalTuple update(DeltaTuple delta)
    throws DataException
  { return entry(delta.getOriginal().getId()).update(delta);
  }
  
  void delete(ArrayJournalTuple tuple)
  { entry(tuple.getId()).delete(tuple);
  }  
  
  void removeEntry(Identifier id)
  { map.remove(id);
  }
  
}

class TupleReference
{
  final ReferenceSet set;
  WeakReference<ArrayJournalTuple> ref;
  
  TupleReference(ReferenceSet set)
  { this.set=set;
  }
  
  /**
   * Cache the specified Tuple (if not already in the cache) and return the
   *   normalized version
   *   
   * @param foreign
   * @return
   * @throws DataException
   */
  ArrayJournalTuple cache(Tuple foreign)
    throws DataException
  { 
    ArrayJournalTuple data;
    if (ref!=null)
    {
      data=ref.get();
      if (data!=null)
      { return data;
      }
    }
    
    ArrayJournalTuple normal;
    if (!(foreign instanceof ArrayJournalTuple))
    { normal=new ArrayJournalTuple(foreign);
    }
    else
    { normal=(ArrayJournalTuple) foreign;
    }
    ref=new WeakReference<ArrayJournalTuple>(normal);
    return normal;
  }  
  
  
  
  ArrayJournalTuple insert(ArrayJournalTuple normal)
    throws DataException
  { 
    ArrayJournalTuple data;
    if (ref!=null)
    {
      data=ref.get();
      if (data!=null)
      { 
        throw new DataException
          ("ID already cached, DI violation in cache: "
            +"new="+normal+"  existing="+data);
      }
    }
    
    replaceRef(normal);    
    set.log.info("CACHE INSERT: "+normal);
    return normal;
  }
  

  void delete(ArrayJournalTuple oldValue)
  {
    if (ref==null)
    { 
      set.log.warning("Deleting "+oldValue+" but reference is null");
      return;
    }
    ArrayJournalTuple data=ref.get();
    if (data==null)
    { set.log.warning("Deleting "+oldValue+" from cache but reference data is null");
    }
    synchronized (set)
    { set.removeEntry(oldValue.getId());
    }
    ref=null;
    set.log.info("CACHE DELETE: "+oldValue);
  }
  
  ArrayJournalTuple update(DeltaTuple delta)
    throws DataException
  { 
    ArrayJournalTuple existing=cache(delta.getOriginal());
    existing.prepareUpdate(delta);
    ArrayJournalTuple newData=existing.getTxVersion();
    replaceRef(newData);
    existing.commit();
    set.log.info("CACHE UPDATE: "+newData);
    return newData;
  }  
  
  private void replaceRef(ArrayJournalTuple newValue)
  { ref=new WeakReference<ArrayJournalTuple>(newValue);
  }
  
}
