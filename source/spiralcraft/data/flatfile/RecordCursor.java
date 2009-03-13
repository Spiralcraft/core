//
// Copyright (c) 2009,2009 Michael Toth
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
package spiralcraft.data.flatfile;

import java.io.IOException;

import spiralcraft.data.DataException;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Identifier;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.access.SerialCursor;
import spiralcraft.data.lang.CursorBinding;
import spiralcraft.data.spi.EditableArrayTuple;
import spiralcraft.io.record.RecordIterator;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Channel;

/**
 * Turns flatfile records in arbitrary formats into data
 * 
 * @author mike
 *
 * @param <T>
 */
public class RecordCursor
  implements SerialCursor<Tuple>
{

  private RecordIterator recordIterator;
  private RecordFormat format;
  private Tuple tuple;
  
  public RecordCursor
    (RecordIterator recordIterator
    ,RecordFormat format
    )
  { 
    this.recordIterator=recordIterator;
    this.format=format;
  }
  
  @Override
  public FieldSet dataGetFieldSet()
  { return format.getType().getFieldSet();
  }

  @Override
  public Tuple dataGetTuple()
    throws DataException
  { return tuple;
  }

  @Override
  public boolean dataNext()
    throws DataException
  {
    try
    {
      if (recordIterator.next())
      {
      
        if (tuple==null)
        { tuple=new EditableArrayTuple(dataGetFieldSet());
        }
        byte[] record=recordIterator.read();
        format.parse(record, tuple);
        return true;
      }
      else
      { return false;
      }
    }
    catch (IOException x)
    { throw new DataException("Error moving to next record",x);
    }
  }

  @Override
  public Channel<Tuple> bind()
    throws BindException
  { return new CursorBinding<Tuple,RecordCursor>(this);
  }

  @Override
  public Identifier getRelationId()
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Type<?> getResultType()
  { return dataGetFieldSet().getType();
  }

}
