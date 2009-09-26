//
// Copyright (c) 2009 Michael Toth
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
// "AS IS" basis, WITHOUNumber WARRANTY OF ANY KIND, either express or implied.
//
package spiralcraft.lang;


public class Range
{

  private final Number start;
  private final Number end;
  private final boolean inclusive;
  
  public Range(Number start,Number end,boolean inclusive)
  { 
    this.start=start;
    this.end=end;
    this.inclusive=inclusive;
  }
  
  public Number getStart()
  { return start;
  }
  
  public Number getEnd()
  { return end;
  }
  
  public boolean isInclusive()
  { return inclusive;
  }
    
}
