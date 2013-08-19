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

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.LinkedList;
import java.util.List;

import spiralcraft.data.Aggregate;
import spiralcraft.data.DataException;
import spiralcraft.data.EditableAggregate;
import spiralcraft.data.spi.ArrayJournalTuple;
import spiralcraft.data.spi.EditableListAggregate;

/**
 * <p>Holds the set of Tuples that match a given key value.
 * </p>
 * 
 * @author mike
 *
 */
//XXX: Upgrade from using SoftReferences to something more predictable
public class CacheEntry
{

  private final CacheIndex index;
  private volatile Reference<EditableListAggregate<ArrayJournalTuple>> ref;
  
  CacheEntry(CacheIndex index)
  { this.index=index;
  }
  
  
  /**
   * 
   * @param oldValue
   * @param newValue
   */
  void updated(ArrayJournalTuple oldValue,ArrayJournalTuple newValue)
  {
    if (ref==null)
    { 
      // Not yet initialized
      return;
    }
    
    EditableAggregate<ArrayJournalTuple> data=ref.get();
    if (data==null)
    { 
      // This reference has expired
      return;
    }
    data.remove(oldValue);
    data.add(newValue);
  }
  
  void movedOut(ArrayJournalTuple oldValue)
  {
    if (ref==null)
    { 
      // Not yet initialized
      return;
    }
    EditableAggregate<ArrayJournalTuple> data=ref.get();
    if (data==null)
    { 
      // This reference has expired
      return;
    }
    data.remove(oldValue);
  }

  void movedIn(ArrayJournalTuple newValue)
  {
    if (ref==null)
    { 
      // Not yet initialized
      return;
    }
    EditableAggregate<ArrayJournalTuple> data=ref.get();
    if (data==null)
    { 
      // This reference has expired
      return;
    }
    data.add(newValue);
  }
  
  /**
   * Install a fetched set of data into cache
   * 
   * @param cursor
   * @return
   * @throws DataException
   */
  Aggregate<ArrayJournalTuple> fetched(List<ArrayJournalTuple> cursor)
    throws DataException
  { 
    EditableListAggregate<ArrayJournalTuple> data
      =new EditableListAggregate<ArrayJournalTuple>
        (index.getAggregateType()
        ,new LinkedList<ArrayJournalTuple>()
        );
    data.addAll(cursor.iterator());
    ref=new SoftReference<EditableListAggregate<ArrayJournalTuple>>(data);
    return data.snapshot();
  }
  


    
  Aggregate<ArrayJournalTuple> get()
    throws DataException
  { 
    if (ref==null)
    { return null;
    }
    Aggregate<ArrayJournalTuple> data=ref.get();
    if (data==null)
    { return null;
    }
    return data.snapshot();
  }
  
}
