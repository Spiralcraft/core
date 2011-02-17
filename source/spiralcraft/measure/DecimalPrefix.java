//
// Copyright (c) 2011 Michael Toth
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
package spiralcraft.measure;

import java.math.BigDecimal;

/**
 * <p>An enumeration of SI Decimal prefixes
 * </p>
 * 
 * @author mike
 */
public enum DecimalPrefix
  implements Prefix
{ 
  DECI(-1,"deci","d")
  ,CENTI(-2,"centi","c")
  ,MILLI(-3,"milli","m")
  ,MICRO(-6,"micro","\u00B5")
  ,NANO(-9,"nano","n")
  ,PICO(-12,"pico","p")
  ,FEMTO(-15,"femto","f")
  ,ATTO(-18,"atto","a")
  ,ZEPTO(-21,"zepto","z")
  ,YOCTO(-24,"yocto","y")
  ,DECA(1,"deca","da")
  ,HECTO(2,"hecto","h")
  ,KILO(3,"kilo","k")
  ,MEGA(6,"mega","M")
  ,GIGA(9,"giga","G")
  ,TERA(12,"tera","T")
  ,PETA(15,"peta","P")
  ,EXA(18,"exa","E")
  ,ZETTA(21,"zetta","Z")
  ,YOTTA(24,"yotta","Y");
  

  public final int base=10;
  private final int exponent;
  private final String longPrefix;
  private final String shortPrefix;
  private final BigDecimal quantity;
  
  private DecimalPrefix(int exponent,String longPrefix,String shortPrefix)
  { 
    this.exponent=exponent;
    this.longPrefix=longPrefix;
    this.shortPrefix=shortPrefix;
    this.quantity=BigDecimal.valueOf(Math.pow(base,exponent));
  }
  
  @Override
  public int getBase()
  { return base;
  }
  
  @Override
  public int getExponent()
  { return exponent;
  }
  
  @Override
  public BigDecimal multiply(BigDecimal multiplicand)
  { return quantity.multiply(multiplicand);
  }
  
  @Override
  public double getMultiple()
  { return Math.pow(base,exponent);
  }
  
  @Override
  public double multiply(double multiplicand)
  { return Math.pow(base,exponent)*multiplicand;
  }
  
  @Override
  public String getLongPrefix()
  { return longPrefix;
  }
  
  @Override
  public String getShortPrefix()
  { return shortPrefix;
  }
  
}
