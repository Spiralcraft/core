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

import java.util.HashMap;


/**
 * A unit of measure for some measurable Property.
 * 
 * @author mike
 *
 */
public abstract class Unit
{

  private final HashMap<Prefix,Unit> derivations
    =new HashMap<Prefix,Unit>();
  
  protected Unit()
  {
  }
  
  public abstract String getSingularName();
  
  public abstract String getPluralName();
  
  public abstract String getAbbreviation();
  
  public abstract Unit getBaseUnit();
  
  public abstract Prefix getPrefix();
  
  public abstract Quantifiable getQuantifiable();
  
  public DoubleQuantity qty(double qty)
  { return new DoubleQuantity(qty,this);
  }
  
  public IntegerQuantity qty(int qty)
  { return new IntegerQuantity(qty,this);
  }

  public Unit getRootUnit()
  { return this;
  }
  
  public double getRootMultiple()
  { return 1;
  }
  
  public Unit derive(Prefix prefix)
  {
    Unit derived=derivations.get(prefix);
    if (derived==null)
    {
      derived=new DerivedUnit(this,prefix);
      derivations.put(prefix,derived);
    }
    return derived;
  }

  
}
