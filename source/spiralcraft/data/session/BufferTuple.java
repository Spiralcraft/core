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

import java.lang.ref.WeakReference;
import java.util.BitSet;

import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.Field;
import spiralcraft.data.FieldNotFoundException;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Key;
import spiralcraft.data.ProjectionField;
import spiralcraft.data.Tuple;
import spiralcraft.data.JournalTuple;
import spiralcraft.data.Type;
import spiralcraft.data.Identifier;
import spiralcraft.data.TypeMismatchException;
import spiralcraft.data.UpdateConflictException;

import spiralcraft.data.spi.ArrayTuple;
import spiralcraft.data.spi.KeyIdentifier;
import spiralcraft.data.util.StaticInstanceResolver;
import spiralcraft.log.ClassLog;
import spiralcraft.util.ArrayUtil;

/**
 * An EditableTuple which holds a copy of data from another Tuple, tracks
 *   modifications, and applies those modifications to the Tuple
 *   in an atomic fashion.
 */
public class BufferTuple
  extends Buffer
  implements EditableTuple,DeltaTuple
{
  private static final ClassLog log=ClassLog.getInstance(BufferTuple.class);
  
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
  
  private Tuple saveResult;
  
  private WeakReference<Object> behaviorRef;
  
  /**
   * Create a new buffer for editing the latest version of the specified
   *   original
   * @param session
   * @param original
   */
  public BufferTuple(DataSession session,Tuple original)
  { 
    this.session=session;
    this.type=Type.getBufferType(original.getType());
    
    if (original instanceof JournalTuple)
    { 
      JournalTuple jt=((JournalTuple) original).latestVersion();
      if (!jt.isDeletedVersion())
      { this.original=jt;
      }
    }
    else
    { this.original=original;
    }
    
    if (this.original!=null)
    {
      this.id=this.original.getId();
      if (this.original.getBaseExtent()!=null)
      { baseExtent=new BufferTuple(session,this.original.getBaseExtent());
      }
    }
    else if (type.getBaseType()!=null)
    { baseExtent=new BufferTuple(session,type.getBaseType());
    }
  }
  
  public BufferTuple(DataSession session,Type<?> type)
  { 
    this.session=session;
    if (! (type instanceof BufferType))
    { 
      log.debug("Buffer tuple created with non-buffer type "+type.getURI());
      throw new IllegalArgumentException
        ("BufferTuple type must be a BufferType");
    }
    this.type=type;
    if (type.getBaseType()!=null)
    { baseExtent=new BufferTuple(session,type.getBaseType());
    }
  }

  @Override
  public Object get(String fieldName)
    throws DataException
  { 
    Field<?> field=type.getField(fieldName);
    if (field==null)
    { throw new FieldNotFoundException(type,fieldName);
    }
    return field.getValue(this);
  }
  
  @Override
  public void set(String fieldName,Object data)
    throws DataException
  {
    Field<Object> field=type.getField(fieldName);
    if (field==null)
    { throw new FieldNotFoundException(type,fieldName);
    }
    field.setValue(this,data);
  }  
  
  
  /** 
   * Update original to a new copy. Any conflicting modifications will
   *   be overwritten. 
   */
  @Override
  public DeltaTuple rebase(Tuple newOriginal)
    throws UpdateConflictException
  {
    // TODO Check for conflict by finding delta between old original and
    //   new original, and looking for dirty fields overlap
    setOriginal(newOriginal);
    return this;
  }
  
  /**
   * Queue the result of the transaction to be the new original
   * 
   * @param tuple
   */
  @Override
  public DeltaTuple updateOriginal(Tuple tuple)
  { 
    if (debug)
    { log.fine("Uncommitted new original "+tuple);
    }
    this.saveResult=tuple;
    return this;
  }
  
  /**
   * Return the buffer to an unchanged state
   */
  @Override
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
  @Override
  public void delete()
  { 
    this.delete=true;
//    this.data=null;
    this.dirty=true;
//    this.dirtyFlags=null;
    if (baseExtent!=null)
    { baseExtent.delete();
    }
  }
  
  /**
   * Reset the deleted flag
   */
  public void undelete()
  { 
    this.delete=false;
    if (baseExtent!=null)
    { baseExtent.undelete();
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
      for (Field<?> field: source.getFieldSet().fieldIterable())
      { copyFieldFrom(field,source);
      }
    }
    if (source.getBaseExtent()!=null)
    { baseExtent.copyFrom(source.getBaseExtent());
    }
    
  }
  
  private <X> void copyFieldFrom(Field<X> field,Tuple source)
    throws DataException
  { 
    X value=field.getValue(source);
    field.setValue(this,value);
  }
  
  /**
   * <p>Copy data from a source tuple of the same type where the source
   *   field value is non-null and is different than the current value
   * </p>
   * 
   * @param source
   * @throws DataException
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void updateFrom(Tuple source)
    throws DataException
  {
    if (getFieldSet()==source.getFieldSet()
        || (getType()!=null && getType().hasArchetype(source.getType()))
       )
    {
      for (Field field:this.type.getArchetype().getScheme().fieldIterable())
      { 
        if (!field.isTransient())
        {
          Object sourceVal=field.getValue(source);
          if (sourceVal!=null)
          {
            Object currentVal=field.getValue(this);
            if (!sourceVal.equals(currentVal))
            { field.setValue(this,sourceVal);
            }
          }
        }
      }
    }
    else
    {
      if (debug)
      { log.fine("Can't update "+this+" from "+source);
      }
    }
    if (baseExtent!=null)
    { baseExtent.updateFrom(source.getBaseExtent());
    }
  }
  
  @Override
  public void updateTo(
    EditableTuple dest)
    throws DataException
  {
    if (debug)
    { log.fine("Starting update to "+dest);
    }
    
    if (getFieldSet()==dest.getFieldSet()
        || (getType()!=null && getType().hasArchetype(dest.getType()))
       )
    { 
      Field<?>[] dirtyFields=getExtentDirtyFields();
      if (dirtyFields!=null && dirtyFields.length>0)
      {
      
        for (Field<?> field : dirtyFields)
        { 
          Object value=field.getValue(this);
          if (value instanceof Buffer)
          { 
            // Don't do anything
            // XXX Need option to cascade. Buffer.getParent()==this?
            if (debug)
            {
              log.fine("Ignoring dirty buffer field "+field.getURI());
            }
          }
          else
          { 
            if (debug)
            { 
              log.fine
                ("Copying "+field.getURI()+" to "+dest.getType().getURI()+": "
                +field.getValue(this)
                );
            }
            copyFieldTo(field,dest);
          }
        }
      }
      else
      {
        if (debug)
        { log.fine("No dirty fields in this extent "+getType().getURI());
        }
      }
    }
    else
    { 
      if (debug)
      { log.fine("Can't update "+this+" to "+dest);
      }
    }
    if (baseExtent!=null)
    { baseExtent.updateTo((EditableTuple) dest.getBaseExtent());
    }
    
  }

  private <X> void copyFieldTo
    (Field<X> field,EditableTuple dest)
    throws DataException
  { 
    dest.getFieldSet().<X>getFieldByIndex(field.getIndex())
      .setValue(dest,field.getValue(this));
  }
    
  @Override
  public void set(
    int index,
    Object data)
    throws DataException
  {
     
    if (data!=null)
    {
      Field<?> field=getFieldSet().getFieldByIndex(index);
      Type<?> fieldType=field.getType();

      if ( (fieldType.isPrimitive()
            && (!(fieldType.getNativeClass().isAssignableFrom(data.getClass())))
           )
           || 
           ( !fieldType.isPrimitive()
           && !(data instanceof Buffer)
           )
        )
      {
        throw new DataException
          ("Cannot update field "+field.getURI()
          +"("+field.getType().getNativeClass()
          +") with data "+data
          );
      }
    }
    
    
    
    
    if (this.data==null)
    { 
      this.data=new Object[getFieldSet().getFieldCount()];
      dirtyFlags=new BitSet(this.data.length);
    }
    
    dirtyFlags.set(index);
    this.data[index]=data;
    dirty=true;
    // log.fine("Set "+index+"="+data+" in "+this);
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
            ("Type "+getType().getURI()+" has no base type compatible with "
            +" wider type "+type.getURI()
            );
        }
      }
    }
    else
    { return null;
    }
    
  }

  /**
   * Return the local object for the field, if any, without delegating to the
   *   original that this tuple buffers.
   * 
   * @param index
   * @return
   */
  Object getBuffer(int index)
  { return data!=null?data[index]:null;
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
    { 
      try
      { return original.get(index);
      }
      catch (ArrayIndexOutOfBoundsException x)
      { throw new DataException(this.toString(),x);
      }
    }
    else
    { return null;
    }
  }

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
  public boolean isVolatile()
  { return false;
  }
  
  @Override
  public Tuple snapshot()
    throws DataException
  { return ArrayTuple.freezeDelta(this);
  }

  @Override
  public BufferAggregate<?,?> asAggregate()
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
  { 
    if (original!=null)
    { 
      return "\r\n"+indent+"Buffer "+toDirtyText(indent+"  ")
        +"\r\n"+indent+"- of original "+original.toText(indent+"  ");
        
    }
    else
    { 
      return 
        "\r\n"+indent+"New Buffer "
        +toDirtyText(indent+"  ");
    }
  }
  
  public String toDirtyText(String indent)
    throws DataException
  { 
    StringBuilder sb=new StringBuilder();
    sb.append("\r\n").append(indent);
    sb.append(super.toString());
    sb.append("\r\n").append(indent).append("==");
    if (getType()!=null)
    { sb.append(getType().getURI());
    }
    else
    { sb.append("(untyped)");
    }
    sb.append("\r\n").append(indent);
    sb.append("[");
    boolean first=true;
    String indent2=indent.concat("  ");
    Field<?>[] dirtyFields=getDirtyFields();

    if (dirtyFields!=null)
    {
      for (Field<?> field : dirtyFields)
      { 
      
        Object val=field.getValue(this);
        if (val!=null)
        { 
          sb.append("\r\n").append(indent2);
          if (!first)
          { sb.append(",");
          }
          else
          { first=false;
          }
        
          sb.append(field.getName()).append("=");
        
          if (val instanceof DataComposite)
          { 
            sb.append("\r\n").append(indent2)
              .append("[")
              .append(((DataComposite) val).toText(indent2+"  "));
            sb.append("\r\n").append(indent2)
              .append("]");
          }
          else
          { 
            sb.append("[");
            sb.append(val.toString());
            sb.append("]");
          }
          
          
        }
      }
    }
    sb.append("\r\n").append(indent); 
    sb.append("]");
    return sb.toString();
  }  

  @Override
  public String dumpData()
    throws DataException
  { return toDirtyText("| ");
  }
  
  @Override
  public String toString()
  { 
    StringBuffer buf=new StringBuffer();
    buf.append(super.toString()+":"+getType().getURI()+"[");
    Field<?>[] dirtyFields=getExtentDirtyFields();
      
    if (dirtyFields!=null)
    {
      boolean first=true;
      for (Field<?> field : dirtyFields)
      { 
        if (first)
        { first=false;
        }
        else
        { buf.append(",");
        }
        try
        { buf.append(field.getName()+"=["+field.getValue(this)+"]");
        }
        catch (DataException x)
        { buf.append(field.getName()+"=!!!"+x);
        }
      }
    
    }
    else
    { buf.append(delete?"(DELETED)":"(clean)");
    }
    buf.append("] ");
    buf.append(" original="+original);
    
    if (baseExtent!=null)
    { buf.append("\r\n baseExtent="+baseExtent.toString());
    }
    return buf.toString();
  }
  
  @Override
  public Field<?>[] getExtentDirtyFields()
  {
    if (!dirty)
    { return null;
    }
    else if (dirtyFlags==null)
    { return null;
    }
    else
    {
      
      FieldSet fieldSet=getFieldSet();
      int j=0;
      Field<?>[] fields=new Field<?>[dirtyFlags.cardinality()];
      for(int i=dirtyFlags.nextSetBit(0); i>=0; i=dirtyFlags.nextSetBit(i+1)) 
      { fields[j++]=fieldSet.getFieldByIndex(i);
      }
      return fields;
    }
  }
  
  @Override
  public Field<?>[] getDirtyFields()
  {
    Field<?>[] baseDirty=(baseExtent!=null?baseExtent.getDirtyFields():null);
    Field<?>[] dirty=getExtentDirtyFields();
    
    if (baseDirty==null)
    { return dirty;
    }
    else if (dirty==null)
    { return baseDirty;
    }
    else
    { return  ArrayUtil.concat(baseDirty,dirty);
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

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public void setId(
    Identifier id)
    throws DataException
  {
    if (original!=null)
    { throw new DataException("Cannot set Identifier for non-new Buffer");
    }
      
    this.id=id;
    Key<?> primaryKey=getType().getScheme().getPrimaryKey();
    if (primaryKey!=null && id instanceof KeyIdentifier)
    { 
      KeyIdentifier<?> keyId=(KeyIdentifier<?>) id;
      int i=0;
      for (ProjectionField field: primaryKey.fieldIterable())
      { 
        field.getSourceField().setValue(this,keyId.get(i++));
      }
    }

    if (baseExtent!=null)
    { baseExtent.setId(id);
    }
  }

 
  @Override
  public Identifier getId()
  { return id;
  }

  @Override
  public DeltaTuple getBaseExtent()
  { return baseExtent;
  }
  
  @Override
  public void setEditable(boolean val)
  { editable=val;
  }
  
  @Override
  public boolean isEditable()
  { 
    return ((original instanceof EditableTuple) 
            || (original instanceof JournalTuple)
            )
           && editable;
           
  }
  
  
  
  @Override
  /**
   * <p>Write the buffer to the store
   * </p>
   * 
   * <p>Starts a transaction if necessary
   * </p>
   * 
   * <p>A buffer will send itself to the updater of the appropriate
   *   type obtained from the space
   * </p>
   */
  public void save()
    throws DataException
  { session.writeTuple(this);
  }

  @Override
  void prepare()
    throws DataException
  {
//    if (original instanceof JournalTuple)
//    { original=((JournalTuple) original).prepareUpdate(this);
//    }
    
  }
  
  @Override
  void rollback()
  {
//    if (original instanceof JournalTuple)
//    { ((JournalTuple) original).rollback();
//    }
  }
  
  @Override
  void commit()
    throws DataException
  { 
    if (debug)
    { log.fine("Committing "+this);
    }
    if (original!=null)
    {
      if (original instanceof JournalTuple)
      { 
//        ((JournalTuple) original).commit();
//        reset();
      }
      else if (original instanceof EditableTuple)
      { // writeThrough();
      }
      else if (original!=null)
      { log.fine("Original is not writable "+original);
      }
    }
    
    if (saveResult!=null)
    { 
      if (debug)
      { log.fine("Got new original "+saveResult.dumpData());
      }
      setOriginal(saveResult);
      reset();
      
    }
    saveResult=null;
      
  }

  void setOriginal(Tuple original)
  { 
    this.original=original;
    if (baseExtent!=null)
    { 
      baseExtent.setOriginal
        (original!=null
          ?original.getBaseExtent()
          :null
        );
    }
  }
  
  @Override
  public synchronized Object getBehavior()
    throws DataException
  {
    if (getType()==null)
    { return null;
    }
    
    if (getType().getNativeClass()==null)
    { return null;
    }
    
    Object behavior=null;
    if (behaviorRef!=null)
    { behavior=behaviorRef.get();
    }
    
    StaticInstanceResolver instanceResolver=null;
    if (behavior!=null)
    { instanceResolver=new StaticInstanceResolver(behavior);
    }
    Object newBehavior=getType().fromData(this,instanceResolver);
    
    if (newBehavior!=behavior)
    { behaviorRef=new WeakReference<Object>(newBehavior);
    }
    
    return newBehavior;
    
  }

  @Override
  public void touch()
  { dirty=true;
  }  

}
