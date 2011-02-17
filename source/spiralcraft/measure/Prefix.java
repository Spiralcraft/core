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
 * A prefix for a unit of measure that represents a multiplier of a given
 *   power of a given base.
 * 
 * @author mike
 *
 */
public interface Prefix
{

  public int getBase();
  
  public int getExponent();
  
  public double getMultiple();
  
  public BigDecimal multiply(BigDecimal multiplicand);

  public double multiply(double multiplicand);

  public String getLongPrefix();

  public String getShortPrefix();
}
