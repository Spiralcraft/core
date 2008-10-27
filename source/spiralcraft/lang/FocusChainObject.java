//
// Copyright (c) 1998,2008 Michael Toth
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

import spiralcraft.util.thread.ContextChain;

/**
 * <p>An object that binds to the Focus chain and optionally contributes its
 *   own Focus to the end of the chain. 
 * </p>
 * 
 * <p>Each instance of this object may only be bound into the FocusChain once.
 * </p>
 * 
 * @author mike
 *
 */
public interface FocusChainObject
  extends ContextChain
{

  /**
   * <p>Obtain any required references from the Focus chain.
   * </p>
   * 
   * <p>If this method is called once successfully, attempting to call it again
   *   may result in an undefined state and should throw an exception.
   * </p>
   * 
   * @param parentFocus
   * @throws BindException If any resources are not found
   */
  void bind(Focus<?> focusChain)
    throws BindException;
  
  /**
   * <p>This BoundObject may publish its own references into the Focus chain.
   * </p>
   * 
   * @return The Focus into which this BoundObject publishes any data. 
   */
  Focus<?> getFocus();
  
  
}
