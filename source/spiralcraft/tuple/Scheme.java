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

import java.net.URI;

/**
 * A class of of Tuple.
 */
public interface Scheme
{
  /**
   * The URI of the Tuple's 'peer'. This URI is used by API clients of the 
   *   Tuple framework to associate Tuples (which represent data only)
   *   with an appropriate "live" functional counterpart if applicable
   *   ie. typed Java interfaces to access data, Objects in another framework,
   *   or Interfaces that synchronize the state of external entities with
   *   Tuple data.
   */
  URI getURI();
  
  /**
   * Return an immutable Collection of the Fields contained in Tuples
   *   associated with this Scheme. The list will be ordered by the field index.
   */ 
  FieldList<Field> getFields();
}
