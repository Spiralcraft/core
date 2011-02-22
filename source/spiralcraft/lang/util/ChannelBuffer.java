//
// Copyright (c) 1998,2010 Michael Toth
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
package spiralcraft.lang.util;

import spiralcraft.lang.Channel;

/**
 * <p>A stateful object that stores the most recent value of a Channel and 
 *   indicates when the value changes.
 * </p> 
 * 
 * @author mike
 *
 */
public class ChannelBuffer<T>
{
  private final Channel<T> source;
  private T value;
  
  public ChannelBuffer(Channel<T> source)
  { this.source=source;
  }
  
  public T get()
  { return value;
  }
  
  public boolean set(T value)
  {
    if (source.set(value))
    { 
      this.value=value;
      return true;
    }
    else
    { return false;
    }
  }
  
  /**
   * Update the internal value from the source Channel and indicate whether
   *   the value has changed.
   * 
   * @return
   */
  public boolean update()
  { 
    T newValue=source.get();
    if (value==null?newValue!=null:!value.equals(newValue))
    { 
      this.value=newValue;
      return true;
    }
    else
    { return false;
    }
  }
  
}
