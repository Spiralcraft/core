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
package spiralcraft.lang;

/**
 * <p>An object that utilizes and/or provides context via the Focus chain.
 * </p>
 * 
 * <p>Binds to the Focus chain and optionally contributes its
 *   own Focus to the end of the chain.
 * </p>
 * 
 * <p>Each instance of this object may only be bound into the FocusChain once.
 * </p>
 * 
 *
 * @author mike
 *
 */
public interface Contextual
{
  /**
   * <p>Obtain any required references from the Focus chain.
   * </p>
   * 
   * <p>If this method is called once successfully, attempting to call it again
   *   may result in an undefined state and should throw an exception.
   * </p>
   * 
   * @param focusChain
   * @return The Focus via which this Contextual publishes its data, or
   *   simply the passed-in focusChain if no chained Focus is created.
   *   
   * @throws BindException If any resources are not found
   */
  Focus<?> bind(Focus<?> focusChain)
    throws BindException;  
}
