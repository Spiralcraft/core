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
import spiralcraft.data.DataException;
import spiralcraft.data.RuntimeDataException;
import spiralcraft.data.TypeMismatchException;
import spiralcraft.data.DataComposite;
import spiralcraft.data.Type;
import spiralcraft.data.Tuple;
import spiralcraft.data.Scheme;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Field;

/**
 * Abstract Base class for Tuple functionality.
 */
public abstract class AbstractTuple
  implements Tuple
{
  protected final FieldSet fieldSet;
  protected final boolean hasScheme;
  protected Tuple baseExtent;
  
  
  /**
   * Construct an ArrayTuple with an empty set of data
   */
  public AbstractTuple(FieldSet fieldSet)
  { 
    this.fieldSet=fieldSet;
    this.hasScheme=fieldSet instanceof Scheme;
  }
  
  /**
   * Construct an ArrayTuple with an empty set of data
   */
  public AbstractTuple(FieldSet fieldSet,Tuple baseExtent)
  { 
    this.fieldSet=fieldSet;
    this.hasScheme=fieldSet instanceof Scheme;
    this.baseExtent=baseExtent;
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
  
  public Aggregate<?> asAggregate()
  { throw new UnsupportedOperationException("Not an Aggregate");
  }
  
  
  public Type<?> getType()
  { 
    if (hasScheme)
    { return ((Scheme) fieldSet).getType();
    }
    else
    { return null;
    }
  }
  
  public FieldSet getFieldSet()
  { return fieldSet;
  }
  
  public Scheme getScheme()
  { 
    if (hasScheme)
    { return (Scheme) fieldSet;
    }
    else
    { return null;
    }
  }
  
  public Tuple widen(Type<?> type)
    throws DataException
  {
    if (getType()!=null)
    {
      if (getType().hasArchetype(type))
      { return this;
      }
      else
      {
        Tuple baseExtent=resolveBaseExtent();
        if (baseExtent!=null)
        { return baseExtent.widen(type);
        }
        else
        { 
          throw new TypeMismatchException
            ("Type "+getType()+" has no base type compatible with "
            +" wider type "+type
            );
        }
      }
    }
    else
    { return null;
    }
  }
  
  public Tuple snapshot()
    throws DataException
  { 
    if (isMutable())
    { return new ArrayTuple(this);
    }
    else
    { return this;
    }
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
    for (Field field : fieldSet.fieldIterable())
    { 
      Object thisVal;
      Object otherVal;
      
      try
      {
        thisVal=get(field.getIndex());
        otherVal=t.get(field.getIndex());
      }
      catch (DataException x)
      { throw new RuntimeDataException("Error reading field ["+field+"]:"+x,x);
      }
      
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
  
  
  public String toString()
  {
    StringBuilder sb=new StringBuilder();
    sb.append(super.toString());
    if (getType()!=null)
    { sb.append(":").append(getType().getURI());
    }
    else
    { sb.append(":(untyped)"+fieldSet);
    }
    sb.append("[");
    boolean first=true;
    for (Field field : fieldSet.fieldIterable())
    { 
      Object val;
      try
      { val=get(field.getIndex());
      }
      catch (DataException x)
      { throw new RuntimeDataException("Error reading field ["+field+"]:"+x,x);
      }

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
              +compositeVal.getType().getURI();
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
    throws DataException
  { 
    StringBuilder sb=new StringBuilder();
    sb.append("\r\n").append(indent);
    sb.append(super.toString());
    sb.append("\r\n").append(indent).append("==");
    if (getType()!=null)
    { sb.append(getType().getURI());
    }
    else
    { sb.append("(untyped)-"+fieldSet);
    }
    sb.append("\r\n").append(indent);
    sb.append("[");
    boolean first=true;
    String indent2=indent.concat("  ");
    for (Field field : fieldSet.fieldIterable())
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
  
  protected Tuple resolveBaseExtent()
  { 
    if (baseExtent!=null)
    { return baseExtent;
    }
    return null;
  }
}