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
 * An EditableTuple which holds a copy of data from another Tuple, tracks
 *   modifications, and applies those modifications to the Tuple
 *   in an atomic fashion.
 */
public interface BufferTuple
  extends EditableTuple,DeltaTuple
{
  /**
   * Apply all changes (creating a new JournalTuple) and reset the BufferTuple
   *   to an unchanged state.
   */
  void commit()
    throws DataException;
  
  /**
   * Discard all changes, resetting the BufferTuple to an unchanged state by
   *   copying data from the original Tuple.
   */
  void revert();
  
  /**
   * Delete the Tuple. As a side effect, this will null all fields and 
   *   commit the buffer.
   */
  void delete()
    throws DataException;
  
  /**
   * Reload the data from the Tuple into the Buffer and merge it with local
   *   changes. Locally modified data that has not been updated in the Tuple
   *   will survive the merge. Non-local changes will always overwrite local
   *   changes.
   */
  void refresh()
    throws DataException;
  
  /**
   * Indicate whether any data has been locally modified
   */
  boolean isDirty();
}