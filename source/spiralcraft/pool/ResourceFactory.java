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
package spiralcraft.pool;

/**
 * Provides the application specific resources to be managed
 *   by the Pool.
 */
public interface ResourceFactory<T>
{
  /**
   * Create a new instance of a resource to be added to the Pool.
   */
  public T createResource()
    throws Exception;

  /**
   * Discard a resource when no longer needed by the Pool.
   */
  public void discardResource(T resource);

}
