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

import spiralcraft.data.FieldSet;
import spiralcraft.data.Tuple;
import spiralcraft.data.spi.AbstractTuple;

import spiralcraft.lang.Optic;

import spiralcraft.util.ArrayUtil;

/**
 * A Tuple where each Field references a language binding to an arbitrary
 *   data source. The Field values may change if the source changes. 
 */
public class BoundTuple
  extends AbstractTuple
  implements Tuple
{
  protected final Optic[] bindings;
  
  /**
   * Construct an ArrayTuple with an empty set of data
   */
  public BoundTuple(FieldSet fieldSet,Optic[] bindings)
  { 
    super(fieldSet);
    if (bindings.length!=fieldSet.getFieldCount())
    {
      throw new IllegalArgumentException
        ("Binding count does not match Field count");
    }
    this.bindings=bindings;
  }
  
  public Object get(int index)
  { return bindings[index].get();
  }
  
  public boolean isMutable()
  { return true;
  }
  
  public int hashCode()
  { 
    Object[] data=new Object[bindings.length];
    int i=0;
    for (Optic opt: bindings)
    { data[i++]=opt.get();
    }
    return ArrayUtil.arrayHashCode(data);
  }
   
}