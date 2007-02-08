//
// Copyright (c) 1998,2007 Michael Toth
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
package spiralcraft.data;

/**
 * Interface for resolving an instance of a Java class from an application
 *   specific context.
 */
public interface InstanceResolver
{
  /**
   * Resolve an Object assignable to the specific Class. 
   *
   *@return the Object, or null if none is available that is derived from the
   *  specified class.
   */
  Object resolve(Class clazz)
    throws DataException;
}