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
package spiralcraft.data.transport;

import spiralcraft.data.query.Queryable;

import spiralcraft.data.DataException;

/**
 * A physical data container. Provides access to a set of data that is reachable
 *   through a single access mechanism, such as a database login, an XML file,
 *   or the subtree of a filesystem directory.
 */
public interface Store
  extends Queryable
{
  /**
   * @return The Space to which this store belongs
   */
  Space getSpace();
  
  /**
   * Prepare the Store for operation
   */
  void initialize()
    throws DataException;
  
}
