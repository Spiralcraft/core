package spiralcraft.data.spi;

import spiralcraft.data.DeltaTuple;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.Field;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Identifier;
import spiralcraft.data.Tuple;
import spiralcraft.data.DataException;
import spiralcraft.data.Aggregate;
import spiralcraft.data.Type;
import spiralcraft.data.UpdateConflictException;
import spiralcraft.data.session.Buffer;
import spiralcraft.util.ArrayUtil;

import java.util.BitSet;
import java.util.ArrayList;

public class ArrayDeltaTuple
  extends ArrayTuple
  implements DeltaTuple
{
  private final BitSet dirtyFlags;

  private Tuple original;
  
  private boolean delete;
  private boolean dirty;
  private volatile ArrayJournalTuple nextVersion;

  /**
   * <p>Constructs a DeltaTuple from another DeltaTuple of a different
   *   nature
   * </p>
   * 
   * @param archetype
   * @param updated
   * @throws DataException
   */  
  public static ArrayDeltaTuple copy(Type<?> archetype,DeltaTuple updated)
    throws DataException
  { 
    ArrayDeltaTuple ret
      =new ArrayDeltaTuple(Type.getDeltaType(archetype).getScheme(),updated);
    Identifier id=updated.getId();
    if (id!=null && id.isPublic())
    { ret.setId(id);
    }
    return ret;
  }
  
  public static ArrayDeltaTuple copy(DeltaTuple updated)
      throws DataException
  { return copy(updated.getType().getArchetype(),updated);
  }
  
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
      ?Type.getDeltaType(original.getFieldSet().getType()).getScheme()
      :Type.getDeltaType(updated.getFieldSet().getType()).getScheme()
      );
    this.dirtyFlags=new BitSet(fieldSet.getFieldCount());
    
    initDelta(original,updated);
  }
  
  /**
   * Create an ArrayJournalTuple from this delta.
   */
  @Override
  public synchronized ArrayJournalTuple freeze()
    throws DataException
  { 
    if (this.nextVersion==null)
    { this.nextVersion=ArrayJournalTuple.freezeDelta(this);
    }
    return this.nextVersion;
  }
  
  private void initDelta(Tuple original,Tuple updated)
    throws DataException
  {
  
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

    if (baseExtent!=null)
    { 
      ((ArrayDeltaTuple) baseExtent).initDelta
        (original!=null?original.getBaseExtent():null
        ,updated!=null?updated.getBaseExtent():null
        );
    }
    
  }
  
  /**
   * 
   * <p>Called from createBaseExtent
   * </p>
   */
  public ArrayDeltaTuple(FieldSet fieldSet)
  { 
    super(fieldSet);
    this.dirtyFlags=new BitSet(fieldSet.getFieldCount());
    this.original=null;
  }

  

  
  private ArrayDeltaTuple(FieldSet fieldSet,DeltaTuple source)
      throws DataException
  { this(fieldSet,source,source.getOriginal());
  }
  
  /**
   * <p>Constructs a DeltaTuple from another DeltaTuple
   * </p>
   * 
   * <p>Called from rebase() and (archetype,updated) constructor
   * </p>
   *   
   * @param archetype
   * @param original
   * @param updated
   * @throws DataException
   */  
  private ArrayDeltaTuple(FieldSet fieldSet,DeltaTuple source,Tuple newOriginal)
    throws DataException
  { 
    super(fieldSet,source);
    dirtyFlags=new BitSet(fieldSet.getFieldCount());
    this.original=newOriginal;
    copyFrom(source);
    Type<?> type=fieldSet.getType();
    
    if (type!=null && type.getPrimaryKey()!=null)
    { 
      for (Field<?> sourceField: type.getArchetype().getPrimaryKey().getSourceFields())
      { 
        
        ArrayDeltaTuple extent
          =(ArrayDeltaTuple) this.widen(sourceField.getFieldSet().getType());
        if (extent==null)
        { 
          throw new DataException
            ("Field "+sourceField+" not found in "+this);
        }
        
       
        Object val=sourceField.getValue(source);
//        log.fine("Copying key field "
//          +sourceField+"(#"+sourceField.getIndex()+") from "+updated+" to "+this
//          +" with value ["+val+"]"
//          );
        if (val instanceof Buffer)
        { log.warning("Ignoring "+sourceField.getURI()+" buffer value "+val);
        }
        else
        {  extent.data[sourceField.getIndex()]=val;
        }
        
      }
    }
  }


  
  /**
   * <p>Constructs a DeltaTuple from another DeltaTuple. 
   * </p>
   *
   *   
   * @param original
   * @param updated
   * @throws DataException
   */  
  protected ArrayDeltaTuple(DeltaTuple updated)
    throws DataException
  { 
    super(updated.getFieldSet());
    this.original=updated.getOriginal();
    dirtyFlags=new BitSet(fieldSet.getFieldCount());
    copyFrom(updated);
  }

  @Override
  public ArrayDeltaTuple updateOriginal(Tuple newOriginal)
    throws DataException
  {
    return new ArrayDeltaTuple
      (getFieldSet(),this,newOriginal);
    
    
  }
  
  
  @Override
  public ArrayDeltaTuple rebase(Tuple newOriginal)
    throws DataException
  { 
    if (newOriginal==null)
    { 
      if (original!=null && !delete)
      { throw new UpdateConflictException(this,null);
      }
      else
      { return this;
      }
    }
    ArrayDeltaTuple incoming=new ArrayDeltaTuple(original,newOriginal);
    Field<?>[] intersection
      =ArrayUtil.intersection(getDirtyFields(),incoming.getDirtyFields());
    if (intersection!=null && intersection.length>0)
    { 
      ArrayList<Field<?>> conflicts=new ArrayList<Field<?>>();
      for (int i=0;i<intersection.length;i++)
      { 
        Object o1=intersection[i].getValue(this);
        Object o2=intersection[i].getValue(incoming);
        if (o1!=o2 && (o1==null || !o1.equals(o2)))
        { conflicts.add(intersection[i]);
        }
      }
      
      if (conflicts.size()>0)
      { 
        throw new UpdateConflictException
          (this,incoming,conflicts.toArray(new Field<?>[conflicts.size()])
          );
      }
    }
    return new ArrayDeltaTuple
      (Type.getDeltaType(newOriginal.getType()).getScheme(),this,newOriginal);
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
    this.delete=updated.isDelete();
    if (baseExtent!=null)
    { 
      if ( original!=null
          && ((ArrayDeltaTuple) baseExtent).getOriginal()==null
          )
      { throw new DataException("Missing original in base extent of "+this);
      }
          
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
        log.fine("ArrayDeltaTuple: "+originalValue+" -> "+updatedValue);
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
    Field<?>[] ret=new Field<?>[fields.size()];
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
  public String toString()
  { 
    StringBuffer buf=new StringBuffer();
    buf.append(getClass().getName()
      +"@"+System.identityHashCode(this)+":"+getType().getURI()+"["
      );
    Field<?>[] dirtyFields=getExtentDirtyFields();
      
    if (delete)
    { buf.append("(DELETED)");
    }
    else if (dirtyFields!=null)
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
    { buf.append("(clean)");
    }
    buf.append("] ");
    buf.append(" original="+original);
    
    if (baseExtent!=null)
    { buf.append("\r\n baseExtent="+baseExtent.toString());
    }
    return buf.toString();
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
  protected AbstractTuple createBaseExtent(Tuple originalBaseExtent)
    throws DataException
  { return new ArrayDeltaTuple((DeltaTuple) originalBaseExtent);
  }
  
  @Override
  protected AbstractTuple createBaseExtent(FieldSet fieldSet,Tuple originalBaseExtent)
    throws DataException
  { return new ArrayDeltaTuple(fieldSet,(DeltaTuple) originalBaseExtent);
  }

}
