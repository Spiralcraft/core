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

import spiralcraft.data.DataException;
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
   * Construct an ArrayTuple that is a copy of another Tuple
   */
  public ArrayTuple(Tuple original)
    throws DataException
  { 
    super(original.getFieldSet());
    this.data=new Object[fieldSet.getFieldCount()];
    
    for (Field<?> field : fieldSet.fieldIterable())
    { 
      Object originalValue=original.get(field.getIndex());
      if (originalValue instanceof Tuple)
      { 
        Tuple originalTuple=(Tuple) originalValue;
        if (originalTuple.isMutable())
        { data[field.getIndex()]=new ArrayTuple(originalTuple);
        }
        else
        { data[field.getIndex()]=originalValue;
        }
      }
      else
      { data[field.getIndex()]=originalValue;
      }
    }
    if (original.getBaseExtent()!=null)
    { baseExtent=createBaseExtent(original.getBaseExtent());
    }
  }

  
  public Object get(int index)
    throws DataException
  { return data[index];
  }
  
  @Override
  public int hashCode()
  { return ArrayUtil.arrayHashCode(data);
  }

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
    Tuple tuple)
    throws DataException
  { return new ArrayTuple(tuple);
  }
  
}