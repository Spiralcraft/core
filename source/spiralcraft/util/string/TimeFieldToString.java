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



import spiralcraft.time.TimeField;
import spiralcraft.util.ArrayUtil;

/**
 * <p>A StringConverter for TimeField objects 
 * </p>
 * @author mike
 *
 */
public final class TimeFieldToString
  extends StringConverter<TimeField>
{
  
  
  @Override
  public String toString(TimeField val)
  { 
    if (val==null)
    { return null;
    }
    return val.toString();
  }
    

  @Override
  public TimeField fromString(String val)
  { 
    if (val==null)
    { return null;
    }
    else
    { 
      TimeField ret= TimeField.fromString(val);
      if (ret==null)
      { 
        throw new IllegalArgumentException
          ("No TimeField named '"+val
            +"': "+ArrayUtil.format(TimeField.values(),",","")
          );
      }
      return ret;
    }

  }
}
