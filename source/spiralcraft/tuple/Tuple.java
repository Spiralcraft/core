//
// Copyright (c) 1998,2005 Michael Toth
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
package spiralcraft.tuple;

/**
 * A Tuple represents a single version of a sequence of related data elements. 
 *
 * A Tuple is defined by a Scheme, which describes the data element at each
 *   position in the Tuple. At a minimum, the Scheme contains a set of 
 *   Fields, which define the type of data at each position,
 *   and associate a programmatic name with the data element that is unique
 *   within the Scheme. 
 *
 * The primary function of a Tuple is to provide a uniform means of access
 *   to data regardless of the physical representation of that data. In that
 *   respect, this interface maximizes the portability of data-centric logic
 *   and functionality.
 *   
 * The Tuple interface separates data-centric logic and functionality from
 *   physical representation. The implementation of physical representation is 
 *   provided by a container. A wide variety of implementations are possible,
 *   and are ultimately dependent on device capacity and application scale.
 *
 * For example, Tuple implementations may store data in an array of Objects,
 *   in a packed buffer of bytes, in page buffers memory mapped to disk, in
 *   OS kernel structures, in relational database implementations, etc.
 * 
 * The Tuple interface provides a versioning facility which groups multiple 
 *   changes to a Tuple into an atomic unit, which produces a new
 *   version of the Tuple. Tuples modified in this way link to their new
 *   versions.
 *
 * A particular Tuple can be modified only once in its lifetime. Once a set
 *   of changes is committed, the Tuple becomes immutable, and a new version
 *   must be created in order to make further changes.
 *
 * A Tuple often represents an object in a problem domain. A Tuple's Identity
 *   is defined by its association with a particular problem domain object. A
 *   Tuple's TupleId uniquely identifies this problem domain object within a
 *   given Scheme.
 *
 * Multiple Tuples that have the same TupleId and the same
 *   Scheme represent different versions of the same problem domain object. In
 *   a given container, only one of these Tuples will represent the latest 
 *   version, and the others will be forwardly linked to this Tuple.
 *
 * Life Cycle:
 *
 * A Tuple is either in a volatile or an immutable state. The volatile state is
 *   the default initial state of the Tuple, and is when Tuple data is modified.
 *   A Tuple exits the volatile state when 'commitBuffer' is called. 
 *
 * Once a Tuple has exited the volatile state, it does so permanently, and it
 *   becomes immutable. At this point, calls the any method which modifies the 
 *   Tuple will throw an IllegalStateException. 
 *
 * The volatile state provides an opportunity to manipulate Tuple data, the
 *   commitBuffer transition provides an opportunity for implementations to
 *   propogate/store that data atomically, and the final, immutable commited
 *   state allows the Tuple to participate in maps and other long term
 *   data structures/operations.
 *   
 * Because a Tuple is immutable once committed, in order to further modify
 *   the data represented by the Tuple it is necessary to create a new Tuple
 *   in a volatile state (a 'buffer') which contains a copy of the data in the
     original Tuple, via the createBuffer() method. 
 *
 * Once a buffer of an immutable Tuple is committed via the commitBuffer method,
 *   it is registered as a successor with the original Tuple. This forms an
 *   update chain (a journal) which permits data processing and storage
 *   components to update their references to point to the latest version of
 *   any given problem domain object.
 * 
 * Identity:
 *
 * Implementations of Tuple must implement the equals() and the hashCode()
 *   methods appropriately. A Tuple is considered equal to another tuple if
 *   and only if all of its data elements are equal. Two tuples which have
 *   different Schemes may be equal. 
 *
 * All Tuples have an Id, an immutable, opaque identifier which, together with
 *   a Tuple's Scheme,  associates the Tuple with problem domain object.
 *
 * All the Tuples in a journal (an update chain) will have the same Id and the
 *   same Scheme.
 */
public interface Tuple
{
  /**
   * Return the Scheme of this Tuple
   */
  Scheme getScheme();
  
  /**
   * Return the Id of this Tuple, which is an opaque association with some
   *   problem domain object. Not all Tuples have an Id.
   *
   * If non-null, the TupleId returned must be immutable, and must support the
   *   the equals()/hashCode() design contract.
   */
  TupleId getId();
  
  /**
   * Return the Object in the specified Field position.
   */
  Object get(int pos);
  
  /**
   * Replace the Object identified by the specified Field.
   *
   *@throws IllegalStateException If the Tuple is not in a buffered state
   */
  void set(int pos,Object value);
  
  /**
   * Commit the data in a buffered Tuple and render the Tuple immutable.
   *
   *@return The immutable Tuple resulting from the commit (may not be the
   *          same as the buffer, esp. in the case where no data has
   *          been modified.
   *@throws IllegalStateException if the Tuple is not in a buffered state
   *@throws BufferConflictException if the original has been updated prior
   *   to this method being caled.
   */
  Tuple commitBuffer()
    throws BufferConflictException;

  /**
   * Create a new Tuple which contains a copy of the data in this Tuple and
   *   is in a modifiable, buffered state.
   *
   *@throws IllegalStageException if the Tuple is already in a buffered state.
   */
  Tuple createBuffer();
  
  /**
   * Indicate whether or not the Tuple is in a volatile (buffered) state.
   */
  boolean isVolatile();
  
  /**
   * Return the most current version of this Tuple. If the problem domain
   *   object represented by this Tuple no longer exists, 
   *   this method will return null. If this Tuple is the most current version,
   *   or this Tuple is volatile, this method will return a reference to itself.
   */
  Tuple currentVersion();
  
  /**
   * Return the next version of this Tuple. This method will return null if 
   *   the Tuple does not have a more up-to date version or if it is volatile.
   */
  Tuple nextVersion();
  
  /**
   * Return the immutable Tuple this buffer is based on. If this Tuple is 
   *   immutable, or this Tuple is the first version of the data it represents
   *   this method will return null.
   */
  Tuple original();
  
  /**
   * Delete a buffered Tuple and commit the buffer.
   *
   *@throws IllegalStateException if the Tuple is not in a buffered state
   *@throws BufferConflictException if the original has been updated prior
   *   to this method being caled.
   */
  void delete()
    throws BufferConflictException;
  
  /**
   * Indicate whether the Tuple has been deleted.
   */
  boolean isDeleted();
}
