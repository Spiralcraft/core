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
import spiralcraft.log.ClassLog;
import spiralcraft.log.Level;

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
  private static final ClassLog log
    =ClassLog.getInstance(RecordCursor.class);

  private final RecordIterator recordIterator;
  private final RecordFormat format;
  private Tuple tuple;
  protected boolean errorTolerant=false;
  
  public RecordCursor
    (RecordIterator recordIterator
    ,RecordFormat format
    )
  { 
    this.recordIterator=recordIterator;
    this.format=format;
  }
  
  /**
   * Transparently skip unparseable records, instead of throwing an
   *   exception in methods that move the cursor.
   * 
   * @param errorTolerant
   */
  public void setErrorTolerant(boolean errorTolerant)
  { this.errorTolerant=errorTolerant;
  }
  
  @Override
  public FieldSet getFieldSet()
  { return format.getType().getFieldSet();
  }

  @Override
  public Tuple getTuple()
    throws DataException
  { return tuple;
  }

  @Override
  public boolean next()
    throws DataException
  {
    try
    {
      while (true)
      {
        try
        {
          if (recordIterator.next())
          {
            update();
            return true;
          }
          else
          { 
            tuple=null;
            return false;
          }
        }
        catch (ParseException x)
        {
          if (errorTolerant)
          { 
            log.log(Level.INFO,"Skipping unreadable record #"
              +recordIterator.getRecordPointer(),x);
          }
          else
          { throw x;
          }
        }
      }
    }
    catch (IOException x)
    { throw new DataException("Error moving to next record",x);
    }
  }

  protected void update()
    throws DataException
  {
    try
    {
      if (!recordIterator.isEOF() && !recordIterator.isBOF())
      {
        byte[] record=recordIterator.read();
        if (tuple==null)
        { 
          tuple=new EditableArrayTuple(getFieldSet())
          {
            @Override
            public boolean isVolatile()
            { return true;
            }
          };
        }
        try
        { format.parse(record,tuple);
        }
        catch (Exception x)
        { 
          throw new ParseException
            ("Error reading record #"+recordIterator.getRecordPointer(),x);
        }
      
      }
      else
      { tuple=null;
      }
    }
    catch (IOException x)
    { throw new DataException("Error checking EOF",x);
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
  { return getFieldSet().getType();
  }

  @Override
  public void close()
    throws DataException
  { 
    try
    { recordIterator.close();
    }
    catch (IOException x)
    { throw new DataException("Error closing recordIterator: "+x);
    }
  }
}
