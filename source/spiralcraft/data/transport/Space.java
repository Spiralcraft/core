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
 * A logical data container. Provides access to a complete set of data used by
 *   one or more applications. Data reachable from a Space may be contained 
 *   contained in or more Stores. There is normally only one Space associated 
 *   with an application.
 */
public interface Space
  extends Queryable
{

  /**
   * Prepare the Space and all Stores for data processing
   */
  void initialize()
    throws DataException;
}
