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



/**
 * <p>A Contextual (aka FocusChainObject) that provides Thread specific
 *   contextual references.
 * </p>
 * 
 * <p>The ThreadContextual expects push() to be called
 *   before downstream bindings are accessed. pop() must -always- be called
 *   to restore references in effect for the Thread before push().
 * </p>
 * 
 * 
 * @author mike
 *
 */
public interface ThreadContextual
  extends FocusChainObject
{


  /**
   * <p>Called by the containing object to publish context to the current
   *   Thread, hiding any previous context published by push().
   * </p>
   */
  void push();
  
  /**
   * <p>Called by the containing object to remove published context from
   *   the current Thread, restoring the previous context.
   * </p>
   * 
   * <p>This method must -always- be called once for every return 
   *   from the push() method. This method must -never- be called if
   *   the associated call to push() throws an Exception. This contract
   *   is typically satisfied by the caller by using a try/finally block 
   *   that begins -after- the call to push().
   * </p>
   */
  void pop();
  
}
