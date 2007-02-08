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
 * A Tuple provides read access to a unit of structured data consisting of 
 *   one or more closely related data elements (Fields).
 *
 * The data structure of a Tuple is defined by a Scheme, which names and 
 *   describes each Field in the Tuple. Each Field is assigned an index
 *   according to the sequence of its definition in the Scheme. 
 *
 * The primary function of a Tuple is to provide a uniform means of access
 *   to data regardless of the source and external representation of that data,
 *   in order to increase the portability and scope of data presentation and
 *   processing components.
 *   
 * The implementation of the Tuple is tied to a given external representation.
 *   A given implementation will provide common components with access to data
 *   stored in a specific type of physical container, be it a simple array of
 *   Objects, a packed buffer of bytes, disk mapped buffers, OS kernel
 *   structures, relational database implementations, etc.
 *
 * Identity:
 *
 * Implementations of Tuple must implement the equals() and the hashCode()
 *   methods appropriately. A Tuple is considered equal to another tuple if
 *   and only if all of its data elements are equal. Two tuples which have
 *   different Schemes may be equal. 
 *
 */
public interface Tuple
  extends DataComposite
{
  /**
   * Return the Scheme of this Tuple
   */
  Scheme getScheme();
  
  /**
   * Return the Object in the specified Field position. The index supplied
   *   corresponds to the Field's order of definition in the Scheme.
   */
  Object get(int index);

  /**
   * Indicate whether the value returned by the get(int index) method may
   *   change at some point in the future. Some elements of data processing
   *   functionality may require that a Tuple be immutable before processing.
   */
  boolean isMutable();
  
  /**
   * Return the Tuple's hash code, which is a function of the Tuple's data
   *   elements.
   */
  int hashCode();
  
  /**
   * Indicate whether this tuple is data-equivalent to another. Tuple 'a'
   *   is considered equal to Tuple 'b' if and only if each data element in
   *   Tuple 'a' is equal to its counterpart in Tuple 'b'.
   */
  boolean equals(Object o);
  
}

