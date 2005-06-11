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
package spiralcraft.ui;

/**
 * Base class for UI Controls. Represents the Controller role
 *  in a Hierarchical Model/View/Controller design pattern
 *. 
 * Controls are normally peered with a single user interface component
 *   from an implementation specific toolkit (ie. Swing, WebUI, etc.)
 */
public interface Control
{
  public void init();

  public void destroy();
}
