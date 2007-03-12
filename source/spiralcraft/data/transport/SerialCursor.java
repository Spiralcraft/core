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

import spiralcraft.data.DataException;
import spiralcraft.data.FieldSet;
import spiralcraft.data.Tuple;

/**
 * A SerialCursor is a controllable Cursor that advances through a set of Tuples
 *   until the end of the set is reached.<P>
 */
public interface SerialCursor<T extends Tuple>
  extends Cursor<T>
{
  /**
   * Advance the cursor to the next Tuple, if any.
   * 
   *@return Whether the Cursor advanced to another Tuple, or false if the Cursor
   *   encountered the end of the stream.   
   */
  boolean dataNext()
    throws DataException;
  
  /**
   *@return The FieldSet common to all the Tuples that will be returned by this Cursor
   */
  FieldSet dataGetFieldSet();
  
  /**
   *@return The Tuple currently positioned under the Cursor
   */
  T dataGetTuple()
    throws DataException;
}
