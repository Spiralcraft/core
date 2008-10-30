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

import java.net.URI;

/**
 * <p>Maps a URI space into some implementation defined type model, and
 *   provides a Reflector instance for each type in the model.
 * </p>
 */
public interface TypeModel
{
  /**
   * <p>A unique name associated with this type model.
   * </p>
   * 
   * <p>The names "java" and "spiralcraft.data" are already used.
   * </p>
   * @return
   */
  String getModelId();
  
  /**
   * <p>Find the type (via a Reflector instance) specified by the given URI.
   * </p>
   * 
   * @param <X> The native class/interface of the object provided by the
   *               reflector. 
   * @param typeURI A URI which identifies the type
   * @return The Reflector instance which represents the type.
   * @throws BindException if an error occurred resolving the type
   */
  <X> Reflector<X> findType(URI typeURI)
    throws BindException;
}
