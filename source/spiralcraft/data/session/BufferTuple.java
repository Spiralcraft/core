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

import java.util.BitSet;

import spiralcraft.data.DataException;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.Field;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Tuple;
import spiralcraft.data.JournalTuple;
import spiralcraft.data.Type;
import spiralcraft.data.Identifier;
import spiralcraft.data.TypeMismatchException;

import spiralcraft.data.access.DataConsumer;
import spiralcraft.data.session.DataSession.DataSessionBranch;
import spiralcraft.data.spi.ArrayTuple;
import spiralcraft.data.transaction.Transaction;
import spiralcraft.log.ClassLogger;

/**
 * An EditableTuple which holds a copy of data from another Tuple, tracks
 *   modifications, and applies those modifications to the Tuple
 *   in an atomic fashion.
 */
public class BufferTuple
  extends Buffer
  implements EditableTuple,DeltaTuple
{
  private static final ClassLogger log=ClassLogger.getInstance(BufferTuple.class);
  
  private DataSession session;
  private Tuple original;
  private Identifier id;
  private Type<?> type;
  private BufferTuple baseExtent;
  
  
  private boolean editable;
  private Object[] data;
  private BitSet dirtyFlags;
  private boolean delete;
  private boolean dirty;
  
  
  
  
  public BufferTuple(DataSession session,Tuple original)
  { 
    this.session=session;
    this.original=original;
    this.type=original.getType();
    this.id=original.getId();
    if (original.getBaseExtent()!=null)
    { baseExtent=new BufferTuple(session,original.getBaseExtent());
    }
  }
  
  public BufferTuple(DataSession session,Type<?> type)
  { 
    this.session=session;
    this.type=type;
    if (type.getBaseType()!=null)
    { baseExtent=new BufferTuple(session,type.getBaseType());
    }
  }

  /**
   * Return the buffer to an unchanged state
   */
  public synchronized void revert()
  {
    reset();
    if (baseExtent!=null)
    { baseExtent.revert();
    }
  }
  
  /**
   * Discard all changes, resetting the BufferTuple to an unchanged state
   *   and dereferencing it from the DataSession.
   */
  public synchronized void discard()
  { 
    reset();
    if (baseExtent!=null)
    { baseExtent.revert();
    }
    session.release(this,id);
  }
  
  private void reset()
  { 
    this.dirty=false;
    this.dirtyFlags=null;
    this.delete=false;
    this.data=null;
  }
  
  /**
   * Indicate that the tuple should be deleted on commit.
   */
  public void delete()
  { 
    this.delete=true;
    this.data=null;
    this.dirty=true;
    this.dirtyFlags=null;
    if (baseExtent!=null)
    { baseExtent.delete();
    }
  }
  
  /**
   * Reload the data from the Tuple into the Buffer and merge it with local
   *   changes. Locally modified data that has not been updated in the Tuple
   *   will survive the merge. 
   */
  void refresh()
  {
    if (original instanceof JournalTuple)
    { 
      JournalTuple jorig=(JournalTuple) original;
      JournalTuple newOrig=jorig.latestVersion();
      
      // XXX Compute delta- where both original and buffer are dirty, we
      // XXX   need to make a decision which to override.
      
      if (newOrig!=jorig)
      { 
        original=jorig;
      }
      if (newOrig.isDeletedVersion())
      { original=null;
      }
    }
    
    if (baseExtent!=null)
    { baseExtent.refresh();
    }
    
  }
  
  /**
   * Indicate whether any data has been locally modified
   */
  @Override
  public boolean isDirty()
  { 
    if (baseExtent!=null)
    { return dirty || baseExtent.isDirty();
    }
    else
    { return dirty;
    }
  }

  /**
   * Copying data from another tuple into this buffer
   */
  @Override
  public void copyFrom(
    Tuple source)
    throws DataException
  {
    if (getFieldSet()==source.getFieldSet()
        || (getType()!=null && getType().hasArchetype(source.getType()))
       )
    { 
      for (Field field: source.getFieldSet().fieldIterable())
      { field.setValue(this,field.getValue(source));
      }
    }
    if (source.getBaseExtent()!=null)
    { baseExtent.copyFrom(source.getBaseExtent());
    }
    
  }

  @Override
  public void set(
    int index,
    Object data)
    throws DataException
  {
    if (this.data==null)
    { 
      this.data=new Object[getFieldSet().getFieldCount()];
      dirtyFlags=new BitSet(this.data.length);
    }
    
    dirtyFlags.set(index);
    this.data[index]=data;
    dirty=true;
  }

  @Override
  public EditableTuple widen(
    Type<?> type)
    throws DataException
  {
    if (getType()!=null)
    {
      if (getType().hasArchetype(type))
      { return this;
      }
      else
      {
        if (baseExtent!=null)
        { return baseExtent.widen(type);
        }
        else
        { 
          throw new TypeMismatchException
            ("Type "+getType()+" has no base type compatible with "
            +" wider type "+type
            );
        }
      }
    }
    else
    { return null;
    }
    
  }

  @Override
  public Object get(
    int index)
    throws DataException
  {
    if (data!=null && dirtyFlags.get(index))
    { return data[index];
    }
    else if (original!=null)
    { return original.get(index);
    }
    else
    { return null;
    }
  }

  /**
   * 
   * @param index
   * @return
   */

/*  
  public Buffer getBuffer(int index)
  { return (Buffer) data[index];
  }
  
  public void setBuffer(int index,Buffer buffer)
  { data[index]=buffer;
  }
*/
  @Override
  public FieldSet getFieldSet()
  { return type.getScheme();
  }

  @Override
  public Type<?> getType()
  { return type;
  }

  @Override
  public boolean isMutable()
  { return true;
  }

  @Override
  public Tuple snapshot()
    throws DataException
  { return new ArrayTuple(this);
  }

  @Override
  public BufferAggregate<?> asAggregate()
  { return null;
  }

  @Override
  public BufferTuple asTuple()
  { return this;
  }

  @Override
  public boolean isAggregate()
  { return false;
  }

  @Override
  public boolean isTuple()
  { return true;
  }

  @Override
  public String toText(
    String indent)
    throws DataException
  { return "Buffer of ["+original.toText(indent+" ")+"\r\n]";
  }

  public String toString()
  { 
    StringBuffer buf=new StringBuffer();
    buf.append(super.toString()+":[\r\n");
    Field[] dirtyFields=getDirtyFields();
      
    if (dirtyFields!=null)
    {
      for (Field field : dirtyFields)
      { 
        try
        { buf.append(field.getName()+"="+field.getValue(this)+"\r\n");
        }
        catch (DataException x)
        { buf.append(field.getName()+"=!!!"+x);
        }
      }
    
      buf.append("]\r\n");
    }
    return buf.toString()+":"+original;
  }
  
  @Override
  public Field[] getDirtyFields()
  {
    if (!dirty)
    { return null;
    }
    else
    {
      FieldSet fieldSet=getFieldSet();
      int j=0;
      Field[] fields=new Field[dirtyFlags.cardinality()];
      for(int i=dirtyFlags.nextSetBit(0); i>=0; i=dirtyFlags.nextSetBit(i+1)) 
      { fields[j++]=fieldSet.getFieldByIndex(i);
      }
      return fields;
    }
  }

  @Override
  public Tuple getOriginal()
  { return original;
  }

  @Override
  public boolean isDelete()
  { return delete;
  }

  @Override
  public boolean isDirty(
    int index)
  { return dirty;
  }

  @Override
  public void setId(
    Identifier id)
    throws DataException
  {
    if (original!=null)
    { throw new DataException("Cannot set Identifier for non-new Buffer");
    }
      
    this.id=id;
    if (baseExtent!=null)
    { baseExtent.setId(id);
    }
  }

 
  @Override
  public Identifier getId()
  { return id;
  }

  @Override
  public Tuple getBaseExtent()
  { return baseExtent;
  }
  
  public void setEditable(boolean val)
  { editable=val;
  }
  
  public boolean isEditable()
  { 
    return ((original instanceof EditableTuple) 
            || (original instanceof JournalTuple)
            )
           && editable;
           
  }
  
  
  
  public void save()
    throws DataException
  {
    if (!isDirty())
    { return;
    }
    
    Transaction transaction
      =Transaction.getContextTransaction();
    
    if (transaction!=null)
    {
      log.fine("Saving "+toString());
      
      if (baseExtent!=null)
      { baseExtent.save();
      }
      
      DataSessionBranch branch
        =session.getResourceManager().branch(transaction);
      branch.addBuffer(this);
      
      
      DataConsumer<DeltaTuple> updater=branch.getUpdater(getType());
      if (updater!=null)
      { updater.dataAvailable(this);
      }
      else
      { log.fine("No updater in Space for Type "+getType());
      }
    }
    else
    { 
      log.fine("Saving "+toString());
      transaction=
        Transaction.startContextTransaction(Transaction.Nesting.ISOLATE);
      try
      {
        if (baseExtent!=null)
        { baseExtent.save();
        }
        
        DataSessionBranch branch
          =session.getResourceManager().branch(transaction);
        branch.addBuffer(this);
        
        DataConsumer<DeltaTuple> updater=branch.getUpdater(getType());
        if (updater!=null)
        { updater.dataAvailable(this);
        }
        else
        { log.fine("No updater in Space for Type "+getType());
        }
        
        
        transaction.commit();
      }
      finally
      {
        transaction.complete();
      }
    }

  }

  
  void prepare()
    throws DataException
  {
    if (original instanceof JournalTuple)
    { original=((JournalTuple) original).prepareUpdate(this);
    }
    
  }
  
  void rollback()
  {
    if (original instanceof JournalTuple)
    { ((JournalTuple) original).rollback();
    }
  }
  
  void commit()
    throws DataException
  { 
    log.fine("Committing "+this);
    if (original instanceof JournalTuple)
    { 
      // XXX Should be prepareUpdate(), which locks to transaction
      ((JournalTuple) original).commit();
    }
    else if (original instanceof EditableTuple)
    {
      EditableTuple tuple=(EditableTuple) original;
      Field[] dirtyFields=getDirtyFields();
      if (dirtyFields!=null)
      {
        for (Field field: getDirtyFields())
        { 
          if (field instanceof BufferField)
          { 
            ((BufferField) field).getArchetypeField()
              .setValue(tuple, ((Buffer) field.getValue(this)).getOriginal());
          }
          else
          { field.setValue(tuple, field.getValue(this));
          }
        }
      }
    }
    else
    {
      log.fine("Original is not writable "+original);
    }
    reset();
      
  }
  
  
}
