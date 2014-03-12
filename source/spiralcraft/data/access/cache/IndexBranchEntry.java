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

import java.util.LinkedList;
import java.util.List;

import spiralcraft.data.Aggregate;
import spiralcraft.data.DataException;
import spiralcraft.data.JournalTuple;
import spiralcraft.data.spi.EditableListAggregate;

/**
 * <p>Holds the set of Tuples that match a given key value within the
 *   context of a transaction-in-progress
 * </p>
 * 
 * @author mike
 *
 */
//XXX: Upgrade from using SoftReferences to something more predictable
public class IndexBranchEntry
{

  private final IndexBranch index;
  private EditableListAggregate<JournalTuple> data;
  
  IndexBranchEntry(IndexBranch index)
  { this.index=index;
  }
  
  
  /**
   * 
   * @param oldValue
   * @param newValue
   */
  void updated(JournalTuple oldValue,JournalTuple newValue)
  {    
    if (data==null)
    { 
      // This reference has expired
      return;
    }
    data.remove(oldValue);
    data.add(newValue);
  }
  
  void movedOut(JournalTuple oldValue)
  {
    if (data==null)
    { 
      // This reference has expired
      return;
    }
    data.remove(oldValue);
  }

  void movedIn(JournalTuple newValue)
  {
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
  Aggregate<JournalTuple> fetched(List<JournalTuple> cursor)
    throws DataException
  { 
    data
      =new EditableListAggregate<JournalTuple>
        (index.getAggregateType()
        ,new LinkedList<JournalTuple>()
        );
    data.addAll(cursor.iterator());
    return data.snapshot();
  }
  


    
  Aggregate<JournalTuple> get()
    throws DataException
  { 
    if (data==null)
    { return null;
    }
    return data.snapshot();
  }
  
}
