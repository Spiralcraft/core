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

import spiralcraft.data.DataException;
import spiralcraft.data.Tuple;

/**
 * <P>Encapsulates a set of heterogenous data, represented by a sequence of SerialCursors
 *   of differing data Types.
 * 
 * <P>A DataStream is initially positioned before the first SerialCursor. The SerialCursor
 *   returned by nextCursor() provides one or more Tuples. Once all the Tuples in the
 *   cursor are read, nextCursor() is called again.
 *   
 * <P>If all the Tuples in a cursor are not read, they will be discarded when
 *   nextCursor is called.
 *   
 * <P>There can be more than one SerialCursor in a single DataStream which references a
 *   specific DataType.
 * 
 */
public interface DataStream<T extends Tuple>
{
  boolean hasMoreCursors();
  
  SerialCursor<T> nextCursor()
    throws DataException;
}
