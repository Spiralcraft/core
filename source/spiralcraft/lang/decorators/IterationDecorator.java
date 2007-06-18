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
package spiralcraft.lang.decorators;

import spiralcraft.lang.Decorator;
import spiralcraft.lang.Optic;

/**
 * Supports Iteration through containers and data structures with multiple
 *   elements.
 *
 * The Optic interface exposes the data at the current position of the 
 *   iteration.
 */
public interface IterationDecorator<T,I>
  extends Decorator<T>,Optic<I>
{
  
  /**
   * Reset the iteration. Must be called -before- every iteration
   */
  public void reset();
  
  /**
   * Indicate whether there are more elements in the iteration
   */
  public boolean hasNext();
  
  /**
   * Advance to the next element
   */
  public void next();
}
