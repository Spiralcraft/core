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
 * <p>A unit of information.
 * </p>
 * 
 * <p>Units are singletons.
 * </p>

 * @author mike
 *
 */
public class DerivedUnit
  extends Unit
{

  private final Unit base;
  private final Prefix prefix;
  private final Unit root;
  private final Quantifiable quantifiable;
  
  DerivedUnit(Unit base,Prefix prefix)
  { 
    this.base=base;
    this.prefix=prefix;
    this.root=base.getRootUnit();
    this.quantifiable=base.getQuantifiable();
    
  }
  
  
  @Override
  public String getSingularName()
  { return prefix.getLongPrefix()+base.getSingularName();
  }

  @Override
  public String getPluralName()
  { return prefix.getLongPrefix()+base.getPluralName();
  }
  
  @Override
  public String getAbbreviation()
  { return prefix.getShortPrefix()+base.getAbbreviation();
  }
  
  
  @Override
  public Unit getBaseUnit()
  { return base;
  }
  
  @Override
  public Unit getRootUnit()
  { return root;
  }
  
  @Override
  public double getRootMultiple()
  { return prefix.getMultiple()*base.getRootMultiple();
  }
  
  @Override
  public Prefix getPrefix()
  { return prefix;
  }

  @Override
  public Quantifiable getQuantifiable()
  { return quantifiable;
  }
  
}
