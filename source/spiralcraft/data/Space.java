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

import java.net.URI;
import java.util.Date;

import spiralcraft.data.access.DataConsumer;
import spiralcraft.data.query.Queryable;

import spiralcraft.lang.Focus;

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
  URI SPACE_URI = URI.create("class:/spiralcraft/data/Space");
  
  /**
   * <p>Retrieve an update 'channel'. The DataConsumer can be used once to update
   *   a batch of Tuples of the same Type.
   * </p>
   * 
   * <p>Expressions contained in Fields may reference components available
   *   from the provided Focus to provide default values, sequences, timestamps,
   *   etc. available through the DataSession.
   * </p>
   * 
   * @return A DataConsumer which is used to push one or more updates into
   *   this Space. 
   */
  DataConsumer<DeltaTuple> getUpdater(Type<?> type,Focus<?> focus)
    throws DataException;
  
  /**
   * <p>Return a Sequence for generating primary key data, or null if
   *   sequential ids are not provided for the specified URI. The URI is
   *   usually that of a specific Field (ie. Type.getURI()+"#"+field.getName())
   *   that denotes a primary key.  
   * </p>
   * 
   * @param type
   * @return
   * @throws DataException
   */
  Sequence getSequence(URI uri)
    throws DataException;
  
  /**
   * A Space provides a time reference, which should be used by all application
   *   components which integrate time into data. The nowTime for a Space
   *   is usually the current time, but may vary in situations where it
   *   is fixed or frozen for testing, development, debugging or other 
   *   special purposes.
   * 
   * @return The current time according to the Space
   */
  Date getNowTime();

}
