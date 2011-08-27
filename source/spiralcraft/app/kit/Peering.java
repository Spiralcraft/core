//
// Copyright (c) 2010 Michael Toth
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
package spiralcraft.app.kit;

import spiralcraft.app.Component;
import spiralcraft.common.ContextualException;
import spiralcraft.lang.Contextual;
import spiralcraft.lang.Focus;

/**
 * A unique relationship between a component and one of its children
 * 
 * @author mike
 *
 */
public class Peering
  implements Contextual
{

  public Component component;
  public String key;
  
  public Peering()
  {
  }
  
  public Peering(Component component)
  { this.component=component;
  }
  
  @Override
  public Focus<?> bind(
    Focus<?> focusChain)
    throws ContextualException
  { return focusChain;
  }
  
}
