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
package spiralcraft.data.core;

import spiralcraft.data.Type;
import spiralcraft.data.Aggregate;
import spiralcraft.data.EditableAggregate;
import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.ValidationResult;
import spiralcraft.data.InstanceResolver;

import spiralcraft.data.spi.EditableArrayListAggregate;

import java.lang.reflect.Array;

import java.net.URI;

/**
 * An Array version of a base type
 */
public class ArrayType
  extends AbstractAggregateType<Object[]>
{  
  
  @SuppressWarnings("unchecked") // Array creation via reflection
  public ArrayType(Type<? super Object> contentType,URI uri)
  { 
    super(uri);
    this.contentType=contentType;
    if (contentType.getNativeClass()!=null)
    { nativeClass=(Class<Object[]>) Array.newInstance(contentType.getNativeClass(),0).getClass();
    }
    else
    { nativeClass=null;
    }
  }

  
  public TypeResolver getTypeResolver()
  { return contentType.getTypeResolver();
  }
  
  public ValidationResult validate(Object value)
  { 
    // More work here-
    //   Go through values and and call type.validate() on each one
    // Validation result should accept multiple messages
    return null;
  }
  
  

  
  public Object[] fromData(DataComposite data,InstanceResolver resolver)
    throws DataException
  { 
    Aggregate<?> aggregate=data.asAggregate();
    
    Object array
      =Array.newInstance(contentType.getNativeClass(),aggregate.size());
    int index=0;
    for (Object val: aggregate)
    { 
      Type<?> type=contentType;
      if (val instanceof DataComposite)
      { type=((DataComposite) val).getType();
      }
      
      if (type.isPrimitive())
      { 
        try
        { Array.set(array,index++,val);
        }
        catch (IllegalArgumentException x)
        { 
          throw new DataException
            ("Cannot apply "+val.getClass().getName()
            +"to array of content type "+contentType.getNativeClass()
            );
        }
      }
      else
      { 
        Object convertedVal=type.fromData((DataComposite) val,resolver);
        try
        { Array.set(array,index++,convertedVal);
        }
        catch (IllegalArgumentException x)
        { 
          throw new DataException
            ("Cannot apply "+convertedVal.getClass().getName()
            +"to array of content type "+contentType.getNativeClass()
            );
        }
      }
    }
    return (Object[]) array;
  }
  
  public DataComposite toData(Object[] array)
    throws DataException
  { 
    if (contentType.isPrimitive())
    {
      EditableAggregate<Object> aggregate=new EditableArrayListAggregate<Object>(this);

      int len=Array.getLength(array);
      for (int index=0;index<len;index++)
      { aggregate.add(Array.get(array,index));
      }
	    
      return aggregate;
    }
    else
    {
      EditableAggregate<DataComposite> aggregate
        =new EditableArrayListAggregate<DataComposite>(this);

      int len=Array.getLength(array);
      for (int index=0;index<len;index++)
      { aggregate.add((DataComposite) contentType.toData(Array.get(array,index)));
      }
      return aggregate;
    }
    
  }

  protected String getAggregateQualifier()
  { return ".list";
  }


  
}
