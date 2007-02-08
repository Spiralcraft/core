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
import spiralcraft.data.Type;
import spiralcraft.data.Tuple;
import spiralcraft.data.Scheme;
import spiralcraft.data.Field;

import spiralcraft.util.ArrayUtil;

/**
 * Base class for a simple in-memory immutable Tuple.
 */
public class ArrayTuple
  implements Tuple
{
  protected final Scheme scheme;
  protected final Object[] data;
  
  
  /**
   * Construct an ArrayTuple with an empty set of data
   */
  public ArrayTuple(Scheme scheme)
  { 
    this.scheme=scheme;
    this.data=new Object[scheme.getFieldCount()];
  }
  
  public boolean isTuple()
  { return true;
  }
  
  public Tuple asTuple()
  { return this;
  }
  
  public boolean isAggregate()
  { return false;
  }
  
  public Aggregate asAggregate()
  { throw new UnsupportedOperationException("Not an Aggregate");
  }
  
  /**
   * Construct an ArrayTuple that is a copy of another Tuple
   */
  public ArrayTuple(Tuple original)
  { 
    this.scheme=original.getScheme();
    this.data=new Object[scheme.getFieldCount()];
    
    for (Field field : scheme.fieldIterable())
    { data[field.getIndex()]=field.getValue(original);
    }
  }
  
  public Type getType()
  { return scheme.getType();
  }
  
  public Scheme getScheme()
  { return scheme;
  }
  
  public Object get(int index)
  { return data[index];
  }
  
  public boolean isMutable()
  { return false;
  }
  
  public boolean equals(Object o)
  {
    if (o==null)
    { return false;
    }
    
    if (!(o instanceof Tuple))
    { return false;
    }
    
    Tuple t=(Tuple) o;
    for (Field field : scheme.fieldIterable())
    { 
      Object thisVal=data[field.getIndex()];
      Object otherVal=field.getValue(t);
      if (thisVal!=null)
      { 
        if (!thisVal.equals(otherVal))
        { return false;
        }
      }
      else
      {
        if (otherVal!=null)
        { return false;
        }
      }
    }
    return true;
  }
  
  public int hashCode()
  { return ArrayUtil.arrayHashCode(data);
  }
  
  public String toString()
  {
    StringBuilder sb=new StringBuilder();
    sb.append(super.toString());
    if (getType()!=null)
    { sb.append(":").append(getType().getUri());
    }
    else
    { sb.append(":(untyped)"+scheme);
    }
    sb.append("[");
    boolean first=true;
    for (Field field : scheme.fieldIterable())
    { 
      Object val=field.getValue(this);
      if (!(val instanceof DataComposite))
      {
        if (val!=null)
        { 
          if (!first)
          { sb.append(",");
          }
          else
          { first=false;
          }
          
          String stringVal=val.toString();
            
          sb.append(field.getName())
            .append("=")
            .append("[")
            .append(stringVal)
            .append("]");
        }
      }
      else
      {
        DataComposite compositeVal=(DataComposite) val;
        if (compositeVal!=null)
        {
          if (!first)
          { sb.append(",");
          }
          else
          { first=false;
          }
          
          String stringVal
            =compositeVal.getClass().getName()+":"
              +compositeVal.getType().getUri();
          sb.append(field.getName())
            .append("=")
            .append("<")
            .append(stringVal)
            .append(">");
          
        }
      }
    }
    sb.append("]");
    return sb.toString();
  }
  
  public String toText(String indent)
  { 
    StringBuilder sb=new StringBuilder();
    sb.append("\r\n").append(indent);
    sb.append(super.toString());
    sb.append("\r\n").append(indent).append("==");
    if (getType()!=null)
    { sb.append(getType().getUri());
    }
    else
    { sb.append("(untyped)-"+scheme);
    }
    sb.append("\r\n").append(indent);
    sb.append("[");
    boolean first=true;
    String indent2=indent.concat("  ");
    for (Field field : scheme.fieldIterable())
    { 
      
      Object val=field.getValue(this);
      if (val!=null)
      { 
        sb.append("\r\n").append(indent2);
        if (!first)
        { sb.append(",");
        }
        else
        { first=false;
        }
        
        sb.append(field.getName()).append("=");
        if (val instanceof DataComposite)
        { 
          sb.append("\r\n").append(indent2)
            .append("[")
            .append(((DataComposite) val).toText(indent2+"  "));
          sb.append("\r\n").append(indent2)
            .append("]");
        }
        else
        { 
          sb.append("[");
          sb.append(val.toString());
          sb.append("]");
        }
          
          
      }
    }
    sb.append("\r\n").append(indent); 
    sb.append("]");
    return sb.toString();
  }
}