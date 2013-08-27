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
import spiralcraft.data.Identifier;
import spiralcraft.data.Tuple;
import spiralcraft.data.spi.ArrayJournalTuple;
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;

/**
 * Holds canonical references to Tuples referenced in 
 *   app memory.
 * 
 * @author mike
 *
 */
public class PrimarySet
{
  final ClassLog log=ClassLog.getInstance(getClass());
  Level logLevel=Level.INFO;
  private final EntityCache cache;
  private final HashMap<Identifier,TupleReference> map
    =new HashMap<Identifier,TupleReference>();
  
  PrimarySet(EntityCache cache)
  { this.cache=cache;
  }
  
  private TupleReference entry(Identifier id)
  {
    synchronized (map)
    { 
      TupleReference entry=map.get(id);
      if (entry==null)
      { 
        if (logLevel.isFine())
        { log.fine("New tuple reference for "+cache.getType().getURI()+" "+id);
        }
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
  
  ArrayJournalTuple get(Identifier id)
  {
    synchronized (map)
    {
      TupleReference entry=map.get(id);
      if (entry==null)
      { return null;
      }
      else
      { return entry.get();
      }
    }
    
  }
  
  ArrayJournalTuple replace(Identifier id,ArrayJournalTuple data)
  { return entry(id).replace(data);
  }
    
  void removeEntry(Identifier id)
  { map.remove(id);
  }
  
}

class TupleReference
{
  final PrimarySet set;
  volatile WeakReference<ArrayJournalTuple> ref;
  
  TupleReference(PrimarySet set)
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
  synchronized ArrayJournalTuple cache(Tuple foreign)
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
  
  synchronized ArrayJournalTuple get()
  {
    if (ref==null)
    { return null;
    }
    return ref.get();
  }
  
  synchronized ArrayJournalTuple replace(ArrayJournalTuple newData)
  { 
    ArrayJournalTuple ret=ref!=null?ref.get():null;
    replaceRef(newData);
    return ret;
  }
  
  private void replaceRef(ArrayJournalTuple newValue)
  { ref=new WeakReference<ArrayJournalTuple>(newValue);
  }
  
}
