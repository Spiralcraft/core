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
package spiralcraft.util.thread;

/**
 * <p>Implemented by components that manage ThreadLocal data
 *   surrounding the invocation of some application
 *   specific functionality.
 * </p>
 * 
 * <p>After chaining together ContextFrames as appropriate for
 *   application specifics at configuration time, the run() method
 *   is invoked with a reference to the delegated functionality.
 * </p>
 * 
 * @author mike
 *
 */
public abstract interface ContextFrame
{


  /**
   * <p>Invoke the specified functionality in the context of this chain of
   *   Frames. If next is null, 
   * </p>
   * 
   * @param <T>
   * @param delegate
   * @return The result of the delegate invocation
   * @throws DelegateException caused by an Exception thrown within the
   *  Delegate.
   */
  <T> T runInContext(Delegate<T> delegate)
    throws DelegateException;
}
