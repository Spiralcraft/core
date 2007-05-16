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
   *@return a List of modified fields
   */
  Field[] getDirtyFields();

  /**
   * Indicate whether the field specified by the index has been locally modified
   */
  boolean isDirty(int index);
  
  
  /**
   * Indicate whether any fields have been locally modified
   */
  boolean isDirty();
  
}