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
package spiralcraft.data.session;

import java.util.ArrayList;
import java.util.Iterator;

import spiralcraft.data.Aggregate;
import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.EditableAggregate;
import spiralcraft.data.RuntimeDataException;
import spiralcraft.data.Type;
import spiralcraft.data.Identifier;

import spiralcraft.data.spi.ArrayListAggregate;
import spiralcraft.data.transaction.Transaction;
import spiralcraft.log.ClassLogger;

public class BufferAggregate<T extends DataComposite>
  extends Buffer
  implements EditableAggregate<Buffer>
{ 
  private final ClassLogger log=ClassLogger.getInstance(BufferAggregate.class);

  
  private final DataSession session;
  private Aggregate<T> original;
  private ArrayList<Buffer> buffers;
  private Type<?> type;
  private Identifier id;
  private boolean editable=true;
    
  public BufferAggregate(DataSession session,Aggregate<T> original)
    throws DataException
  { 
    this.session=session;
    this.original=original.snapshot();
    this.type=Type.getBufferType(original.getType());
  }
  
  public BufferAggregate(DataSession session,Type<?> type)
  { 
    this.session=session;
    this.type=type;
  }
  
  public boolean isDirty()
  { 
    if (buffers==null)
    { return false;
    }

    for (Buffer buffer : buffers)
    { 
      if (buffer!=null && buffer.isDirty())
      { return true;
      }
    }

    return false;
  }
  
  public synchronized void revert()
  { buffers=null;
  }
  
  
  public Identifier getId()
  { return id;
  }
  
  public void setId(Identifier id)
  { this.id=id;
  }
  
  @Override
  public BufferAggregate<T> asAggregate()
  { return this;
  }

  @Override
  public BufferTuple asTuple()
  { return null;
  }

  @Override
  public Type<?> getType()
  { return type;
  }

  @Override
  public boolean isAggregate()
  { return true;
  }

  @Override
  public boolean isTuple()
  { return false;
  }

  @Override
  public String toText(
    String indent)
    throws DataException
  { return "Buffer:["+original.toText(indent+"  ")+"\r\n]";
  }

  @Override
  public void add(
    Buffer val)
  {
    
    if (val==null)
    { throw new IllegalArgumentException
        ("Cannot add a null value to an Aggregate");
    }
    if (buffers==null)
    { buffers=new ArrayList<Buffer>();
    }
    buffers.add(val);
    // XXX Should check type, might have to dirty fields, etc. Might be
    //   a new Tuple which requires data, etc.
    
  }

  @Override
  public void addAll(
    Aggregate<Buffer> values)
  {
    // TODO Auto-generated method stub
    for (Buffer value : values)
    { add(value);
    }
  }

  @Override
  public Buffer get(
    int index)
    throws DataException
  { 
    if (index < size())
    { 
      return buffer(index);
    }
    else throw new IndexOutOfBoundsException
        ("Index "+index+" exceeds size "+size());
  }
  
  private Buffer buffer(int index)
    throws DataException
  { 
    if (index<size())
    {
      if (buffers==null)
      { buffers=new ArrayList<Buffer>(index+1);
      }
      
      for (int i=buffers.size();i<=index;i++)
      { buffers.add(null);
      }
      Buffer buffer=buffers.get(index);
      if (buffer==null)
      { 
        if (index<original.size())
        { 
          buffer=session.buffer(original.get(index));
          buffers.set(index,buffer);
        }
        else
        { buffer=null;
        }
      }
      return buffer;
    }
    else throw new IndexOutOfBoundsException
      ("Index "+index+" exceeds size "+size());
  }
  

  @Override
  public boolean isMutable()
  {
    // TODO Auto-generated method stub
    return true;
  }

  @Override
  public int size()
  {
    if (buffers==null)
    { return original.size();
    }
    else
    { return Math.max(original.size(),buffers.size());
    }
  }

  @Override
  public Aggregate<Buffer> snapshot()
    throws DataException
  { return new ArrayListAggregate<Buffer>(this);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Iterator<Buffer> iterator()
  {
    return new Iterator<Buffer>()
    {
      private int i=0;
      
      public Buffer next()
      { 
        if (i<size())
        {
          try
          { return buffer(i++);
          }
          catch (DataException x)
          { throw new RuntimeDataException("Error buffering",x);
          }
        }
        else
        { return null;
        }

        
      }
      
      public boolean hasNext()
      { return i<size();
      }
      
      public void remove()
      {
      }
    };
    
  }
  
  public void setEditable(boolean val)
  { editable=val;
  }
  
  public boolean isEditable()
  { return (original instanceof EditableAggregate && editable);
  }
  
  public Aggregate<T> getOriginal()
  { return original;
  }

  public void save()
    throws DataException
  {
    if (buffers==null)
    { return;
    }
    log.fine("Saving..."+this);
    
    Transaction transaction
      =Transaction.getContextTransaction();
  
    if (transaction!=null)
    {
      
      for (Buffer buffer: this)
      { buffer.save();
      }
    }
    else
    { 
      transaction=
        Transaction.startContextTransaction(Transaction.Nesting.ISOLATE);
      try
      {
        for (Buffer buffer: this)
        { buffer.save();
        }
      
        transaction.commit();
      }
      finally
      {
        transaction.complete();
      }
    }
  }
  
}


