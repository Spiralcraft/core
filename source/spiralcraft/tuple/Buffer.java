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
 * Provides write access to Tuple data in a manner that supports atomic
 *   modification and journaling. 
 *
 * * Life Cycle:
 *
 * A Buffer is either in a volatile or an immutable state. The volatile state is
 *   the default initial state of the Buffer, and is when Tuple data is modified.
 *   A Buffer exits the volatile state when 'commitBuffer' is called. 
 *
 * Once a Buffer has exited the volatile state, it does so permanently, and it
 *   becomes immutable. At this point, calls the any method which modifies the 
 *   Tuple will throw an IllegalStateException. 
 *
 * The volatile state provides an opportunity to manipulate Tuple data, the
 *   commitBuffer transition provides an opportunity for implementations to
 *   propogate/store that data atomically.
 *   
 * Because a Buffer is immutable once committed, in order to further modify
 *   the data represented by the Tuple it is necessary to create a new Buffer
 *   in a volatile state which contains a copy of the data in the
 *   original Tuple, via the createBuffer() method. 
 *
 * Once a Buffer of an immutable Tuple is committed via the commitBuffer method,
 *   a new Tuple is created which is registered as a successor with the original
 *   Tuple. This forms an update chain (a journal) which permits data processing
 *   and storage components to update their references to point to the latest
 *   version of any given problem domain object.
 * 
 */
public interface Buffer
  extends Tuple
{
  
  /**
   * Replace the Object identified by the specified Field.
   *
   *@throws IllegalStateException If the Tuple cannot be modified
   */
  void set(int pos,Object value);

  /**
   * Commit the data in the Buffer to a new Tuple version.
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
   * Indicate whether or not the Buffer is still in a volatile (editable)
   *   state.
   */
  boolean isVolatile();

  /**
   * Return the immutable Tuple this Buffer is based on. If this Buffer is the
   *   first version of the data it represents this method will return null.
   */
  Tuple original();
  
  /**
   * Delete a Buffered tuple and commit the buffer.
   *
   *@throws IllegalStateException if the Tuple is not in a buffered state
   *@throws BufferConflictException if the original has been updated prior
   *   to this method being caled.
   */
  void delete()
    throws BufferConflictException;
  
}