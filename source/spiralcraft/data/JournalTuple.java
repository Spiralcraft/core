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

import spiralcraft.data.session.BufferTuple;

/**
 * An immutable Tuple that is part of a series of other tuples which represent
 *   successive versions or snapshots of the same conceptual object.
 *   
 * This interface provides a versioning facility which groups multiple 
 *   changes to a Tuple into an atomic unit, which produces a new
 *   version of the Tuple. Tuples modified in this way link to their new
 *   versions.
 *
 * All the Tuples in a journal (an update chain) will have the same Scheme.
 */
public interface JournalTuple
  extends Tuple
{
  
  /**
   * Return the most current version of this Tuple. If the problem domain
   *   object represented by this Tuple no longer exists (ie. was deleted)
   *   this method will return null. If this Tuple is the most current version,
   *   this method will return a reference to itself.
   */
  JournalTuple latestVersion();
  
  /**
   * Return the next version of this Tuple. This method will return null if 
   *   the Tuple does not have a more up-to date version, or, if the data has been
   *   deleted, will return a JournalTuple where isDeletedVersion()==true.
   */
  JournalTuple nextVersion();
  
  /**
   * Return the DeltaTuple which specified the changes made between this version
   *   and the next.
   */
  DeltaTuple nextDelta();
   
  /**
   * Indicate whether the problem domain object was deleted.
   * Deleted versions will also have all data nulled.
   */
  boolean isDeletedVersion();
  
  /**
   * Create a new JournalTuple from the changes made in the specified DeltaTuple
   *   and set it as the next version in the Journal.
   */
  JournalTuple update(DeltaTuple delta)
    throws DataException;
  
  /**
   * Create a new JournalTuple from the changes made in the specified DeltaTuple
   *   and remain in a locked state until commit() or rollback() is called, or
   *   the Transaction is aborted.
   */
  JournalTuple prepareUpdate(DeltaTuple delta)
    throws DataException;
  
  /**
   * Commit a prepared Update and unlock the JournalTuple
   */
  void commit();
  
  /**
   * Discard a prepared update and unlock the JournalTuple
   */
  void rollback();

}
