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
import spiralcraft.lang.FocusChainObject;



/**
 * A Container is an interface for managing a group of child components
 * 
 * @author mike
 *
 */
public interface Container
  extends FocusChainObject,Lifecycle
{
  
  
  /**
   * FocusChainObject.bind() must bind and register children
   */
  Focus<?> bind(Focus<?> focus)
    throws BindException;
  
  Component[] getChildren();
  
  Component getChild(int child);
  
  int getChildCount();

}
