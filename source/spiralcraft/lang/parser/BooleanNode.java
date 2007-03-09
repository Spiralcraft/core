//
// Copyright (c) 1998,2005 Michael Toth
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
package spiralcraft.lang.parser;

import spiralcraft.lang.Optic;
import spiralcraft.lang.OpticFactory;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;

import spiralcraft.lang.optics.Prism;

public abstract class BooleanNode
  extends Node
{

  public static Prism<Boolean> BOOLEAN_PRISM;
  
  { 
    try
    { BOOLEAN_PRISM=OpticFactory.getInstance().<Boolean>findPrism(Boolean.class);
    }
    catch (BindException x)
    { }
      
  }
  
  public abstract Optic bind(Focus<?> focus)
    throws BindException;
  
  public Prism<Boolean> getPrism()
  { return BOOLEAN_PRISM;
  }
    
  public abstract String getSymbol();
  
}
