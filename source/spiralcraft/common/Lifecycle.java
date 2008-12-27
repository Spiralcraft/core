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
 * An interface for components that need to perform internal configuration and
 *   resource allocation after external configuration is complete but before
 *   accepting calls to its public API.
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
