//
// Copyright (c) 1998,2008 Michael Toth
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
package spiralcraft.util.string;


/**
 * <p>A StringConverter for Date objects which uses a 
 *   java.text.SimpleDateFormat and an associated format string to perform
 *   the conversion.
 * </p>
 * 
 * <p>The default format string is yyyy-MM-dd HH:mm:ss.S Z
 * </p>
 * @author mike
 *
 */

public final class BooleanToString
  extends StringConverter<Boolean>
{
  private String trueValue;
  private String falseValue;
  private String nullValue;
  
  public BooleanToString()
  { 
    trueValue="true";
    falseValue="false";
  }
  
  public BooleanToString(String trueValue,String falseValue)
  {
    this.trueValue=trueValue;
    this.falseValue=falseValue;
  }
  
  public void setTrueValue(String trueValue)
  { this.trueValue=trueValue;
  }
  
  public void setFalseValue(String falseValue)
  { this.falseValue=falseValue;
  }
  
  public void setNullValue(String nullValue)
  { this.nullValue=nullValue;
  }

  @Override
  public String toString(Boolean val)
  { return val!=null?(val?trueValue:falseValue):nullValue;
  }

  @Override
  public Boolean fromString(String val)
  { 
    if (val==null)
    { return null;
    }
    val=val.trim();
    if (val.equals(trueValue))
    { return Boolean.TRUE;
    }
    else if (val.equals(falseValue))
    { return Boolean.FALSE;
    }
    else if (nullValue!=null && val.equals(nullValue))
    { return null;
    }
    else
    { throw new IllegalArgumentException("["+val+"]");
    }
  }
}