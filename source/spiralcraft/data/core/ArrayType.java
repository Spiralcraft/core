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

import spiralcraft.data.spi.EditableArrayListAggregate;
import spiralcraft.data.util.InstanceResolver;

import java.lang.reflect.Array;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * An Array version of a base type
 */
public class ArrayType<T>
  extends AbstractAggregateType<T[],T>
{  
  
  @SuppressWarnings("unchecked") // Array creation via reflection
  public ArrayType(Type<T> contentType,URI uri)
  { 
    super(uri);
    this.contentType=contentType;
    if (contentType.getNativeClass()!=null)
    { nativeClass
        =(Class<T[]>) Array.newInstance
          (contentType.getNativeClass(),0).getClass();
    }
    else
    { nativeClass=null;
    }
  }

  
  @Override
  public TypeResolver getTypeResolver()
  { return contentType.getTypeResolver();
  }
    
  @SuppressWarnings("unchecked")
  @Override
  public T[] fromData(DataComposite data,InstanceResolver resolver)
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
    return (T[]) array;
  }
  
  @SuppressWarnings("unchecked")
  @Override
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
      { aggregate.add(contentType.toData((T) Array.get(array,index)));
      }
      return aggregate;
    }
    
  }

  @Override
  protected String getAggregateQualifier()
  { return ".list";
  }
  
  @Override
  public boolean isStringEncodable()
  { return contentType.isStringEncodable();
  }
  
  @SuppressWarnings("unchecked")
  @Override
  public T[] fromString(String val)
    throws DataException
  { 
    if (val==null)
    { return null;
    }
    
    StringTokenizer tok=new StringTokenizer(val,"\\|",true);
    
    List<T> values=new ArrayList<T>();
    StringBuilder buf=new StringBuilder();
    boolean escaping=false;
    while (tok.hasMoreTokens())
    {
      String token=tok.nextToken();
      if (escaping)
      {
        escaping=false;
        buf.append(token);
      }
      else if (token.equals("|"))
      { 
        values.add(contentType.fromString(buf.toString()));
        buf.setLength(0);
      }
      else if (token.equals("\\"))
      { escaping=true;
      }
      else
      { buf.append(token);
      }
    }
    
    if (escaping)
    { 
      throw new DataException
        ("Incomplete escape sequence in "+val+" reading "+getURI());
    }
    values.add(contentType.fromString(buf.toString()));
    
    return values.toArray
      ((T[]) Array.newInstance(contentType.getNativeClass(), values.size()));
  }
  
  @Override
  public String toString(T[] valArray)
  {
    if (valArray==null)
    { return null;
    }
    boolean first=true;
    StringBuilder buf=new StringBuilder();
    for (T val: valArray)
    {
      if (!first)
      { buf.append("|");
      }
      else
      { first=false;
      }
      String str=contentType.toString(val);
      if (str!=null)
      { buf.append(str.replace("\\","\\\\").replace("|","\\|"));
      }
    }
    return buf.toString();
  }
  

  
}
