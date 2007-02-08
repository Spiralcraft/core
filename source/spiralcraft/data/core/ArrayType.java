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
import spiralcraft.data.TypeNotFoundException;
import spiralcraft.data.Tuple;
import spiralcraft.data.Aggregate;
import spiralcraft.data.EditableAggregate;
import spiralcraft.data.DataComposite;
import spiralcraft.data.DataException;
import spiralcraft.data.Scheme;
import spiralcraft.data.TypeResolver;
import spiralcraft.data.ValidationResult;
import spiralcraft.data.InstanceResolver;

import spiralcraft.data.wrapper.ReflectionType;

import spiralcraft.data.spi.EditableArrayListAggregate;

import spiralcraft.util.ArrayUtil;


import java.lang.reflect.Array;

import java.net.URI;

/**
 * An Array version of a base type
 */
public class ArrayType
  implements Type
{  
  private final Type contentType;
  private final URI uri;
  private final Class nativeType;
  
  public ArrayType(Type contentType,URI uri)
  { 
    this.contentType=contentType;
    this.uri=uri;
    if (contentType.getNativeClass()!=null)
    { 
      this.nativeType=Array.newInstance(contentType.getNativeClass(),0).getClass();
    }
    else
    { nativeType=null;
    }
  }

  public Type getMetaType()
  {
    try
    { return getTypeResolver().resolve(ReflectionType.canonicalUri(getClass()));
    }
    catch (TypeNotFoundException x)
    { throw new RuntimeException(x);
    }
  }
  
  /**
   * The public Java class or interface used to programatically access or
   *   manipulate this data element.
   */
  public Class getNativeClass()
  { return nativeType;
  }
  
  /**
   * Arrays are always represented by aggregates
   */
  public boolean isPrimitive()
  { return false;
  }
  
  public TypeResolver getTypeResolver()
  { return contentType.getTypeResolver();
  }
  
  public URI getUri()
  { return uri;
  }
  
  /**
   * @return The Scheme which describes the structure of this type, or null if
   *   this type is not a complex type. 
   */
  public Scheme getScheme()
  { return contentType.getScheme();
  }
  
  public boolean isAggregate()
  { return true;
  }
  
  public Type getContentType()
  { return contentType;
  }
  
  public Type getCoreType()
  {
    Type ret=this;
    while (ret.isAggregate())
    { ret=ret.getContentType();
    }
    return ret;
  }
  
  public ValidationResult validate(Object value)
  { 
    // More work here-
    //   Go through values and and call type.validate() on each one
    // Validation result should accept multiple messages
    return null;
  }
  
  public void link()
    throws DataException
  { contentType.link();
  }
  
  public boolean isStringEncodable()
  { return true;
  }
  
  public Object fromString(String val)
  { return null;
  }
  
  public String toString(Object val)
  { return null;
  }
  
  public Object fromData(DataComposite data,InstanceResolver resolver)
    throws DataException
  { 
    Aggregate aggregate=data.asAggregate();
    
    Object array
      =Array.newInstance(contentType.getNativeClass(),aggregate.size());
    int index=0;
    for (Object val: aggregate)
    { 
      if (contentType.isPrimitive())
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
        Object convertedVal=contentType.fromData((DataComposite) val,resolver);
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
    return array;
  }
  
  public DataComposite toData(Object array)
    throws DataException
  { 
    EditableAggregate aggregate=new EditableArrayListAggregate(this);

    int len=Array.getLength(array);
    for (int index=0;index<len;index++)
    {
      if (contentType.isPrimitive())
      { aggregate.add(Array.get(array,index));
      }
      else
      { aggregate.add(contentType.toData(Array.get(array,index)));
      }
    }
    
    return aggregate;
  }
  
    
  public String toString()
  { return super.toString()+":"+uri.toString();
  }
}
