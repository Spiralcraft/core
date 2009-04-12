//
// Copyright (c) 1998,2009 Michael Toth
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

import spiralcraft.common.Lifecycle;

/**
 * <p>Base interface for UI Controls. Represents the Controller role
 *   in a recursiveHierarchical Model/View/Controller design pattern. This
 *   abstract interface provides a means to realize common UI functionality
 *   that is independent of a given UI implementation.
 * </p>
 *. 
 * <p>Controls are normally peered with a single user interface component
 *   from an implementation specific toolkit (ie. Swing, WebUI, etc.), and/or
 *   a reference (in some arbitrary form) to some aspect of a model. At
 *   the current level of abstraction, we do not assume that either the
 *   "view" or the "model" is a Java object referencable by a bean property-
 *   hence there is no "getter" method or type variable for either.
 * 
 * </p>
 *   
 */
public interface Control
  extends Lifecycle
{
}
