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
package spiralcraft.data.types.standard;

import spiralcraft.data.TypeResolver;

import spiralcraft.data.core.PrimitiveTypeImpl;

import java.math.BigDecimal;

import java.net.URI;

public class BigDecimalType
  extends PrimitiveTypeImpl<BigDecimal>
{
  private int precision;
  private int scale;
  
  public BigDecimalType(TypeResolver resolver,URI uri)
  { super(resolver,uri,BigDecimal.class);
  }
  
  /**
   *@return The total number of digits in the number
   */
  public int getPrecision()
  { return precision;
  }
  
  public void setPrecision(int precision)
  { this.precision=precision;
  }
  
  /**
   *@return The number of digits to the right of the decimal point
   */
  public int getScale()
  { return scale;
  }
  
  public void setScale(int scale)
  { this.scale=scale;
  }
}