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
package spiralcraft.data.access;

import java.net.URI;

import spiralcraft.data.query.Queryable;

import spiralcraft.data.DataException;
import spiralcraft.data.Type;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.Tuple;

import spiralcraft.builder.Lifecycle;

/**
 * A logical data container. Provides access to a complete set of data used by
 *   one or more applications. Data reachable from a Space may be contained 
 *   contained in or more Stores. 
 *   
 * There is normally only one Space associated with an application. A Space is
 *   never contained within another Space.
 */
public interface Space
  extends Queryable<Tuple>,Lifecycle
{
  URI SPACE_URI = URI.create("class:/spiralcraft/data/access/Space");
  
  /**
   * Retrieve an update 'channel'. The DataConsumer can be used once to update
   *   a batch of Tuples of the same Type.
   * 
   * @return A DataConsumer which is used to push one or more updates into
   *   this Space. 
   */
  DataConsumer<DeltaTuple> getUpdater(Type<?> type)
    throws DataException;
  
}
