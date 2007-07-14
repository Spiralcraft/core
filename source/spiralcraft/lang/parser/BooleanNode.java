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

import spiralcraft.lang.Channel;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Reflector;

import spiralcraft.lang.spi.BeanReflector;


public abstract class BooleanNode
  extends Node
{

  public static Reflector<Boolean> BOOLEAN_REFLECTOR;
  
  { 
    try
    { BOOLEAN_REFLECTOR=BeanReflector.<Boolean>getInstance(Boolean.class);
    }
    catch (BindException x)
    { }
      
  }
  
  public abstract Channel<Boolean> bind(Focus<?> focus)
    throws BindException;
  
  public Reflector<Boolean> getReflector()
  { return BOOLEAN_REFLECTOR;
  }
    
  public abstract String getSymbol();
  
}
