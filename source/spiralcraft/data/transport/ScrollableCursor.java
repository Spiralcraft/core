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
import spiralcraft.data.Tuple;

/**
 * A ScrollableCursor is a controllable Cursor that provides the ability to
 *   navigate a linear data structure in both directions.
 */
public interface ScrollableCursor<T extends Tuple>
  extends SerialCursor<T>
{

  /**
   * Move the cursor to the next Tuple, if any.
   * 
   *@return Whether the Cursor moved to another Tuple, or false if the Cursor
   *   encountered the end of the structure.   
   */
  boolean dataNext()
    throws DataException;

  /**
   * Move the cursor to the previous Tuple, if any.
   * 
   *@return Whether the Cursor moved to another Tuple, or false if the Cursor
   *   encountered the beginning of the structure.   
   */
  boolean dataPrevious()
    throws DataException;

  /**
   * Move the cursor to the first Tuple, if there are any Tuples in the
   *   structure.
   * 
   *@return Whether the Cursor moved to another Tuple, or false if the Cursor
   *   encountered the beginning of the structure.   
   */
  boolean dataMoveFirst()
    throws DataException;
  
  /**
   * Move the cursor to the last Tuple, if any.
   * 
   *@return Whether the Cursor moved to another Tuple, or false if the Cursor
   *   encountered the end of the structure.   
   */
  boolean dataMoveLast()
    throws DataException;

  /**
   * Move the cursor to the beginning of the structure, before the first Tuple.
   */
  void dataMoveBeforeFirst()
    throws DataException;

  /**
   * Move the cursor to the end of the structure, after the last Tuple.
   */
  void dataMoveAfterLast()
    throws DataException;

}
