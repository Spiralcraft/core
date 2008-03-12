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
package spiralcraft.data.access;



import spiralcraft.data.DataException;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Space;
import spiralcraft.data.Tuple;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.Type;
import spiralcraft.data.Key;

import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;

import spiralcraft.data.query.BoundQuery;

import spiralcraft.data.lang.TupleFocus;

import spiralcraft.data.spi.ArrayDeltaTuple;

/**
 * <P>Accepts Tuples from the caller, compares them with those found in
 *   a Space, and generates DeltaTuples which reflect the difference and
 *   can be used to update the Space.
 *   
 * <P>The DataSynchronizer is most useful when a Store must be kept in-sync
 *   with an externally maintained data source that does not provide an update
 *   log.
 */
@SuppressWarnings("unchecked") // Chains are heterogeneous
public class DataSynchronizer
  implements DataConsumerChain
{

  private final Key primaryKey;
  private final Channel<Tuple> keyBinding;
  private final Space space;
  private final TupleFocus<Tuple> primaryKeyFocus;
  private final TupleFocus<Tuple> tupleFocus;
  
  
  private DataConsumer nextConsumer;
  
  public DataSynchronizer(Space space,Type<?> type)
    throws DataException
  { 
    this.space=space;
    tupleFocus=new TupleFocus<Tuple>(type.getScheme());
    nextConsumer=space.getUpdater(type,tupleFocus);
    primaryKey=type.getScheme().getPrimaryKey();
    if (primaryKey==null)
    { 
      throw new DataException
        ("DataSynchronizer: Type "+type.getURI()+" must have a primary key");
    }
    primaryKeyFocus=new TupleFocus<Tuple>(primaryKey);
    try
    {
      keyBinding=primaryKey.bind(tupleFocus);
    }
    catch (BindException x)
    { throw new DataException("Error Binding key:",x);
    }
    
    
  }
  
  public void insertDataConsumer(DataConsumerChain consumerChain)
  { 
    if (nextConsumer!=null)
    { consumerChain.setDataConsumer(nextConsumer);
    }
    nextConsumer=consumerChain;
  }

  public void setDataConsumer(DataConsumer consumer)
  { nextConsumer=consumer;
  }

  @SuppressWarnings("unchecked") // Chain pass-through is not compile-time safe
  public void dataAvailable(Tuple tuple)
    throws DataException
  {
    Tuple storeTuple=findTuple(tuple);
    DeltaTuple delta=new ArrayDeltaTuple(storeTuple,tuple);
    if (delta.isDirty())
    { nextConsumer.dataAvailable(delta);
    }
  }
  
  private Tuple findTuple(Tuple tuple)
    throws DataException
  {
    tupleFocus.setTuple(tuple);
    Tuple keyValue=keyBinding.get();
    primaryKeyFocus.setTuple(keyValue);
    BoundQuery<?,?> query=space.query(primaryKey.getQuery(),primaryKeyFocus);
    SerialCursor<?> cursor=query.execute();
    Tuple storeTuple=null;
    while (cursor.dataNext())
    {
      if (storeTuple==null)
      { storeTuple=cursor.dataGetTuple();
      }
      else
      { 
        throw new DataException
          ("Ambiguous primary key for ["+cursor.dataGetTuple()+"]");
      }
    }
    return storeTuple;
  }

  public void dataInitialize(FieldSet fieldSet)
    throws DataException
  { 
    nextConsumer.dataInitialize(fieldSet);
  }
  
  public void dataFinalize()
    throws DataException
  { nextConsumer.dataFinalize();
  }

 

}

// History:
//
// 2008-02-26 mike: fixed up binding mechanism
