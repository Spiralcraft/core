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

/**
 * A Cursor is a window onto one or more Tuples of compatible Types.<P>
 * 
 * Cursors provide a means for data processing components to bind to a data stream,
 *   and for streaming data providers to expose data.
 */
public interface Cursor<T extends Tuple>
{
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
