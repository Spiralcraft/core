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
 * A Tuple which represents an atomic data modification operation to be applied to
 *   an 'original' Tuple. 
 * 
 * An 'updated' Tuple can be created by applying the changes referenced in the Delta
 *   Tuple. The changes may also result in the deletion of the original Tuple.
 */
public interface DeltaTuple
  extends Tuple
{
  
  /**
   *@return whether a deletion should be performed.
   */
  boolean isDelete();

  /**
   *@return the Tuple on which this Delta is based
   */
  Tuple getOriginal();
  
  /**
   *@return a List of modified fields (in this extent and in base extents)
   */
  Field<?>[] getDirtyFields();

  /**
   * @return a List of modified fields for this extent only
   */
  Field<?>[] getExtentDirtyFields();
  
  /**
   * Indicate whether the field from this extent specified by the index has 
   *   been locally modified
   */
  boolean isDirty(int index);
  
  
  /**
   * Indicate whether any fields have been locally modified
   */
  boolean isDirty();
  
  /**
   * Write the changes represented by this DeltaTuple to the specified
   *   EditableTuple
   *   
   * @param tuple
   */
  void updateTo(EditableTuple tuple)
    throws DataException;
  
  @Override
  DeltaTuple getBaseExtent();
  
  /**
   * Return a DeltaTuple that is based on the latest original, throwing an
   *   exception if any changes conflict
   * 
   * @param latest
   * @return
   * @throws DataException If overlapping changes occur
   */
  DeltaTuple rebase(Tuple latest)
    throws DataException;
  
  /**
   * Return a DeltaTuple that is based on the latest original, ignoring any
   *   conflicting changes
   * 
   * @param latest
   * @return
   * @throws DataException 
   */
  DeltaTuple updateOriginal(Tuple latest)
    throws DataException;
  
  /**
   * Return a JournalTuple that reflects the application of this Delta to the
   *   referenced original. 
   * 
   * @return
   */
  JournalTuple freeze()
    throws DataException;
  
}