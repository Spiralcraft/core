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
package spiralcraft.app;

import spiralcraft.common.Lifecycle;
import spiralcraft.lang.BindException;
import spiralcraft.lang.Focus;
import spiralcraft.lang.Contextual;



/**
 * A Container is an interface for managing a group of child components
 * 
 * @author mike
 *
 */
public interface Container
  extends Contextual,Lifecycle
{
  
  
  /**
   * Contextual.bind() must bind and register children
   */
  @Override
  Focus<?> bind(Focus<?> focus)
    throws BindException;
  
  Component[] getChildren();
  
  /**
   * The child component associated with the specified state index
   * 
   * @param child
   * @return
   */
  Component getChild(int stateIndex);
  
  int getChildCount();
  
  void messageChild(Dispatcher dispatcher,int index,Message message);
  
  void messageChildren(Dispatcher dispatcher,Message message);
}
