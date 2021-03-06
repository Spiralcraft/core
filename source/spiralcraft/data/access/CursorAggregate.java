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
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.spi.ListAggregate;

/**
 * An Aggregate backed by a Cursor
 * 
 * @author mike
 */
public class CursorAggregate<Tt extends Tuple>
  extends ListAggregate<Tt>
{

  
  /**
   * <p>Construct the CursorAggregate by reading the specified cursor
   *   in its entirety
   * </p>
   * 
   * <p>It is the responsibility of the caller to close the cursor
   * </p>
   */
  @SuppressWarnings("unchecked")
  public CursorAggregate(SerialCursor<Tt> cursor)
    throws DataException
  { 
    super( Type.getAggregateType(cursor.getResultType()));

    while (cursor.next())
    { 
      Tt tuple=cursor.getTuple();
      if (tuple!=null)
      {
        if (tuple.isVolatile())
        { tuple=(Tt) tuple.snapshot();
        }
      
        list.add(tuple);
      }
    }
    
  }
  
  /**
   * <p>Construct the CursorAggregate by reading a limited part of
   *   the specified cursor.
   * </p>
   *
   * <p>It is the responsibility of the caller to close the cursor
   * </p>
   */
  @SuppressWarnings("unchecked")
  public CursorAggregate(SerialCursor<Tt> cursor,int limit)
    throws DataException
  { 
    super( Type.getAggregateType(cursor.getResultType()));

    int count=0;
    while (cursor.next() && count++<limit)
    { 
      Tt tuple=cursor.getTuple();
      if (tuple!=null)
      {
        if (tuple.isVolatile())
        { tuple=(Tt) tuple.snapshot();
        }
      
        list.add(tuple);
      }
    }
    
  }
  
  
}
