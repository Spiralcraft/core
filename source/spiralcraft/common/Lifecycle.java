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
package spiralcraft.common;

/**
 * <p>An interface for components that need to be started and stopped.
 * </p>
 * 
 * <p>A Component implements this interface to:
 *   <ul><li>
 *     be atomically configured before its public API is called
 *   </li>
 *   <li>explicitly manage resource allocation and de-allocation
 *   </li>
 *   <li>be coordinated by a container which manages dependencies
 *   </li>
 *   <li>contain other components which implement Lifecycle in order to
 *     propogate start() and stop() events down the containership hierarchy.
 *   </li>
 * </p>
 * 
 * @author mike
 */
public interface Lifecycle
{

  /**
   * The component should complete internal operations to make itself ready to
   *   accept calls to its public API.
   * 
   * @throws LifecycleException
   */
  public void start()
    throws LifecycleException;
  
  /**
   * The component should release any resources allocated and stop accepting
   *   calls to its public API
   * 
   * @throws LifecycleException
   */
  public void stop()
    throws LifecycleException;
  
  
}
