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
package spiralcraft.tuple;

/**
 * Provides access to a collection of data via the interfaces in the Tuple
 *   package.
 */
public interface Store
{
  /**
   *@return The Tuple with the specified Scheme and TupleId, or null
   *  if the Tuple does not exist.
   */
  public Tuple find(Scheme scheme,TupleId id);
  
  /**
   * Create a new Tuple for the specified Scheme.
   *
   *@return A new Tuple in buffered state.
   */
  public Tuple create(Scheme scheme);
}
