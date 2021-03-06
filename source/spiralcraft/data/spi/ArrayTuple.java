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
package spiralcraft.data.spi;

import spiralcraft.data.Aggregate;
import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.Tuple;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Field;

import spiralcraft.util.ArrayUtil;


/**
 * Base class for a simple in-memory immutable Tuple.
 */
public class ArrayTuple
  extends AbstractTuple
  implements Tuple
{
  protected final Object[] data;
  
  public static ArrayTuple freezeDelta(DeltaTuple delta)
      throws DataException
  { return new ArrayTuple(delta);
  }

  /**
   * Construct an ArrayTuple with an empty set of data
   */
  public ArrayTuple(FieldSet fieldSet)
  { 
    super(fieldSet);
    this.data=new Object[fieldSet.getFieldCount()];
    
    if (fieldSet.getType()!=null)
    {
      if (fieldSet.getType().getBaseType()!=null)
      { 
        FieldSet baseScheme=fieldSet.getType().getBaseType().getScheme();
        if (baseScheme!=null)
        { baseExtent=createBaseExtent(baseScheme);
        }
      }
    }
  }
  
  
  /**
   * <p>Construct an ArrayTuple that is a copy of another Tuple
   * </p>
   * 
   * <p>If DataComposites are directly referenced and this Tuple is immutable,
   *   immutable copies will be made if they are not already immutable.
   * </p>
   * 
   * <p>In all other cases, a shallow copy will be made of the raw field
   *   data.
   * </p>
   *   
   */
  public ArrayTuple(Tuple original)
    throws DataException
  { 
    super(original.getFieldSet());
    this.data=copyData(original);
    if (original.getBaseExtent()!=null)
    { baseExtent=createBaseExtent(original.getBaseExtent());
    }
  }
  

  /**
   * <p>Construct an ArrayTuple to receive data after the extent structure
   *   is created
   * </p>
   */
  protected ArrayTuple(FieldSet fieldSet,Tuple original)
    throws DataException
  { 
    super(fieldSet);
    this.data=new Object[fieldSet.getFieldCount()];
    
    if (fieldSet.getType()!=null)
    {
      if (fieldSet.getType().getBaseType()!=null)
      { 
        FieldSet baseScheme=fieldSet.getType().getBaseType().getScheme();
        if (baseScheme!=null)
        { baseExtent=createBaseExtent(baseScheme,original.getBaseExtent());
        }
      }
    }
  }

  /**
   * Construct an ArrayTuple that contains a copy of the
   *   data in the specified DeltaTuple
   */
  protected ArrayTuple
    (DeltaTuple delta)
    throws DataException
  { 
    super(delta.getType().getArchetype().getScheme());
    this.data=copyData(delta);
    if (delta.getBaseExtent()!=null)
    { baseExtent=createDeltaBaseExtent(delta.getBaseExtent());
    }
  }  
  
  private Object[] copyData(Tuple original)
    throws DataException
  {
    Object[] data=new Object[fieldSet.getFieldCount()];
  
    for (Field<?> field : fieldSet.fieldIterable())
    { 
      Object originalValue=original.get(field.getIndex());
      if (originalValue instanceof DataComposite)
      {
        DataComposite originalComposite=(DataComposite) originalValue;
        if (originalComposite.isTuple())
        { 
          Tuple originalTuple=originalComposite.asTuple();
          if (originalTuple.isMutable() && !isMutable())
          { data[field.getIndex()]=copyTupleField(originalTuple);
          }
          else
          { data[field.getIndex()]=originalValue;
          }
        }
        else if (originalComposite.isAggregate())
        {
          Aggregate<?> originalAggregate=originalComposite.asAggregate();
          if (originalAggregate.isMutable() &&!isMutable())
          { data[field.getIndex()]=originalAggregate.snapshot();
          }
          else
          { data[field.getIndex()]=originalValue;
          }
  
        }
      }
      else
      { data[field.getIndex()]=originalValue;
      }
    }
    return data;
    
  }
  


  @Override
  public Object get(int index)
    throws DataException
  { return data[index];
  }
  
  @Override
  public int hashCode()
  { 
    if (baseExtent!=null)
    { return ArrayUtil.arrayHashCode(data)*37 + baseExtent.hashCode();
    }
    else
    { return ArrayUtil.arrayHashCode(data);
    }
  }

  @Override
  public boolean isVolatile()
  { return false;
  }

  @Override
  protected AbstractTuple createBaseExtent(
    FieldSet fieldSet)
  { return new ArrayTuple(fieldSet);
  }

  @Override
  protected AbstractTuple createBaseExtent(
    Tuple original)
    throws DataException
  { return new ArrayTuple(original);
  }
  
  protected AbstractTuple createBaseExtent(
    FieldSet fieldSet,Tuple original)
    throws DataException
  { return new ArrayTuple(fieldSet,original);
  }
  
  @Override
  protected AbstractTuple createDeltaBaseExtent(
    DeltaTuple tuple)
    throws DataException
  { return ArrayTuple.freezeDelta(tuple);
  }  
  
  @Override
  protected AbstractTuple copyTupleField(Tuple fieldValue)
    throws DataException
  { return new ArrayTuple(fieldValue);
  }
}