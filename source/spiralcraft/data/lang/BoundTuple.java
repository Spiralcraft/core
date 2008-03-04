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
package spiralcraft.data.lang;

import spiralcraft.data.DataException;
import spiralcraft.data.EditableTuple;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Tuple;
import spiralcraft.data.Type;
import spiralcraft.data.spi.AbstractTuple;
import spiralcraft.data.spi.ArrayTuple;

import spiralcraft.lang.Channel;

import spiralcraft.util.ArrayUtil;

/**
 * A Tuple where each Field references a language binding to an arbitrary
 *   data source. The Field values may change if the source changes. 
 */
@SuppressWarnings("unchecked")
public class BoundTuple
  extends AbstractTuple
  implements EditableTuple
{
  protected final Channel[] bindings;
  
  /**
   * Construct an ArrayTuple with an empty set of data
   */
  public BoundTuple(FieldSet fieldSet,Channel<?>[] bindings)
  { 
    super(fieldSet);
    if (bindings.length!=fieldSet.getFieldCount())
    {
      throw new IllegalArgumentException
        ("Binding count does not match Field count");
    }
    this.bindings=bindings;
  }
  
  @Override
  protected AbstractTuple createBaseExtent(
    FieldSet fieldSet)
  { throw new RuntimeException("Bound Tuple cannot have a base extent");
  }

  @Override
  protected AbstractTuple createBaseExtent(
    Tuple tuple)
    throws DataException
  { throw new RuntimeException("Bound Tuple cannot have a base extent");
  }

  
  public Object get(int index)
  { return bindings[index].get();
  }

  @Override
  public void set(
    int index,
    Object data)
    throws DataException
  { bindings[index].set(data);
    // TODO Auto-generated method stub
    
  }

  public boolean isMutable()
  { return true;
  }
  
  public int hashCode()
  { 
    Object[] data=new Object[bindings.length];
    int i=0;
    for (Channel<?> opt: bindings)
    { data[i++]=opt.get();
    }
    return ArrayUtil.arrayHashCode(data);
  }

  @Override
  public void copyFrom(
    Tuple source)
    throws DataException
  {
    // TODO Auto-generated method stub
    
  }

  
  public EditableTuple widen(Type<?> type)
    throws DataException
  { return (EditableTuple) super.widen(type);
  }
   
}