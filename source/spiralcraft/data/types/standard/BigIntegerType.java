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

import java.math.BigInteger;

import java.net.URI;

public class BigIntegerType
  extends PrimitiveTypeImpl<BigInteger>
{
  private int precision;
  
  public BigIntegerType(TypeResolver resolver,URI uri)
  { super(resolver,uri,BigInteger.class);
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
  
}