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
 * <p>Base units are singletons.
 * </p>

 * @author mike
 *
 */
public class Byte
  extends Unit
{
  private static final Byte instance
    =new Byte();

  public static final Byte instance()
  { return instance;
  }

  private Byte()
  {
  }
  
  @Override
  public String getSingularName()
  { return "byte";
  }

  @Override
  public String getPluralName()
  { return "bytes";
  }
  
  @Override
  public String getAbbreviation()
  { return "B";
  }
  
  @Override
  public Unit getBaseUnit()
  { return null;
  }
  
  @Override
  public Prefix getPrefix()
  { return null;
  }

  @Override
  public Quantifiable getQuantifiable()
  { return Information.instance();
  }

}
