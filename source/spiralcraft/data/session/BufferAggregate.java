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
import spiralcraft.data.Projection;
import spiralcraft.data.RuntimeDataException;
import spiralcraft.data.Type;
import spiralcraft.data.Identifier;

import spiralcraft.data.spi.ListAggregate;
import spiralcraft.data.transaction.Transaction;
import spiralcraft.log.ClassLog;

public class BufferAggregate<T extends Buffer,Torig extends DataComposite>
  extends Buffer
  implements EditableAggregate<T>
{ 
  private final ClassLog log=ClassLog.getInstance(BufferAggregate.class);

  
  private final DataSession session;
  private Aggregate<Torig> original;
  private ArrayList<T> buffers;
  private Type<?> type;
  private Identifier id;
  private boolean editable=true;
  private boolean touched=false;
    
  public BufferAggregate(DataSession session,Aggregate<Torig> original)
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
  

  
  @Override
  public void clear()
  { buffers.clear();
  }
  
  @Override
  public boolean isDirty()
  { 
    if (touched)
    { return true;
    }
    
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
  
  @Override
  public synchronized void revert()
  { 
    buffers=null;
    touched=false;
  }
  
  /**
   * Called immediately after successful save
   */
  public synchronized void reset()
  { 
    buffers=null;
    touched=false;
  }
  
  @Override
  public Identifier getId()
  { return id;
  }
  
  @Override
  public void setId(Identifier id)
  { this.id=id;
  }
  
  @Override
  public BufferAggregate<T,Torig> asAggregate()
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
    T val)
  {
    
    if (val==null)
    { throw new IllegalArgumentException
        ("Cannot add a null value to an Aggregate");
    }
    if (buffers==null)
    { buffers=new ArrayList<T>();
    }
    buffers.add(val);
    // XXX Should check type, might have to dirty fields, etc. Might be
    //   a new Tuple which requires data, etc.
    
  }

  @Override
  public void addAll(
    Aggregate<T> values)
  {
    // TODO Auto-generated method stub
    for (T value : values)
    { add(value);
    }
  }

  @Override
  public void addAll(
    Iterator<T> values)
  {
    // TODO Auto-generated method stub
    while (values.hasNext())
    { add(values.next());
    }
  }
  
  @Override
  public T get(int index)
  { 
    if (index < size())
    { 
      try
      { return buffer(index);
      }
      catch (DataException x)
      { 
        throw new RuntimeDataException
          ("Error buffering data of type "+getType().getURI(),x);
      }
    }
    else throw new IndexOutOfBoundsException
        ("Index "+index+" exceeds size "+size());
  }
  
  @SuppressWarnings("unchecked")
  private T buffer(int index)
    throws DataException
  { 
    if (index<size())
    {
      if (buffers==null)
      { buffers=new ArrayList<T>(index+1);
      }
      
      for (int i=buffers.size();i<=index;i++)
      { buffers.add(null);
      }
      T buffer=buffers.get(index);
      if (buffer==null)
      { 
        if (index<original.size())
        { 
          buffer=(T) session.buffer(original.get(index));
          buffers.set(index,buffer);
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
    { return original!=null?original.size():0;
    }
    else
    { return Math.max(original!=null?original.size():0,buffers.size());
    }
  }
  
  @Override
  public boolean isEmpty()
  {
    if (buffers==null)
    { return original!=null?original.isEmpty():true;
    }
    else
    { return original!=null?original.isEmpty():true && buffers.isEmpty();
    }
  }
  
  @Override
  public Aggregate<T> snapshot()
    throws DataException
  { return new ListAggregate<T>(this);
  }

  @Override
  public String toString()
  {
    StringBuilder builder=new StringBuilder();
    builder.append(getClass().getName()+"{");
    boolean first=true;
    if (buffers!=null)
    {
      for (Object o: buffers)
      {
        if (!first)
        { builder.append(",");
        }
        else
        { first=false;
        }
        if (o!=null)
        { builder.append(o.toString());
        }
        else
        { builder.append("null");
        }
      }
    }
    builder.append("}");
    return super.toString()+builder.toString();

  }
  
  @Override
  public Iterator<T> iterator()
  {
    return new Iterator<T>()
    {
      private int i=0;
      
      @Override
      public T next()
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
      
      @Override
      public boolean hasNext()
      { return i<size();
      }
      
      @Override
      public void remove()
      {
      }
    };
    
  }
  
  @Override
  public void setEditable(boolean val)
  { editable=val;
  }
  
  @Override
  public boolean isEditable()
  { return (original instanceof EditableAggregate<?> && editable);
  }
  
  @Override
  public Aggregate<? extends DataComposite> getOriginal()
  { return original;
  }

  @Override
  public void save()
    throws DataException
  {
    if (buffers==null)
    { return;
    }
    if (debug)
    { log.fine("Saving..."+this);
    }
    
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

  @Override
  public void remove(
    T val)
  { buffers.remove(val);
  }
  
  @Override
  public Index<T> getIndex(Projection<T> projection,boolean create)
  {
    return null;
  }

  @Override
  public void touch()
  { touched=true;
  }
  
}


