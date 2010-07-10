//
// Copyright (c) 2010 Michael Toth
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
package spiralcraft.time;

import spiralcraft.util.ArrayUtil;

/**
 * A Calendar independent immutable expression of a date/time value of 
 *   arbitrary precision in terms of a set of TimeFields and integer 
 *   quantifiers.
 * 
 * @author mike
 *
 */
public class TimeX
{
  public static class TimeValue
  {
    final TimeField field;
    final int value;
    final int hashCode;
    
    public TimeValue(TimeField field,int value)
    { 
      this.field=field;
      this.value=value;
      this.hashCode=37*field.hashCode()+37*value;
    }
    
    @Override
    public String toString()
    { return field.toString(value);
    }
    
    @Override
    public int hashCode()
    { return hashCode;
    }
    
    @Override
    public boolean equals(Object o)
    { 
      if (o!=null && o instanceof TimeValue)
      { 
        TimeValue timeValue=(TimeValue) o;
        return timeValue.field==field && timeValue.value==value;
      }
      else
      { return false;
      }
    }
  }

  
  private final TimeValue[] values;
  private final int hashCode;
  
  public TimeX(TimeValue ... values)
  {
    this.values=values;
    this.hashCode=ArrayUtil.arrayHashCode(values);
  }
  
  @Override
  public int hashCode()
  { return hashCode;
  }
  
  @Override
  public boolean equals(Object o)
  { 
    if (o!=null && o instanceof TimeX && o.hashCode()==hashCode)
    { return ArrayUtil.arrayEquals(values,((TimeX) o).values);
    }
    else
    { return false;
    }
  }
  
  public TimeValue get(TimeField field)
  {
    for (TimeValue value:values)
    { 
      if (value.field==field)
      { return value;
      }
    }
    return null;
  }
  
  public TimeValue[] getValues()
  { return values;
  }

  @Override
  public String toString()
  {
    StringBuilder out=new StringBuilder();
    boolean first=true;
    for (TimeValue timeValue:values)
    { 
      if (first)
      { first=false;
      }
      else
      { out.append(",");
      }
      out.append(timeValue.toString());
    }
    return out.toString();
  }
}
