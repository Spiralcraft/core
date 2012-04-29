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
package spiralcraft.data.reflect;

import spiralcraft.data.FieldSet;
import spiralcraft.data.Field;
import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;
import spiralcraft.data.FieldNotFoundException;

/**
 * A set of Fields used to generate an anonymous, fixed length Object[]
 *  defined by a fixed length Class[] from Tuple data. Suitable for resolving
 *  and invoking Constructors and Methods.
 */
public class ParameterBinding
{

  protected final FieldSet fieldSet;
  protected final Class<?>[] signature;
  protected final Field<?>[] fields;
  
  public ParameterBinding(FieldSet fieldSet,String ... fieldNames)
    throws DataException
  {
    this.fieldSet=fieldSet;
    signature=new Class<?>[fieldNames.length];
    fields=new Field<?>[fieldNames.length];
    int i=0;
    for (String fieldName: fieldNames)
    {
      Field<?> field=fieldSet.getFieldByName(fieldName);
      if (field==null)
      { throw new FieldNotFoundException(fieldSet,fieldName);
      }
      signature[i]=field.getType().getNativeClass();
      fields[i]=field;
      i++;
      
    }
  }
  
  public Field<?>[] getFields()
  { return fields;
  }
  
  public Class<?>[] getSignature()
  { return signature;
  }
  
  public Object[] getValues(Tuple tuple)
    throws DataException
  {
    Object[] values=new Object[fields.length];
    int i=0;
    for (Field<?> field: fields)
    { values[i++]=field.getValue(tuple);
    }
    return values;
  }
  
  
}
