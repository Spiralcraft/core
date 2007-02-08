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
 * A Tuple provides access to a unit of structured data. 
 *
 * A Tuple is defined by a Scheme, which describes each data element
 *   in the Tuple.
 *
 * The primary function of a Tuple is to provide a uniform means of access
 *   to data regardless of the physical representation of that data, in order
 *   to increase the portability and scope of data presentation and
 *   processing components.
 *   
 * The implementation of the Tuple is tied to a given physical representation.
 *   A given implementation will provide common components with access to data
 *   stored in a specific type of physical container, be it a simple array of
 *   Objects, a packed buffer of bytes, disk mapped buffers, OS kernel
 *   structures, relational database implementations, etc.
 *   
 * The Tuple interface provides a versioning facility which groups multiple 
 *   changes to a Tuple into an atomic unit, which produces a new
 *   version of the Tuple. Tuples modified in this way link to their new
 *   versions.
 *
 * A Tuple often represents an object in a problem domain. A Tuple's Identity
 *   is defined by its association with a particular problem domain object. A
 *   Tuple's TupleId uniquely identifies this problem domain object within a
 *   given Scheme and data store implementation.
 *
 * Multiple Tuples that have the same TupleId and the same
 *   Scheme represent different versions of the same problem domain object. In
 *   a given container, only one of these Tuples will represent the latest 
 *   version, and the others will be forwardly linked to this Tuple.
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
   * Create a new Buffer which contains a copy of the data in this Tuple.
   *
   *@throws IllegalStageException if the Tuple is already in a buffered state.
   */
  Buffer createBuffer();
  
  
  /**
   * Return the most current version of this Tuple. If the problem domain
   *   object represented by this Tuple no longer exists (ie. was deleted)
   *   this method will return null. If this Tuple is the most current version,
   *   this method will return a reference to itself.
   */
  Tuple currentVersion();
  
  /**
   * Return the next version of this Tuple. This method will return null if 
   *   the Tuple does not have a more up-to date version.
   */
  Tuple nextVersion();
   
  /**
   * Indicate whether the problem domain object was deleted after this version. 
   */
  boolean isDeletedVersion();
}
