//
// Copyright (c) 1998,2019 Michael Toth
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
package spiralcraft.meter;

/**
 * An object obtained from a meter that allows the application to track a single 
 *   metric. 
 */
public class Register
{
  private final String name;
  private long value;
  private boolean hasChanged;
  
  public Register(String name)
  { this.name=name;
  }
  
  public synchronized void setValue(long value)
  { 
    if (value!=this.value)
    {
      this.value=value;
      this.hasChanged=true;
    }
  }
  
  public synchronized void incrementValue()
  { 
    value++;
    hasChanged=true;
  }

  public synchronized void decrementValue()
  { 
    value--;
    hasChanged=true;
  }

  public synchronized void adjustValue(long increment)
  { 
    if (increment!=0)
    {
      value+=increment;
      hasChanged=true;
    }
  }

  /**
   * Read the value and reset the changed flag 
   */
  public synchronized long readValue()
  {
    hasChanged=false;
    return value;
  }

  /**
   * Return the value
   */
  public long getValue()
  { return value;
  }

  /**
   * Indicate whether the value has changed since the last read
   */
  public boolean hasChanged()
  { return hasChanged;
  }


  /**
   * Return the attribute name of the register
   */
  public String getName()
  { return name;
  }
}
