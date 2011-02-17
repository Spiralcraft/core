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

/**
 * <p>An expression of a quantity in terms of a particular unit
 * </p>
 * 
 * @author mike
 */
public abstract class Quantity<N extends Number>
{
  protected final N amount;
  protected final Unit unit;
  
  public Quantity(N amount,Unit unit)
  {
    this.amount=amount;
    this.unit=unit;
  }
  
  public N getAmount()
  { return amount;
  }
  
  public Unit getUnit()
  { return unit;
  }
  
  public final Quantity<Double> in(Unit targetUnit)
  {
    if (unit.getRootUnit()==targetUnit.getRootUnit())
    {
      double conversionFactor
        =unit.getRootMultiple()/targetUnit.getRootMultiple();
      return new DoubleQuantity
        (conversionFactor*amount.doubleValue(),targetUnit);
    }
    else
    { 
      throw new RuntimeException
        ("Cannot convert between "
        +unit.getPluralName()+" and "+targetUnit.getPluralName()
        );
    }
    
  }
  
}
