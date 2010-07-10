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

import java.math.BigDecimal;

/**
 * <p>A relative span of time, expressed in terms of Chronoms and optionally
 *   progressively finer Durations.
 * </p>
 * 
 * <p>A Duration is immutable
 * </p>
 * 
 * @author mike
 *
 */
public class Duration
{
  
  private final long count;
  private final Chronom unit;
  private final Duration rest;
  
  
  public Duration(long count,Chronom unit)
  { 
    this.count=count;
    this.unit=unit;
    this.rest=null;
  }
  
  public Duration(long count,Chronom unit,Duration rest)
  {
    if (unit.compareTo(rest.getUnit())<=0)
    { 
      throw new IllegalArgumentException
        ("A Duration may not be expressed in terms of a coarser Duration: "
        +rest.unit.getClass().getSimpleName()+" is not finer than "+
        unit.getClass().getSimpleName()
        );
    }
    this.count=count;
    this.unit=unit;
    this.rest=rest;
  }
  
  public long getCount()
  { return count;
  }
  
  public Chronom getUnit()
  { return unit;
  }
  
  public Duration getRest()
  { return rest;
  }
  
  public Duration add(long count,Chronom unit)
  {
    if (this.unit==unit)
    { return new Duration(count+this.count,unit,rest);
    }
    else if (this.unit.compareTo(unit)>0)
    {
      // Adding a smaller interval must insert as rest
      if (rest==null)
      { return new Duration(this.count,this.unit,new Duration(count,unit));
      }
      else
      { return new Duration(this.count,this.unit,rest.add(count,unit));
      }
    }
    else
    { 
      // Adding a larger interval just chains
      return new Duration(count,unit,this);
    }
  }
  
  public BigDecimal getStandardSeconds()
  {
    BigDecimal standardSeconds=unit.getStandardSeconds();
    standardSeconds=standardSeconds.multiply(BigDecimal.valueOf(count));
    if (rest==null)
    { return standardSeconds;
    }
    else
    { return standardSeconds.add(rest.getStandardSeconds());
    }
  }
  
  public long getStandardMilliseconds()
  { 
    return (unit.getStandardMilliseconds()*count)
      +(rest!=null?rest.getStandardMilliseconds():0);
  }
  
  @Override
  public String toString()
  { return count+" "+unit.name()+(rest!=null?", "+rest.toString():".");
  }
  
}
