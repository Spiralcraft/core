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

import spiralcraft.data.query.Queryable;

import spiralcraft.data.DataException;
import spiralcraft.data.Type;
import spiralcraft.data.DeltaTuple;
import spiralcraft.data.Tuple;

import spiralcraft.builder.Lifecycle;

/**
 * <P>A physical data container. Provides access to a set of data that is
 *   reachable through a single access mechanism, such as a database login,
 *   an XML file, the subtree of a filesystem directory, etc.
 *   
 * <P>A Store is always participates in a single Space.
 */
public interface Store
  extends Queryable<Tuple>,Lifecycle
{
  /**
   * @return The Space to which this store belongs
   */
  Space getSpace();
  
  
  /**
   * Retrieve an update 'channel'. The DataConsumer can be used once to update
   *   a batch of Tuples of the same Type.
   * 
   * @return A DataConsumer which is used to push one or more updates into
   *   this Store
   */
  DataConsumer<DeltaTuple> getUpdater(Type<?> type)
    throws DataException;
}
