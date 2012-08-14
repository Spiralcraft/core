//
// Copyright (c) 2012 Michael Toth
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
package spiralcraft.app.components;

import spiralcraft.app.Component;
import spiralcraft.app.kit.AbstractExpansionController;
import spiralcraft.app.kit.ExpansionContainer;
import spiralcraft.lang.Binding;
import spiralcraft.lang.Channel;
import spiralcraft.lang.Focus;

/**
 * <p>Manages a state subtree for for each item in an input set
 * </p>
 * 
 * @author mike
 *
 */
public class SetManager<C,T>
  extends AbstractExpansionController<C,T>
{

  private Binding<C> setX;
  
  /**
   * Create the container for child components. Defaults to creating a
   *   StandardContainer.
   * 
   * @param children
   * @return
   */
  @Override
  protected ExpansionContainer<C,T> createChildContainer(Component[] children)
  { 
    ExpansionContainer<C,T> container=super.createChildContainer(children);
    container.setInitializeContent(true);
    return container;
  }
  
  public void setSetX(Binding<C> setX)
  {
    removeParentContextual(this.setX);
    this.setX=setX;
    addParentContextual(this.setX);
  }
  
  @Override
  protected Channel<C> resolveCollection(Focus<?> chain)
  { return setX;
  }
}
