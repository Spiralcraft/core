package spiralcraft.data.spi;

import spiralcraft.data.DeltaTuple;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.Field;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Tuple;
import spiralcraft.data.DataException;
import spiralcraft.data.Aggregate;
import spiralcraft.data.session.Buffer;
import spiralcraft.util.ArrayUtil;

import java.util.BitSet;
import java.util.ArrayList;

public class ArrayDeltaTuple
  extends ArrayTuple
  implements DeltaTuple
{
  private final BitSet dirtyFlags;

  private final Tuple original;
  
  private boolean delete;
  private boolean dirty;

  
  /**
   * Constructs a DeltaTuple based on the difference between and
   *   original Tuple and and updated Tuple
   *   
   * @param original
   * @param updated
   * @throws DataException
   */
  public ArrayDeltaTuple(Tuple original,Tuple updated)
    throws DataException
  { 
    super
      (original!=null
      ?original.getFieldSet()
      :updated.getFieldSet()
      );
    
    dirtyFlags=new BitSet(fieldSet.getFieldCount());
    this.original=original;
    
    if (updated==null)
    { 
      delete=true;
      dirty=true;
    }
    else if (original==null)
    {
      for (Field<?> field: fieldSet.fieldIterable())
      { 
        if (updated.get(field.getIndex())!=null)
        { setDirtyValue(field,makeDirtyValue(null,updated.get(field.getIndex())));
        }
      }
    }
    else
    {
      for (Field<?> field: fieldSet.fieldIterable())
      { 
        Object originalValue=original.get(field.getIndex());
        Object updatedValue=updated.get(field.getIndex());

        if (originalValue!=updatedValue)
        { 
          if (originalValue!=null)
          {  
            if (!originalValue.equals(updatedValue))
            { setDirtyValue(field,makeDirtyValue(originalValue,updatedValue));
            }
          }
          else
          { setDirtyValue(field,makeDirtyValue(null,updatedValue));
          }
        }
      }
    }
  }

  public ArrayDeltaTuple(FieldSet fieldSet)
  { 
    super(fieldSet);
    this.dirtyFlags=new BitSet(fieldSet.getFieldCount());
    this.original=null;
  }

  
  /**
   * Constructs a DeltaTuple from another DeltaTuple
   *   
   * @param original
   * @param updated
   * @throws DataException
   */  
  public ArrayDeltaTuple(DeltaTuple updated)
    throws DataException
  { 
    super(updated.getFieldSet());
    dirtyFlags=new BitSet(fieldSet.getFieldCount());
    copyFrom(updated);
    this.original=updated.getOriginal();
  }

  void copyFrom(DeltaTuple updated)
    throws DataException
  { 
    if (updated==null)
    { throw new IllegalArgumentException("Can't copy from null");
    }
    Field<?>[] dirtyFields=updated.getExtentDirtyFields();
    if (dirtyFields!=null)
    {
      for (Field<?> field : dirtyFields)
      { 
        int i=field.getIndex();
        data[i]=updated.get(i);
        dirtyFlags.set(i);
      }
    }
    this.dirty=updated.isDirty();
    if (baseExtent!=null)
    { 
      ((ArrayDeltaTuple) baseExtent)
        .copyFrom(updated.getBaseExtent());
    }
  }
  
  
  @Override
  public void updateTo(
    EditableTuple dest)
    throws DataException
  {
    if (getFieldSet()==dest.getFieldSet()
        || (getType()!=null && getType().hasArchetype(dest.getType()))
       )
    { 
      Field<?>[] dirtyFields=getExtentDirtyFields();
      if (dirtyFields!=null)
      {
      
        for (Field<?> field : dirtyFields)
        { 
          Object value=field.getValue(this);
          if (value instanceof Buffer)
          { 
            // XXX Follow content update-to
            
          }
          else
          { copyFieldTo(field,dest);
          }
        }
      }
    }
    if (baseExtent!=null)
    { ((ArrayDeltaTuple) baseExtent).updateTo((EditableTuple) dest.getBaseExtent());
    }
    
  }  
  protected Object makeDirtyValue(Object originalValue,Object updatedValue)
    throws DataException
  {
    if (updatedValue!=null)
    {
      if (updatedValue instanceof Tuple)
      { 
        System.err.println("ArrayDeltaTuple: "+originalValue+" -> "+updatedValue);
        return new ArrayDeltaTuple((Tuple) originalValue, (Tuple) updatedValue);
      }
      else if (updatedValue instanceof Aggregate<?>)
      { return updatedValue;
      }
      else
      { return updatedValue;
      }
    }
    else
    { 
      if (originalValue instanceof Tuple)
      { return new ArrayDeltaTuple((Tuple) originalValue,null);
      }
      else
      { return null;
      }
    }
  }
  
  protected void setDirtyValue(Field<?> field,Object value)
  { 
    data[field.getIndex()]=value;
//    System.err.println("ArrayDeltaTuple: "+field.getName()+"="+value);
    dirty=true;
    dirtyFlags.set(field.getIndex(),true);
  }

  @Override
  public Field<?>[] getExtentDirtyFields()
  {
    ArrayList<Field<?>> fields=new ArrayList<Field<?>>();
    for (int i=0;i<dirtyFlags.size();i++)
    { 
      if (dirtyFlags.get(i))
      { fields.add(fieldSet.getFieldByIndex(i));
      }
    }
    Field<?>[] ret=new Field[fields.size()];
    fields.toArray(ret);
    return ret;
  }

  @Override
  public Field<?>[] getDirtyFields()
  {
    Field<?>[] baseDirty
      =(baseExtent!=null?((ArrayDeltaTuple) baseExtent).getDirtyFields():null);
    Field<?>[] dirty=getExtentDirtyFields();
    
    if (baseDirty!=null && dirty==null)
    { return baseDirty;
    }
    else if (baseDirty==null && dirty!=null)
    { return dirty;
    }
    else
    { return ArrayUtil.concat(baseDirty,dirty);
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
  public boolean isDirty(int index)
  { return dirtyFlags.get(index);
  }

  @Override
  public boolean isDirty()
  { 
    if (baseExtent!=null)
    { return dirty || ((ArrayDeltaTuple) baseExtent).isDirty();
    }
    else
    { return dirty;
    }
  }
  
  @Override
  public Object get(int index)
    throws DataException
  { 
    if (dirtyFlags.get(index))
    { return super.get(index);
    }
    else
    {
      if (original!=null)
      { return original.get(index);
      }
      else
      { return null;
      }
    }
  }

  @Override
  public DeltaTuple getBaseExtent()
  { return (DeltaTuple) super.getBaseExtent();
  }

  @Override
  protected AbstractTuple createBaseExtent(
    FieldSet fieldSet)
  { return new ArrayDeltaTuple(fieldSet);
  }

  @Override
  protected AbstractTuple createBaseExtent(
    Tuple tuple)
    throws DataException
  { return new ArrayDeltaTuple((DeltaTuple) tuple);
  }
}
