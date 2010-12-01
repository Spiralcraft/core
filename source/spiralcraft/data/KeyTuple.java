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
package spiralcraft.data;

import spiralcraft.data.spi.AbstractTuple;
import spiralcraft.util.ArrayUtil;

/**
 * <p>A lightweight, immutable Tuple designed to store key values.
 * </p>
 * 
 * @author mike
 *
 */
public class KeyTuple
  implements Tuple
{

  private final Object[] data;
  private final FieldSet fieldSet;
  private final int hashCode;

  public KeyTuple(FieldSet fieldSet,Object[] data,boolean useReference)
  { 
    this.fieldSet=fieldSet;
    if (useReference)
    { this.data=data;
    }
    else
    { 
      this.data=new Object[fieldSet.getFieldCount()];
      for (int i=0;i<data.length;i++)
      { this.data[i]=data[i];
      }
    }
    this.hashCode=ArrayUtil.arrayHashCode(data);
  }

  public KeyTuple(Tuple original)
    throws DataException
  { 
    fieldSet=original.getFieldSet();
    data=new Object[fieldSet.getFieldCount()];
    for (int i=0;i<data.length;i++)
    { data[i]=original.get(i);
    }
    this.hashCode=ArrayUtil.arrayHashCode(data);
  }
  
  @Override
  public Object get(String fieldName)
    throws DataException
  { 
    Field<?> field=fieldSet.getFieldByName(fieldName);
    if (field==null)
    { throw new FieldNotFoundException(fieldSet,fieldName);
    }
    else
    { return field.getValue(this);
    }
  }
  
  public Object[] getData()
  {
    Object[] data=new Object[this.data.length];
    System.arraycopy(this.data,0,data,0,data.length);
    return data;
  }
  
  @Override
  public int hashCode()
  { return hashCode;
  }
  
  @Override
  public final boolean equals(Object o)
  {
    if (this==o)
    { return true;
    }
    
    if (o==null)
    { return false;
    }

    if (o==this)
    { return true;
    }
    
    if (!(o instanceof Tuple))
    { return false;
    }

    
    if (o instanceof KeyTuple)
    { 
      if (o.hashCode()!=hashCode())
      { return false;
      }
      else
      { return ArrayUtil.arrayEquals(data,((KeyTuple) o).data);
      }
    }
    else
    { return AbstractTuple.tupleEquals(this,(Tuple) o);
    }
  }
  
  @Override
  public boolean isMutable()
  { return false;
  }
  
  @Override
  public boolean isVolatile()
  { return false;
  }

  @Override
  public String dumpData()
    throws DataException
  { return toText(" |");
  }

  @Override
  public Object get(
    int index)
    throws DataException
  { return data[index];
  }

  @Override
  public Tuple getBaseExtent()
  { return null;
  }

  @Override
  public Object getBehavior()
    throws DataException
  { return null;
  }

  @Override
  public FieldSet getFieldSet()
  { return fieldSet;
  }

  @Override
  public Type<?> getType()
  { return fieldSet.getType();
  }

  @Override
  public Tuple snapshot()
    throws DataException
  { return this;
  }

  @Override
  public Tuple widen(
    Type<?> type)
    throws DataException
  { return null;
  }

  @Override
  public Aggregate<?> asAggregate()
  { return null;
  }

  @Override
  public Tuple asTuple()
  { return this;
  }

  @Override
  public Identifier getId()
  { return null;
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
  public String toString()
  { return AbstractTuple.tupleToString(this);
  }
  
  @Override
  public String toText(
    String indent)
    throws DataException
  { return AbstractTuple.tupleToText(this,indent);
  }
  
  
}
